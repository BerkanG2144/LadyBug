package bt;

import model.Board;
import model.Ladybug;

public class PlaceLeaf implements NodeBehavior {
    @Override
    public NodeStatus tick(Board board, Ladybug ladybug) {
        if (board == null || ladybug == null) {
            return NodeStatus.FAILURE;
        }
        return board.placeLeaf(ladybug) ? NodeStatus.SUCCESS : NodeStatus.FAILURE;
    }
}
