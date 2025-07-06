package source.hanger.akkagraph.actor;

import akka.actor.typed.Behavior;
import akka.actor.typed.ActorRef;
import akka.actor.typed.javadsl.*;
import source.hanger.akkagraph.action.ExecuteSubgraphAction;
import source.hanger.akkagraph.graph.CompiledGraphDefinition;
import source.hanger.akkagraph.model.NodeOutput;
import source.hanger.akkagraph.model.RunnableConfig;
import source.hanger.akkagraph.model.State;
import source.hanger.akkagraph.router.IRouter;
import source.hanger.akkagraph.shared.FlowData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SubgraphActor 负责调度子图流程：
 * 1. 启动子图 NodeExecutorActor，传递本层 router。
 * 2. 将子图的进度/结果转发给父流程的 replyTo。
 */
public class SubgraphActor extends AbstractBehavior<SubgraphActor.Command> {
    private static final Logger log = LoggerFactory.getLogger(SubgraphActor.class);
    private final IRouter<State> router; // 路由策略

    private SubgraphActor(ActorContext<Command> ctx, IRouter<State> router) {
        super(ctx);
        this.router = router;
    }

    /**
     * 构造方法。
     * @param router 路由策略
     */
    public static Behavior<Command> create(IRouter<State> router) {
        return Behaviors.setup(ctx -> new SubgraphActor(ctx, router));
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
            .onMessage(ExecuteSubgraph.class, this::onExecuteSubgraph)
            .onMessage(SubgraphProgress.class, this::onSubgraphProgress)
            .build();
    }

    /**
     * 处理 ExecuteSubgraph 命令，启动子图 NodeExecutorActor。
     * @param msg 执行子图命令
     */
    private Behavior<Command> onExecuteSubgraph(ExecuteSubgraph msg) {
        CompiledGraphDefinition subDef = msg.subgraphAction.subgraphDefinition();
        log.info("[SubgraphActor] 启动子图: subDef={}, router={}, replyTo={}", subDef, router, msg.replyTo);
        IRouter<State> subRouter = msg.subgraphAction.router();
        log.info("[SubgraphActor][debug] 启动子图: router实例={}, router.hashCode={}, subDef.nodes={}", subRouter, subRouter != null ? subRouter.hashCode() : null, subDef.getAllNodeIds());
        log.info("[SubgraphActor][debug] 子图所有节点: {}", subDef.getAllNodeIds());
        ActorRef<NodeExecutorActor.Command> subExecutor = getContext().spawn(
            NodeExecutorActor.create("subgraph-" + System.nanoTime(), subDef, subRouter),
            "subgraph-executor-" + System.nanoTime()
        );
        // 创建一个临时 ActorRef<FlowData<NodeOutput>>，用于接收子图流式进度并转发给 replyTo
        ActorRef<FlowData<NodeOutput>> progressForwarder = getContext().spawn(
            Behaviors.receive((Class<FlowData<NodeOutput>>) (Class<?>) FlowData.class)
                .onMessage((Class<FlowData<NodeOutput>>) (Class<?>) FlowData.class, data -> {
                    log.info("[SubgraphActor] 子图进度转发: {} -> {}", data, msg.replyTo);
                    msg.replyTo.tell((FlowData<NodeOutput>) data);
                    return Behaviors.same();
                })
                .build(),
            "subgraph-progress-forwarder-" + System.nanoTime()
        );
        log.info("[SubgraphActor] 启动子图 NodeExecutorActor: subExecutor={}, progressForwarder={}", subExecutor, progressForwarder);
        subExecutor.tell(new NodeExecutorActor.StartExecution(msg.subgraphInput, msg.config, progressForwarder, (ActorRef) getContext().getSelf()));
        return this;
    }

    /**
     * 处理子图进度转发命令。
     * @param msg 进度消息
     */
    private Behavior<Command> onSubgraphProgress(SubgraphProgress msg) {
        // 兼容备用：可用于外部主动推送进度
        return this;
    }

    /**
     * 子图调度消息协议。
     */
    public interface Command {}

    /**
     * 执行子图命令。
     * @param subgraphInput 子图输入状态
     * @param config 运行配置
     * @param replyTo 结果回调
     * @param subgraphAction 子图动作
     */
    public static class ExecuteSubgraph implements Command {
        public final State subgraphInput;
        public final RunnableConfig config;
        public final ActorRef<FlowData<NodeOutput>> replyTo;
        public final ExecuteSubgraphAction subgraphAction;
        public ExecuteSubgraph(State subgraphInput, RunnableConfig config, ActorRef<FlowData<NodeOutput>> replyTo, ExecuteSubgraphAction subgraphAction) {
            this.subgraphInput = subgraphInput;
            this.config = config;
            this.replyTo = replyTo;
            this.subgraphAction = subgraphAction;
        }
    }

    /**
     * 子图流式进度转发命令。
     * @param data 进度数据
     */
    public static class SubgraphProgress implements Command {
        public final FlowData<NodeOutput> data;
        public SubgraphProgress(FlowData<NodeOutput> data) { this.data = data; }
    }
} 