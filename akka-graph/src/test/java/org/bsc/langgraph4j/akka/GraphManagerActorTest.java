package org.bsc.langgraph4j.akka;

import akka.actor.testkit.typed.javadsl.ActorTestKit;
import akka.actor.typed.ActorRef;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class GraphManagerActorTest {
    static final ActorTestKit testKit = ActorTestKit.create();

    @BeforeAll
    static void setup() {}

    @AfterAll
    static void teardown() {
        testKit.shutdownTestKit();
    }

    @Test
    void testSimpleGraphExecution() {
        GraphCompiler.GraphNodeDef nodeA = new GraphCompiler.GraphNodeDef("A", (state, config) -> CompletableFuture.completedFuture(NodeOutput.of(state.with("a", 1))));
        GraphCompiler.GraphNodeDef nodeB = new GraphCompiler.GraphNodeDef("B", (state, config) -> CompletableFuture.completedFuture(NodeOutput.of(state.with("b", 2))));
        List<GraphCompiler.GraphNodeDef> nodes = List.of(nodeA, nodeB);
        List<GraphCompiler.GraphEdgeDef> edges = List.of(
                new GraphCompiler.GraphEdgeDef("A", "", "B"),
                new GraphCompiler.GraphEdgeDef("B", "", "END")
        );
        CompiledGraphDefinition compiledGraph = GraphCompiler.compile(nodes, edges, "A");
        IRouter<State> router = new LinearRouter(compiledGraph);
        ActorRef<GraphManagerActor.Command> manager = testKit.spawn(GraphManagerActor.create(compiledGraph, router));
        State initialState = new State(Map.of("currentNodeId", "A"));
        manager.tell(new GraphManagerActor.StartGraph(initialState, null));
        // 可扩展断言，验证状态或输出
    }
} 