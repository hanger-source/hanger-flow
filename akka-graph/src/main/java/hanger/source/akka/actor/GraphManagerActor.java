package hanger.source.akka.actor;

import akka.actor.typed.Behavior;
import akka.actor.typed.ActorRef;
import akka.actor.typed.javadsl.*;
import hanger.source.akka.graph.CompiledGraphDefinition;
import hanger.source.akka.model.NodeOutput;
import hanger.source.akka.model.RunnableConfig;
import hanger.source.akka.model.State;
import hanger.source.akka.router.IRouter;
import hanger.source.akka.shared.FlowData;

/**
 * GraphManagerActor 是流程引擎的总入口和总管，负责：
 * 1. 接收外部 StartGraph/StartGraphStream 命令，启动主流程。
 * 2. 监听流程完成/失败，进行后续处理（如通知、日志等）。
 * 3. 持有主流程的 CompiledGraphDefinition 和 router。
 */
public class GraphManagerActor extends AbstractBehavior<GraphManagerActor.Command> {
    private final CompiledGraphDefinition compiledGraph; // 编译后的流程结构
    private final IRouter<State> router; // 路由策略
    private ActorRef<NodeExecutorActor.Command> nodeExecutor; // 主流程调度器
    private GraphManagerActor(ActorContext<Command> ctx, CompiledGraphDefinition compiledGraph, IRouter<State> router) {
        super(ctx);
        this.compiledGraph = compiledGraph;
        this.router = router;
        nodeExecutor = null;
    }

    /**
     * 构造方法。
     * @param ctx Actor 上下文
     * @param compiledGraph 编译后的流程结构
     * @param router 路由策略
     */
    public static Behavior<Command> create(CompiledGraphDefinition compiledGraph, IRouter<State> router) {
        return Behaviors.setup(ctx -> new GraphManagerActor(ctx, compiledGraph, router));
    }

    public static Behavior<Command> createWithPersistenceId(CompiledGraphDefinition compiledGraph,
        IRouter<State> router, String persistenceId) {
        return Behaviors.setup(ctx -> {
            final class Manager extends AbstractBehavior<Command> {
                private ActorRef<NodeExecutorActor.Command> nodeExecutor;

                Manager() {super(ctx);}

                @Override
                public Receive<Command> createReceive() {
                    return newReceiveBuilder()
                        .onMessage(StartGraph.class, msg -> {
                            nodeExecutor = ctx.spawn(NodeExecutorActor.create(persistenceId, compiledGraph, router),
                                "node-executor-" + persistenceId);
                            nodeExecutor.tell(new NodeExecutorActor.StartExecution(msg.initialState, null, msg.replyTo,
                                ctx.getSelf()));
                            return this;
                        })
                        .onMessage(StartGraphStream.class, msg -> {
                            nodeExecutor = ctx.spawn(NodeExecutorActor.create(persistenceId, compiledGraph, router),
                                "node-executor-" + persistenceId);
                            nodeExecutor.tell(
                                new NodeExecutorActor.StartExecution(msg.initialState, msg.config, msg.replyTo,
                                    ctx.getSelf()));
                            return this;
                        })
                        .onMessage(GraphCompleted.class, msg -> this)
                        .onMessage(GraphFailed.class, msg -> this)
                        .build();
                }
            }
            return new Manager();
        });
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
            .onMessage(StartGraph.class, this::onStartGraph)
            .onMessage(StartGraphStream.class, this::onStartGraphStream)
            .onMessage(GraphCompleted.class, this::onGraphCompleted)
            .onMessage(GraphFailed.class, this::onGraphFailed)
            .build();
    }

    /**
     * 处理 StartGraph 命令，启动 NodeExecutorActor（一次性结果）。
     * @param msg 启动命令
     */
    private Behavior<Command> onStartGraph(StartGraph msg) {
        nodeExecutor = getContext().spawn(NodeExecutorActor.create("main-graph", compiledGraph, router), "node-executor");
        nodeExecutor.tell(new NodeExecutorActor.StartExecution(msg.initialState, null, msg.replyTo, getContext().getSelf()));
        return this;
    }

    /**
     * 处理 StartGraphStream 命令，启动 NodeExecutorActor（流式输出）。
     * @param msg 启动命令
     */
    private Behavior<Command> onStartGraphStream(StartGraphStream msg) {
        nodeExecutor = getContext().spawn(NodeExecutorActor.create("main-graph", compiledGraph, router), "node-executor");
        nodeExecutor.tell(new NodeExecutorActor.StartExecution(msg.initialState, msg.config, msg.replyTo, getContext().getSelf()));
        return this;
    }

    /**
     * 处理图完成通知。
     * @param msg 完成消息
     */
    private Behavior<Command> onGraphCompleted(GraphCompleted msg) {
        // 可扩展：日志、通知等
        return this;
    }

    /**
     * 处理图失败通知。
     * @param msg 失败消息
     */
    private Behavior<Command> onGraphFailed(GraphFailed msg) {
        // 可扩展：日志、告警等
        return this;
    }

    /**
     * 消息协议：所有 GraphManagerActor 支持的命令。
     */
    public interface Command {}

    /**
     * 启动图命令（一次性结果）。
     * @param initialState 初始状态
     * @param replyTo 结果回调
     */
    public record StartGraph(State initialState, ActorRef<FlowData<NodeOutput>> replyTo) implements Command {
    }

    /**
     * 启动图命令（流式输出）。
     * @param initialState 初始状态
     * @param config 运行配置
     * @param replyTo 结果回调
     */
    public record StartGraphStream(State initialState, RunnableConfig config, ActorRef<FlowData<NodeOutput>> replyTo)
        implements Command {
    }

    /**
     * 图执行完成通知。
     * @param output 最终输出
     */
    public record GraphCompleted(FlowData<NodeOutput> output) implements Command {
    }

    /**
     * 图执行失败通知。
     * @param error 异常信息
     */
    public record GraphFailed(Throwable error) implements Command {
    }
}
