package bt;

import exceptions.LadybugException;
import model.Board;
import model.Ladybug;
import model.Position;

/**
 * Behavior tree node that moves a ladybug directly
 * to a specified target position by "flying".
 *
 * @author ujnaa
 * @version SS25
 */
public class Fly implements NodeBehavior, LogArgsProvider  {
    private static final String SEPERATOR = ",";
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
    public NodeStatus tick(Board board, Ladybug ladybug) throws LadybugException {
        if (board == null || ladybug == null) {
            return NodeStatus.FAILURE;
        }
        Position target = new Position(x, y);
        return board.flyTo(ladybug, target) ? NodeStatus.SUCCESS : NodeStatus.FAILURE;
    }

    @Override
    public String logArgs() {
        return x + SEPERATOR + y; // genau kein Leerzeichen nach dem Komma
    }
}

