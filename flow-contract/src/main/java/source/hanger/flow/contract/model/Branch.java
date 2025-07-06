package source.hanger.flow.contract.model;

import source.hanger.flow.contract.runtime.common.FlowRuntimePredicate;

/**
 * 并行/异步分支定义（Branch）
 * <p>
 * 作用：
 *   - 表示并行或异步节点中的单个分支
 *   - 包含分支的条件表达式和目标节点名称
 * <p>
 * 典型用法：
 *   - 由DSL branch 'xxx' when { ... } 语法块生成
 *   - 用于ParallelStepDefinition、AsyncStepDefinition等模型中
 * <p>
 * 字段说明：
 *   - flowRuntimePredicate：分支条件，返回true时该分支被激活
 *   - nextStepName：分支目标节点名称
 * <p>
 * 设计说明：
 *   - 支持灵活的分支条件和目标配置
 *   - 可扩展更多分支属性（如优先级、标签等）
 */
public record Branch(FlowRuntimePredicate flowRuntimePredicate, String nextStepName) {
}