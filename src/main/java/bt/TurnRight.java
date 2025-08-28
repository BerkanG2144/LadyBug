package bt;

import model.Board;
import model.Ladybug;

public class TurnRight implements NodeBehavior {
    @Override
    public NodeStatus tick(Board board, Ladybug ladybug) {
        if (board == null || ladybug == null) {
            return NodeStatus.FAILURE;
        }
        return board.turnRight(ladybug) ? NodeStatus.SUCCESS : NodeStatus.FAILURE;
    }
}
