// src/test/java/bt/LeafNodeTest.java
package bt;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LeafNodeTest {

    @Test
    void leaf_returns_success_from_behavior() {
        LeafNode leaf = new LeafNode("leaf-ok", new TestBehavior(NodeStatus.SUCCESS), LeafNode.LeafKind.ACTION);
        assertEquals(NodeStatus.SUCCESS, leaf.tick(null, null));
    }

    @Test
    void leaf_returns_failure_from_behavior() {
        LeafNode leaf = new LeafNode("leaf-fail", new TestBehavior(NodeStatus.FAILURE), LeafNode.LeafKind.CONDITION);
        assertEquals(NodeStatus.FAILURE, leaf.tick(null, null));
    }
}
