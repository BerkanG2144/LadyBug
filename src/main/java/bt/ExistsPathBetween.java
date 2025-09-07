package bt;

import model.Board;
import model.Ladybug;

/**
 * Behavior tree node that checks whether a valid path exists
 * between two specified coordinates on the board.
 *
 * Returns SUCCESS if a path exists, otherwise FAILURE.
 *
 * @author ujnaa
 */
public class ExistsPathBetween implements NodeBehavior, LogArgsProvider, LogNameProvider {
    private final int x1;
    private final int y1;
    private final int x2;
    private final int y2;

    /**
     * Constructs an ExistsPathBetween node with start and end coordinates.
     * @param x1 The x-coordinate of the start position.
     * @param y1 The y-coordinate of the start position.
     * @param x2 The x-coordinate of the end position.
     * @param y2 The y-coordinate of the end position.
     */
    public ExistsPathBetween(int x1, int y1, int x2, int y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    @Override
    public NodeStatus tick(Board board, Ladybug ladybug) {
        if (board == null) {
            return NodeStatus.FAILURE;
        }
        // Ladybug is not needed for this condition, but included for interface compliance
        return board.existsPath(x1, y1, x2, y2) ? NodeStatus.SUCCESS : NodeStatus.FAILURE;
    }

    @Override
    public String logArgs() {
        return x1 + "," + y1 + " " + x2 + "," + y2; // genau kein Leerzeichen nach dem Komma
    }

    @Override
    public String logName() {
        return "existsPath";
    }
}