package bt;

import model.Board;
import model.Ladybug;

public class TakeLeaf implements NodeBehaviour {
    @Override
    public NodeStatus tick(Board board, Ladybug ladybug) {
        if (board == null || ladybug == null) {
            return NodeStatus.FAILURE;
        }
        return board.takeLeaf(ladybug) ? NodeStatus.SUCCESS : NodeStatus.FAILURE;
    }
}
