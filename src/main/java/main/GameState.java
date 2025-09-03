package main;

import bt.BehaviorTreeNode;
import engine.TreeExecution;
import model.Board;
import model.BoardGrid;
import model.LadybugManager;
import model.PathFinder;

import java.util.HashMap;
import java.util.Map;

public class GameState {
    private Board board;
    private BoardGrid grid;
    private LadybugManager ladybugManager;
    private PathFinder pathFinder;

    private Map<Integer, BehaviorTreeNode> ladybugTrees = new HashMap<>();
    private Map<Integer, TreeExecution> executions = new HashMap<>();

    public Board getBoard() {
        return board;
    }

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

    // Direct access to components
    public BoardGrid getGrid() {
        return grid;
    }

    public LadybugManager getLadybugManager() {
        return ladybugManager;
    }

    public PathFinder getPathFinder() {
        return pathFinder;
    }

    public void clearTrees() {
        ladybugTrees.clear();
        executions.clear();
    }

    public void addTree(int ladybugId, BehaviorTreeNode tree) {
        ladybugTrees.put(ladybugId, tree);
        executions.put(ladybugId, new TreeExecution(tree, System.out::println));
    }

    public Map<Integer, BehaviorTreeNode> getLadybugTrees() {
        return ladybugTrees;
    }

    public Map<Integer, TreeExecution> getExecutions() {
        return executions;
    }
}