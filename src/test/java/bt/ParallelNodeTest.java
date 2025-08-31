// src/test/java/bt/ParallelNodeTest.java
package bt;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ParallelNodeTest {

    @Test
    void no_children_required_zero_successes_is_success() {
        ParallelNode par = new ParallelNode("par0", 0);
        assertEquals(NodeStatus.SUCCESS, par.tick(null, null));
    }

    @Test
    void success_when_required_successes_met_or_exceeded() {
        TestBehavior b1 = new TestBehavior(NodeStatus.SUCCESS);
        TestBehavior b2 = new TestBehavior(NodeStatus.FAILURE);
        TestBehavior b3 = new TestBehavior(NodeStatus.SUCCESS);

        ParallelNode par = new ParallelNode("par", 2);
        par.addChild(new LeafNode("n1", b1, LeafNode.LeafKind.ACTION));
        par.addChild(new LeafNode("n2", b2, LeafNode.LeafKind.ACTION));
        par.addChild(new LeafNode("n3", b3, LeafNode.LeafKind.ACTION));

        assertEquals(NodeStatus.SUCCESS, par.tick(null, null));
        // should evaluate all children (current implementation counts all)
        assertEquals(1, b1.calls);
        assertEquals(1, b2.calls);
        assertEquals(1, b3.calls);
    }

    @Test
    void failure_when_required_successes_not_met() {
        TestBehavior b1 = new TestBehavior(NodeStatus.FAILURE);
        TestBehavior b2 = new TestBehavior(NodeStatus.FAILURE);

        ParallelNode par = new ParallelNode("par", 1);
        par.addChild(new LeafNode("n1", b1, LeafNode.LeafKind.ACTION));
        par.addChild(new LeafNode("n2", b2, LeafNode.LeafKind.ACTION));

        assertEquals(NodeStatus.FAILURE, par.tick(null, null));
        assertEquals(1, b1.calls);
        assertEquals(1, b2.calls);
    }

    @Test
    void negative_required_successes_currently_always_success() {
        // Documenting current behavior: requiredSuccesses < 0 will be trivially met
        ParallelNode par = new ParallelNode("par-neg", -1);
        assertEquals(NodeStatus.SUCCESS, par.tick(null, null));
    }
}
