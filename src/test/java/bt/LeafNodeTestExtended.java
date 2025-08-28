// src/test/java/bt/LeafNodeTestExtended.java
package bt;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LeafNodeTestExtended {

    @Test
    void constructor_throws_on_null_behavior() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new LeafNode("leaf-null", null, LeafNode.LeafKind.ACTION));
        assertTrue(ex.getMessage() == null || ex.getMessage().toLowerCase().contains("behavior"));
    }

    @Test
    void getType_and_kind_and_behavior_accessors_work() {
        TestBehavior tb = new TestBehavior(NodeStatus.SUCCESS);
        LeafNode leaf = new LeafNode("L1", tb, LeafNode.LeafKind.CONDITION);
        assertEquals("leaf", leaf.getType());
        assertEquals(LeafNode.LeafKind.CONDITION, leaf.getKind());
        assertSame(tb, leaf.getBehavior());
    }

    @Test
    void tick_delegates_to_behavior_once() {
        TestBehavior tb = new TestBehavior(NodeStatus.SUCCESS);
        LeafNode leaf = new LeafNode("L-tick", tb, LeafNode.LeafKind.ACTION);
        assertEquals(NodeStatus.SUCCESS, leaf.tick(null, null));
        assertEquals(1, tb.calls);
    }
}
