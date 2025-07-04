# akka-graph

本模块为 LangGraph4j 提供 Akka Actor 驱动的高并发、消息驱动、事件溯源的图执行引擎实现。

## 主要特性
- 基于 Akka Typed Actor 和 Akka Persistence
- 完全异步、非阻塞的图执行模型
- 支持事件溯源与自动恢复
- 支持并行、分支、子图等复杂控制流

## 用法
1. 定义 GraphDefinition、NodeId 等核心结构
2. 使用 NodeExecutorActor 启动和驱动图执行
3. 通过消息协议与 Actor 交互，实现高弹性 AI 工作流

详细 API 和示例请见源码与测试用例。 