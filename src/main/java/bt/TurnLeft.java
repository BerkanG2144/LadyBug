package bt;

import model.Board;
import model.Ladybug;

/**
 * Behavior tree node that turns the ladybug
 * one step to the left.
 *
 * Always returns SUCCESS.
 *
 * @author ujnaa
 */
public class TurnLeft implements NodeBehavior {
    @Override
    public NodeStatus tick(Board board, Ladybug ladybug) {
        if (board == null || ladybug == null) {
            return NodeStatus.FAILURE;
        }
        return board.turnLeft(ladybug) ? NodeStatus.SUCCESS : NodeStatus.FAILURE;
    }
}
