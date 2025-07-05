package hanger.source.akka;

import akka.actor.typed.ActorSystem;
import hanger.source.akka.graph.GraphCompiler;
import hanger.source.akka.graph.GraphCompiler.GraphEdgeDef;
import hanger.source.akka.graph.GraphCompiler.GraphNodeDef;
import hanger.source.akka.model.NodeOutput;
import hanger.source.akka.model.State;
import hanger.source.akka.router.IRouter;
import hanger.source.akka.router.LinearRouter;
import hanger.source.akka.graph.CompiledGraphDefinition;
import hanger.source.akka.actor.GraphManagerActor;
import hanger.source.akka.shared.FlowData;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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