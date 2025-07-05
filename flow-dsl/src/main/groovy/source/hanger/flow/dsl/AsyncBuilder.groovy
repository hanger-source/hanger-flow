package source.hanger.flow.dsl

import groovy.transform.Internal
import source.hanger.flow.contract.model.AsyncStepDefinition
import source.hanger.flow.dsl.hint.AsyncHint

/**
 * 异步分支块DSL构建器
 * 负责解析async { ... } DSL块，将Groovy闭包映射为异步分支模型（AsyncStepDefinition）
 * 支持异步分支名称、描述等DSL语法
 */
class AsyncBuilder implements AsyncHint {
    @Internal
    /** 当前正在构建的异步分支节点模型 */
    private AsyncStepDefinition asyncStepDefinition

    /**
     * 构造方法，初始化异步分支节点模型
     * @param asyncStepDefinition 异步分支节点模型
     */
    AsyncBuilder(AsyncStepDefinition asyncStepDefinition) {
        this.asyncStepDefinition = asyncStepDefinition
    }

    /**
     * DSL关键词：name
     * 设置异步分支块名称
     */
    void name(String text) {
        asyncStepDefinition.name = text
    }

    /**
     * DSL关键词：description
     * 设置异步分支块描述信息
     */
    void description(String text) {
        asyncStepDefinition.description = text
    }

    /**
     * DSL关键词：branch
     * 定义异步分支目标
     * @param text 分支目标任务名称
     */
    void branch(String text) {
        asyncStepDefinition.addBranchName(text)
    }
}