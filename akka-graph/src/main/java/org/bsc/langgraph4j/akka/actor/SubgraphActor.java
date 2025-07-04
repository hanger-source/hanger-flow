package org.bsc.langgraph4j.akka.actor;

import akka.actor.typed.Behavior;
import akka.actor.typed.ActorRef;
import akka.actor.typed.javadsl.*;
import org.bsc.langgraph4j.akka.graph.CompiledGraphDefinition;
import org.bsc.langgraph4j.akka.model.State;
import org.bsc.langgraph4j.akka.model.NodeOutput;
import org.bsc.langgraph4j.akka.router.IRouter;
import org.bsc.langgraph4j.akka.action.ExecuteSubgraphAction;
import org.bsc.langgraph4j.akka.model.RunnableConfig;
import org.bsc.langgraph4j.akka.shared.FlowData;

/**
 * 子图调度 Actor。负责启动子图 NodeExecutorActor 并转发流式结果。
 */
public class SubgraphActor extends AbstractBehavior<SubgraphActor.Command> {
    /**
     * 子图调度消息协议。
     */
    public interface Command {}
    /**
     * 执行子图命令。
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
     */
    public static class SubgraphProgress implements Command {
        public final FlowData<NodeOutput> data;
        public SubgraphProgress(FlowData<NodeOutput> data) { this.data = data; }
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(SubgraphActor::new);
    }

    private SubgraphActor(ActorContext<Command> ctx) {
        super(ctx);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
            .onMessage(ExecuteSubgraph.class, this::onExecuteSubgraph)
            .onMessage(SubgraphProgress.class, this::onSubgraphProgress)
            .build();
    }

    private Behavior<Command> onExecuteSubgraph(ExecuteSubgraph msg) {
        CompiledGraphDefinition subDef = msg.subgraphAction.subgraphDefinition();
        IRouter<State> router = null; // 可根据需要传递或构建子图路由器
        ActorRef<NodeExecutorActor.Command> subExecutor = getContext().spawn(
            NodeExecutorActor.create("subgraph-" + System.nanoTime(), subDef, router),
            "subgraph-executor-" + System.nanoTime()
        );
        // 创建一个临时 ActorRef<FlowData<NodeOutput>>，用于接收子图流式进度并转发给 replyTo
        ActorRef<FlowData<NodeOutput>> progressForwarder = getContext().spawn(
            Behaviors.receive(FlowData.class)
                .onMessage(FlowData.class, data -> {
                    msg.replyTo.tell(data);
                    return Behaviors.same();
                })
                .build(),
            "subgraph-progress-forwarder-" + System.nanoTime()
        );
        subExecutor.tell(new NodeExecutorActor.StartExecution(msg.subgraphInput, msg.config, progressForwarder, getContext().getSelf().narrow()));
        return this;
    }

    private Behavior<Command> onSubgraphProgress(SubgraphProgress msg) {
        // 兼容备用：可用于外部主动推送进度
        return this;
    }
} 