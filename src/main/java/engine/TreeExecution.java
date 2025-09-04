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
        NodeStatus result = leaf.getBehavior().tick(board, agent);

        String actionName = getLeafBehaviorName(leaf);
        log.accept(agent.getId() + " " + leaf.getId() + " " + actionName + " " + result);

        // Nach Action: bereite nächsten Zustand vor
        prepareNextState(state, action);

        return true;
    }

    private void prepareNextState(ExecuteState state, BehaviorTreeNode executedAction) {
        // Nach einer Action: gehe zurück zur Wurzel für nächsten Durchlauf
        state.setCurrentNode(state.getRootNode());
    }

    private BehaviorTreeNode findNextAction(BehaviorTreeNode node, Board board, Ladybug agent, ExecuteState state) {
        if (!(node instanceof LeafNode)) {
            log.accept(agent.getId() + " " + node.getId() + " " + node.getType() + " ENTRY");
        }

        if (node instanceof LeafNode leaf) {
            if (leaf.isCondition()) {
                NodeStatus result = leaf.getBehavior().tick(board, agent);
                String conditionName = getLeafBehaviorName(leaf);
                log.accept(agent.getId() + " " + leaf.getId() + " " + conditionName + " " + result);
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

                // Prüfe den tatsächlichen Status des Kindes
                NodeStatus childResult = state.getStatusCache().get(child.getId());
                if (childResult == null) {
                    // Für Composite-Kinder, die keinen cached Status haben
                    childResult = NodeStatus.SUCCESS; // oder was auch immer die Logik erfordert
                }

                if (childResult == NodeStatus.FAILURE) {
                    log.accept(agent.getId() + " " + node.getId() + " " + node.getType() + " FAILURE");
                    return null;
                }
            }
            log.accept(agent.getId() + " " + node.getId() + " " + node.getType() + " SUCCESS");
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
            int success = 0;
            int fail = 0;
            int run = 0;
            int unknown = 0;
            var children = par.getChildren();
            int M = children.size();
            int N = par.getRequiredSuccesses();

            for (BehaviorTreeNode child : children) {
                BehaviorTreeNode action = findNextAction(child, board, agent, state);
                if (action != null) {
                    return action; //one action per tick
                }

                NodeStatus s = state.getStatusCache().get(child.getId());
                if (s == null) {
                    unknown++;
                    continue;
                }
                switch (s) {
                    case SUCCESS -> success++;
                    case FAILURE -> fail++;
                }
            }

            NodeStatus overall = null;
            if (success >= N) {
                overall = NodeStatus.SUCCESS;
            } else if (fail > (M - N)) {
                overall = NodeStatus.FAILURE;
            }

            log.accept(agent.getId() + " " + node.getId() + " " + node.getType() + " " + overall);
            state.getStatusCache().put(node.getId(), overall);
            return null;
        }

        return null;
    }

    private String getLeafBehaviorName(LeafNode leaf) {
        String className = leaf.getBehavior().getClass().getSimpleName();

        // Konvertiere erste Buchstabe zu lowercase für camelCase
        if (className.length() > 0) {
            return Character.toLowerCase(className.charAt(0)) + className.substring(1);
        }
        return className;
    }

    public void reset(Ladybug agent) {
        stateOf(agent).reset();
    }

}
