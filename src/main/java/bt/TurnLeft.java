package bt;

import model.Board;
import model.Ladybug;

public class TurnLeft implements NodeBehavior {
    @Override
    public NodeStatus tick(Board board, Ladybug ladybug) {
        if (board == null || ladybug == null) {
            return NodeStatus.FAILURE;
        }
        return board.turnLeft(ladybug) ? NodeStatus.SUCCESS : NodeStatus.FAILURE;
    }
}
