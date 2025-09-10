package engine;

import bt.*;
import exceptions.LadybugException;
import model.Board;
import model.Ladybug;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Executes a behavior tree for agents.
 * @author ujnaa
 */
public class TreeExecution {
    private final BehaviorTreeNode root;
    private final Map<Ladybug, ExecuteState> states = new HashMap<>();
    private final Logger log;

    /**
     * Creates executor.
     * @param root root node
     * @param log logger
     */
    public TreeExecution(BehaviorTreeNode root, Logger log) {
        this.root = Objects.requireNonNull(root);
        this.log = log != null ? log : message -> { };
    }

    /**
     * Gets execution state.
     * @param agent the agent
     * @return execution state
     */
    public ExecuteState stateOf(Ladybug agent) {
        ExecuteState state = states.computeIfAbsent(agent, k -> new ExecuteState());
        if (state.getRootNode() == null) {
            state.setRootNode(root);
        }
        return state;
    }

    /**
     * Finds next action for head.
     * @param board game board
     * @param agent the agent
     * @return next action or null
     * @throws LadybugException if error
     */
    public BehaviorTreeNode findNextActionNode(Board board, Ladybug agent) throws LadybugException {
        ExecuteState state = stateOf(agent);
        BehaviorTreeNode currentNode = state.getCurrentNode();
        return findNextForHead(currentNode != null ? currentNode : root, board, agent, state);
    }

    private BehaviorTreeNode findNextForHead(BehaviorTreeNode node, Board board,
                                             Ladybug agent, ExecuteState state) throws LadybugException {
        String type = node.getType();

        if ("leaf".equals(type)) {
            LeafNode leaf = (LeafNode) node;
            return (leaf.isCondition() || state.getStatusCache().containsKey(leaf.getId())) ? null : leaf;
        }

        if ("sequence".equals(type)) {
            return findNextInSequenceHead((SequenceNode) node, board, agent, state);
        }

        if ("fallback".equals(type)) {
            return findNextInFallbackHead((FallbackNode) node, board, agent, state);
        }

        if ("parallel".equals(type)) {
            return findNextInParallelHead((ParallelNode) node, board, agent, state);
        }

        return null;
    }

    private BehaviorTreeNode findNextInSequenceHead(SequenceNode seq, Board board,
                                                    Ladybug agent, ExecuteState state) throws LadybugException {
        for (BehaviorTreeNode child : seq.getChildren()) {
            NodeStatus cached = state.getStatusCache().get(child.getId());
            if (cached != null) {
                if (cached == NodeStatus.FAILURE) {
                    return null;
                }
                continue;
            }
            BehaviorTreeNode next = findNextForHead(child, board, agent, state);
            if (next != null) {
                return next;
            }
        }
        return null;
    }

    private BehaviorTreeNode findNextInFallbackHead(FallbackNode fb, Board board,
                                                    Ladybug agent, ExecuteState state) throws LadybugException {
        for (BehaviorTreeNode child : fb.getChildren()) {
            NodeStatus cached = state.getStatusCache().get(child.getId());
            if (cached != null) {
                if (cached == NodeStatus.SUCCESS) {
                    return null;
                }
                continue;
            }
            BehaviorTreeNode next = findNextForHead(child, board, agent, state);
            if (next != null) {
                return next;
            }
        }
        return null;
    }

    private BehaviorTreeNode findNextInParallelHead(ParallelNode par, Board board,
                                                    Ladybug agent, ExecuteState state) throws LadybugException {
        for (BehaviorTreeNode child : par.getChildren()) {
            if (state.getStatusCache().get(child.getId()) == null) {
                BehaviorTreeNode next = findNextForHead(child, board, agent, state);
                if (next != null) return next;
            }
        }
        return null;
    }

    /**
     * Jumps to node.
     * @param agent the agent
     * @param nodeId node ID
     * @return success
     */
    public boolean jumpTo(Ladybug agent, String nodeId) {
        ExecuteState state = stateOf(agent);
        BehaviorTreeNode targetNode = state.findNodeById(nodeId);
        if (targetNode == null) {
            return false;
        }

        state.getStatusCache().clear();
        state.getOpenCompositeEntries().clear();
        markSkippedSiblings(targetNode, state);
        state.setCurrentNode(targetNode);
        return true;
    }

    private void markSkippedSiblings(BehaviorTreeNode target, ExecuteState state) {
        BehaviorTreeNode parent = findParent(root, target);
        if (parent == null) {
            return;
        }

        List<BehaviorTreeNode> children = parent.getChildren();
        int targetIndex = children.indexOf(target);
        String parentType = parent.getType();

        NodeStatus skipStatus = ("sequence".equals(parentType)) ? NodeStatus.SUCCESS : NodeStatus.FAILURE;

        for (int i = 0; i < targetIndex; i++) {
            state.getStatusCache().put(children.get(i).getId(), skipStatus);
        }
    }

    /**
     * Executes next action.
     * @param board game board
     * @param agent the agent
     * @return success
     * @throws LadybugException if error
     */
    public boolean tick(Board board, Ladybug agent) throws LadybugException {
        ExecuteState state = stateOf(agent);
        BehaviorTreeNode currentNode = state.getCurrentNode();
        if (currentNode == null) {
            currentNode = root;
            state.setCurrentNode(currentNode);
        }

        BehaviorTreeNode action = findNextAction(currentNode, board, agent, state);
        if (action == null && handleTreeCompletion(state)) {
            action = findNextAction(root, board, agent, state);
            if (action == null) return false;
        } else if (action == null) {
            return false;
        }

        executeLeafAction((LeafNode) action, board, agent, state);
        state.setCurrentNode(state.getRootNode());
        return true;
    }

    private boolean handleTreeCompletion(ExecuteState state) {
        if (state.getStatusCache().get(root.getId()) != null) {
            state.setCurrentNode(root);
            state.getStatusCache().clear();
            state.getOpenCompositeEntries().clear();
            return true;
        }
        return false;
    }

    private void executeLeafAction(LeafNode leaf, Board board, Ladybug agent, ExecuteState state)
            throws LadybugException {
        String name = leaf.getLogNameOrDefault();
        String argsForLog = leaf.getLogArgsOrEmpty();
        NodeStatus result = leaf.getBehavior().tick(board, agent);
        log.log(agent.getId() + " " + leaf.getId() + " " + name + argsForLog + " " + result);
        state.setLastExecutedLeaf(leaf);
        state.getStatusCache().put(leaf.getId(), result);
    }

    private BehaviorTreeNode findNextAction(BehaviorTreeNode node, Board board,
                                            Ladybug agent, ExecuteState state) throws LadybugException {
        String type = node.getType();

        if (!"leaf".equals(type)) {
            logCompositeEntry(agent, state, node);
        }

        if ("leaf".equals(type)) {
            return handleLeaf((LeafNode) node, board, agent, state);
        }

        if ("sequence".equals(type)) {
            return handleSequence((SequenceNode) node, board, agent, state);
        }

        if ("fallback".equals(type)) {
            return handleFallback((FallbackNode) node, board, agent, state);
        }

        if ("parallel".equals(type)) {
            return handleParallel((ParallelNode) node, board, agent, state);
        }

        return null;
    }

    private BehaviorTreeNode handleLeaf(LeafNode leaf, Board board, Ladybug agent, ExecuteState state)
            throws LadybugException {
        if (leaf.isCondition()) {
            if (state.getStatusCache().containsKey(leaf.getId())) return null;

            NodeStatus result = leaf.getBehavior().tick(board, agent);
            String name = leaf.getLogNameOrDefault();
            String args = leaf.getLogArgsOrEmpty();
            log.log(agent.getId() + " " + leaf.getId() + " " + name + args + " " + result);
            state.setLastExecutedLeaf(leaf);
            state.getStatusCache().put(leaf.getId(), result);
            return null;
        }
        return state.getStatusCache().containsKey(leaf.getId()) ? null : leaf;
    }

    private BehaviorTreeNode handleSequence(SequenceNode seq, Board board, Ladybug agent, ExecuteState state)
            throws LadybugException {
        for (BehaviorTreeNode child : seq.getChildren()) {
            NodeStatus cached = state.getStatusCache().get(child.getId());
            if (cached != null) {
                if (cached == NodeStatus.FAILURE) {
                    logAndCache(agent, seq, NodeStatus.FAILURE, state);
                    return null;
                }
                continue;
            }

            if (!"leaf".equals(child.getType())) {
                logCompositeEntry(agent, state, child);
            }

            BehaviorTreeNode next = findNextAction(child, board, agent, state);
            if (next != null) return next;

            NodeStatus childResult = state.getStatusCache().get(child.getId());
            if (childResult == null && !"leaf".equals(child.getType())) return null;
            if (childResult == NodeStatus.FAILURE) {
                logAndCache(agent, seq, NodeStatus.FAILURE, state);
                return null;
            }
        }
        logAndCache(agent, seq, NodeStatus.SUCCESS, state);
        return null;
    }

    private BehaviorTreeNode handleFallback(FallbackNode fb, Board board, Ladybug agent, ExecuteState state)
            throws LadybugException {
        for (BehaviorTreeNode child : fb.getChildren()) {
            NodeStatus cached = state.getStatusCache().get(child.getId());
            if (cached != null) {
                if (cached == NodeStatus.SUCCESS) {
                    logAndCache(agent, fb, NodeStatus.SUCCESS, state);
                    return null;
                }
                continue;
            }

            if (!"leaf".equals(child.getType())) {
                logCompositeEntry(agent, state, child);
            }

            BehaviorTreeNode next = findNextAction(child, board, agent, state);
            if (next != null) return next;

            NodeStatus res = state.getStatusCache().getOrDefault(child.getId(), NodeStatus.FAILURE);
            if (res == NodeStatus.SUCCESS) {
                logAndCache(agent, fb, NodeStatus.SUCCESS, state);
                return null;
            }
        }
        logAndCache(agent, fb, NodeStatus.FAILURE, state);
        return null;
    }

    private BehaviorTreeNode handleParallel(ParallelNode par, Board board, Ladybug agent, ExecuteState state)
            throws LadybugException {
        int succ = 0;
        int fail = 0;
        var children = par.getChildren();
        int totalChildren = children.size();
        int requiredSuccesses = par.getRequiredSuccesses();

        for (BehaviorTreeNode child : children) {
            NodeStatus cached = state.getStatusCache().get(child.getId());
            if (cached != null) {
                if (cached == NodeStatus.SUCCESS) succ++;
                else if (cached == NodeStatus.FAILURE) fail++;
                continue;
            }

            BehaviorTreeNode action = findNextAction(child, board, agent, state);
            if (action != null) return action;

            NodeStatus childStatus = state.getStatusCache().get(child.getId());
            if (childStatus == NodeStatus.SUCCESS) succ++;
            else if (childStatus == NodeStatus.FAILURE) fail++;
        }

        if (succ >= requiredSuccesses) {
            logAndCache(agent, par, NodeStatus.SUCCESS, state);
            clearChildStatuses(par, state);
        } else if (fail > (totalChildren - requiredSuccesses)) {
            logAndCache(agent, par, NodeStatus.FAILURE, state);
            clearChildStatuses(par, state);
        }
        return null;
    }

    private void logAndCache(Ladybug agent, BehaviorTreeNode node, NodeStatus status, ExecuteState state) {
        log.log(agent.getId() + " " + node.getId() + " " + node.getType() + " " + status);
        state.getStatusCache().put(node.getId(), status);
        state.getOpenCompositeEntries().remove(node.getId());
    }

    private void logCompositeEntry(Ladybug agent, ExecuteState state, BehaviorTreeNode node) {
        if (state.getOpenCompositeEntries().add(node.getId())) {
            log.log(agent.getId() + " " + node.getId() + " " + node.getType() + " ENTRY");
        }
    }

    private void clearChildStatuses(BehaviorTreeNode parent, ExecuteState state) {
        for (BehaviorTreeNode child : parent.getChildren()) {
            state.getStatusCache().remove(child.getId());
            state.getOpenCompositeEntries().remove(child.getId());
            if (!"leaf".equals(child.getType())) {
                clearChildStatuses(child, state);
            }
        }
    }

    private BehaviorTreeNode findParent(BehaviorTreeNode current, BehaviorTreeNode target) {
        for (BehaviorTreeNode child : current.getChildren()) {
            if (child == target) return current;
            BehaviorTreeNode parent = findParent(child, target);
            if (parent != null) return parent;
        }
        return null;
    }

    /**
     * Resets execution state.
     * @param agent agent to reset
     */
    public void reset(Ladybug agent) {
        stateOf(agent).reset();
    }
}