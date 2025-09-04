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
        ExecuteState state = states.computeIfAbsent(agent, k -> new ExecuteState());

        if (state.getRootNode() == null) {
            state.setRootNode(root);
        }
        return state;
    }

    public BehaviorTreeNode getCurrentNode(Ladybug agent) {
        ExecuteState state = stateOf(agent);
        return state.getCurrentNode();
    }

    public boolean jumpTo(Ladybug agent, String nodeId) {
        ExecuteState state = stateOf(agent);
        BehaviorTreeNode targetNode = state.findNodeById(nodeId);

        if (targetNode == null) {
            return false;
        }

        state.setCurrentNode(targetNode);
        state.getStatusCache().clear();
        return true;
    }

    public boolean tick(Board board, Ladybug agent) {
        ExecuteState state = stateOf(agent);
        BehaviorTreeNode currentNode = state.getCurrentNode();

        if (currentNode == null) {
            currentNode = root;
            state.setCurrentNode(currentNode);
        }

        // Finde und führe nächste Action aus
        BehaviorTreeNode action = findNextAction(currentNode, board, agent, state);
        if (action == null) {
            // Kein Action gefunden -> zurück zur Wurzel für nächsten Durchlauf
            state.setCurrentNode(root);
            return false;
        }

        LeafNode leaf = (LeafNode) action;
        log.accept(agent.getId() + " " + leaf.getId() + " ENTRY");
        NodeStatus result = leaf.getBehavior().tick(board, agent);
        log.accept(agent.getId() + " " + leaf.getId() + " " + result);

        // Nach Action: bereite nächsten Zustand vor
        prepareNextState(state, action);

        return true;
    }

    private void prepareNextState(ExecuteState state, BehaviorTreeNode executedAction) {
        // Nach einer Action: gehe zurück zur Wurzel für nächsten Durchlauf
        // (Das ist vereinfacht - in einer vollständigen Implementation würde man
        // den Execution-Kontext beibehalten)
        state.setCurrentNode(state.getRootNode());
        state.getStatusCache().clear();
    }

    private BehaviorTreeNode findNextAction(BehaviorTreeNode node, Board board, Ladybug agent, ExecuteState state) {
        log.accept(agent.getId() + " " + node.getId() + " " + node.getType() + " ENTRY");

        if (node instanceof LeafNode leaf) {
            if (leaf.isCondition()) {
                NodeStatus result = leaf.getBehavior().tick(board, agent);
                log.accept(agent.getId() + " " + leaf.getId() + " " + result);
                state.getStatusCache().put(leaf.getId(), result);
                return null;
            } else {
                return leaf; // Action gefunden
            }
        }

        if (node instanceof SequenceNode seq) {
            for (BehaviorTreeNode child : seq.getChildren()) {
                BehaviorTreeNode next = findNextAction(child, board, agent, state);
                if (next != null) return next;

                NodeStatus res = state.getStatusCache().getOrDefault(child.getId(), NodeStatus.SUCCESS);
                if (res == NodeStatus.FAILURE) {
                    log.accept(agent.getId() + " " + node.getId() + " FAILURE");
                    return null;
                }
            }
            log.accept(agent.getId() + " " + node.getId() + " SUCCESS");
            return null;
        }

        if (node instanceof FallbackNode fb) {
            for (BehaviorTreeNode child : fb.getChildren()) {
                BehaviorTreeNode next = findNextAction(child, board, agent, state);
                if (next != null) return next;

                NodeStatus res = state.getStatusCache().getOrDefault(child.getId(), NodeStatus.FAILURE);
                if (res == NodeStatus.SUCCESS) {
                    log.accept(agent.getId() + " " + node.getId() + " SUCCESS");
                    return null;
                }
            }
            log.accept(agent.getId() + " " + node.getId() + " FAILURE");
            return null;
        }

        if (node instanceof ParallelNode par) {
            int successCount = 0;
            for (BehaviorTreeNode child : par.getChildren()) {
                BehaviorTreeNode next = findNextAction(child, board, agent, state);
                if (next != null) return next;
                NodeStatus res = state.getStatusCache()
                        .getOrDefault(child.getId(), NodeStatus.FAILURE);
                if (res == NodeStatus.SUCCESS) successCount++;
            }
            if (successCount >= par.getRequiredSuccesses()) {
                log.accept(agent.getId() + " " + node.getId() + " SUCCESS");
            } else {
                log.accept(agent.getId() + " " + node.getId() + " FAILURE");
            }
            return null;
        }

        return null;
    }

    public void reset(Ladybug agent) {
        stateOf(agent).reset();
    }

}
