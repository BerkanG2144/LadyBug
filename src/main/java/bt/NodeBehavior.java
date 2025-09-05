package bt;

import model.Board;
import model.Ladybug;

/**
 * Common interface for all behavior tree nodes.
 * Each node must implement a {@code tick} method that executes
 * its logic on a given board and ladybug.
 *
 * @author ujnaa
 */
public interface NodeBehavior {
    /**
     * Executes the node's behavior with the given board and ladybug.
     *
     * @param board   the game board
     * @param ladybug the ladybug agent
     * @return the execution status (SUCCESS, FAILURE, or RUNNING)
     */
    NodeStatus tick(Board board, Ladybug ladybug);
}
