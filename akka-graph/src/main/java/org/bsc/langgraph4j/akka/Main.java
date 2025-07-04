package org.bsc.langgraph4j.akka;

import akka.actor.typed.ActorSystem;
import org.bsc.langgraph4j.akka.action.INodeAction;
import org.bsc.langgraph4j.akka.graph.CompiledGraphDefinition;
import org.bsc.langgraph4j.akka.graph.GraphCompiler;
import org.bsc.langgraph4j.akka.model.State;
import org.bsc.langgraph4j.akka.router.LinearRouter;
import org.bsc.langgraph4j.akka.actor.GraphManagerActor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class Main {
    public static void main(String[] args) throws Exception {
        // 构建简单图：A -> B -> END
        GraphCompiler.GraphNodeDef nodeA = new GraphCompiler.GraphNodeDef("A", (state, config) -> {
            System.out.println("执行节点A");
            return CompletableFuture.completedFuture(org.bsc.langgraph4j.akka.model.NodeOutput.of(state.with("a", 1)));
        });
        GraphCompiler.GraphNodeDef nodeB = new GraphCompiler.GraphNodeDef("B", (state, config) -> {
            System.out.println("执行节点B");
            return CompletableFuture.completedFuture(org.bsc.langgraph4j.akka.model.NodeOutput.of(state.with("b", 2)));
        });
        List<GraphCompiler.GraphNodeDef> nodes = List.of(nodeA, nodeB);
        List<GraphCompiler.GraphEdgeDef> edges = List.of(
                new GraphCompiler.GraphEdgeDef("A", "", "B"),
                new GraphCompiler.GraphEdgeDef("B", "", "END")
        );
        CompiledGraphDefinition compiledGraph = GraphCompiler.compile(nodes, edges, "A");
        org.bsc.langgraph4j.akka.router.IRouter<State> router = new LinearRouter(compiledGraph);
        ActorSystem<GraphManagerActor.Command> system = ActorSystem.create(
                GraphManagerActor.create(compiledGraph, router), "LangGraphAkkaSystem");
        State initialState = new State(Map.of("currentNodeId", "A"));
        system.tell(new GraphManagerActor.StartGraph(initialState, null));
        Thread.sleep(2000); // 等待执行输出
        system.terminate();
    }
} 