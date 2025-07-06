package source.hanger.flow.core.util;

import source.hanger.flow.contract.model.*;

import java.util.*;

/**
 * 流程结构适配工具类
 * <p>
 * 统一为 core 层和 runtime 层提供对 contract 层结构的无侵入访问，
 * 避免 core 层直接依赖 contract 实现细节（如强制类型转换、instanceof等）。
 * 支持获取分支、边、transitions等结构信息。
 */
public class FlowStructureUtils {

    public static StepDefinition findStepDefinition(FlowDefinition flowDefinition, Branch branch) {
        return findStepDefinition(flowDefinition, branch.nextStepName());
    }

    public static StepDefinition findStepDefinition(FlowDefinition flowDefinition, String targetStepName) {
        return flowDefinition.getStepDefinitions().stream()
            .filter(e -> e.getName().equals(targetStepName)).findAny()
            .orElse(null);
    }

    public static List<Transition> collectTransitions(StepDefinition step) {
        if (step instanceof AbstractStepDefinition) {
            return ((AbstractStepDefinition)step).getTransitions();
        }
        return Collections.emptyList();
    }
}