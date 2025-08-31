// src/test/java/bt/FallbackNodeTest.java
package bt;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FallbackNodeTest {

    @Test
    void empty_fallback_returns_failure() {
        FallbackNode fb = new FallbackNode("fb-empty");
        assertEquals(NodeStatus.FAILURE, fb.tick(null, null));
    }

    @Test
    void first_success_short_circuits_to_success() {
        TestBehavior b1 = new TestBehavior(NodeStatus.SUCCESS);
        TestBehavior b2 = new TestBehavior(NodeStatus.SUCCESS);

        FallbackNode fb = new FallbackNode("fb");
        fb.addChild(new LeafNode("c1", b1, LeafNode.LeafKind.CONDITION)); // should run and stop
        fb.addChild(new LeafNode("a1", b2, LeafNode.LeafKind.ACTION));    // should NOT run

        assertEquals(NodeStatus.SUCCESS, fb.tick(null, null));
        assertEquals(1, b1.calls);
        assertEquals(0, b2.calls);
    }

    @Test
    void all_fail_results_in_failure() {
        TestBehavior b1 = new TestBehavior(NodeStatus.FAILURE);
        TestBehavior b2 = new TestBehavior(NodeStatus.FAILURE);

        FallbackNode fb = new FallbackNode("fb-all-fail");
        fb.addChild(new LeafNode("c1", b1, LeafNode.LeafKind.CONDITION));
        fb.addChild(new LeafNode("a1", b2, LeafNode.LeafKind.ACTION));

        assertEquals(NodeStatus.FAILURE, fb.tick(null, null));
        assertEquals(1, b1.calls);
        assertEquals(1, b2.calls);
    }
}
