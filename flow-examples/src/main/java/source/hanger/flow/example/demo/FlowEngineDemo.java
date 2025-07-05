package source.hanger.flow.example.demo;

import java.io.IOException;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlowEngineDemo {

    private static final Logger logger = LoggerFactory.getLogger(FlowEngineDemo.class);

    public static void main(String[] args) throws IOException, InstantiationException, IllegalAccessException {
        try {
            Binding binding = new Binding();
            GroovyShell shell = new GroovyShell(binding);
            Script script = shell.parse(
                new java.io.File("flow-examples/src/main/resources/script/MyComplexProcess.groovy"));
            Object result = script.run();
            logger.info("DSL script result: {}", result);
        } catch (Exception e) {
            //System.err.println("Error executing DSL script: " + e.getMessage());
            e.printStackTrace();
        }
    }
}