package source.hanger.flow.dsl

import groovy.transform.Internal
import source.hanger.flow.contract.model.AsyncStepDefinition
import source.hanger.flow.dsl.hint.AsyncHint

class AsyncBuilder implements AsyncHint {
    @Internal
    private AsyncStepDefinition asyncStepDefinition

    AsyncBuilder(AsyncStepDefinition asyncStepDefinition) {
        this.asyncStepDefinition = asyncStepDefinition
    }

    void name(String text) {
        asyncStepDefinition.name = text
    }

    // DSL 关键词: description (在 parallel 内部)
    void description(String text) {
        asyncStepDefinition.description = text
    }

    void branch(String text) {
        asyncStepDefinition.addBranchName(text)
    }
}