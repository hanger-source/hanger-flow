package org.bsc.langgraph4j.akka;

import akka.actor.testkit.typed.javadsl.ActorTestKit;
import akka.actor.typed.ActorRef;
import org.bsc.langgraph4j.akka.action.ExecuteSubgraphAction;
import org.bsc.langgraph4j.akka.action.INodeAction;
import org.bsc.langgraph4j.akka.action.ParallelJoinAction;
import org.bsc.langgraph4j.akka.graph.CompiledGraphDefinition;
import org.bsc.langgraph4j.akka.graph.GraphCompiler;
import org.bsc.langgraph4j.akka.model.NodeOutput;
import org.bsc.langgraph4j.akka.model.State;
import org.bsc.langgraph4j.akka.router.ConditionalRouter;
import org.bsc.langgraph4j.akka.router.IRouter;
import org.bsc.langgraph4j.akka.actor.GraphManagerActor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

public class GraphFlowIntegrationTest {
    static final ActorTestKit testKit = ActorTestKit.create();

    @BeforeAll
    static void setup() {}

    @AfterAll
    static void teardown() {
        testKit.shutdownTestKit();
    }

    @Test
    void testFullGraphFlow() throws Exception {
        // 节点定义
        INodeAction nodeA = (state, config) -> CompletableFuture.completedFuture(NodeOutput.of(state.with("step", "A")));
        INodeAction nodeB = (state, config) -> CompletableFuture.completedFuture(NodeOutput.of(state.with("step", "B")));
        INodeAction nodeC = (state, config) -> CompletableFuture.completedFuture(NodeOutput.of(state.with("step", "C")));
        INodeAction nodeD = (state, config) -> CompletableFuture.completedFuture(NodeOutput.of(state.with("step", "D")));
        INodeAction subgraphAction = new ExecuteSubgraphAction("sub1");
        INodeAction joinAction = new ParallelJoinAction();

        // 子图节点
        GraphCompiler.GraphNodeDef subNode1 = new GraphCompiler.GraphNodeDef("sub1_node1", (state, config) -> CompletableFuture.completedFuture(NodeOutput.of(state.with("sub", 1))));
        GraphCompiler.GraphNodeDef subNode2 = new GraphCompiler.GraphNodeDef("sub1_node2", (state, config) -> CompletableFuture.completedFuture(NodeOutput.of(state.with("sub", 2))));
        List<GraphCompiler.GraphNodeDef> subNodes = List.of(subNode1, subNode2);
        List<GraphCompiler.GraphEdgeDef> subEdges = List.of(
                new GraphCompiler.GraphEdgeDef("sub1_node1", "", "sub1_node2"),
                new GraphCompiler.GraphEdgeDef("sub1_node2", "", "END")
        );
        CompiledGraphDefinition subCompiled = GraphCompiler.compile(subNodes, subEdges, "sub1_node1");

        // 主图节点
        List<GraphCompiler.GraphNodeDef> nodes = List.of(
                new GraphCompiler.GraphNodeDef("A", nodeA),
                new GraphCompiler.GraphNodeDef("B", nodeB),
                new GraphCompiler.GraphNodeDef("C", nodeC),
                new GraphCompiler.GraphNodeDef("D", nodeD),
                new GraphCompiler.GraphNodeDef("SUB", subgraphAction),
                new GraphCompiler.GraphNodeDef("JOIN", joinAction)
        );
        List<GraphCompiler.GraphEdgeDef> edges = List.of(
                new GraphCompiler.GraphEdgeDef("A", "", "B"),
                new GraphCompiler.GraphEdgeDef("B", "", "SUB"),
                new GraphCompiler.GraphEdgeDef("SUB", "", "C"),
                new GraphCompiler.GraphEdgeDef("C", "", "D"),
                new GraphCompiler.GraphEdgeDef("D", "", "JOIN"),
                new GraphCompiler.GraphEdgeDef("JOIN", "", "END")
        );
        CompiledGraphDefinition compiledGraph = GraphCompiler.compile(nodes, edges, "A");
        IRouter<State> router = new ConditionalRouter(compiledGraph);
        ActorRef<GraphManagerActor.Command> manager = testKit.spawn(GraphManagerActor.create(compiledGraph, router));
        State initialState = new State(Map.of("currentNodeId", "A"));
        manager.tell(new GraphManagerActor.StartGraph(initialState, null));
        // 这里只做流程覆盖，实际可通过 TestProbe 等断言输出
    }
} 