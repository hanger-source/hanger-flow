# Groovy Flow DSL Demo

本项目演示了如何用 Groovy 的 @DelegatesTo 构建一个流程DSL，支持 flow、task、entry、agent、parallel 等核心语法。

## 目录结构

```
.
├── pom.xml
├── README.md
├── src/
│   ├── main/
│   │   ├── groovy/
│   │   │   ├── dsl/
│   │   │   │   ├── FlowDsl.groovy
│   │   │   │   ├── builder/
│   │   │   │   │   ├── FlowBuilder.groovy
│   │   │   │   │   ├── TaskBuilder.groovy
│   │   │   │   │   ├── EntryBuilder.groovy
│   │   │   │   │   ├── AgentBuilder.groovy
│   │   │   │   │   └── ParallelBuilder.groovy
│   │   │   │   └── model/
│   │   │   │       ├── Flow.groovy
│   │   │   │       ├── Task.groovy
│   │   │   │       ├── Entry.groovy
│   │   │   │       ├── Agent.groovy
│   │   │   │       ├── Parallel.groovy
│   │   │   │       ├── Next.groovy
│   │   │   │       └── Branch.groovy
│   │   │   └── demo.groovy
│   │   └── java/
│   │       └── source/
│   │           └── hanger/
│   │               └── Main.java
```

## 编译与运行

1. **编译项目**

```bash
mvn clean compile
```

2. **运行 DSL 示例**

```bash
mvn exec:java -Dexec.mainClass=source.hanger.Main
```

你将看到 demo.groovy 中定义的流程输出。

## 扩展说明

- 你可以在 `dsl/model/` 下扩展更多流程元素。
- 在 `dsl/builder/` 下添加更多 builder 支持复杂嵌套。
- 修改 `demo.groovy`，即可自定义你的业务流程 DSL。

## 依赖

- JDK 17+
- Maven 3.x
- Groovy 3.0.9

---
如有问题欢迎提issue！ 