package bt;

import model.Board;
import model.Ladybug;

/**
 * Behavior tree node that checks whether a valid path exists
 * from the current ladybug position to a target coordinate.
 *
 * Returns SUCCESS if a path exists, otherwise FAILURE.
 *
 * @author ujnaa
 * @version SS25
 */
public class ExistsPath implements NodeBehavior, LogArgsProvider, LogNameProvider {
    private static final String SEPERATOR = ",";
    private static final String EXISTS_PATH = "existsPath";
    private final int x;
    private final int y;

    /**
     * Constructs an ExistsPath node with target coordinates.
     * @param x The x-coordinate of the target position.
     * @param y The y-coordinate of the target position.
     */
    public ExistsPath(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public NodeStatus tick(Board board, Ladybug ladybug) {
        if (board == null || ladybug == null) {
            return NodeStatus.FAILURE;
        }
        return board.existsPath(ladybug, x, y) ? NodeStatus.SUCCESS : NodeStatus.FAILURE;
    }

    @Override
    public String logArgs() {
        return x + SEPERATOR + y;
    }

    @Override
    public String logName() {
        return EXISTS_PATH;
    }
}