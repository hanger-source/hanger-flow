package source.hanger.akkagraph;

import akka.actor.testkit.typed.javadsl.ActorTestKit;
import akka.actor.typed.ActorRef;
import source.hanger.akkagraph.action.ExecuteSubgraphAction;
import source.hanger.akkagraph.action.INodeAction;
import source.hanger.akkagraph.action.ParallelJoinAction;
import source.hanger.akkagraph.actor.GraphManagerActor;
import source.hanger.akkagraph.actor.GraphManagerActor.Command;
import source.hanger.akkagraph.graph.CompiledGraphDefinition;
import source.hanger.akkagraph.graph.GraphCompiler;
import source.hanger.akkagraph.graph.GraphCompiler.GraphEdgeDef;
import source.hanger.akkagraph.graph.GraphCompiler.GraphNodeDef;
import source.hanger.akkagraph.model.NodeOutput;
import source.hanger.akkagraph.model.State;
import source.hanger.akkagraph.router.ConditionalRouter;
import source.hanger.akkagraph.router.IRouter;
import source.hanger.akkagraph.shared.FlowData;
import akka.stream.javadsl.Source;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import source.hanger.akkagraph.router.LinearRouter;

import java.util.List;
import java.util.Map;

public class GraphFlowIntegrationTest {
    static final ActorTestKit testKit = ActorTestKit.create();
    CompiledGraphDefinition subCompiled;

    @BeforeAll
    static void setup() {}

    @AfterAll
    static void teardown() {
        testKit.shutdownTestKit();
    }

    @Test
    void testFullGraphFlow() throws Exception {
        // 子图节点定义
        GraphNodeDef subNode1 = new GraphNodeDef("sub1_node1",
            (state, config) -> Source.single(FlowData.done(NodeOutput.of(state.with("sub", 1)))));
        GraphNodeDef subNode2 = new GraphNodeDef("sub1_node2",
            (state, config) -> Source.single(FlowData.done(NodeOutput.of(state.with("sub", 2)))));
        List<GraphNodeDef> subNodes = List.of(subNode1, subNode2);
        List<GraphEdgeDef> subEdges = List.of(
            new GraphEdgeDef("sub1_node1", "", "sub1_node2"),
            new GraphEdgeDef("sub1_node2", "", "END")
        );
        subCompiled = GraphCompiler.compile(subNodes, subEdges, "sub1_node1");

        // 节点定义
        INodeAction nodeA = (state, config) -> Source.single(FlowData.done(NodeOutput.of(state.with("step", "A"))));
        INodeAction nodeB = (state, config) -> Source.single(FlowData.done(NodeOutput.of(state.with("step", "B"))));
        INodeAction nodeC = (state, config) -> Source.single(FlowData.done(NodeOutput.of(state.with("step", "C"))));
        INodeAction nodeD = (state, config) -> Source.single(FlowData.done(NodeOutput.of(state.with("step", "D"))));
        INodeAction subgraphAction = new ExecuteSubgraphAction("sub1", subCompiled, new LinearRouter(java.util.List.of("sub1_node1", "sub1_node2", "END")));
        INodeAction joinAction = new ParallelJoinAction();

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
        IRouter<State> router = new ConditionalRouter(null, null);
        ActorRef<Command> manager = testKit.spawn(GraphManagerActor.create(compiledGraph, router));
        State initialState = new State(Map.of("currentNodeId", "A"));
        manager.tell(new GraphManagerActor.StartGraph(initialState, null));
        // 这里只做流程覆盖，实际可通过 TestProbe 等断言输出
    }
} 