package bt;

import model.Board;
import model.Ladybug;


public class LeafFront implements NodeBehaviour {

    @Override
    public NodeStatus tick(Board board, Ladybug ladybug) {
        if (board == null || ladybug == null) {
            return NodeStatus.FAILURE;
        }
        return board.leafFront(ladybug) ? NodeStatus.SUCCESS : NodeStatus.FAILURE;
    }
}
