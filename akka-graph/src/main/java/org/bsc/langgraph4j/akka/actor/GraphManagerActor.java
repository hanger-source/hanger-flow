package org.bsc.langgraph4j.akka.actor;

import akka.actor.typed.Behavior;
import akka.actor.typed.ActorRef;
import akka.actor.typed.javadsl.*;
import org.bsc.langgraph4j.akka.graph.CompiledGraphDefinition;
import org.bsc.langgraph4j.akka.model.State;
import org.bsc.langgraph4j.akka.model.NodeOutput;
import org.bsc.langgraph4j.akka.model.RunnableConfig;
import org.bsc.langgraph4j.akka.router.IRouter;
import org.bsc.langgraph4j.akka.shared.FlowData;

/**
 * GraphManagerActor 是整个图执行系统的入口和总管，负责接收外部请求、启动和监控图执行。
 * 主要职责包括：
 * <ul>
 *   <li>接收 StartGraph/StartGraphStream 命令，启动 NodeExecutorActor 执行图</li>
 *   <li>接收图完成/失败通知，进行后续处理</li>
 * </ul>
 */
public class GraphManagerActor extends AbstractBehavior<GraphManagerActor.Command> {
    /**
     * 消息协议：所有 GraphManagerActor 支持的命令。
     */
    public interface Command {}
    /**
     * 启动图命令（一次性结果）。
     */
    public static class StartGraph implements Command {
        public final State initialState;
        public final ActorRef<NodeOutput> replyTo;
        public StartGraph(State initialState, ActorRef<NodeOutput> replyTo) {
            this.initialState = initialState;
            this.replyTo = replyTo;
        }
    }
    /**
     * 启动图命令（流式输出）。
     */
    public static class StartGraphStream implements Command {
        public final State initialState;
        public final RunnableConfig config;
        public final ActorRef<FlowData<NodeOutput>> replyTo;
        public StartGraphStream(State initialState, RunnableConfig config, ActorRef<FlowData<NodeOutput>> replyTo) {
            this.initialState = initialState;
            this.config = config;
            this.replyTo = replyTo;
        }
    }
    /**
     * 图执行完成通知。
     */
    public static class GraphCompleted implements Command {
        public final NodeOutput output;
        public GraphCompleted(NodeOutput output) { this.output = output; }
    }
    /**
     * 图执行失败通知。
     */
    public static class GraphFailed implements Command {
        public final Throwable error;
        public GraphFailed(Throwable error) { this.error = error; }
    }

    private final CompiledGraphDefinition compiledGraph;
    private final IRouter<State> router;
    private ActorRef<NodeExecutorActor.Command> nodeExecutor;

    /**
     * 创建 GraphManagerActor。
     */
    public static Behavior<Command> create(CompiledGraphDefinition compiledGraph, IRouter<State> router) {
        return Behaviors.setup(ctx -> new GraphManagerActor(ctx, compiledGraph, router));
    }

    private GraphManagerActor(ActorContext<Command> ctx, CompiledGraphDefinition compiledGraph, IRouter<State> router) {
        super(ctx);
        this.compiledGraph = compiledGraph;
        this.router = router;
        this.nodeExecutor = null;
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
     */
    private Behavior<Command> onStartGraph(StartGraph msg) {
        nodeExecutor = getContext().spawn(NodeExecutorActor.create("main-graph", compiledGraph, router), "node-executor");
        nodeExecutor.tell(new NodeExecutorActor.StartExecution(msg.initialState, null, msg.replyTo, getContext().getSelf()));
        return this;
    }
    /**
     * 处理 StartGraphStream 命令，启动 NodeExecutorActor（流式输出）。
     */
    private Behavior<Command> onStartGraphStream(StartGraphStream msg) {
        nodeExecutor = getContext().spawn(NodeExecutorActor.create("main-graph", compiledGraph, router), "node-executor");
        nodeExecutor.tell(new NodeExecutorActor.StartExecution(msg.initialState, msg.config, msg.replyTo, getContext().getSelf()));
        return this;
    }
    /**
     * 处理图完成通知。
     */
    private Behavior<Command> onGraphCompleted(GraphCompleted msg) {
        // 可扩展：日志、通知等
        return this;
    }
    /**
     * 处理图失败通知。
     */
    private Behavior<Command> onGraphFailed(GraphFailed msg) {
        // 可扩展：日志、告警等
        return this;
    }
}
