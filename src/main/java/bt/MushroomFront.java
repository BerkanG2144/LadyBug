package bt;

import model.Board;
import model.Ladybug;

public class MushroomFront implements NodeBehaviour{
    @Override
    public NodeStatus tick(Board board, Ladybug ladybug) {
        if (board == null || ladybug == null) {
            return NodeStatus.FAILURE;
        }
        return board.mushroomFront(ladybug) ? NodeStatus.SUCCESS : NodeStatus.FAILURE;
    }
}
