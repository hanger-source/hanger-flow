# Hanger Flow 流程引擎（Groovy DSL Demo）

本项目是一个基于 Java/Groovy 的高扩展性流程引擎，支持流程编排、异步/并行、结构化日志、DSL自定义、类型安全参数链路等特性。适用于业务流程自动化、复杂任务流、可视化流程等场景。

---

## 目录结构与设计理念

```
.
├── flow-examples/              # 【案例层】典型流程案例与主类，面向业务/测试/演示
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/source/hanger/flow/engine/
│   │   │   │   ├── CompletableFlowExample.java         # 复杂流程主案例，演示全链路推进与日志
│   │   │   │   ├── DslCompletableFlowExample.java      # 纯DSL驱动的流程案例
│   │   │   │   ├── EnhancedLoggingExample.java         # 日志增强与结构化输出演示
│   │   │   │   ├── FlowEngineDemo.java                 # 最简流程引擎演示
│   │   │   │   ├── MyComplexProcessTest.java           # 复杂流程测试用例
│   │   │   │   └── SimpleCompletableFlowTest.java      # 简单流程测试用例
│   │   └── resources/script/
│   │       └── MyComplexProcess.groovy                 # 复杂订单流程DSL脚本
│   └── pom.xml
├── flow-completable-runtime/   # 【引擎实现层】可组合、异步、并行等核心执行逻辑
│   └── src/main/java/source/hanger/flow/completable/runtime/
│       ├── CompletableFlowEngine.java                  # 流程引擎主类
│       ├── AsyncStepExecutor.java                      # 异步步骤执行器
│       ├── ParallelStepExecutor.java                   # 并行步骤执行器
│       ├── ...（Access、Context、Lifecycle、Util等）
├── flow-dsl/                   # 【DSL解析层】Groovy DSL 解析、校验、构建
│   └── src/main/groovy/source/hanger/flow/dsl/
│       ├── FlowBuilder.groovy                          # 流程构建器
│       ├── AsyncBuilder.groovy                         # 异步分支构建
│       ├── ...（Hint、Util等）
├── flow-core/                  # 【基础能力层】流程上下文、生命周期、日志、执行状态
│   └── src/main/java/source/hanger/flow/core/runtime/
│       ├── FlowExecutionContext.java                   # 执行上下文
│       ├── FlowLogger.java                             # 统一日志入口
│       ├── ...（Handler、Result、Status等）
├── flow-contract/              # 【契约与模型层】流程/步骤/分支/参数等接口与数据结构
│   └── src/main/java/source/hanger/flow/contract/
│       ├── model/StepDefinition.java                   # 步骤定义
│       ├── model/FlowDefinition.java                   # 流程定义
│       ├── ...（Transition、Branch、Async等）
│       ├── runtime/                                    # 运行时接口与上下文
│       └── ...
├── pom.xml                     # 父级Maven依赖与聚合
├── README.md                   # 项目说明文档
└── ...
```

### 设计原因与分层说明

#### 1. flow-examples/（案例层）

- **作用**：沉淀所有典型业务流程、测试用例、演示主类，便于团队和新手一键复现、对比、回归。
- **结构**：每个主类对应一个业务场景，配套 DSL 脚本，支持多种流程分支、并行、异步等复杂场景。
- **好处**：案例与引擎解耦，便于扩展和维护；每个案例都可独立运行和分析。

#### 2. flow-completable-runtime/（引擎实现层）

- **作用**：实现流程的核心推进、异步/并行调度、生命周期回调、日志钩子等。
- **结构**：分为执行器、上下文、访问器、生命周期、工具类等，职责清晰。
- **好处**：所有流程推进逻辑集中于此，便于优化性能和扩展新特性。

#### 3. flow-dsl/（DSL解析层）

- **作用**：负责 Groovy DSL 的语法解析、校验、构建流程定义对象。
- **结构**：Builder、Hint、Util 等，支持灵活扩展 DSL 语法。
- **好处**：DSL 解析与引擎解耦，支持多种业务自定义流程结构。

#### 4. flow-core/（基础能力层）

- **作用**：提供流程执行上下文、生命周期管理、统一日志、执行状态等基础能力。
- **结构**：Context、Logger、Handler、Result、Status 等。
- **好处**：为上层引擎和DSL提供通用能力，便于横向扩展和复用。

#### 5. flow-contract/（契约与模型层）

- **作用**：定义所有流程、步骤、分支、参数、上下文等接口和数据结构。
- **结构**：model（定义）、runtime（运行时接口）、function（回调函数）等。
- **好处**：契约先行，保证各层解耦和类型安全，便于多团队协作和扩展。

---

### 目录分层的核心理念

- **解耦**：案例、引擎、DSL、基础能力、契约模型完全分离，互不影响。
- **可扩展**：每一层都可独立扩展、替换、测试，支持多业务场景和新特性。
- **可维护**：结构清晰，便于新成员快速理解和定位问题。
- **可复现**：所有案例都可一键运行，便于回归和对比。

---

## 快速开始

### 1. 编译项目

```bash
mvn clean package
```

### 2. 运行典型流程案例

以“订单处理与通知”流程为例，执行：

```bash
java -cp "flow-examples/target/classes:flow-completable-runtime/target/classes:flow-dsl/target/classes:flow-core/target/classes:flow-contract/target/classes:/Users/fuhangbo/.m2/repository/org/slf4j/slf4j-simple/2.0.13/slf4j-simple-2.0.13.jar:/Users/fuhangbo/.m2/repository/org/slf4j/slf4j-api/2.0.13/slf4j-api-2.0.13.jar:/Users/fuhangbo/.m2/repository/org/codehaus/groovy/groovy/3.0.9/groovy-3.0.9.jar" source.hanger.flow.engine.CompletableFlowExample
```

你将看到结构化的流程推进日志和最终参数输出。

---

## 典型案例沉淀

你可以将每个主类/DSL案例的运行命令、说明、预期输出追加到本节，便于团队一键复现和对比：

### 案例一：订单处理与通知

- 入口类：`source.hanger.flow.engine.CompletableFlowExample`
- DSL脚本：`flow-examples/src/main/resources/script/MyComplexProcess.groovy`
- 运行命令：见上

**预期输出：**

- 结构化日志，完整推进每个节点（如订单初始化、库存检查、通知库存不足等）
- 最终参数如：`status=库存不足，已关闭`，`stockOk=false` 等

### 案例二：自定义流程（可补充）

- 入口类/DSL脚本/命令/预期输出...

---

## 主要特性

- **Groovy DSL**：支持自定义流程结构，语法灵活，业务可读性强
- **异步/并行/分支**：支持复杂流程编排
- **结构化日志**：统一格式，支持 traceId、stepName、flowName 等上下文
- **参数链路**：DSL、AccessContext、日志全链路参数一致
- **类型安全**：全局 Map<String, Serializable> 参数体系，类型兼容
- **自动推进**：支持递归推进所有节点，兼容分支、并行、异步
- **自动化测试**：可一键运行主类，输出完整日志

---

## 依赖环境

- JDK 17+
- Maven 3.x
- Groovy 3.0.9
- SLF4J 2.x

---

## 扩展与自定义

- 新增流程/步骤/分支：编辑 DSL 脚本或 Java/Groovy 类
- 扩展日志/参数：实现自定义 AccessContext、FlowLogger
- 兼容更多业务场景：参考 flow-examples/ 目录下案例

---

## 常见问题

- **依赖缺失/无法运行？**  
  请确保 Maven 仓库可用，已执行 `mvn package`，并按上方命令拼接 classpath。
- **日志未输出？**  
  检查 slf4j-simple 是否在 classpath，或查看日志级别设置。

---

## 贡献与反馈

如有问题、建议或新案例，欢迎提 issue 或 PR！

---