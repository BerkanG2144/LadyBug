package bt;

import model.Board;
import model.Ladybug;

/**
 * Behavior tree node that checks whether there is a mushroom
 * directly in front of the ladybug.
 *
 * Returns SUCCESS if a mushroom is in front, otherwise FAILURE.
 *
 * @author ujnaa
 */
public class MushroomFront implements NodeBehavior {
    @Override
    public NodeStatus tick(Board board, Ladybug ladybug) {
        if (board == null || ladybug == null) {
            return NodeStatus.FAILURE;
        }
        return board.mushroomFront(ladybug) ? NodeStatus.SUCCESS : NodeStatus.FAILURE;
    }
}
