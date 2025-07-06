package source.hanger.akkagraph;

import akka.actor.typed.ActorSystem;
import source.hanger.akkagraph.graph.GraphCompiler;
import source.hanger.akkagraph.graph.GraphCompiler.GraphEdgeDef;
import source.hanger.akkagraph.graph.GraphCompiler.GraphNodeDef;
import source.hanger.akkagraph.model.NodeOutput;
import source.hanger.akkagraph.model.State;
import source.hanger.akkagraph.router.IRouter;
import source.hanger.akkagraph.router.LinearRouter;
import source.hanger.akkagraph.graph.CompiledGraphDefinition;
import source.hanger.akkagraph.actor.GraphManagerActor;
import source.hanger.akkagraph.shared.FlowData;

import java.util.List;
import java.util.Map;

import static akka.stream.javadsl.Source.single;

public class Main {
    public static void main(String[] args) throws Exception {
        // 构建简单图：A -> B -> END
        GraphNodeDef nodeA = new GraphNodeDef("A", (state, config) -> {
            System.out.println("执行节点A");
            return single(FlowData.done(NodeOutput.of(state.with("a", 1))));
        });
        GraphNodeDef nodeB = new GraphNodeDef("B", (state, config) -> {
            System.out.println("执行节点B");
            return single(FlowData.done(NodeOutput.of(state.with("b", 2))));
        });
        List<GraphNodeDef> nodes = List.of(nodeA, nodeB);
        List<GraphEdgeDef> edges = List.of(
            new GraphEdgeDef("A", "", "B"),
            new GraphEdgeDef("B", "", "END")
        );
        CompiledGraphDefinition compiledGraph = GraphCompiler.compile(nodes, edges, "A");
        IRouter<State> router = new LinearRouter(List.of("A", "B", "END"));
        ActorSystem<GraphManagerActor.Command> system = ActorSystem.create(
                GraphManagerActor.create(compiledGraph, router), "LangGraphAkkaSystem");
        State initialState = new State(Map.of("currentNodeId", "A"));
        system.tell(new GraphManagerActor.StartGraph(initialState, null));
        Thread.sleep(2000); // 等待执行输出
        system.terminate();
    }
} 