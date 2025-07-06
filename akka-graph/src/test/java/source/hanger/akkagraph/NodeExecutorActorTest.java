package source.hanger.akkagraph;

import akka.actor.testkit.typed.javadsl.ActorTestKit;
import akka.actor.typed.ActorRef;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import source.hanger.akkagraph.actor.NodeExecutorActor;
import source.hanger.akkagraph.actor.NodeExecutorActor.Command;

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
        ActorRef<Command> ref = testKit.spawn(NodeExecutorActor.create("test-graph-1", null, null));
        ref.tell(new NodeExecutorActor.StartExecution(null, null, null, null));
        // 可扩展断言，验证状态或消息
    }
} 