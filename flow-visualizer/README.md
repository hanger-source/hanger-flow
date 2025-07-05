# flow-visualizer

本模块用于将 HangerFlow DSL（FlowDefinition）流程结构可视化为 PlantUML 流程图。

## 依赖
- flow-dsl
- plantuml

## 用法示例

```java
import source.hanger.flow.contract.model.FlowDefinition;
import source.hanger.flow.visualizer.FlowDslPlantUmlVisualizer;

FlowDefinition flow = ... // 通过DSL或Java构建
String plantUml = FlowDslPlantUmlVisualizer.toPlantUml(flow);
System.out.println(plantUml); // 可直接粘贴到 PlantUML 工具渲染
```

## 典型输出

```
@startuml
title 超级复杂订单处理 @2.0.0
state "开始" as START
state "商品处理" as ITEM
START --> ITEM
...
@enduml
```

## 后续扩展
- 支持自动渲染图片
- 支持节点类型高亮、分支条件注释等 