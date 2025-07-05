package source.hanger.flow.contract.factory;

import source.hanger.flow.contract.model.FlowDefinition;

/**
 * @author fuhangbo.hanger.uhfun
 **/
public class DefinitionFactory {
    public static FlowDefinition createFlowDefinition() {
        return new FlowDefinition();
    }
}
