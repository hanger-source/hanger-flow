package org.bsc.langgraph4j.akka;

import akka.actor.testkit.typed.javadsl.ActorTestKit;
import akka.actor.typed.ActorRef;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class NodeExecutorActorTest {
    static final ActorTestKit testKit = ActorTestKit.create();

    @BeforeAll
    static void setup() {}

    @AfterAll
    static void teardown() {
        testKit.shutdownTestKit();
    }

    @Test
    void testStartExecution() {
        ActorRef<NodeExecutorActor.Command> ref = testKit.spawn(NodeExecutorActor.create("test-graph-1"));
        ref.tell(new NodeExecutorActor.StartExecution("graph-def", "input"));
        // 可扩展断言，验证状态或消息
    }
} 