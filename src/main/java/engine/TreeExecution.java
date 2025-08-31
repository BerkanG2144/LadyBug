package engine;

import bt.*;
import model.Board;
import model.Ladybug;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class TreeExecution {

    private final BehaviorTreeNode root;
    private final Map<Ladybug, ExecuteState> states = new HashMap<>();
    private final Consumer<String> log;

    public TreeExecution(BehaviorTreeNode root, Consumer<String> log) {
        this.root = Objects.requireNonNull(root);
        this.log = log != null ? log : s -> {};
    }

    private ExecuteState stateOf(Ladybug agent) {
        return states.computeIfAbsent(agent, k -> new ExecuteState());
    }

    public boolean tick(Board board, Ladybug agent) {
        ExecuteState state = stateOf(agent);
        BehaviorTreeNode action = findNextAction(root, board, agent, state);
        if (action == null) return false;

        LeafNode leaf = (LeafNode) action;
        // ENTRY-Log für die Action
        log.accept(agent.getId() + " " + leaf.getId() + " ENTRY");

        NodeStatus result = leaf.getBehavior().tick(board, agent);

        // Ergebnis loggen + im State merken
        log.accept(agent.getId() + " " + leaf.getId() + " " + result);
        state.getStatusCache().put(leaf.getId(), result);

        // Simple Strategie: Cache nach Action leeren -> frische Traversal im nächsten Tick
        state.getStatusCache().clear();
        return true;
    }

    private BehaviorTreeNode findNextAction(BehaviorTreeNode node, Board board, Ladybug agent, ExecuteState state) {
        log.accept(agent.getId() + " " + node.getType() + " ENTRY ");

        if (node instanceof LeafNode leaf) {
            NodeStatus result = leaf.getBehavior().tick(board, agent);
            log.accept(agent.getId() + " " + node.getType() + " " + result);
            if (leaf.isCondition()) {
                return result == NodeStatus.SUCCESS ? null : null;
            } else {
                return leaf;
            }
        }

        if (node instanceof SequenceNode seq) {
            for (BehaviorTreeNode child : seq.getChildren()) {
                BehaviorTreeNode next = findNextAction(child, board, agent, state);
                if (next != null) return next;
                NodeStatus res = state.getStatusCache().getOrDefault(child.getType(), NodeStatus.SUCCESS);
                if (res == NodeStatus.FAILURE) {
                    log.accept(agent.getId() + " " + node.getType() + "FAILURE");
                    return null;
                }
            }
            log.accept(agent.getId() + " " + node.getType() + "SUCCESS");
            return null;
        }

        if (node instanceof FallbackNode fb) {
            for (BehaviorTreeNode child : fb.getChildren()) {
                BehaviorTreeNode next = findNextAction(child, board, agent, state);
                if (next != null) return next;
                NodeStatus res = state.getStatusCache().getOrDefault(child.getType(), NodeStatus.FAILURE);
                if (res == NodeStatus.SUCCESS) {
                    log.accept(agent.getId() + " " + node.getType() + "SUCCESS" );
                    return null;
                }
            }
            log.accept(agent.getId() + " " + node.getType() + "FAILURE");
            return null;
        }

        if (node instanceof ParallelNode par) {
            int successCount = 0;
            for (BehaviorTreeNode child : par.getChildren()) {
                BehaviorTreeNode next = findNextAction(child, board, agent, state);
                if (next != null) return next;
                NodeStatus res = state.getStatusCache()
                        .getOrDefault(child.getType(), NodeStatus.FAILURE);
                if (res == NodeStatus.SUCCESS) successCount++;
            }
            if (successCount >= par.getRequiredSuccesses()) {
                log.accept(agent.getId() + " " + node.getType() + " SUCCESS");
            } else {
                log.accept(agent.getId() + " " + node.getType() + " FAILURE");
            }
            return null;
        }

        return null;
    }

    public void reset(Ladybug agent) {
        stateOf(agent).reset();
    }

}
