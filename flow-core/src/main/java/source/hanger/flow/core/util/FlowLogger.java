package source.hanger.flow.core.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 流程执行日志工具类
 * <p>
 * 提供统一的日志格式，包含时间戳、执行ID、步骤名称、执行状态等信息。
 * 支持不同级别的日志输出，便于调试和监控。
 */
public class FlowLogger {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final AtomicLong LOG_SEQUENCE = new AtomicLong(0);
    private static final Logger logger = LoggerFactory.getLogger(FlowLogger.class);

    public static void info(FlowLogContext ctx, String message, Object... args) {
        log(Level.INFO, ctx, message, args);
    }

    public static void error(FlowLogContext ctx, String message, Object... args) {
        log(Level.ERROR, ctx, message, args);
    }
    /**
     * 新版统一日志输出方法，支持flowName、version、stepName
     */
    public static void log(Level level, FlowLogContext ctx, String message, Object... args) {
        String timestamp = LocalDateTime.now().format(FlowLogger.TIME_FORMATTER);
        String sequence = String.format("%06d", FlowLogger.LOG_SEQUENCE.incrementAndGet());
        String threadName = Thread.currentThread().getName();
        String logMessage = String.format("[%s@%s] [%s] [%s] [%s] [%s] [%s] %s %s",
            ctx.flowName, ctx.version, ctx.executionId, ctx.stepName, timestamp, sequence, threadName, level.getEmoji(),
            message);
        if (level == Level.ERROR) {
            logger.error(logMessage, args);
        } else {
            logger.info(logMessage, args);
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

}