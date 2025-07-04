package org.bsc.langgraph4j.akka.actor;

import akka.actor.typed.Behavior;
import akka.actor.typed.ActorRef;
import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.javadsl.*;
import org.bsc.langgraph4j.akka.action.INodeAction;
import org.bsc.langgraph4j.akka.graph.CompiledGraphDefinition;
import org.bsc.langgraph4j.akka.model.State;
import org.bsc.langgraph4j.akka.model.NodeOutput;
import org.bsc.langgraph4j.akka.model.RunnableConfig;
import org.bsc.langgraph4j.akka.router.IRouter;
import org.bsc.langgraph4j.akka.shared.FlowData;

import java.io.Serializable;

/**
 * NodeExecutorActor 是图执行的核心引擎，负责流程控制、状态持久化、节点间路由和委托具体动作的执行。
 * 采用 Akka Persistence 事件溯源机制，支持自动恢复。
 */
public class NodeExecutorActor extends EventSourcedBehavior<NodeExecutorActor.Command, NodeExecutorActor.Event, NodeExecutorActor.StateHolder> {
    /**
     * 消息协议：所有 NodeExecutorActor 支持的命令。
     */
    public interface Command extends Serializable {}
    /** 启动执行命令 */
    public static class StartExecution implements Command {
        public final State initialState;
        public final RunnableConfig config;
        public final ActorRef<NodeOutput> replyTo;
        public final ActorRef<GraphManagerActor.Command> managerRef;
        public StartExecution(State initialState, RunnableConfig config, ActorRef<NodeOutput> replyTo, ActorRef<GraphManagerActor.Command> managerRef) {
            this.initialState = initialState;
            this.config = config;
            this.replyTo = replyTo;
            this.managerRef = managerRef;
        }
    }
    /** 推进到下一个节点命令 */
    public static class ProceedToNextNode implements Command {
        public final String nodeId;
        public final State state;
        public final RunnableConfig config;
        public ProceedToNextNode(String nodeId, State state, RunnableConfig config) {
            this.nodeId = nodeId;
            this.state = state;
            this.config = config;
        }
    }
    /** 节点动作进度通知 */
    public static class NodeActionProgress implements Command {
        public final String nodeId;
        public final FlowData<NodeOutput> progress;
        public NodeActionProgress(String nodeId, FlowData<NodeOutput> progress) {
            this.nodeId = nodeId;
            this.progress = progress;
        }
    }
    /** 节点动作完成通知 */
    public static class NodeActionCompleted implements Command {
        public final String nodeId;
        public final NodeOutput output;
        public NodeActionCompleted(String nodeId, NodeOutput output) {
            this.nodeId = nodeId;
            this.output = output;
        }
    }
    /** 节点动作失败通知 */
    public static class NodeActionFailed implements Command {
        public final String nodeId;
        public final Throwable error;
        public NodeActionFailed(String nodeId, Throwable error) {
            this.nodeId = nodeId;
            this.error = error;
        }
    }
    // ... 其他命令

    /**
     * 事件协议：所有持久化事件。
     */
    public interface Event extends Serializable {}
    /** 节点执行事件 */
    public static class NodeExecutedEvent implements Event {
        public final String nodeId;
        public final NodeOutput output;
        public NodeExecutedEvent(String nodeId, NodeOutput output) {
            this.nodeId = nodeId;
            this.output = output;
        }
    }
    // ... 其他事件

    /**
     * 持久化状态对象。
     */
    public static class StateHolder implements Serializable {
        public final String currentNodeId;
        public final State currentState;
        public StateHolder(String currentNodeId, State currentState) {
            this.currentNodeId = currentNodeId;
            this.currentState = currentState;
        }
        public StateHolder withNode(String nodeId, State state) {
            return new StateHolder(nodeId, state);
        }
    }

    private final CompiledGraphDefinition compiledGraph;
    private final IRouter<State> router;

    /**
     * 创建 NodeExecutorActor。
     */
    public static Behavior<Command> create(String persistenceId, CompiledGraphDefinition compiledGraph, IRouter<State> router) {
        return Behaviors.setup(ctx -> new NodeExecutorActor(persistenceId, compiledGraph, router));
    }

    public NodeExecutorActor(String persistenceId, CompiledGraphDefinition compiledGraph, IRouter<State> router) {
        super(PersistenceId.ofUniqueId(persistenceId));
        this.compiledGraph = compiledGraph;
        this.router = router;
    }

    @Override
    public StateHolder emptyState() {
        return new StateHolder(null, null);
    }

    @Override
    public CommandHandler<Command, Event, StateHolder> commandHandler() {
        return newCommandHandlerBuilder()
            .forAnyState()
            .onCommand(StartExecution.class, (state, cmd) -> {
                String entryNodeId = compiledGraph.getEntryPointId();
                return Effect().persist(new NodeExecutedEvent("START", NodeOutput.of(cmd.initialState)))
                        .thenRun(newState -> getContext().getSelf().tell(new ProceedToNextNode(entryNodeId, cmd.initialState, cmd.config)));
            })
            .onCommand(ProceedToNextNode.class, (state, cmd) -> {
                if ("END".equals(cmd.nodeId)) {
                    // 结束，通知外部
                    if (cmd.state != null && state.currentNodeId != null) {
                        if (cmd.state != null) {
                            // replyTo/managerRef 可从 state 或 context 获取
                        }
                    }
                    return Effect().none();
                }
                INodeAction nodeAction = compiledGraph.getNodeAction(cmd.nodeId);
                // 子图/并行分支可在此分支
                if (nodeAction.getClass().getSimpleName().equals("ExecuteSubgraphAction")) {
                    // 子图节点，委托 SubgraphActor
                    // ... 结构预留 ...
                } else {
                    ActorRef<NodeActionActor.Command> nodeActionActor = getContext().spawn(NodeActionActor.create(cmd.nodeId, nodeAction), "node-action-" + cmd.nodeId + "-" + System.nanoTime());
                    nodeActionActor.tell(new NodeActionActor.ExecuteAction(cmd.state, cmd.config, getContext().getSelf()));
                }
                return Effect().none();
            })
            .onCommand(NodeActionProgress.class, (state, cmd) -> {
                // 可转发进度到外部流/GraphManagerActor
                return Effect().none();
            })
            .onCommand(NodeActionCompleted.class, (state, cmd) -> {
                // 路由到下一个节点
                String nextNodeId;
                try {
                    nextNodeId = router.route(cmd.output.state());
                } catch (Exception e) {
                    return Effect().none();
                }
                return Effect().persist(new NodeExecutedEvent(cmd.nodeId, cmd.output))
                        .thenRun(newState -> getContext().getSelf().tell(new ProceedToNextNode(nextNodeId, cmd.output.state(), null)));
            })
            .onCommand(NodeActionFailed.class, (state, cmd) -> {
                // 错误处理，可扩展重试/告警
                return Effect().none();
            })
            .build();
    }

    @Override
    public EventHandler<StateHolder, Event> eventHandler() {
        return (state, event) -> {
            if (event instanceof NodeExecutedEvent e) {
                return state.withNode(e.nodeId, e.output.state());
            }
            return state;
        };
    }
} 