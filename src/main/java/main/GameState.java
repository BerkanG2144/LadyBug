package main;

import bt.BehaviorTreeNode;
import engine.TreeExecution;
import model.Board;
import model.BoardGrid;
import model.LadybugManager;
import model.PathFinder;

import java.util.HashMap;
import java.util.Map;


/**
 * Holds and coordinates all runtime state of the game.
 * <p>
 * The {@code GameState} aggregates the loaded {@link Board} and provides
 * direct access to its core components ({@link BoardGrid}, {@link LadybugManager},
 * {@link PathFinder}). It also manages behavior trees per ladybug and the
 * corresponding {@link TreeExecution} instances used to tick trees during
 * gameplay.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Store and expose the current {@link Board} and its components.</li>
 *   <li>Register, clear, and look up behavior trees for ladybugs.</li>
 *   <li>Maintain execution contexts for behavior trees.</li>
 * </ul>
 *
 * <p>This class does not perform game logic itself; it serves as a shared,
 * mutable state container used by commands and the engine.</p>
 * @author ujnaa
 */
public class GameState {
    private Board board;
    private BoardGrid grid;
    private LadybugManager ladybugManager;
    private PathFinder pathFinder;

    private Map<Integer, BehaviorTreeNode> ladybugTrees = new HashMap<>();
    private Map<Integer, TreeExecution> executions = new HashMap<>();

    /**
     * Returns the currently loaded board.
     *
     * @return the current {@link Board}, or {@code null} if none is loaded
     */
    public Board getBoard() {
        return board;
    }

    /**
     * Sets the current board and extracts its components for direct access.
     * <p>
     * When the board changes, previously registered trees and executions
     * are cleared to avoid stale state.
     * </p>
     *
     * @param board the new board (maybe {@code null} to clear the state)
     */
    public void setBoard(Board board) {
        this.board = board;
        // Extract components for direct access
        if (board != null) {
            this.grid = board.getGrid(); // Wir müssen einen Getter hinzufügen
            this.ladybugManager = board.getLadybugManager(); // Wir müssen einen Getter hinzufügen
            this.pathFinder = board.getPathFinder(); // Wir müssen einen Getter hinzufügen
        } else {
            this.grid = null;
            this.ladybugManager = null;
            this.pathFinder = null;
        }
        clearTrees();
    }

    /**
     * Returns the board grid extracted from the current board.
     *
     * @return the {@link BoardGrid}, or {@code null} if no board is set
     */
    public BoardGrid getGrid() {
        return grid;
    }

    /**
     * Returns the ladybug manager extracted from the current board.
     *
     * @return the {@link LadybugManager}, or {@code null} if no board is set
     */
    public LadybugManager getLadybugManager() {
        return ladybugManager;
    }

    /**
     * Returns the pathfinder extracted from the current board.
     *
     * @return the {@link PathFinder}, or {@code null} if no board is set
     */
    public PathFinder getPathFinder() {
        return pathFinder;
    }

    /**
     * Removes all registered behavior trees and their executions.
     * <p>
     * Typically called when a new board is loaded.
     * </p>
     */
    public void clearTrees() {
        ladybugTrees.clear();
        executions.clear();
    }

    /**
     * Registers a behavior tree for the given ladybug and initializes its execution.
     *
     * @param ladybugId the unique ladybug ID
     * @param tree      the root node of the behavior tree
     * @throws IllegalArgumentException if {@code tree} is {@code null}
     */
    public void addTree(int ladybugId, BehaviorTreeNode tree) {
        ladybugTrees.put(ladybugId, tree);
        executions.put(ladybugId, new TreeExecution(tree, System.out::println));
    }
    /**
     * Returns the mapping of ladybug IDs to their behavior trees.
     *
     * @return a mutable map of {@code ladybugId -> tree}
     */
    public Map<Integer, BehaviorTreeNode> getLadybugTrees() {
        return ladybugTrees;
    }

    /**
     * Returns the mapping of ladybug IDs to their tree executions.
     *
     * @return a mutable map of {@code ladybugId -> execution}
     */
    public Map<Integer, TreeExecution> getExecutions() {
        return executions;
    }
}