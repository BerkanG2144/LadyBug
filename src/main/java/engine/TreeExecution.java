package engine;
import bt.BehaviorTreeNode;
import bt.LeafNode;
import bt.CompositeNode;
import bt.SequenceNode;
import bt.ParallelNode;
import bt.NodeStatus;
import bt.FallbackNode;
import exceptions.LadybugException;
import model.Board;
import model.Ladybug;

import java.util.*;

/**
 * Executes a behavior tree for one or more {@link Ladybug} agents.
 * @author ujnaa
 */
public class TreeExecution {
    private final BehaviorTreeNode root;
    private final Map<Ladybug, ExecuteState> states = new HashMap<>();
    private final Logger log;
    /**
     * Creates a new executor for the given behavior tree root.
     * @param root the root node of the behavior tree (must not be {@code null})
     * @param log  optional logger; if {@code null}, logging is disabled
     * @throws NullPointerException if {@code root} is {@code null}
     */
    public TreeExecution(BehaviorTreeNode root, Logger log) {
        this.root = Objects.requireNonNull(root);
        this.log = log != null ? log : message -> { };
    }
    /**
     * Returns the mutable execution state associated with the given agent.
     * @param agent the agent whose state to obtain
     * @return the (created if necessary) {@link ExecuteState} for the agent
     */
    public ExecuteState stateOf(Ladybug agent) {
        ExecuteState state = states.computeIfAbsent(agent, k -> new ExecuteState());

        if (state.getRootNode() == null) {
            state.setRootNode(root);
        }
        return state;
    }
    /**
     * Advances the behavior tree for the given agent by finding and executing the next.
     * @param board the game board (environment)
     * @param agent the agent to tick
     * @return {@code true} if an action was executed during this tick; {@code false} otherwise
     * @throws LadybugException if a leaf behavior throws during execution
     */
    public boolean tick(Board board, Ladybug agent) throws LadybugException {
        ExecuteState state = stateOf(agent);
        BehaviorTreeNode currentNode = state.getCurrentNode();
        if (currentNode == null) {
            currentNode = root;
            state.setCurrentNode(currentNode);
        }
        BehaviorTreeNode action = findNextAction(currentNode, board, agent, state);
        if (action == null) {
            NodeStatus rootStatus = state.getStatusCache().get(root.getId());
            if (rootStatus != null) {
                state.setCurrentNode(root);
                state.getStatusCache().clear();
                state.getOpenCompositeEntries().clear();
                action = findNextAction(root, board, agent, state);
                if (action == null) {
                    return false; // Really no action possible
                }
            } else {
                return false;
            }
        }
        LeafNode leaf = (LeafNode) action;
        String name = leaf.getLogNameOrDefault();
        String argsForLog = leaf.getLogArgsOrEmpty();
        NodeStatus result = leaf.getBehavior().tick(board, agent);
        log.log(agent.getId() + " " + leaf.getId() + " " + name + argsForLog + " " + result);
        state.setLastExecutedLeaf(leaf);
        state.getStatusCache().put(leaf.getId(), result);
        prepareNextState(state, action);
        return true;
    }
    /**
     * Finds (but does not execute) the next actionable leaf that would be run for the agent.
     * @param board the game board (environment)
     * @param agent the agent
     * @return the next {@link LeafNode} that represents an action, or {@code null} if none is pending
     * @throws LadybugException if evaluating a condition throws during the lookahead
     */
    public BehaviorTreeNode findNextActionNode(Board board, Ladybug agent) throws LadybugException {
        ExecuteState state = stateOf(agent);
        BehaviorTreeNode currentNode = state.getCurrentNode();

        if (currentNode == null) {
            currentNode = root;
        }
        return findNextActionForHead(currentNode, board, agent, state);
    }
    /**
     * Hilfsmethode für head Command - findet nächsten Action ohne Seiteneffekte
     */
    private BehaviorTreeNode findNextActionForHead(BehaviorTreeNode node, Board board, Ladybug agent, ExecuteState state)
            throws LadybugException {
        if (node instanceof LeafNode leaf) {
            if (leaf.isCondition()) {
                if (state.getStatusCache().containsKey(leaf.getId())) {
                    return null;
                }
                return null;
            } else {
                if (state.getStatusCache().containsKey(leaf.getId())) {
                    return null;
                }
                return leaf;
            }
        }
        if (node instanceof SequenceNode seq) {
            return findNextActionInSequence(seq, board, agent, state);
        }
        if (node instanceof FallbackNode fb) {
            return findNextActionInFallback(fb, board, agent, state);
        }
        if (node instanceof ParallelNode par) {
            return findNextActionInParallel(par, board, agent, state);
        }
        return null;
    }
    private BehaviorTreeNode findNextActionInSequence(SequenceNode seq, Board board, Ladybug agent, ExecuteState state)
            throws LadybugException {
        for (BehaviorTreeNode child : seq.getChildren()) {
            NodeStatus cached = state.getStatusCache().get(child.getId());
            if (cached != null) {
                if (cached == NodeStatus.FAILURE) {
                    return null;
                }
                continue;
            }
            BehaviorTreeNode next = findNextActionForHead(child, board, agent, state);
            if (next != null) {
                return next;
            }
        }
        return null;
    }
    private BehaviorTreeNode findNextActionInFallback(FallbackNode fb, Board board, Ladybug agent, ExecuteState state)
            throws LadybugException {
        for (BehaviorTreeNode child : fb.getChildren()) {
            NodeStatus cached = state.getStatusCache().get(child.getId());
            if (cached != null) {
                if (cached == NodeStatus.SUCCESS) {
                    return null;
                }
                continue;
            }
            BehaviorTreeNode next = findNextActionForHead(child, board, agent, state);
            if (next != null) {
                return next;
            }
        }
        return null;
    }
    private BehaviorTreeNode findNextActionInParallel(ParallelNode par, Board board, Ladybug agent, ExecuteState state)
            throws LadybugException {
        for (BehaviorTreeNode child : par.getChildren()) {
            NodeStatus cached = state.getStatusCache().get(child.getId());
            if (cached == null) {
                BehaviorTreeNode next = findNextActionForHead(child, board, agent, state);
                if (next != null) {
                    return next;
                }
            }
        }
        return null;
    }
    /**
     * Moves the agent's current node to the node with the given ID, if present.
     * @param agent  the agent whose execution pointer to move
     * @param nodeId the target node ID
     * @return {@code true} if the node was found and the jump performed; {@code false} otherwise
     */
    public boolean jumpTo(Ladybug agent, String nodeId) {
        ExecuteState state = stateOf(agent);
        BehaviorTreeNode targetNode = state.findNodeById(nodeId);
        if (targetNode == null) {
            return false;
        }
        List<BehaviorTreeNode> pathToTarget = findPathToNode(root, targetNode);
        if (pathToTarget == null) {
            return false;
        }
        state.getStatusCache().clear();
        for (BehaviorTreeNode ancestor : pathToTarget) {
            if (ancestor instanceof CompositeNode && ancestor != targetNode) {
                state.getOpenCompositeEntries().add(ancestor.getId());
            }
        }
        BehaviorTreeNode parent = findParent(root, targetNode);
        if (parent != null && parent instanceof CompositeNode) {
            List<BehaviorTreeNode> children = parent.getChildren();
            int targetIndex = children.indexOf(targetNode);
            for (int i = 0; i < targetIndex; i++) {
                BehaviorTreeNode skippedChild = children.get(i);
                if (parent instanceof FallbackNode) {
                    state.getStatusCache().put(skippedChild.getId(), NodeStatus.FAILURE);
                } else if (parent instanceof SequenceNode) {
                    state.getStatusCache().put(skippedChild.getId(), NodeStatus.SUCCESS);
                } else if (parent instanceof ParallelNode) {
                    state.getStatusCache().put(skippedChild.getId(), NodeStatus.FAILURE);
                }
            }
        }
        state.setCurrentNode(root);
        return true;
    }
    private List<BehaviorTreeNode> findPathToNode(BehaviorTreeNode root, BehaviorTreeNode target) {
        if (root == target) {
            List<BehaviorTreeNode> path = new ArrayList<>();
            path.add(root);
            return path;
        }
        for (BehaviorTreeNode child : root.getChildren()) {
            List<BehaviorTreeNode> childPath = findPathToNode(child, target);
            if (childPath != null) {
                List<BehaviorTreeNode> path = new ArrayList<>();
                path.add(root);
                path.addAll(childPath);
                return path;
            }
        }
        return null;
    }
    private BehaviorTreeNode findParent(BehaviorTreeNode current, BehaviorTreeNode target) {
        for (BehaviorTreeNode child : current.getChildren()) {
            if (child == target) {
                return current;
            }
            BehaviorTreeNode parent = findParent(child, target);
            if (parent != null) {
                return parent;
            }
        }
        return null;
    }
    private void prepareNextState(ExecuteState state, BehaviorTreeNode executedAction) {
        state.setCurrentNode(state.getRootNode());
    }
    private BehaviorTreeNode findNextAction(BehaviorTreeNode node, Board board, Ladybug agent, ExecuteState state) throws LadybugException {
        if (!(node instanceof LeafNode)) {
            logCompositeEntryOnce(agent, state, node);
        }
        if (node instanceof LeafNode leaf) {
            return handleLeafNode(leaf, board, agent, state);
        }
        if (node instanceof SequenceNode seq) {
            return handleSequenceNode(seq, board, agent, state);
        }
        if (node instanceof FallbackNode fb) {
            return handleFallbackNode(fb, board, agent, state);
        }
        if (node instanceof ParallelNode par) {
            return handleParallelNode(par, board, agent, state);
        }
        return null;
    }
    private BehaviorTreeNode handleLeafNode(LeafNode leaf, Board board, Ladybug agent, ExecuteState state) throws LadybugException {
        if (leaf.isCondition()) {
            if (state.getStatusCache().containsKey(leaf.getId())) {
                return null;
            }
            NodeStatus result = leaf.getBehavior().tick(board, agent);
            String name = leaf.getLogNameOrDefault();
            String args = leaf.getLogArgsOrEmpty();
            log.log(agent.getId() + " " + leaf.getId() + " " + name + args + " " + result);
            state.setLastExecutedLeaf(leaf);
            state.getStatusCache().put(leaf.getId(), result);
            return null;
        } else {
            if (state.getStatusCache().containsKey(leaf.getId())) {
                return null;
            }
            return leaf;
        }
    }
    private BehaviorTreeNode handleSequenceNode(SequenceNode seq, Board board, Ladybug agent, ExecuteState state) throws LadybugException {
        for (BehaviorTreeNode child : seq.getChildren()) {
            NodeStatus cached = state.getStatusCache().get(child.getId());
            if (cached != null) {
                if (cached == NodeStatus.FAILURE) {
                    log.log(agent.getId() + " " + seq.getId() + " " + seq.getType() + " FAILURE");
                    state.getStatusCache().put(seq.getId(), NodeStatus.FAILURE);
                    resolveComposite(state, seq);
                    return null;
                }
                continue;
            }
            if (child instanceof CompositeNode) {
                logCompositeEntryOnce(agent, state, child);
            }
            BehaviorTreeNode next = findNextAction(child, board, agent, state);
            if (next != null) {
                return next;
            }
            NodeStatus childResult = state.getStatusCache().get(child.getId());
            if (childResult == null && child instanceof CompositeNode) {
                return null;
            }
            if (childResult == NodeStatus.FAILURE) {
                log.log(agent.getId() + " " + seq.getId() + " " + seq.getType() + " FAILURE");
                state.getStatusCache().put(seq.getId(), NodeStatus.FAILURE);
                resolveComposite(state, seq);
                return null;
            }
        }
        log.log(agent.getId() + " " + seq.getId() + " " + seq.getType() + " SUCCESS");
        state.getStatusCache().put(seq.getId(), NodeStatus.SUCCESS);
        return null;
    }
    private BehaviorTreeNode handleFallbackNode(FallbackNode fb, Board board, Ladybug agent, ExecuteState state)
            throws LadybugException {
        for (BehaviorTreeNode child : fb.getChildren()) {
            NodeStatus cached = state.getStatusCache().get(child.getId());
            if (cached != null) {
                if (cached == NodeStatus.SUCCESS) {
                    log.log(agent.getId() + " " + fb.getId() + " " + fb.getType() + " SUCCESS");
                    state.getStatusCache().put(fb.getId(), NodeStatus.SUCCESS);
                    resolveComposite(state, fb);
                    return null;
                }
                continue;
            }
            if (child instanceof CompositeNode) {
                logCompositeEntryOnce(agent, state, child);
            }
            BehaviorTreeNode next = findNextAction(child, board, agent, state);
            if (next != null) {
                return next;
            }
            NodeStatus res = state.getStatusCache().getOrDefault(child.getId(), NodeStatus.FAILURE);
            if (res == NodeStatus.SUCCESS) {
                log.log(agent.getId() + " " + fb.getId() + " " + fb.getType() + " SUCCESS");
                state.getStatusCache().put(fb.getId(), NodeStatus.SUCCESS);
                resolveComposite(state, fb);
                return null;
            }
        }
        log.log(agent.getId() + " " + fb.getId() + " " + fb.getType() + " FAILURE");
        state.getStatusCache().put(fb.getId(), NodeStatus.FAILURE);
        return null;
    }
    private BehaviorTreeNode handleParallelNode(ParallelNode par, Board board, Ladybug agent, ExecuteState state)
            throws LadybugException {
        int succ = 0;
        int fail = 0;
        var children = par.getChildren();
        int totalChildren = children.size();
        int requiredSuccesses = par.getRequiredSuccesses();
        for (BehaviorTreeNode child : children) {
            NodeStatus cached = state.getStatusCache().get(child.getId());
            if (cached != null) {
                if (cached == NodeStatus.SUCCESS) {
                    succ++;
                } else if (cached == NodeStatus.FAILURE) {
                    fail++;
                }
                continue;
            }
            BehaviorTreeNode action = findNextAction(child, board, agent, state);
            if (action != null) {
                return action;
            }
            NodeStatus childStatus = state.getStatusCache().get(child.getId());
            if (childStatus == NodeStatus.SUCCESS) {
                succ++;
            } else if (childStatus == NodeStatus.FAILURE) {
                fail++;
            }
        }
        if (succ >= requiredSuccesses) {
            log.log(agent.getId() + " " + par.getId() + " " + par.getType() + " SUCCESS");
            state.getStatusCache().put(par.getId(), NodeStatus.SUCCESS);
            clearChildStatuses(par, state);
            resolveComposite(state, par);
        } else if (fail > (totalChildren - requiredSuccesses)) {
            log.log(agent.getId() + " " + par.getId() + " " + par.getType() + " FAILURE");
            state.getStatusCache().put(par.getId(), NodeStatus.FAILURE);
            clearChildStatuses(par, state);
            resolveComposite(state, par);
        } else { //
        }
        return null;
    }
    private void clearChildStatuses(BehaviorTreeNode parent, ExecuteState state) {
        for (BehaviorTreeNode child : parent.getChildren()) {
            state.getStatusCache().remove(child.getId());
            state.getOpenCompositeEntries().remove(child.getId()); // ADD THIS LINE
            if (child instanceof CompositeNode) {
                clearChildStatuses(child, state);
            }
        }
    }
    private void logCompositeEntryOnce(Ladybug agent, ExecuteState state, BehaviorTreeNode node) {
        if (node instanceof LeafNode) {
            return;
        }
        var open = state.getOpenCompositeEntries();
        if (open.add(node.getId())) { // nur wenn neu
            log.log(agent.getId() + " " + node.getId() + " " + node.getType() + " ENTRY");
        }
    }
    private void resolveComposite(ExecuteState state, BehaviorTreeNode node) {
        state.getOpenCompositeEntries().remove(node.getId());
    }
    /**
     * Resets the execution state for the given agent, clearing cached node statuses.
     * @param agent the agent to reset
     */
    public void reset(Ladybug agent) {
        ExecuteState state = stateOf(agent);
        state.reset();
    }
}