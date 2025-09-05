package bt;

import model.Board;
import model.Ladybug;
import model.Position;

/**
 * Behavior tree node that moves a ladybug directly
 * to a specified target position by "flying".
 *
 * Returns SUCCESS if the move is possible,
 * otherwise FAILURE.
 *
 * @author ujnaa
 */
public class Fly implements NodeBehavior {
    private final int x;
    private final int y;

    /**
     * Constructs a Fly node with target coordinates.
     *
     * @param x the x-coordinate of the target position
     * @param y the y-coordinate of the target position
     */
    public Fly(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public NodeStatus tick(Board board, Ladybug ladybug) {
        if (board == null || ladybug == null) {
            return NodeStatus.FAILURE;
        }
        Position target = new Position(x, y);
        return board.flyTo(ladybug, target) ? NodeStatus.SUCCESS : NodeStatus.FAILURE;
    }
}

