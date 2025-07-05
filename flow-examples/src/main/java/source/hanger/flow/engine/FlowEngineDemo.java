// src/main/java/com/example/flow/engine/FlowEngineDemo.java
package source.hanger.flow.engine;

import java.io.IOException;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

public class FlowEngineDemo {

    public static void main(String[] args) throws IOException, InstantiationException, IllegalAccessException {
        try {
            Binding binding = new Binding();
            GroovyShell shell = new GroovyShell(binding);
            Script script = shell.parse(
                new java.io.File("flow-examples/src/main/resources/script/MyComplexProcess.groovy"));
            Object result = script.run();
            System.out.println("DSL script result: " + result);
        } catch (Exception e) {
            //System.err.println("Error executing DSL script: " + e.getMessage());
            e.printStackTrace();
        }
    }
}