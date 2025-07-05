package source.hanger.flow.util

import groovy.transform.CompileStatic

import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author fuhangbo.hanger.uhfun 
 * */
@CompileStatic
class DslValidationUtils {
    /**
     * 验证某个 DSL 块是否已被定义过，如果已定义则抛出异常。
     * 主要用于确保 DSL 中某个块（如 onEnter {}, onError {}）只能出现一次。
     *
     * @param isDefinedFlag 表示该 DSL 块是否已定义过的布尔标志。
     * @param blockName DSL 块的名称 (例如 "onEnter", "onError")，用于错误消息。
     * @throws IllegalStateException 如果该 DSL 块已被定义过。
     */
    static <T> T ensureSingleDefinition(AtomicBoolean atomicBoolean, String blockName, Closure<T> closure) {
        if (atomicBoolean.get()) {
            throw new IllegalStateException("The '${blockName} {}' block can only be defined once within this scope. Multiple definitions are not allowed.")
        }
        atomicBoolean.set(true)
        closure.call()
    }

    /**
     * 生成一个关于无法读取未知属性的错误消息。
     *
     * @param contextObjectName 当前 DSL 上下文对象的名称（例如 "HttpCallStepBuilder"）
     * @param missingPropertyName 尝试读取的属性名
     * @param expectedProperties 可选：该上下文对象支持的预期属性列表
     * @return 格式化的错误消息字符串
     */
    static String getUnknownPropertyReadErrorMessage(
            String contextObjectName,
            String missingPropertyName,
            List<String> expectedProperties = null) {

        def suggestionPart = ""
        if (expectedProperties != null && !expectedProperties.isEmpty()) {
            suggestionPart = "此配置块当前支持的属性包括: ${expectedProperties.join(', ')}。"
        }

        return """
        在 '${contextObjectName}' 配置块中无法识别属性 '${missingPropertyName}'。
        请检查属性名称是否拼写正确，或确认此配置块是否支持该属性。${suggestionPart}
        更多信息请参考官方hanger Flow DSL文档。
        """.stripIndent().strip()
    }
}
