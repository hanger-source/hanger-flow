package source.hanger.akkagraph;

import akka.actor.typed.ActorRef;
import akka.actor.testkit.typed.javadsl.ActorTestKit;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.stream.javadsl.Source;
import source.hanger.akkagraph.action.ExecuteSubgraphAction;
import source.hanger.akkagraph.actor.GraphManagerActor;
import source.hanger.akkagraph.graph.CompiledGraphDefinition;
import source.hanger.akkagraph.graph.GraphCompiler;
import source.hanger.akkagraph.model.NodeOutput;
import source.hanger.akkagraph.model.State;
import source.hanger.akkagraph.router.IRouter;
import source.hanger.akkagraph.router.LinearRouter;
import source.hanger.akkagraph.shared.FlowData;
import org.junit.jupiter.api.Test;
import source.hanger.akkagraph.graph.GraphCompiler.GraphEdgeDef;
import source.hanger.akkagraph.graph.GraphCompiler.GraphNodeDef;

import java.util.List;
import java.util.Map;

public class GraphRoutingEdgeCaseTest {

    @Test
    void testLargeScaleGraph() {
        ActorTestKit testKit = ActorTestKit.create();
        try {
            String persistenceId = "complex-graph-10-" + System.nanoTime();
            int N = 20;
            List<GraphNodeDef> nodes = new java.util.ArrayList<>();
            List<GraphEdgeDef> edges = new java.util.ArrayList<>();
            for (int i = 0; i < N; i++) {
                int idx = i;
                nodes.add(new GraphNodeDef("N" + idx,
                    (state, config) -> {
                        System.out.println("[TEST][testLargeScaleGraph] 执行节点N" + idx + ", state=" + state);
                        return Source.single(FlowData.done(NodeOutput.of(state.with("n" + idx, idx))));
                    }));
                if (i < N - 1) {
                    edges.add(new GraphEdgeDef("N" + idx, "", "N" + (idx + 1)));
                }
            }
            edges.add(new GraphEdgeDef("N" + (N - 1), "", "END"));
            CompiledGraphDefinition compiledGraph = GraphCompiler.compile(nodes, edges, "N0");
            IRouter<State> router = new IRouter<>() {
                @Override
                public String route(State state, Object output) {
                    String current = (String)state.data().get("currentNodeId");
                    System.out.println(
                        "[TEST][testLargeScaleGraph] router.route: state=" + state + ", current=" + current);
                    if ("N19".equals(current)) {
                        System.out.println("[TEST][testLargeScaleGraph] router.route: current==N19, next=END");
                        return "END";
                    }
                    int idx = Integer.parseInt(current.substring(1));
                    String next = "N" + (idx + 1);
                    System.out.println(
                        "[TEST][testLargeScaleGraph] router.route: current=" + current + ", next=" + next);
                    return next;
                }
            };
            ActorRef<GraphManagerActor.Command> manager = testKit.spawn(
                GraphManagerActor.createWithPersistenceId(compiledGraph, router, persistenceId));
            State initialState = new State(Map.of("currentNodeId", "N0"));
            TestProbe<FlowData<NodeOutput>> probe = testKit.createTestProbe();
            System.out.println("[TEST][testLargeScaleGraph] manager.tell StartGraph");
            manager.tell(new GraphManagerActor.StartGraph(initialState, probe.getRef()));
            System.out.println("[TEST][testLargeScaleGraph] probe.receiveMessage 前");
            FlowData<NodeOutput> result = probe.receiveMessage(java.time.Duration.ofSeconds(15));
            System.out.println("[TEST][testLargeScaleGraph] probe.receiveMessage 后: result=" + result);
            if (result != null) {
                System.out.println("[TEST][testLargeScaleGraph] 收到result.state: " + result.result().state());
            }
            assert result.isDone();
            for (int i = 0; i < N; i++) {
                assert ((Integer)result.result().state().data().get("n" + i)) == i;
            }
        } catch (Exception e) {
            e.printStackTrace();
            assert false;
        } finally {
            testKit.shutdownTestKit();
        }
    }

    @Test
    void testExtremeNestedSubgraphParallelException() {
        ActorTestKit testKit = ActorTestKit.create();
        try {
            String persistenceId = "extreme-nested-parallel-exception-" + System.nanoTime();
            // 最内层子图
            GraphCompiler.GraphNodeDef innerA = new GraphCompiler.GraphNodeDef("innerA",
                (state, config) -> {
                    System.out.println("[TEST][testExtreme][innerSubgraph] innerA, state=" + state);
                    return Source.single(FlowData.done(NodeOutput.of(state.with("innerA", 1))));
                });
            GraphCompiler.GraphNodeDef innerB = new GraphCompiler.GraphNodeDef("innerB",
                (state, config) -> {
                    System.out.println("[TEST][testExtreme][innerSubgraph] innerB throw, state=" + state);
                    throw new RuntimeException("innerB异常");
                });
            java.util.List<GraphCompiler.GraphNodeDef> innerNodes = java.util.Arrays.asList(innerA, innerB);
            java.util.List<GraphCompiler.GraphEdgeDef> innerEdges = java.util.Arrays.asList(
                new GraphCompiler.GraphEdgeDef("innerA", "", "innerB"),
                new GraphCompiler.GraphEdgeDef("innerB", "", "END")
            );
            System.out.println("[TEST][testExtreme] innerNodes=" + innerNodes.stream().map(n -> n.id()).toList());
            System.out.println("[TEST][testExtreme] innerEdges=" + innerEdges);
            CompiledGraphDefinition innerCompiled = GraphCompiler.compile(innerNodes, innerEdges, "innerA");
            // innerSubgraph router
            IRouter<State> innerRouter = (state, unused) -> {
                Object flagObj = state.data().get("flag");
                int flag = flagObj instanceof Integer ? (Integer) flagObj : 0;
                java.util.Set<String> nodeIds = innerCompiled.getAllNodeIds();
                System.out.println("[TEST][testExtreme][innerRouter] state=" + state + ", flag=" + flag + ", next=" + (flag == 1 ? "innerA" : "innerB") + ", graphDef.nodes=" + nodeIds);
                return flag == 1 ? "innerA" : "innerB";
            };
            // 中间层子图
            GraphCompiler.GraphNodeDef midA = new GraphCompiler.GraphNodeDef("midA",
                (state, config) -> {
                    System.out.println("[TEST][testExtreme][midSubgraph] midA, state=" + state);
                    return Source.single(FlowData.done(NodeOutput.of(state.with("midA", 1))));
                });
            GraphCompiler.GraphNodeDef midSub = new GraphCompiler.GraphNodeDef("midSub",
                new ExecuteSubgraphAction("inner", innerCompiled, innerRouter));
            java.util.List<GraphCompiler.GraphNodeDef> midNodes = java.util.Arrays.asList(midA, midSub);
            java.util.List<GraphCompiler.GraphEdgeDef> midEdges = java.util.Arrays.asList(
                new GraphCompiler.GraphEdgeDef("midA", "", "midSub"),
                new GraphCompiler.GraphEdgeDef("midSub", "", "END")
            );
            System.out.println("[TEST][testExtreme] midNodes=" + midNodes.stream().map(n -> n.id()).toList());
            System.out.println("[TEST][testExtreme] midEdges=" + midEdges);
            CompiledGraphDefinition midCompiled = GraphCompiler.compile(midNodes, midEdges, "midA");
            // midSubgraph router
            IRouter<State> midRouter = (state, unused) -> {
                Object flagObj = state.data().get("flag");
                int flag = flagObj instanceof Integer ? (Integer) flagObj : 0;
                java.util.Set<String> nodeIds = midCompiled.getAllNodeIds();
                System.out.println("[TEST][testExtreme][midRouter] state=" + state + ", flag=" + flag + ", next=" + (flag == 1 ? "midA" : "midSub") + ", graphDef.nodes=" + nodeIds);
                return flag == 1 ? "midA" : "midSub";
            };
            // 主图
            GraphCompiler.GraphNodeDef mainA = new GraphCompiler.GraphNodeDef("mainA",
                (state, config) -> {
                    System.out.println("[TEST][testExtreme][mainGraph] mainA, state=" + state);
                    return Source.single(FlowData.done(NodeOutput.of(state.with("mainA", 1))));
                });
            GraphCompiler.GraphNodeDef mainPar = new GraphCompiler.GraphNodeDef("mainPar",
                (state, config) -> {
                    System.out.println("[TEST][testExtreme][mainGraph] mainPar, state=" + state);
                    return Source.single(FlowData.done(NodeOutput.of(state.with("mainPar", 1))));
                });
            GraphCompiler.GraphNodeDef mainSub = new GraphCompiler.GraphNodeDef("mainSub",
                new ExecuteSubgraphAction("mid", midCompiled, midRouter));
            java.util.List<GraphCompiler.GraphNodeDef> mainNodes = java.util.Arrays.asList(mainA, mainPar, mainSub);
            java.util.List<GraphCompiler.GraphEdgeDef> mainEdges = java.util.Arrays.asList(
                new GraphCompiler.GraphEdgeDef("mainA", "", "mainSub"),
                new GraphCompiler.GraphEdgeDef("mainPar", "", "mainSub"),
                new GraphCompiler.GraphEdgeDef("mainSub", "", "END"),
                new GraphCompiler.GraphEdgeDef("mainA", "", "mainPar")
            );
            System.out.println("[TEST][testExtreme] mainNodes=" + mainNodes.stream().map(n -> n.id()).toList());
            System.out.println("[TEST][testExtreme] mainEdges=" + mainEdges);
            CompiledGraphDefinition compiled = GraphCompiler.compile(mainNodes, mainEdges, "mainA");
            // mainGraph router
            IRouter<State> mainRouter = (state, unused) -> {
                Object flagObj = state.data().get("flag");
                int flag = flagObj instanceof Integer ? (Integer) flagObj : 0;
                java.util.Set<String> nodeIds = compiled.getAllNodeIds();
                System.out.println("[TEST][testExtreme][mainRouter] state=" + state + ", flag=" + flag + ", next=" + (flag == 1 ? "mainPar" : "mainSub") + ", graphDef.nodes=" + nodeIds);
                return flag == 1 ? "mainPar" : "mainSub";
            };
            // 启动测试
            ActorRef<GraphManagerActor.Command> manager = testKit.spawn(GraphManagerActor.create(compiled, mainRouter));
            TestProbe<FlowData<NodeOutput>> probe = testKit.createTestProbe();
            State initialState = new State(Map.of("currentNodeId", "mainA"));
            System.out.println("[TEST][testExtreme] manager.tell StartGraph");
            manager.tell(new GraphManagerActor.StartGraph(initialState, probe.getRef()));
            System.out.println("[TEST][testExtreme] probe.receiveMessage 前");
            FlowData<NodeOutput> result = probe.receiveMessage();
            System.out.println("[TEST][testExtreme] probe.receiveMessage 后: result=" + result);
            if (result.isDone()) {
                NodeOutput output = result.result();
                System.out.println("[TEST][testExtreme] DONE: output=" + output);
                // 可加 done 断言
            } else if (result.isError()) {
                Throwable error = result.error();
                System.out.println("[TEST][testExtreme] ERROR: error=" + error);
                org.junit.jupiter.api.Assertions.assertTrue(error instanceof RuntimeException);
                org.junit.jupiter.api.Assertions.assertTrue(error.getMessage().contains("innerB异常"));
            } else {
                org.junit.jupiter.api.Assertions.fail("收到未知类型 FlowData: " + result);
            }
        } finally {
            testKit.shutdownTestKit();
        }
    }

    @Test
    void testConditionalBranchPartialFailure() {
        ActorTestKit testKit = ActorTestKit.create();
        try {
            String persistenceId = "cond-branch-partial-fail-" + System.nanoTime();
            GraphCompiler.GraphNodeDef nodeA = new GraphCompiler.GraphNodeDef("A",
                (state, config) -> Source.single(FlowData.done(NodeOutput.of(state.with("flag", 1)))));
            GraphCompiler.GraphNodeDef nodeB = new GraphCompiler.GraphNodeDef("B",
                (state, config) -> {
                    System.out.println("[TEST][testConditionalBranch] B throw");
                    throw new RuntimeException("B分支异常");
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
            IRouter<State> router = (state, unused) -> ((Integer)state.data().get("flag")) == 1 ? "B" : "C";
            CompiledGraphDefinition compiledGraph = GraphCompiler.compile(nodes, edges, "A");
            ActorRef<GraphManagerActor.Command> manager = testKit.spawn(
                GraphManagerActor.createWithPersistenceId(compiledGraph, router, persistenceId));
            State initialState = new State(Map.of("currentNodeId", "A"));
            TestProbe<FlowData<NodeOutput>> probe = testKit.createTestProbe();
            System.out.println("[TEST][testConditionalBranch] manager.tell StartGraph");
            manager.tell(new GraphManagerActor.StartGraph(initialState, probe.getRef()));
            System.out.println("[TEST][testConditionalBranch] probe.receiveMessage 前");
            FlowData<NodeOutput> result = probe.receiveMessage(java.time.Duration.ofSeconds(15));
            System.out.println("[TEST][testConditionalBranch] probe.receiveMessage 后: result=" + result);
            assert result.isError();
            assert result.error().getMessage().contains("B分支异常");
        } finally {
            testKit.shutdownTestKit();
        }
    }

    @Test
    void testFragmentAndFutureMix() {
        ActorTestKit testKit = ActorTestKit.create();
        try {
            String persistenceId = "fragment-future-mix-" + System.nanoTime();
            GraphCompiler.GraphNodeDef nodeA = new GraphCompiler.GraphNodeDef("A",
                (state, config) -> Source.from(List.of(
                    FlowData.of(NodeOutput.of(state.with("a", 1))),
                    FlowData.of(NodeOutput.of(state.with("a", 2))),
                    FlowData.done(NodeOutput.of(state.with("a", 3)))
                )));
            GraphCompiler.GraphNodeDef nodeB = new GraphCompiler.GraphNodeDef("B",
                (state, config) -> Source.fromCompletionStage(
                    java.util.concurrent.CompletableFuture.supplyAsync(
                        () -> FlowData.done(NodeOutput.of(state.with("b", 2))))
                ));
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
            System.out.println("[TEST][testFragmentAndFutureMix] manager.tell StartGraph");
            manager.tell(new GraphManagerActor.StartGraph(initialState, probe.getRef()));
            System.out.println("[TEST][testFragmentAndFutureMix] probe.receiveMessage 前");
            FlowData<NodeOutput> result = probe.receiveMessage(java.time.Duration.ofSeconds(15));
            System.out.println("[TEST][testFragmentAndFutureMix] probe.receiveMessage 后: result=" + result);
            assert result.isDone();
            assert result.result().state().with("a", 3).with("b", 2) != null;
        } finally {
            testKit.shutdownTestKit();
        }
    }
} 