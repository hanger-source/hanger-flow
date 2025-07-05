package hanger.source.akka;

import akka.actor.testkit.typed.javadsl.ActorTestKit;
import akka.actor.typed.ActorRef;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import hanger.source.akka.graph.GraphCompiler;
import hanger.source.akka.graph.CompiledGraphDefinition;
import hanger.source.akka.router.IRouter;
import hanger.source.akka.model.State;
import hanger.source.akka.model.NodeOutput;
import hanger.source.akka.router.LinearRouter;
import hanger.source.akka.shared.FlowData;
import akka.stream.javadsl.Source;
import hanger.source.akka.actor.GraphManagerActor;
import akka.actor.testkit.typed.javadsl.TestProbe;
import hanger.source.akka.action.ExecuteSubgraphAction;

public class GraphManagerActorTest {
    static final ActorTestKit testKit = ActorTestKit.create();

    @BeforeAll
    static void setup() {}

    @AfterAll
    static void teardown() {
        testKit.shutdownTestKit();
    }

    @Test
    void testNodeExceptionHandling() {
        String persistenceId = "main-graph-" + System.nanoTime();
        GraphCompiler.GraphNodeDef nodeA = new GraphCompiler.GraphNodeDef("A", (state, config) -> {
            throw new RuntimeException("节点A异常");
        });
        List<GraphCompiler.GraphNodeDef> nodes = List.of(nodeA);
        List<GraphCompiler.GraphEdgeDef> edges = List.of();
        CompiledGraphDefinition compiledGraph = GraphCompiler.compile(nodes, edges, "A");
        IRouter<State> router = new LinearRouter(List.of("A", "END"));
        ActorRef<GraphManagerActor.Command> manager = testKit.spawn(
            GraphManagerActor.createWithPersistenceId(compiledGraph, router, persistenceId));
        State initialState = new State(Map.of("currentNodeId", "A"));
        TestProbe<FlowData<NodeOutput>> probe = testKit.createTestProbe();
        System.out.println("[TEST][testNodeExceptionHandling] probe.getRef()=" + probe.getRef());
        manager.tell(new GraphManagerActor.StartGraph(initialState, probe.getRef()));
        System.out.println("[TEST][testNodeExceptionHandling] manager.tell 后");
        System.out.println("[TEST][testNodeExceptionHandling] probe.receiveMessage 前");
        FlowData<NodeOutput> result = probe.receiveMessage();
        System.out.println("[TEST][testNodeExceptionHandling] probe.receiveMessage 后: result=" + result);
        assert result.isError();
        assert result.error().getMessage().contains("节点A异常");
    }

    @Test
    void testParallelAndSubgraph() {
        String persistenceId = "main-graph-" + System.nanoTime();
        // 子图
        GraphCompiler.GraphNodeDef subNode1 = new GraphCompiler.GraphNodeDef("sub1_node1",
            (state, config) -> Source.single(FlowData.done(NodeOutput.of(state.with("sub", 1)))));
        GraphCompiler.GraphNodeDef subNode2 = new GraphCompiler.GraphNodeDef("sub1_node2",
            (state, config) -> Source.single(FlowData.done(NodeOutput.of(state.with("sub", 2)))));
        List<GraphCompiler.GraphNodeDef> subNodes = List.of(subNode1, subNode2);
        List<GraphCompiler.GraphEdgeDef> subEdges = List.of(
            new GraphCompiler.GraphEdgeDef("sub1_node1", "", "sub1_node2"),
            new GraphCompiler.GraphEdgeDef("sub1_node2", "", "END")
        );
        CompiledGraphDefinition subCompiled = GraphCompiler.compile(subNodes, subEdges, "sub1_node1");
        // 主图
        GraphCompiler.GraphNodeDef nodeA = new GraphCompiler.GraphNodeDef("A",
            (state, config) -> Source.single(FlowData.done(NodeOutput.of(state.with("a", 1)))));
        GraphCompiler.GraphNodeDef nodeB = new GraphCompiler.GraphNodeDef("B",
            (state, config) -> Source.single(FlowData.done(NodeOutput.of(state.with("b", 2)))));
        GraphCompiler.GraphNodeDef subgraph = new GraphCompiler.GraphNodeDef("SUB",
            new ExecuteSubgraphAction("sub1", subCompiled, new LinearRouter(List.of("sub1_node1", "sub1_node2", "END"))));
        List<GraphCompiler.GraphNodeDef> nodes = List.of(nodeA, nodeB, subgraph);
        List<GraphCompiler.GraphEdgeDef> edges = List.of(
            new GraphCompiler.GraphEdgeDef("A", "", "B"),
            new GraphCompiler.GraphEdgeDef("B", "", "SUB"),
            new GraphCompiler.GraphEdgeDef("SUB", "", "END")
        );
        CompiledGraphDefinition compiledGraph = GraphCompiler.compile(nodes, edges, "A");
        IRouter<State> router = new LinearRouter(List.of("A", "B", "SUB", "END"));
        ActorRef<GraphManagerActor.Command> manager = testKit.spawn(
            GraphManagerActor.createWithPersistenceId(compiledGraph, router, persistenceId));
        State initialState = new State(Map.of("currentNodeId", "A"));
        TestProbe<FlowData<NodeOutput>> probe = testKit.createTestProbe();
        manager.tell(new GraphManagerActor.StartGraph(initialState, probe.getRef()));
        FlowData<NodeOutput> result = probe.receiveMessage();
        assert result.isDone();
        assert result.result().state().with("a", 1).with("b", 2).with("sub", 2) != null;
    }

    @Test
    void testNestedSubgraphAndParallel() {
        String persistenceId = "complex-graph-1-" + System.nanoTime();
        // 子图2
        GraphCompiler.GraphNodeDef sub2_node1 = new GraphCompiler.GraphNodeDef("sub2_node1",
            (state, config) -> Source.single(FlowData.done(NodeOutput.of(state.with("sub2", 1)))));
        GraphCompiler.GraphNodeDef sub2_node2 = new GraphCompiler.GraphNodeDef("sub2_node2",
            (state, config) -> Source.single(FlowData.done(NodeOutput.of(state.with("sub2", 2)))));
        List<GraphCompiler.GraphNodeDef> sub2Nodes = List.of(sub2_node1, sub2_node2);
        List<GraphCompiler.GraphEdgeDef> sub2Edges = List.of(
            new GraphCompiler.GraphEdgeDef("sub2_node1", "", "sub2_node2"),
            new GraphCompiler.GraphEdgeDef("sub2_node2", "", "END")
        );
        CompiledGraphDefinition sub2Compiled = GraphCompiler.compile(sub2Nodes, sub2Edges, "sub2_node1");
        // 子图1
        GraphCompiler.GraphNodeDef sub1_node1 = new GraphCompiler.GraphNodeDef("sub1_node1",
            (state, config) -> Source.single(FlowData.done(NodeOutput.of(state.with("sub1", 1)))));
        GraphCompiler.GraphNodeDef sub1_node2 = new GraphCompiler.GraphNodeDef("sub1_node2",
            new ExecuteSubgraphAction("sub2", sub2Compiled, new LinearRouter(List.of("sub2_node1", "sub2_node2", "END"))));
        List<GraphCompiler.GraphNodeDef> sub1Nodes = List.of(sub1_node1, sub1_node2);
        List<GraphCompiler.GraphEdgeDef> sub1Edges = List.of(
            new GraphCompiler.GraphEdgeDef("sub1_node1", "", "sub1_node2"),
            new GraphCompiler.GraphEdgeDef("sub1_node2", "", "END")
        );
        CompiledGraphDefinition sub1Compiled = GraphCompiler.compile(sub1Nodes, sub1Edges, "sub1_node1");
        // 主图
        GraphCompiler.GraphNodeDef nodeA = new GraphCompiler.GraphNodeDef("A",
            (state, config) -> Source.single(FlowData.done(NodeOutput.of(state.with("a", 1)))));
        GraphCompiler.GraphNodeDef nodeB = new GraphCompiler.GraphNodeDef("B",
            (state, config) -> Source.single(FlowData.done(NodeOutput.of(state.with("b", 2)))));
        GraphCompiler.GraphNodeDef subgraph = new GraphCompiler.GraphNodeDef("SUB",
            new ExecuteSubgraphAction("sub1", sub1Compiled, new LinearRouter(List.of("sub1_node1", "sub1_node2", "END"))));
        List<GraphCompiler.GraphNodeDef> nodes = List.of(nodeA, nodeB, subgraph);
        List<GraphCompiler.GraphEdgeDef> edges = List.of(
            new GraphCompiler.GraphEdgeDef("A", "", "B"),
            new GraphCompiler.GraphEdgeDef("B", "", "SUB"),
            new GraphCompiler.GraphEdgeDef("SUB", "", "END")
        );
        CompiledGraphDefinition compiledGraph = GraphCompiler.compile(nodes, edges, "A");
        IRouter<State> router = new LinearRouter(List.of("A", "B", "SUB", "END"));
        ActorRef<GraphManagerActor.Command> manager = testKit.spawn(
            GraphManagerActor.createWithPersistenceId(compiledGraph, router, persistenceId));
        State initialState = new State(Map.of("currentNodeId", "A"));
        TestProbe<FlowData<NodeOutput>> probe = testKit.createTestProbe();
        manager.tell(new GraphManagerActor.StartGraph(initialState, probe.getRef()));
        FlowData<NodeOutput> result = probe.receiveMessage();
        assert result.isDone();
        assert result.result().state().with("a", 1).with("b", 2).with("sub1", 1).with("sub2", 2) != null;
    }

    @Test
    void testNodeFragmentOutput() {
        String persistenceId = "complex-graph-4-" + System.nanoTime();
        GraphCompiler.GraphNodeDef nodeA = new GraphCompiler.GraphNodeDef("A",
            (state, config) -> Source.from(List.of(
                FlowData.of(NodeOutput.of(state.with("a", 1))),
                FlowData.of(NodeOutput.of(state.with("a", 2))),
                FlowData.done(NodeOutput.of(state.with("a", 3)))
            )));
        List<GraphCompiler.GraphNodeDef> nodes = List.of(nodeA);
        List<GraphCompiler.GraphEdgeDef> edges = List.of();
        CompiledGraphDefinition compiledGraph = GraphCompiler.compile(nodes, edges, "A");
        IRouter<State> router = new LinearRouter(List.of("A", "END"));
        ActorRef<GraphManagerActor.Command> manager = testKit.spawn(
            GraphManagerActor.createWithPersistenceId(compiledGraph, router, persistenceId));
        State initialState = new State(Map.of("currentNodeId", "A"));
        TestProbe<FlowData<NodeOutput>> probe = testKit.createTestProbe();
        try {
            manager.tell(new GraphManagerActor.StartGraph(initialState, probe.getRef()));
            FlowData<NodeOutput> result = probe.receiveMessage();
            assert result.isDone();
            assert result.result().state().with("a", 3) != null;
        } catch (Exception e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    void testNodeDifferentExceptionTypes() {
        String persistenceId = "complex-graph-5-" + System.nanoTime();
        GraphCompiler.GraphNodeDef nodeA = new GraphCompiler.GraphNodeDef("A", (state, config) -> {
            throw new IllegalArgumentException("A参数异常");
        });
        GraphCompiler.GraphNodeDef nodeB = new GraphCompiler.GraphNodeDef("B", (state, config) -> {
            throw new IllegalStateException("B状态异常");
        });
        List<GraphCompiler.GraphNodeDef> nodes = List.of(nodeA, nodeB);
        List<GraphCompiler.GraphEdgeDef> edges = List.of(
            new GraphCompiler.GraphEdgeDef("A", "", "B"),
            new GraphCompiler.GraphEdgeDef("B", "", "END")
        );
        CompiledGraphDefinition compiledGraph = GraphCompiler.compile(nodes, edges, "A");
        IRouter<State> router = new LinearRouter(List.of("A", "B", "END"));
        ActorRef<GraphManagerActor.Command> manager = testKit.spawn(
            GraphManagerActor.createWithPersistenceId(compiledGraph, router, persistenceId));
        State initialState = new State(Map.of("currentNodeId", "A"));
        TestProbe<FlowData<NodeOutput>> probe = testKit.createTestProbe();
        try {
            manager.tell(new GraphManagerActor.StartGraph(initialState, probe.getRef()));
            FlowData<NodeOutput> result = probe.receiveMessage();
            assert result.isError();
            assert result.error().getMessage().contains("A参数异常");
        } catch (Exception e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    void testMidwayException() {
        String persistenceId = "complex-graph-6-" + System.nanoTime();
        GraphCompiler.GraphNodeDef nodeA = new GraphCompiler.GraphNodeDef("A",
            (state, config) -> Source.single(FlowData.done(NodeOutput.of(state.with("a", 1)))));
        GraphCompiler.GraphNodeDef nodeB = new GraphCompiler.GraphNodeDef("B",
            (state, config) -> {
                throw new RuntimeException("B节点异常");
            });
        GraphCompiler.GraphNodeDef nodeC = new GraphCompiler.GraphNodeDef("C",
            (state, config) -> Source.single(FlowData.done(NodeOutput.of(state.with("c", 3)))));
        List<GraphCompiler.GraphNodeDef> nodes = List.of(nodeA, nodeB, nodeC);
        List<GraphCompiler.GraphEdgeDef> edges = List.of(
            new GraphCompiler.GraphEdgeDef("A", "", "B"),
            new GraphCompiler.GraphEdgeDef("B", "", "C"),
            new GraphCompiler.GraphEdgeDef("C", "", "END")
        );
        CompiledGraphDefinition compiledGraph = GraphCompiler.compile(nodes, edges, "A");
        IRouter<State> router = new LinearRouter(List.of("A", "B", "C", "END"));
        ActorRef<GraphManagerActor.Command> manager = testKit.spawn(
            GraphManagerActor.createWithPersistenceId(compiledGraph, router, persistenceId));
        State initialState = new State(Map.of("currentNodeId", "A"));
        TestProbe<FlowData<NodeOutput>> probe = testKit.createTestProbe();
        try {
            manager.tell(new GraphManagerActor.StartGraph(initialState, probe.getRef()));
            FlowData<NodeOutput> result = probe.receiveMessage();
            assert result.isError();
            assert result.error().getMessage().contains("B节点异常");
        } catch (Exception e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    void testSubgraphNodeException() {
        String persistenceId = "complex-graph-7-" + System.nanoTime();
        // 子图
        GraphCompiler.GraphNodeDef subNode1 = new GraphCompiler.GraphNodeDef("sub1_node1",
            (state, config) -> {
                throw new RuntimeException("子图节点异常");
            });
        List<GraphCompiler.GraphNodeDef> subNodes = List.of(subNode1);
        List<GraphCompiler.GraphEdgeDef> subEdges = List.of();
        CompiledGraphDefinition subCompiled = GraphCompiler.compile(subNodes, subEdges, "sub1_node1");
        // 主图
        GraphCompiler.GraphNodeDef nodeA = new GraphCompiler.GraphNodeDef("A",
            (state, config) -> Source.single(FlowData.done(NodeOutput.of(state.with("a", 1)))));
        GraphCompiler.GraphNodeDef subgraph = new GraphCompiler.GraphNodeDef("SUB",
            new ExecuteSubgraphAction("sub1", subCompiled, new LinearRouter(List.of("sub1_node1", "sub1_node2", "END"))));
        List<GraphCompiler.GraphNodeDef> nodes = List.of(nodeA, subgraph);
        List<GraphCompiler.GraphEdgeDef> edges = List.of(
            new GraphCompiler.GraphEdgeDef("A", "", "SUB"),
            new GraphCompiler.GraphEdgeDef("SUB", "", "END")
        );
        CompiledGraphDefinition compiledGraph = GraphCompiler.compile(nodes, edges, "A");
        IRouter<State> router = new LinearRouter(List.of("A", "SUB", "END"));
        ActorRef<GraphManagerActor.Command> manager = testKit.spawn(
            GraphManagerActor.createWithPersistenceId(compiledGraph, router, persistenceId));
        State initialState = new State(Map.of("currentNodeId", "A"));
        TestProbe<FlowData<NodeOutput>> probe = testKit.createTestProbe();
        try {
            manager.tell(new GraphManagerActor.StartGraph(initialState, probe.getRef()));
            FlowData<NodeOutput> result = probe.receiveMessage();
            assert result.isError();
            assert result.error().getMessage().contains("子图节点异常");
        } catch (Exception e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    void testParallelPartialFailure() {
        String persistenceId = "complex-graph-8-" + System.nanoTime();
        GraphCompiler.GraphNodeDef nodeA = new GraphCompiler.GraphNodeDef("A",
            (state, config) -> Source.single(FlowData.done(NodeOutput.of(state.with("a", 1)))));
        GraphCompiler.GraphNodeDef nodeB = new GraphCompiler.GraphNodeDef("B",
            (state, config) -> {
                throw new RuntimeException("B并行失败");
            });
        GraphCompiler.GraphNodeDef nodeC = new GraphCompiler.GraphNodeDef("C",
            (state, config) -> Source.single(FlowData.done(NodeOutput.of(state.with("c", 3)))));
        List<GraphCompiler.GraphNodeDef> nodes = List.of(nodeA, nodeB, nodeC);
        List<GraphCompiler.GraphEdgeDef> edges = List.of(
            new GraphCompiler.GraphEdgeDef("A", "", "B"),
            new GraphCompiler.GraphEdgeDef("A", "", "C"),
            new GraphCompiler.GraphEdgeDef("B", "", "END"),
            new GraphCompiler.GraphEdgeDef("C", "", "END")
        );
        IRouter<State> router = (state, unused) -> ((Integer)state.data().get("a")) == 1 ? "B" : "C";
        CompiledGraphDefinition compiledGraph = GraphCompiler.compile(nodes, edges, "A");
        ActorRef<GraphManagerActor.Command> manager = testKit.spawn(
            GraphManagerActor.createWithPersistenceId(compiledGraph, router, persistenceId));
        State initialState = new State(Map.of("currentNodeId", "A"));
        TestProbe<FlowData<NodeOutput>> probe = testKit.createTestProbe();
        try {
            manager.tell(new GraphManagerActor.StartGraph(initialState, probe.getRef()));
            FlowData<NodeOutput> result = probe.receiveMessage();
            // 只要有一个分支失败就算 error
            assert result.isError();
            assert result.error().getMessage().contains("B并行失败");
        } catch (Exception e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    void testNodeWithFutureDependency() {
        String persistenceId = "complex-graph-9-" + System.nanoTime();
        GraphCompiler.GraphNodeDef nodeA = new GraphCompiler.GraphNodeDef("A",
            (state, config) -> Source.fromCompletionStage(
                CompletableFuture.supplyAsync(() -> FlowData.done(NodeOutput.of(state.with("a", 1))))
            ));
        GraphCompiler.GraphNodeDef nodeB = new GraphCompiler.GraphNodeDef("B",
            (state, config) -> Source.single(FlowData.done(NodeOutput.of(state.with("b", 2)))));
        List<GraphCompiler.GraphNodeDef> nodes = List.of(nodeA, nodeB);
        List<GraphCompiler.GraphEdgeDef> edges = List.of(
            new GraphCompiler.GraphEdgeDef("A", "", "B"),
            new GraphCompiler.GraphEdgeDef("B", "", "END")
        );
        CompiledGraphDefinition compiledGraph = GraphCompiler.compile(nodes, edges, "A");
        IRouter<State> router = new LinearRouter(List.of("A", "B", "END"));
        ActorRef<GraphManagerActor.Command> manager = testKit.spawn(
            GraphManagerActor.createWithPersistenceId(compiledGraph, router, persistenceId));
        State initialState = new State(Map.of("currentNodeId", "A"));
        TestProbe<FlowData<NodeOutput>> probe = testKit.createTestProbe();
        try {
            manager.tell(new GraphManagerActor.StartGraph(initialState, probe.getRef()));
            FlowData<NodeOutput> result = probe.receiveMessage();
            assert result.isDone();
            assert result.result().state().with("a", 1).with("b", 2) != null;
        } catch (Exception e) {
            e.printStackTrace();
            assert false;
        }
    }
} 