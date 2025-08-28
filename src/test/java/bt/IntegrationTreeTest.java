// src/test/java/bt/IntegrationTreeTest.java
package bt;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple end-to-end test for a small tree:
 * root (Fallback)
 *   |- Leaf(FAILURE)
 *   |- Sequence
 *        |- Leaf(SUCCESS)
 *        |- Leaf(SUCCESS)
 *
 * Expect: SUCCESS, and only the sequence children after the first leaf fails.
 */
public class IntegrationTreeTest {

    @Test
    void fallback_then_sequence_happy_path() {
        TestBehavior first = new TestBehavior(NodeStatus.FAILURE);
        TestBehavior seq1  = new TestBehavior(NodeStatus.SUCCESS);
        TestBehavior seq2  = new TestBehavior(NodeStatus.SUCCESS);

        FallbackNode root = new FallbackNode("root");
        root.addChildren(new LeafNode("fail-first", first, LeafNode.LeafKind.CONDITION));

        SequenceNode seq = new SequenceNode("seq");
        seq.addChildren(new LeafNode("s1", seq1, LeafNode.LeafKind.CONDITION));
        seq.addChildren(new LeafNode("s2", seq2, LeafNode.LeafKind.ACTION));

        root.addChildren(seq);

        assertEquals(NodeStatus.SUCCESS, root.tick(null, null));
        assertEquals(1, first.calls); // evaluated
        assertEquals(1, seq1.calls);  // evaluated
        assertEquals(1, seq2.calls);  // evaluated
    }
}
