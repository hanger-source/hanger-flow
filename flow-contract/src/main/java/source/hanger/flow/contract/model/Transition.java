package source.hanger.flow.contract.model;

import source.hanger.flow.contract.runtime.common.FlowRuntimePredicate;

/**
 * 流程流转条件记录类
 * 定义了流程中从一个步骤到另一个步骤的流转条件
 * 
 * @param flowRuntimePredicate 流转条件，返回 true/false
 * @param nextStepName         目标节点名称 (在 FlowDefinition.nodes 中查找)
 */
public record Transition(FlowRuntimePredicate flowRuntimePredicate, String nextStepName) {
}