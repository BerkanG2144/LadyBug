package bt;

import exceptions.LadybugException;
import model.Board;
import model.Ladybug;

/**
 * Common interface for all behavior tree nodes.
 * Each node must implement a {@code tick} method that executes
 * its logic on a given board and ladybug.
 *
 * @author ujnaa
 * @version SS25
 */
public interface NodeBehavior {
    /**
     * Executes the node's behavior with the given board and ladybug.
     *
     * @param board   the game board
     * @param ladybug the ladybug agent
     * @return the execution status (SUCCESS, FAILURE, or RUNNING)
     * @throws LadybugException if the ladybug is in an invalid state (e.g. null position/direction)
     */
    NodeStatus tick(Board board, Ladybug ladybug) throws LadybugException;
}
