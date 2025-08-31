// src/test/java/bt/BehaviorTreeNodeCoreTest.java
package bt;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class BehaviorTreeNodeCoreTest {

    @Test
    void id_and_type_are_exposed() {
        BehaviorTreeNode n1 = new SequenceNode("seq-1");
        BehaviorTreeNode n2 = new FallbackNode("fb-1");
        BehaviorTreeNode n3 = new ParallelNode("par-1", 1);
        BehaviorTreeNode n4 = new LeafNode("leaf-1", new TestBehavior(NodeStatus.SUCCESS), LeafNode.LeafKind.ACTION);

        assertEquals("seq-1", n1.getId());
        assertEquals("sequence", n1.getType());

        assertEquals("fb-1", n2.getId());
        assertEquals("fallback", n2.getType());

        assertEquals("par-1", n3.getId());
        assertEquals("parallel", n3.getType());

        assertEquals("leaf-1", n4.getId());
        assertEquals("leaf", n4.getType());
    }

    @Test
    void addChildren_preserves_order_and_getChildren_is_defensive_copy() {
        SequenceNode seq = new SequenceNode("S");
        LeafNode a = new LeafNode("A", new TestBehavior(NodeStatus.SUCCESS), LeafNode.LeafKind.ACTION);
        LeafNode b = new LeafNode("B", new TestBehavior(NodeStatus.SUCCESS), LeafNode.LeafKind.ACTION);
        seq.addChild(a);
        seq.addChild(b);

        // order
        List<BehaviorTreeNode> children = seq.getChildren();
        assertEquals(2, children.size());
        assertSame(a, children.get(0));
        assertSame(b, children.get(1));

        // defensive copy: mutating the returned list must not affect internal children
        children.clear();
        List<BehaviorTreeNode> childrenAgain = seq.getChildren();
        assertEquals(2, childrenAgain.size());
    }
}
