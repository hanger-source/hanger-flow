package source.hanger.flow.example.visualize;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import source.hanger.flow.contract.model.FlowDefinition;
import source.hanger.flow.visualizer.FlowDslPlantUmlVisualizer;

import java.io.File;

public class MyComplexProcessPlantUmlTest {
    public static void main(String[] args) throws Exception {
        // 1. 解析Groovy DSL脚本为FlowDefinition
        Binding binding = new Binding();
        GroovyShell shell = new GroovyShell(binding);
        Object script = shell.evaluate(new File("flow-examples/src/main/resources/script/MyComplexProcess.groovy"));
        FlowDefinition flow;
        if (script instanceof FlowDefinition) {
            flow = (FlowDefinition)script;
        } else if (binding.getVariables().values().stream().anyMatch(v -> v instanceof FlowDefinition)) {
            flow = (FlowDefinition)binding.getVariables().values().stream().filter(v -> v instanceof FlowDefinition)
                .findFirst().get();
        } else {
            throw new IllegalStateException("未能从DSL脚本中获取FlowDefinition");
        }
        // 2. 生成PlantUML源码
        String plantUml = FlowDslPlantUmlVisualizer.toPlantUml(flow);
        System.out.println("\n=== PlantUML DSL ===\n");
        System.out.println(plantUml);

        String nestedStateDiagramPlantUml = FlowDslPlantUmlVisualizer.toNestedStateDiagramPlantUml(flow);
        System.out.println("\n=== NestedStateDiagramPlantUml DSL ===\n");
        System.out.println(nestedStateDiagramPlantUml);
    }
} 