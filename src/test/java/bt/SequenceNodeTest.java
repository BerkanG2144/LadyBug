// src/test/java/bt/SequenceNodeTest.java
package bt;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SequenceNodeTest {

    @Test
    void empty_sequence_returns_success() {
        SequenceNode seq = new SequenceNode("seq-empty");
        assertEquals(NodeStatus.SUCCESS, seq.tick(null, null));
    }

    @Test
    void all_success_returns_success() {
        SequenceNode seq = new SequenceNode("seq-all-ok");
        seq.addChildren(new LeafNode("c1", new TestBehavior(NodeStatus.SUCCESS), LeafNode.LeafKind.CONDITION));
        seq.addChildren(new LeafNode("a1", new TestBehavior(NodeStatus.SUCCESS), LeafNode.LeafKind.ACTION));
        assertEquals(NodeStatus.SUCCESS, seq.tick(null, null));
    }

    @Test
    void stops_on_first_failure_and_returns_failure() {
        TestBehavior b1 = new TestBehavior(NodeStatus.SUCCESS);
        TestBehavior b2 = new TestBehavior(NodeStatus.FAILURE);
        TestBehavior b3 = new TestBehavior(NodeStatus.SUCCESS);

        SequenceNode seq = new SequenceNode("seq-shortcircuit");
        seq.addChildren(new LeafNode("c1", b1, LeafNode.LeafKind.CONDITION)); // should run
        seq.addChildren(new LeafNode("c2", b2, LeafNode.LeafKind.CONDITION)); // should run and stop
        seq.addChildren(new LeafNode("a1", b3, LeafNode.LeafKind.ACTION));    // should NOT run

        assertEquals(NodeStatus.FAILURE, seq.tick(null, null));
        assertEquals(1, b1.calls);
        assertEquals(1, b2.calls);
        assertEquals(0, b3.calls);
    }
}
