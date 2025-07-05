package source.hanger.flow.contract.model;

import source.hanger.flow.contract.runtime.common.predicate.FlowRuntimePredicate;

/**
 * @param flowRuntimePredicate 流转条件，返回 true/false
 * @param nextStepName         目标节点名称 (在 FlowDefinition.nodes 中查找)
 */
public record Transition(FlowRuntimePredicate flowRuntimePredicate, String nextStepName) {
}