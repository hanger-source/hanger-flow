package source.hanger.flow.core.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 流程执行日志工具类
 * <p>
 * 提供统一的日志格式，包含时间戳、执行ID、步骤名称、执行状态等信息。
 * 支持不同级别的日志输出，便于调试和监控。
 */
public class FlowLogger {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final AtomicLong LOG_SEQUENCE = new AtomicLong(0);

    /**
     * 新版统一日志输出方法，支持flowName、version、stepName
     */
    public static void log(Level level, FlowLogContext ctx, String message) {
        String timestamp = LocalDateTime.now().format(FlowLogger.TIME_FORMATTER);
        String sequence = String.format("%06d", FlowLogger.LOG_SEQUENCE.incrementAndGet());
        String threadName = Thread.currentThread().getName();
        String logMessage = String.format("[%s@%s] [%s] [%s] [%s] [%s] [%s] %s %s",
            ctx.flowName, ctx.version, ctx.executionId, ctx.stepName, timestamp, sequence, threadName, level.getEmoji(),
            message);
        if (level == Level.ERROR) {
            System.err.println(logMessage);
        } else {
            System.out.println(logMessage);
        }
    }

    /**
     * 日志级别枚举
     */
    public enum Level {
        INFO("ℹ️"),
        SUCCESS("✅"),
        WARNING("⚠️"),
        ERROR("❌"),
        DEBUG("🔍");

        private final String emoji;

        Level(String emoji) {
            this.emoji = emoji;
        }

        public String getEmoji() {
            return emoji;
        }
    }

    /**
     * 日志上下文对象，统一封装日志所需所有元信息
     */
    public record FlowLogContext(String flowName, String version, String executionId, String stepName) {
    }
} 