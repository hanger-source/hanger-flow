package source.hanger.akkagraph.actor;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import akka.stream.javadsl.Source;
import akka.stream.javadsl.Sink;
import akka.NotUsed;
import source.hanger.akkagraph.action.INodeAction;
import source.hanger.akkagraph.model.NodeOutput;
import source.hanger.akkagraph.model.RunnableConfig;
import source.hanger.akkagraph.model.State;
import source.hanger.akkagraph.shared.FlowData;
import akka.actor.typed.ActorRef;

public class NodeActionActor extends AbstractBehavior<NodeActionActor.Command> {
    private final String nodeId;
    private final INodeAction nodeAction;

    private NodeActionActor(ActorContext<Command> ctx, String nodeId, INodeAction nodeAction) {
        super(ctx);
        this.nodeId = nodeId;
        this.nodeAction = nodeAction;
    }

    public static Behavior<Command> create(String nodeId, INodeAction nodeAction) {
        return Behaviors.setup(ctx -> new NodeActionActor(ctx, nodeId, nodeAction));
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
            .onMessage(ExecuteAction.class, this::onExecuteAction)
            .build();
    }

    private Behavior<Command> onExecuteAction(ExecuteAction msg) {
        try {
            Source<FlowData<NodeOutput>, NotUsed> source = nodeAction.execute(msg.inputState, msg.config);
            source.runWith(Sink.foreach(flowData -> {
                if (flowData.isFragment()) {
                    msg.replyTo.tell(new NodeExecutorActor.NodeActionProgress(nodeId, flowData));
                } else if (flowData.isDone()) {
                    msg.replyTo.tell(new NodeExecutorActor.NodeActionCompleted(nodeId, flowData.result()));
                } else if (flowData.isError()) {
                    getContext().getLog().error(
                        "[NodeActionActor][异常分支] 节点执行异常: nodeId={}, error={}, errorReplyTo={}, msg.replyTo={}",
                        nodeId, flowData.error(), msg.errorReplyTo, msg.replyTo);
                    msg.replyTo.tell(
                        new NodeExecutorActor.NodeActionFailed(nodeId, flowData.error(), msg.errorReplyTo));
                    getContext().getLog().info(
                        "[NodeActionActor] 已发送 NodeActionFailed: nodeId={}, error={}, errorReplyTo={}", nodeId,
                        flowData.error(), msg.errorReplyTo);
                }
            }), getContext().getSystem());
        } catch (Exception e) {
            getContext().getLog().error(
                "[NodeActionActor][execute异常] nodeId={}, errorType={}, errorMsg={}, msg.replyTo={}, msg"
                    + ".errorReplyTo={}",
                nodeId, e.getClass().getName(), e.getMessage(), msg.replyTo, msg.errorReplyTo, e);
            msg.replyTo.tell(new NodeExecutorActor.NodeActionFailed(nodeId, e, msg.errorReplyTo));
            getContext().getLog().info(
                "[NodeActionActor][catch] 已发送 NodeActionFailed: nodeId={}, errorType={}, errorMsg={}, errorReplyTo={}",
                nodeId, e.getClass().getName(), e.getMessage(), msg.errorReplyTo);
        }
        return Behaviors.same();
    }

    public interface Command {}

    public record ExecuteAction(State inputState, RunnableConfig config, ActorRef<NodeExecutorActor.Command> replyTo,
                                ActorRef<FlowData<NodeOutput>> errorReplyTo) implements Command {
    }
} 