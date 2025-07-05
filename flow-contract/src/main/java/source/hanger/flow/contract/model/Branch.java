package source.hanger.flow.contract.model;

import source.hanger.flow.contract.runtime.common.predicate.FlowRuntimePredicate;

/**
 *
 */
public record Branch(FlowRuntimePredicate flowRuntimePredicate, String nextStepName) {
}