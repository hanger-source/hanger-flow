package source.hanger.flow.visualizer;

import source.hanger.flow.contract.model.*;

import java.util.*;

/**
 * Flow DSL -> PlantUML 可视化工具
 */
public class FlowDslPlantUmlVisualizer {
    /**
     * State Diagram版本（原有）
     */
    public static String toPlantUml(FlowDefinition flow) {
        return toStateDiagramPlantUml(flow);
    }

    private static StepDefinition findStepByName(FlowDefinition flow, String name) {
        for (StepDefinition step : flow.getStepDefinitions()) {
            if (name.equals(step.getName())) {
                return step;
            }
        }
        return null;
    }

    // 别名映射：节点名 -> step序号
    private static Map<String, String> buildStepAliasMap(FlowDefinition flow) {
        Map<String, String> map = new HashMap<>();
        List<StepDefinition> steps = flow.getStepDefinitions();
        for (int i = 0; i < steps.size(); i++) {
            map.put(steps.get(i).getName(), "step" + i);
        }
        return map;
    }

    /**
     * 嵌套 State Diagram 版本，支持复合节点递归嵌套
     */
    public static String toNestedStateDiagramPlantUml(FlowDefinition flow) {
        StringBuilder sb = new StringBuilder();
        sb.append("@startuml\n");
        sb.append("title ").append(flow.getName()).append(" @").append(flow.getVersion()).append("\n");
        Set<String> rendered = new HashSet<>();
        Map<String, String> aliasMap = buildStepAliasMap(flow);
        // 找到起始节点
        StepDefinition start = null;
        for (StepDefinition step : flow.getStepDefinitions()) {
            if ("__START__".equals(step.getName())) {
                start = step;
                break;
            }
        }
        if (start == null && !flow.getStepDefinitions().isEmpty()) {
            start = flow.getStepDefinitions().get(0);
        }
        if (start != null) {
            renderNestedStateFixed(flow, start, sb, rendered, 0, aliasMap);
        }
        // --- 自动补充 END 节点及连线 ---
        sb.append("state \"__END__\" as end\n");
        // 只要有transition指向__END__，就连end
        for (StepDefinition step : flow.getStepDefinitions()) {
            String fromAlias = aliasMap.get(step.getName());
            if (step instanceof AbstractStepDefinition abs) {
                if (abs.getTransitions() != null) {
                    for (Transition t : abs.getTransitions()) {
                        if ("__END__".equals(t.nextStepName())) {
                            sb.append(fromAlias).append(" --> end\n");
                        }
                    }
                }
            } else if (step instanceof ParallelStepDefinition parallel) {
                if (parallel.getTransitions() != null) {
                    for (Transition t : parallel.getTransitions()) {
                        if ("__END__".equals(t.nextStepName())) {
                            sb.append(fromAlias).append(" --> end\n");
                        }
                    }
                }
            } else if (step instanceof AsyncStepDefinition async) {
                if (async.getTransitions() != null) {
                    for (Transition t : async.getTransitions()) {
                        if ("__END__".equals(t.nextStepName())) {
                            sb.append(fromAlias).append(" --> end\n");
                        }
                    }
                }
            }
        }
        sb.append("@enduml\n");
        return sb.toString();
    }

    // 全新并行块渲染逻辑
    private static void renderNestedStateFixed(FlowDefinition flow, StepDefinition step, StringBuilder sb,
        Set<String> rendered, int indent, Map<String, String> aliasMap) {
        String pad = "  ".repeat(indent);
        String alias = aliasMap.get(step.getName());
        if (step instanceof ParallelStepDefinition parallel) {
            sb.append(pad).append("state \"").append(step.getName()).append("\" as ").append(alias).append(" {")
                .append("\n");
            // 1. 获取所有分支节点名（branch）
            Set<String> branchNames = parallel.getBranches().keySet();
            String joinName = null;
            // 2. 输出所有分支节点的 state 语句（只 state，不递归）
            for (String branchName : branchNames) {
                String childAlias = aliasMap.get(branchName);
                sb.append(pad).append("  state \"").append(branchName).append("\" as ").append(childAlias).append("\n");
            }
            // 3. 输出所有分支节点的连线
            for (String branchName : branchNames) {
                String childAlias = aliasMap.get(branchName);
                sb.append(pad).append("  ").append(alias).append(" --> ").append(childAlias).append("\n");
            }
            sb.append(pad).append("}\n");
            // 4. 并行块外部递归渲染所有分支节点的子流程
            for (String branchName : branchNames) {
                StepDefinition child = findStepByName(flow, branchName);
                if (child != null && !rendered.contains(child.getName())) {
                    Set<String> renderedCopy = new HashSet<>(rendered);
                    renderedCopy.add(step.getName());
                    renderNestedStateFixed(flow, child, sb, renderedCopy, indent + 1, aliasMap);
                }
            }
            // 5. 并行块外部输出 join 节点 state 语句和连线（如有）
            // 这里简单用 transitions 里 name 包含"完成"作为 join 节点（可根据实际 joinBranchNames 优化）
            List<Transition> transitions = parallel.getTransitions();
            if (transitions != null) {
                for (Transition t : transitions) {
                    StepDefinition child = findStepByName(flow, t.nextStepName());
                    if (child != null && child instanceof TaskStepDefinition && child.getName().contains("完成")) {
                        String joinAlias = aliasMap.get(child.getName());
                        sb.append("state \"").append(child.getName()).append("\" as ").append(joinAlias).append("\n");
                        sb.append(pad).append(alias).append(" --> ").append(joinAlias).append(" : [并行]\n");
                    }
                }
            }
        } else if (step instanceof AsyncStepDefinition async) {
            sb.append(pad).append("state \"").append(step.getName()).append("\" as ").append(alias).append(" {")
                .append("\n");
            List<String> branchNames = async.getBranchNames();
            for (String branchName : branchNames) {
                String childAlias = aliasMap.get(branchName);
                sb.append(pad).append("  state \"").append(branchName).append("\" as ").append(childAlias).append("\n");
            }
            for (String branchName : branchNames) {
                String childAlias = aliasMap.get(branchName);
                sb.append(pad).append("  ").append(alias).append(" --> ").append(childAlias).append("\n");
            }
            sb.append(pad).append("}\n");
            for (String branchName : branchNames) {
                StepDefinition child = findStepByName(flow, branchName);
                if (child != null && !rendered.contains(child.getName())) {
                    Set<String> renderedCopy = new HashSet<>(rendered);
                    renderedCopy.add(step.getName());
                    renderNestedStateFixed(flow, child, sb, renderedCopy, indent + 1, aliasMap);
                }
            }
        } else {
            if (!rendered.contains(step.getName())) {
                sb.append(pad).append("state \"").append(step.getName()).append("\" as ").append(alias).append("\n");
                rendered.add(step.getName());
            }
            if (step instanceof AbstractStepDefinition abs) {
                List<Transition> transitions = abs.getTransitions();
                if (transitions != null) {
                    for (Transition t : transitions) {
                        StepDefinition next = findStepByName(flow, t.nextStepName());
                        if (next != null) {
                            renderNestedStateFixed(flow, next, sb, rendered, indent, aliasMap);
                            sb.append(pad).append(alias).append(" --> ").append(aliasMap.get(next.getName())).append(
                                "\n");
                        }
                    }
                }
            }
        }
    }

    // 保留原有State Diagram方法
    private static String toStateDiagramPlantUml(FlowDefinition flow) {
        StringBuilder sb = new StringBuilder();
        sb.append("@startuml\n");
        sb.append("title ").append(flow.getName()).append(" @").append(flow.getVersion()).append("\n");
        List<StepDefinition> steps = flow.getStepDefinitions();
        Map<String, String> stepAliasMap = buildStepAliasMap(flow);
        for (int i = 0; i < steps.size(); i++) {
            StepDefinition step = steps.get(i);
            String alias = stepAliasMap.get(step.getName());
            if (step instanceof source.hanger.flow.contract.model.ParallelStepDefinition) {
                sb.append("state \"").append(step.getName()).append("\" as ").append(alias).append(" <<parallel>>\n");
            } else {
                sb.append("state \"").append(step.getName()).append("\" as ").append(alias).append("\n");
            }
        }
        sb.append("\n");
        for (StepDefinition step : steps) {
            String from = stepAliasMap.get(step.getName());
            if (step instanceof source.hanger.flow.contract.model.ParallelStepDefinition) {
                List<Transition> transitions = ((ParallelStepDefinition)step).getTransitions();
                if (transitions != null) {
                    for (Transition t : transitions) {
                        String to = stepAliasMap.get(t.nextStepName());
                        if (from != null && to != null) {
                            sb.append(from).append(" --> ").append(to).append(" : [branch]\n");
                        }
                    }
                }
            } else if (step instanceof source.hanger.flow.contract.model.AsyncStepDefinition) {
                List<Transition> transitions = ((AbstractStepDefinition)step).getTransitions();
                if (transitions != null) {
                    for (Transition t : transitions) {
                        String to = stepAliasMap.get(t.nextStepName());
                        if (from != null && to != null) {
                            sb.append(from).append(" --> ").append(to).append(" : [async]\n");
                        }
                    }
                }
            } else if (step instanceof AbstractStepDefinition abs) {
                List<Transition> transitions = abs.getTransitions();
                if (transitions != null) {
                    for (Transition t : transitions) {
                        String to = stepAliasMap.get(t.nextStepName());
                        if (from != null && to != null) {
                            sb.append(from).append(" --> ").append(to).append(" : [next]\n");
                        }
                    }
                }
            }
        }
        sb.append("@enduml\n");
        return sb.toString();
    }
} 