package source.hanger.akkagraph.actor;

import akka.actor.typed.Behavior;
import akka.actor.typed.ActorRef;
import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.javadsl.*;
import source.hanger.akkagraph.action.INodeAction;
import source.hanger.akkagraph.graph.CompiledGraphDefinition;
import source.hanger.akkagraph.model.NodeOutput;
import source.hanger.akkagraph.model.RunnableConfig;
import source.hanger.akkagraph.model.State;
import source.hanger.akkagraph.router.IRouter;
import source.hanger.akkagraph.shared.FlowData;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.ActorContext;
import source.hanger.akkagraph.action.ExecuteSubgraphAction;

import java.util.Map;
import java.util.LinkedList;
import java.util.Queue;

import java.io.Serial;
import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NodeExecutorActor 是每个流程（主图/子图）的核心调度器，负责：
 * 1. 推进节点执行，持久化每一步状态（Akka Persistence）。
 * 2. 根据 router 决定下一个节点，支持分支、并行、子图等复杂流程。
 * 3. 处理节点执行结果/异常，向上游 replyTo 反馈 done/error。
 * 4. 支持自动恢复、异常重试等。
 */
public class NodeExecutorActor extends EventSourcedBehavior<NodeExecutorActor.Command, NodeExecutorActor.Event, NodeExecutorActor.StateHolder> {
    private static final Logger log = LoggerFactory.getLogger(NodeExecutorActor.class);
    private final CompiledGraphDefinition compiledGraph;
    private final IRouter<State> router;
    private final ActorContext<Command> context;

    public NodeExecutorActor(ActorContext<Command> context, String persistenceId, CompiledGraphDefinition compiledGraph,
        IRouter<State> router) {
        super(PersistenceId.ofUniqueId(persistenceId));
        this.compiledGraph = compiledGraph;
        this.router = router;
        this.context = context;
    }

    /**
     * 构造方法。
     * @param context Actor 上下文
     * @param persistenceId 持久化ID
     * @param compiledGraph 编译后的流程结构
     * @param router 路由策略
     */
    public static Behavior<Command> create(String persistenceId, CompiledGraphDefinition compiledGraph,
        IRouter<State> router) {
        return Behaviors.setup(ctx -> new NodeExecutorActor(ctx, persistenceId, compiledGraph, router));
    }

    @Override
    public StateHolder emptyState() {
        return new StateHolder(null, null, null);
    }

    @Override
    public CommandHandler<Command, Event, StateHolder> commandHandler() {
        return newCommandHandlerBuilder()
            .forAnyState()
            .onCommand(StartExecution.class, (state, cmd) -> {
                log.info("[NodeExecutorActor][onCommand] StartExecution: cmdClass={}, cmd={}, state.replyTo={}",
                    cmd.getClass().getSimpleName(), cmd, state.replyTo);
                log.info("[NodeExecutorActor][StartExecution] 收到 StartExecution: initialState={}, replyTo={}",
                    cmd.initialState, cmd.replyTo);
                String entryNodeId = compiledGraph.getEntryPointId();
                log.info("[NodeExecutorActor][StartExecution] persist NodeExecutedEvent(START) with replyTo={}",
                    cmd.replyTo);
                return Effect().persist(new NodeExecutedEvent("START", NodeOutput.of(cmd.initialState), cmd.replyTo))
                    .thenRun(newState -> {
                        log.info(
                            "[NodeExecutorActor][StartExecution] 持久化 START 事件后，推进到节点: {}，newState.replyTo={}",
                            entryNodeId, newState.replyTo);
                        context.getSelf().tell(
                            new ProceedToNextNode(entryNodeId, cmd.initialState, cmd.config, newState.replyTo));
                    });
            })
            .onCommand(ProceedToNextNode.class, (state, cmd) -> {
                log.info("[NodeExecutorActor][debug] 当前graphDef所有节点: {}，推进到nodeId: {}",
                    compiledGraph.getAllNodeIds(), cmd.nodeId);
                log.info("[NodeExecutorActor][onCommand] ProceedToNextNode: cmdClass={}, cmd={}, state.replyTo={}",
                    cmd.getClass().getSimpleName(), cmd, state.replyTo);
                log.info("[NodeExecutorActor] ProceedToNextNode: nodeId={}, state(before)={}, replyTo={}, router={}",
                    cmd.nodeId, cmd.state, cmd.replyTo, router);
                // 强制推进 currentNodeId
                State nextState = cmd.state.with("currentNodeId", cmd.nodeId);
                log.info("[NodeExecutorActor] ProceedToNextNode: nodeId={}, state(after)={}, replyTo={}, router={}",
                    cmd.nodeId, nextState, cmd.replyTo, router);
                // 新增：检查 nodeAction 是否为 null
                if ("END".equals(cmd.nodeId)) {
                    log.info("[NodeExecutorActor] 到达 END 节点，准备向 replyTo 发送 done");
                    if (state.currentNodeId != null) {
                        ActorRef<FlowData<NodeOutput>> replyTo = cmd.replyTo != null ? cmd.replyTo : state.replyTo;
                        if (replyTo != null) {
                            log.info("[NodeExecutorActor] 向 replyTo 发送 FlowData.done: {}", nextState);
                            replyTo.tell(FlowData.done(NodeOutput.of(nextState)));
                        } else {
                            log.warn("[NodeExecutorActor] replyTo 为空，无法发送 done");
                        }
                    }
                    return Effect().none();
                }
                INodeAction nodeAction = compiledGraph.getNodeAction(cmd.nodeId);
                if (nodeAction == null) {
                    log.error("[NodeExecutorActor][FATAL] nodeAction is null! nodeId={}, 已注册节点id={}", cmd.nodeId,
                        compiledGraph.getAllNodeIds());
                    throw new IllegalStateException(
                        "[NodeExecutorActor][FATAL] nodeAction is null! nodeId=" + cmd.nodeId + ", 已注册节点id="
                            + compiledGraph.getAllNodeIds());
                }
                if (nodeAction.getClass().getSimpleName().equals("ExecuteSubgraphAction")) {
                    log.info("[NodeExecutorActor] 进入子图节点: {}, router={}，replyTo={}", cmd.nodeId, router,
                        cmd.replyTo != null ? cmd.replyTo : state.replyTo);
                    ExecuteSubgraphAction subgraphAction = (ExecuteSubgraphAction)nodeAction;
                    ActorRef<SubgraphActor.Command> subgraphActor = context.spawn(SubgraphActor.create(router),
                        "subgraph-actor-" + cmd.nodeId + "-" + System.nanoTime());
                    subgraphActor.tell(new SubgraphActor.ExecuteSubgraph(nextState, cmd.config,
                        cmd.replyTo != null ? cmd.replyTo : state.replyTo, subgraphAction));
                    return Effect().none();
                } else {
                    log.info("[NodeExecutorActor] 进入普通节点: {}，启动 NodeActionActor，nextState={}", cmd.nodeId,
                        nextState);
                    ActorRef<NodeActionActor.Command> nodeActionActor = context.spawn(
                        NodeActionActor.create(cmd.nodeId, nodeAction),
                        "node-action-" + cmd.nodeId + "-" + System.nanoTime());
                    ActorRef<FlowData<NodeOutput>> errorReplyTo = cmd.replyTo;
                    log.info("[NodeExecutorActor] 向 NodeActionActor 发送 ExecuteAction: nodeId={}, nextState={}",
                        cmd.nodeId, nextState);
                    nodeActionActor.tell(
                        new NodeActionActor.ExecuteAction(nextState, cmd.config, context.getSelf(), errorReplyTo));
                }
                return Effect().none();
            })
            .onCommand(NodeActionProgress.class, (state, cmd) -> {
                log.info("[NodeExecutorActor][onCommand] NodeActionProgress: cmdClass={}, cmd={}, state.replyTo={}",
                    cmd.getClass().getSimpleName(), cmd, state.replyTo);
                log.info("[NodeExecutorActor] NodeActionProgress: nodeId={}, progress={}", cmd.nodeId, cmd.progress);
                return Effect().none();
            })
            .onCommand(NodeActionCompleted.class, (state, cmd) -> {
                log.info(
                    "[NodeExecutorActor][debug] NodeActionCompleted: nodeId={}, output.state={}, 当前graphDef所有节点: "
                        + "{}，router实例={}，state={}",
                    cmd.nodeId, cmd.output.state(), compiledGraph.getAllNodeIds(), router, state);
                log.info("[NodeExecutorActor][debug] router实例={}, router.hashCode={}", router, router.hashCode());
                String nextNodeId;
                try {
                    nextNodeId = router.route(cmd.output.state(), null);
                    log.info("[NodeExecutorActor][debug] router.route 返回: nextNodeId={}", nextNodeId);
                } catch (Exception e) {
                    log.error("[NodeExecutorActor] 路由异常: {}", e.getMessage(), e);
                    return Effect().none();
                }
                return Effect().persist(new NodeExecutedEvent(cmd.nodeId, cmd.output, state.replyTo))
                    .thenRun(newState -> {
                        log.info("[NodeExecutorActor] 持久化 NodeExecutedEvent 后，推进到节点: {}，replyTo={}",
                            nextNodeId, newState.replyTo);
                        context.getSelf().tell(
                            new ProceedToNextNode(nextNodeId, cmd.output.state(), null, newState.replyTo));
                    });
            })
            .onCommand(NodeActionFailed.class, (state, cmd) -> {
                log.info("[NodeExecutorActor][onCommand] NodeActionFailed: cmdClass={}, cmd={}, state.replyTo={}",
                    cmd.getClass().getSimpleName(), cmd, state.replyTo);
                log.error("[NodeExecutorActor][异常分支] NodeActionFailed: nodeId={}, error={}, replyTo={}", cmd.nodeId,
                    cmd.error, cmd.replyTo);
                if (cmd.replyTo != null) {
                    log.info("[NodeExecutorActor][异常分支] 向 replyTo 发送 FlowData.error: {}, replyTo={}", cmd.error,
                        cmd.replyTo);
                    cmd.replyTo.tell(FlowData.error(cmd.error));
                    log.info("[NodeExecutorActor][异常分支] 已发送 FlowData.error: nodeId={}, error={}, replyTo={}",
                        cmd.nodeId, cmd.error, cmd.replyTo);
                } else {
                    log.warn("[NodeExecutorActor][异常分支] replyTo 为空，无法发送 error: nodeId={}, error={}",
                        cmd.nodeId, cmd.error);
                }
                return Effect().none();
            })
            .build();
    }
    // ... 其他命令

    @Override
    public EventHandler<StateHolder, Event> eventHandler() {
        return (state, event) -> {
            StateHolder newState = state;
            if (event instanceof NodeExecutedEvent e) {
                ActorRef<FlowData<NodeOutput>> newReplyTo = e.replyTo != null ? e.replyTo : state.replyTo;
                log.info(
                    "[NodeExecutorActor][eventHandler] NodeExecutedEvent, nodeId={}, event.replyTo={}, state"
                        + ".replyTo={}，newReplyTo={}，状态={}",
                    e.nodeId, e.replyTo, state.replyTo, newReplyTo, state);
                newState = state.withNode(e.nodeId, e.output.state(), newReplyTo);
            } else if (event instanceof NodeFailedEvent e) {
                ActorRef<FlowData<NodeOutput>> newReplyTo = e.replyTo != null ? e.replyTo : state.replyTo;
                log.error(
                    "[NodeExecutorActor][eventHandler] NodeFailedEvent, nodeId={}, error={}, event.replyTo={}, state"
                        + ".replyTo={}，newReplyTo={}，状态={}",
                    e.nodeId, e.error, e.replyTo, state.replyTo, newReplyTo, state);
                newState = state.withNode(e.nodeId, state.currentState, newReplyTo);
            }
            // eventHandler 不做副作用
            return newState;
        };
    }
    /**
     * 命令协议：所有 NodeExecutorActor 支持的命令。
     */
    public interface Command extends Serializable {}
    // ... 其他事件

    /**
     * 事件协议：所有持久化事件。
     */
    public interface Event extends Serializable {}

    /**
     * 启动执行命令。
     * @param initialState 初始状态
     * @param config 运行配置
     * @param replyTo 结果回调
     * @param managerRef 管理者引用
     */
    public record StartExecution(State initialState, RunnableConfig config, ActorRef<FlowData<NodeOutput>> replyTo,
                                 ActorRef<GraphManagerActor.Command> managerRef) implements Command {
        @Serial
        private static final long serialVersionUID = -8490334500927286760L;
    }

    /**
     * 推进到下一个节点命令。
     * @param nodeId 节点ID
     * @param state 当前状态
     * @param config 运行配置
     * @param replyTo 结果回调
     */
    public record ProceedToNextNode(String nodeId, State state, RunnableConfig config,
                                    ActorRef<FlowData<NodeOutput>> replyTo) implements Command {
        @Serial
        private static final long serialVersionUID = 3054109923672668498L;
    }

    /**
     * 节点动作进度通知。
     * @param nodeId 节点ID
     * @param progress 进度数据
     */
    public record NodeActionProgress(String nodeId, FlowData<NodeOutput> progress) implements Command {
        @Serial
        private static final long serialVersionUID = 8162235569278814357L;
    }

    /**
     * 节点动作完成通知。
     * @param nodeId 节点ID
     * @param output 节点输出
     */
    public record NodeActionCompleted(String nodeId, NodeOutput output) implements Command {
        @Serial
        private static final long serialVersionUID = 5037436942471629607L;
    }

    /**
     * 节点动作失败通知。
     * @param nodeId 节点ID
     * @param error 异常
     * @param replyTo 结果回调
     */
    public record NodeActionFailed(String nodeId, Throwable error, ActorRef<FlowData<NodeOutput>> replyTo)
        implements Command {
        @Serial
        private static final long serialVersionUID = -163910620696284473L;
    }

    /**
     * 节点执行事件。
     * @param nodeId 节点ID
     * @param output 节点输出
     * @param replyTo 结果回调
     */
    public record NodeExecutedEvent(String nodeId, NodeOutput output, ActorRef<FlowData<NodeOutput>> replyTo)
        implements Event {
        @Serial
        private static final long serialVersionUID = 8736116906761751717L;
    }

    /**
     * 新增异常事件。
     * @param nodeId 节点ID
     * @param error 异常
     * @param replyTo 结果回调
     */
    public record NodeFailedEvent(String nodeId, Throwable error, ActorRef<FlowData<NodeOutput>> replyTo)
        implements Event {
        @Serial
        private static final long serialVersionUID = 8736116906761751718L;
    }

    /**
     * 持久化状态对象。
     * 记录当前节点、状态、replyTo、并行分支等。
     */
    public static class StateHolder implements Serializable {
        @Serial
        private static final long serialVersionUID = -7169977034185219975L;
        public final String currentNodeId;
        public final State currentState;
        public final ActorRef<FlowData<NodeOutput>> replyTo;
        public final Map<String, Boolean> parallelDoneMap;
        public final Queue<NodeActionFailed> pendingFailures;

        public StateHolder(String currentNodeId, State currentState, ActorRef<FlowData<NodeOutput>> replyTo) {
            this(currentNodeId, currentState, replyTo, null, new LinkedList<>());
        }

        public StateHolder(String currentNodeId, State currentState, ActorRef<FlowData<NodeOutput>> replyTo,
            Map<String, Boolean> parallelDoneMap) {
            this(currentNodeId, currentState, replyTo, parallelDoneMap, new LinkedList<>());
        }

        public StateHolder(String currentNodeId, State currentState, ActorRef<FlowData<NodeOutput>> replyTo,
            Map<String, Boolean> parallelDoneMap, Queue<NodeActionFailed> pendingFailures) {
            this.currentNodeId = currentNodeId;
            this.currentState = currentState;
            this.replyTo = replyTo;
            this.parallelDoneMap = parallelDoneMap;
            this.pendingFailures = pendingFailures == null ? new LinkedList<>() : pendingFailures;
        }

        public StateHolder withNode(String nodeId, State state, ActorRef<FlowData<NodeOutput>> replyTo) {
            return new StateHolder(nodeId, state, replyTo, parallelDoneMap, pendingFailures);
        }

        public StateHolder withReplyTo(ActorRef<FlowData<NodeOutput>> replyTo) {
            return new StateHolder(currentNodeId, currentState, replyTo, parallelDoneMap, pendingFailures);
        }

        public StateHolder withParallelDoneMap(Map<String, Boolean> parallelDoneMap) {
            return new StateHolder(currentNodeId, currentState, replyTo, parallelDoneMap, pendingFailures);
        }

        public StateHolder withPendingFailures(Queue<NodeActionFailed> pendingFailures) {
            return new StateHolder(currentNodeId, currentState, replyTo, parallelDoneMap, pendingFailures);
        }
    }
} 