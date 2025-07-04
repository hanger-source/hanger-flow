package org.bsc.langgraph4j.akka.actor;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import akka.stream.javadsl.Source;
import akka.stream.javadsl.Sink;
import akka.NotUsed;
import org.bsc.langgraph4j.akka.action.INodeAction;
import org.bsc.langgraph4j.akka.model.State;
import org.bsc.langgraph4j.akka.model.NodeOutput;
import org.bsc.langgraph4j.akka.model.RunnableConfig;
import org.bsc.langgraph4j.akka.shared.FlowData;

public class NodeActionActor extends AbstractBehavior<NodeActionActor.Command> {
    public interface Command {}
    public static class ExecuteAction implements Command {
        public final State inputState;
        public final RunnableConfig config;
        public final ActorRef<NodeExecutorActor.Command> replyTo;
        public ExecuteAction(State inputState, RunnableConfig config, ActorRef<NodeExecutorActor.Command> replyTo) {
            this.inputState = inputState;
            this.config = config;
            this.replyTo = replyTo;
        }
    }

    private final String nodeId;
    private final INodeAction nodeAction;

    public static Behavior<Command> create(String nodeId, INodeAction nodeAction) {
        return Behaviors.setup(ctx -> new NodeActionActor(ctx, nodeId, nodeAction));
    }

    private NodeActionActor(ActorContext<Command> ctx, String nodeId, INodeAction nodeAction) {
        super(ctx);
        this.nodeId = nodeId;
        this.nodeAction = nodeAction;
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
            .onMessage(ExecuteAction.class, this::onExecuteAction)
            .build();
    }

    private Behavior<Command> onExecuteAction(ExecuteAction msg) {
        Source<FlowData<NodeOutput>, NotUsed> source = nodeAction.execute(msg.inputState, msg.config);
        source.runWith(Sink.foreach(flowData -> {
            if (flowData.isFragment()) {
                msg.replyTo.tell(new NodeExecutorActor.NodeActionProgress(nodeId, flowData));
            } else if (flowData.isDone()) {
                msg.replyTo.tell(new NodeExecutorActor.NodeActionCompleted(nodeId, flowData.result()));
            } else if (flowData.isError()) {
                msg.replyTo.tell(new NodeExecutorActor.NodeActionFailed(nodeId, flowData.error()));
            }
        }), getContext().getSystem());
        return this;
    }
} 