package bt;

import model.Board;
import model.Ladybug;

/**
 * Simple test double that returns a fixed NodeStatus and counts calls.
 */
public class TestBehavior implements NodeBehavior {
    private final NodeStatus statusToReturn;
    public int calls = 0;

    public TestBehavior(NodeStatus statusToReturn) {
        this.statusToReturn = statusToReturn;
    }

    @Override
    public NodeStatus tick(Board board, Ladybug ladybug) {
        calls++;
        return statusToReturn;
    }
}
