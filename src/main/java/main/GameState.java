package main;

import bt.BehaviorTreeNode;
import engine.TreeExecution;
import model.Board;
import model.Ladybug;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class GameState {
    private Board board;
    private Map<Integer, BehaviorTreeNode> ladybugTrees = new HashMap<>();
    private Map<Integer, TreeExecution> executions = new HashMap<>();

    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public void clearTrees() {
        ladybugTrees.clear();
        executions.clear();
    }

    public void addTree(int ladybugId, BehaviorTreeNode tree) {
        ladybugTrees.put(ladybugId, tree);
        executions.put(ladybugId, new TreeExecution(tree, System.out::println));
    }
}