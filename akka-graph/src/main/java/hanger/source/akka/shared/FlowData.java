package hanger.source.akka.shared;

/**
 * FlowData<T> 用于封装流式节点/图执行过程中的数据、完成信号和异常。
 * 统一支持流式 LLM 输出、进度报告、最终结果和异常传递。
 * 典型用法：所有 INodeAction、Actor 间流式消息、外部流式消费均基于此协议。
 *
 * @param <T> 流中传递的数据类型（如 NodeOutput）
 */
public abstract class FlowData<T> {
    private FlowData() {}

    /**
     * 创建中间数据块。
     */
    public static <T> FlowData<T> of(T item) {
        return new Fragment<>(item);
    }
    /**
     * 创建流完成信号，携带最终结果。
     */
    public static <T> FlowData<T> done(T finalResult) {
        return new Done<>(finalResult);
    }
    /**
     * 创建异常信号。
     */
    public static <T> FlowData<T> error(Throwable error) {
        return new Error<>(error);
    }
    public boolean isFragment() { return this instanceof Fragment; }
    public boolean isDone() { return this instanceof Done; }
    public boolean isError() { return this instanceof Error; }
    /**
     * 获取中间数据片段。
     */
    public T fragment() {
        if (isFragment()) {
            return ((Fragment<T>)this).item;
        }
        throw new IllegalStateException("Not a 'fragment' data.");
    }
    /**
     * 获取最终结果。
     */
    public T result() {
        if (isDone()) {
            return ((Done<T>)this).result;
        }
        throw new IllegalStateException("Not a 'done' data.");
    }
    /**
     * 获取异常。
     */
    public Throwable error() {
        if (isError()) {
            return ((Error<T>)this).error;
        }
        throw new IllegalStateException("Not an 'error' data.");
    }
    /**
     * 中间数据类型。
     */
    private static final class Fragment<T> extends FlowData<T> {
        final T item;
        Fragment(T item) { this.item = item; }
    }
    /**
     * 完成信号类型。
     */
    private static final class Done<T> extends FlowData<T> {
        final T result;
        Done(T result) { this.result = result; }
    }
    /**
     * 异常信号类型。
     */
    private static final class Error<T> extends FlowData<T> {
        final Throwable error;
        Error(Throwable error) { this.error = error; }
    }
} 