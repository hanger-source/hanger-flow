# CompletableFuture 流程运行时

基于CompletableFuture实现的异步流程执行引擎，支持任务、并行、异步分支等所有流程节点类型。

## 特性

- **完全异步执行**: 基于CompletableFuture实现，支持高并发
- **多种节点类型**: 支持任务、并行、异步分支等
- **完整的生命周期管理**: 提供进入、执行、错误处理回调
- **状态跟踪**: 实时跟踪流程和步骤执行状态
- **错误处理**: 完善的异常处理和恢复机制
- **线程安全**: 支持多线程并发执行

## 核心组件

### CompletableFlowEngine
流程执行引擎的核心类，负责：
- 流程定义解析和执行
- 异步任务调度
- 状态管理和监控
- 错误处理和恢复

### FlowExecutionContext
流程执行上下文，提供：
- 执行ID管理
- 参数存储和访问
- 流程定义访问
- 线程安全的数据共享

### FlowResult
流程执行结果，包含：
- 执行状态（成功/失败）
- 执行参数
- 异常信息
- 执行ID

## 使用示例

### 基本用法

```java
// 创建流程引擎
CompletableFlowEngine engine = new CompletableFlowEngine();

// 创建流程定义
FlowDefinition flowDefinition = createFlowDefinition();

// 执行流程
CompletableFuture<FlowResult> future = engine.execute(flowDefinition);

// 等待完成
FlowResult result = future.get();
```

### 创建任务步骤

```java
TaskStepDefinition task = new TaskStepDefinition();
task.setName("myTask");
task.setDescription("我的任务");
task.setTaskRunnable(access -> {
    access.log("执行任务逻辑");
    // 执行具体业务逻辑
});
task.setEnterHandingRunnable(access -> {
    access.log("任务开始执行");
});
task.setErrorHandingRunnable(access -> {
    access.log("任务执行出错: " + access.getException().getMessage());
});
```

### 创建并行步骤

```java
ParallelStepDefinition parallel = new ParallelStepDefinition();
parallel.setName("parallel");
parallel.setDescription("并行处理");

// 添加分支
Branch branch1 = new Branch(condition1, "task1");
Branch branch2 = new Branch(condition2, "task2");
parallel.addBranch(branch1);
parallel.addBranch(branch2);

// 设置汇合点
parallel.setJoinBranchNames(Arrays.asList("task1", "task2"));
```

### 创建异步步骤

```java
AsyncStepDefinition async = new AsyncStepDefinition();
async.setName("async");
async.setDescription("异步处理");

// 添加异步分支
async.addBranchName("task1");
async.addBranchName("task2");
```

## 高级特性

### 自定义线程池

```java
Executor customExecutor = Executors.newFixedThreadPool(10);
CompletableFlowEngine engine = new CompletableFlowEngine(customExecutor);
```

### 状态监控

```java
// 获取执行状态
FlowExecutionState state = engine.getExecutionState(executionId);

// 检查步骤状态
FlowStepStatus stepStatus = state.getStepStatus("task1");

// 检查是否完成
boolean completed = state.isAllStepsCompleted();
```

### 错误处理

```java
// 流程级错误处理
flowDefinition.setErrorHandingRunnable(access -> {
    access.log("流程执行出错: " + access.getException().getMessage());
});

// 任务级错误处理
taskStep.setErrorHandingRunnable(access -> {
    access.log("任务执行出错: " + access.getException().getMessage());
});
```

## 设计优势

1. **异步非阻塞**: 基于CompletableFuture，支持高并发
2. **类型安全**: 强类型接口，编译时检查
3. **可扩展性**: 支持自定义节点类型和处理器
4. **监控友好**: 提供详细的状态跟踪和日志
5. **错误恢复**: 完善的异常处理机制
6. **线程安全**: 支持多线程环境

## 与DSL集成

CompletableFuture运行时可以与现有的DSL解析器无缝集成：

```java
// 从DSL解析得到流程定义
FlowDefinition flowDef = dslParser.parse(script);

// 使用CompletableFuture运行时执行
CompletableFlowEngine engine = new CompletableFlowEngine();
CompletableFuture<FlowResult> result = engine.execute(flowDef);
```

## 性能特点

- **高并发**: 支持大量流程并发执行
- **低延迟**: 异步执行，响应迅速
- **资源高效**: 基于线程池，资源利用率高
- **可扩展**: 支持水平扩展和负载均衡

## 注意事项

1. 确保所有回调方法都是线程安全的
2. 合理配置线程池大小
3. 及时处理异常，避免流程卡死
4. 监控内存使用，避免内存泄漏
5. 在生产环境中配置适当的日志级别 