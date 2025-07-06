package source.hanger.flow.core.runtime.data;

/**
 * FlowData<T> 是流程引擎的核心数据抽象，用于统一处理流式数据、完成信号和异常。
 * 
 * 设计动机：
 * 1. 支持流式 LLM 输出 - 逐字逐句的文本生成
 * 2. 统一数据传输协议 - 在 Actor 系统间传递消息
 * 3. 实时进度报告 - 长时间流程的状态监控
 * 4. 异步和同步的统一抽象 - 支持多种执行模式
 * 
 * @param <T> 流中传递的数据类型
 */
public abstract class FlowData<T> {
    private FlowData() {}

    /**
     * 创建中间数据片段，用于流式传输。
     * 典型场景：LLM 逐字输出、进度报告、中间结果等。
     */
    public static <T> FlowData<T> fragment(T item) {
        return new Fragment<>(item);
    }

    /**
     * 创建流完成信号，携带最终结果。
     * 表示流程正常结束，携带最终输出。
     */
    public static <T> FlowData<T> done(T finalResult) {
        return new Done<>(finalResult);
    }

    /**
     * 创建异常信号。
     * 表示流程异常终止，携带异常信息。
     */
    public static <T> FlowData<T> error(Throwable error) {
        return new Error<>(error);
    }

    /**
     * 类型检查方法
     */
    public boolean isFragment() { return this instanceof Fragment; }
    public boolean isDone() { return this instanceof Done; }
    public boolean isError() { return this instanceof Error; }

    /**
     * 获取中间数据片段。
     * @throws IllegalStateException 如果不是 Fragment 类型
     */
    public T fragment() {
        if (isFragment()) {
            return ((Fragment<T>)this).item;
        }
        throw new IllegalStateException("Not a 'fragment' data.");
    }

    /**
     * 获取最终结果。
     * @throws IllegalStateException 如果不是 Done 类型
     */
    public T result() {
        if (isDone()) {
            return ((Done<T>)this).result;
        }
        throw new IllegalStateException("Not a 'done' data.");
    }

    /**
     * 获取异常。
     * @throws IllegalStateException 如果不是 Error 类型
     */
    public Throwable error() {
        if (isError()) {
            return ((Error<T>)this).error;
        }
        throw new IllegalStateException("Not an 'error' data.");
    }

    /**
     * 中间数据类型 - 用于流式传输
     */
    private static final class Fragment<T> extends FlowData<T> {
        final T item;
        Fragment(T item) { this.item = item; }
        
        @Override
        public String toString() {
            return "Fragment(" + item + ")";
        }
    }

    /**
     * 完成信号类型 - 表示流程正常结束
     */
    private static final class Done<T> extends FlowData<T> {
        final T result;
        Done(T result) { this.result = result; }
        
        @Override
        public String toString() {
            return "Done(" + result + ")";
        }
    }

    /**
     * 异常信号类型 - 表示流程异常终止
     */
    private static final class Error<T> extends FlowData<T> {
        final Throwable error;
        Error(Throwable error) { this.error = error; }
        
        @Override
        public String toString() {
            return "Error(" + error + ")";
        }
    }
} 