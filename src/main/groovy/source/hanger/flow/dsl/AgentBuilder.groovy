// src/main/groovy/com/example/flow/dsl/AgentBuilder.groovy
package source.hanger.flow.dsl

import source.hanger.flow.model.AgentDefinition
import groovy.transform.Internal

// AgentBuilder 处理 agent 关键词内部的逻辑
class AgentBuilder {
    @Internal AgentDefinition agentDef // 当前代理模型

    AgentBuilder(AgentDefinition agentDef) {
        this.agentDef = agentDef
    }

    def name(String text) {
        agentDef.name = text
    }

    // DSL 关键词: description (在 agent 内部)
    def description(String text) {
        agentDef.description = text
    }

    // DSL 关键词: config
    def config(@DelegatesTo(value=AgentBuilder, strategy=Closure.DELEGATE_FIRST) Closure<?> closure) {
        closure.delegate = new ConfigDelegate(agentDef.config) // ConfigDelegate 代理到 Map
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.call()
    }

    // 内部类，用于处理 config { key "value" } 这样的 DSL
    @Internal
    class ConfigDelegate {
        Map<String, String> configMap

        ConfigDelegate(Map<String, String> configMap) {
            this.configMap = configMap
        }

        // 当 DSL 中使用 `apiUrl "..."` 这样的形式时，Groovy 会尝试调用 `apiUrl()` 方法
        // 这里拦截所有方法调用，将其视为配置项
        def methodMissing(String name, Object args) {
            if (args != null && args.length == 1 && args[0] instanceof String) {
                configMap.put(name, args[0])
            } else {
                throw new MissingMethodException(name, this.class, args)
            }
        }
    }

    // DSL 关键词: capability
    def capability(String name, @DelegatesTo(value=AgentBuilder, strategy=Closure.DELEGATE_FIRST) Closure<?> closure) {
        def capMap = [:] as Map // 定义能力参数
        agentDef.capabilities.put(name, capMap)

        closure.delegate = new CapabilityDelegate(capMap)
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.call()
    }

    // 内部类，用于处理 capability { parameters { to: String } } 这样的 DSL
    @Internal
    class CapabilityDelegate {
        Map<String, Map> capabilityMap

        CapabilityDelegate(Map<String, Map> capabilityMap) {
            this.capabilityMap = capabilityMap
        }

        def parameters(@DelegatesTo(value=CapabilityDelegate, strategy=Closure.DELEGATE_FIRST) Closure<?> closure) {
            def params = [:] as Map
            closure.delegate = new ParametersDelegate(params)
            closure.resolveStrategy = Closure.DELEGATE_FIRST
            closure.call()
            capabilityMap.put("parameters", params)
        }
    }

    @Internal
    class ParametersDelegate {
        Map<String, String> paramsMap

        ParametersDelegate(Map<String, String> paramsMap) {
            this.paramsMap = paramsMap
        }

        // 拦截参数定义，如 `to: String`
        def propertyMissing(String name, Object value) {
            if (value instanceof String) {
                paramsMap.put(name, value)
            } else {
                throw new MissingPropertyException(name, this.class)
            }
        }
    }
}