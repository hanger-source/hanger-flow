package source.hanger.flow.model

class AgentDefinition {
    String name
    String description
    Map<String, String> config = [:] // 代理配置
    Map<String, Map> capabilities = [:] // 代理能力定义

    String toString() { "Agent(name='$name')" }
}