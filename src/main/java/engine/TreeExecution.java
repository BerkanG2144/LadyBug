package engine;

import bt.*;
import exceptions.LadybugException;
import model.Board;
import model.Ladybug;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Executes a behavior tree for one or more {@link Ladybug} agents.
 * <p>
 * The executor keeps per-agent execution state (current node, cached statuses, logs).
 * Each call to {@link #tick(Board, Ladybug)} advances the tree just far enough
 * to find and execute the next action (a {@link LeafNode} that is not a condition).
 * Conditions are evaluated immediately and their results cached within the current context.
 * </p>
 * <p>Logging is performed via the provided {@link Consumer}.</p>
 * @author ujnaa
 */
public class TreeExecution {

    private final BehaviorTreeNode root;
    private final Map<Ladybug, ExecuteState> states = new HashMap<>();
    private final Logger log;

    /**
     * Creates a new executor for the given behavior tree root.
     *
     * @param root the root node of the behavior tree (must not be {@code null})
     * @param log  optional logger; if {@code null}, logging is disabled
     * @throws NullPointerException if {@code root} is {@code null}
     */
    public TreeExecution(BehaviorTreeNode root, Logger log) {
        this.root = Objects.requireNonNull(root);
        this.log = log != null ? log : message -> { };
    }

    /**
     * Returns the mutable execution state associated with the given agent,
     * creating it on first access.
     *
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
     * Returns the node that would be considered the current position in the tree
     * for the given agent (may be {@code null} until first tick).
     *
     * @param agent the agent
     * @return the current node for this agent, or {@code null} if none set yet
     */
    public BehaviorTreeNode getCurrentNode(Ladybug agent) {
        ExecuteState state = stateOf(agent);
        return state.getCurrentNode();
    }

    /**
     * Moves the agent's current node to the node with the given ID, if present
     * in the tree. Also clears the cached statuses to start a fresh evaluation
     * from that node.
     *
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

        state.setCurrentNode(targetNode);
        state.getStatusCache().clear();
        return true;
    }

    /**
     * Advances the behavior tree for the given agent by finding and executing the next
     * action (non-condition leaf). If no immediate action is found, the method may
     * evaluate conditions and composites to determine the next actionable leaf.
     *
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
        // Find and execute next Action
        BehaviorTreeNode action = findNextAction(currentNode, board, agent, state);
        if (action == null) {
            //Check if we completed tree
            NodeStatus rootStatus = state.getStatusCache().get(root.getId());
            if (rootStatus != null) {
                //Tree completed, restart from root
                state.setCurrentNode(root);
                state.getStatusCache().clear();
                //Try again from root
                action = findNextAction(root, board, agent, state);
                if (action == null) {
                    return false; //really no action possible
                }
            } else {
                return false; //No action found and tree not completed
            }
        }

        LeafNode leaf = (LeafNode) action;
        String actionName = getLeafBehaviorName(leaf);
        NodeStatus result = leaf.getBehavior().tick(board, agent);
        log.log(agent.getId() + " " + leaf.getId() + " " + actionName + " " + result);
        state.setLastExecutedLeaf(leaf);
        state.getStatusCache().put(leaf.getId(), result);
        prepareNextState(state, action);
        return true;
    }


    private void prepareNextState(ExecuteState state, BehaviorTreeNode executedAction) {
        // After an Action: go back to root for next tick
        state.setCurrentNode(state.getRootNode());
    }

    private BehaviorTreeNode findNextAction(BehaviorTreeNode node, Board board, Ladybug agent, ExecuteState state)
            throws LadybugException {
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

    private BehaviorTreeNode handleLeafNode(LeafNode leaf, Board board, Ladybug agent, ExecuteState state)
            throws LadybugException {
        if (leaf.isCondition()) {
            // Check if this condition was already evaluated in current parallel context
            if (state.getStatusCache().containsKey(leaf.getId())) {
                // Already evaluated, don't re-evaluate
                return null;
            }

            NodeStatus result = leaf.getBehavior().tick(board, agent);
            String conditionName = getLeafBehaviorName(leaf);
            log.log(agent.getId() + " " + leaf.getId() + " " + conditionName + " " + result);
            state.setLastExecutedLeaf(leaf);
            state.getStatusCache().put(leaf.getId(), result);
            return null;
        } else {
            // Check if this action was already executed in current parallel context
            if (state.getStatusCache().containsKey(leaf.getId())) {
                // Already executed, skip
                return null;
            }
            return leaf; // Action found
        }
    }

    private BehaviorTreeNode handleSequenceNode(SequenceNode seq, Board board, Ladybug agent, ExecuteState state)
            throws LadybugException {
        for (BehaviorTreeNode child : seq.getChildren()) {
            // Check if child already has a cached result
            NodeStatus cached = state.getStatusCache().get(child.getId());
            if (cached != null) {
                if (cached == NodeStatus.FAILURE) {
                    log.log(agent.getId() + " " + seq.getId() + " " + seq.getType() + " FAILURE");
                    state.getStatusCache().put(seq.getId(), NodeStatus.FAILURE);
                    resolveComposite(state, seq);
                    return null;
                }
                // SUCCESS -> continue to next child
                continue;
            }

            if (child instanceof CompositeNode) {
                logCompositeEntryOnce(agent, state, child);
            }

            BehaviorTreeNode next = findNextAction(child, board, agent, state);
            if (next != null) {
                return next;
            }

            // Check the actual status of the child after recursive call
            NodeStatus childResult = state.getStatusCache().get(child.getId());
            if (childResult == null && child instanceof CompositeNode) {
                // For composite children without cached status, check their children
                continue;
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
            // Check if child already has a cached result
            NodeStatus cached = state.getStatusCache().get(child.getId());
            if (cached != null) {
                if (cached == NodeStatus.SUCCESS) {
                    log.log(agent.getId() + " " + fb.getId() + " " + fb.getType() + " SUCCESS");
                    state.getStatusCache().put(fb.getId(), NodeStatus.SUCCESS);
                    resolveComposite(state, fb);
                    return null;
                }
                // FAILURE -> continue to next child
                continue;
            }

            // Log entry for composite children that haven't been entered yet
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
            // IMPORTANT: Check if child already has a definite result in cache
            NodeStatus cached = state.getStatusCache().get(child.getId());
            if (cached != null) {
                // This child was already evaluated in a previous tick
                if (cached == NodeStatus.SUCCESS) {
                    succ++;
                } else if (cached == NodeStatus.FAILURE) {
                    fail++;
                }
                // Skip this child - don't re-evaluate
                continue;
            }

            // Only search for action in non-evaluated children
            BehaviorTreeNode action = findNextAction(child, board, agent, state);
            if (action != null) {
                // Found an action to execute - return it immediately
                return action;
            }

            // After recursive call, check if child now has a status
            NodeStatus childStatus = state.getStatusCache().get(child.getId());
            if (childStatus == NodeStatus.SUCCESS) {
                succ++;
            } else if (childStatus == NodeStatus.FAILURE) {
                fail++;
            }
            // If childStatus is null, child is not yet decided (composite still in progress)
        }

        // Check if parallel node can be decided
        if (succ >= requiredSuccesses) {
            log.log(agent.getId() + " " + par.getId() + " " + par.getType() + " SUCCESS");
            state.getStatusCache().put(par.getId(), NodeStatus.SUCCESS);
            // Clear child statuses when parallel completes
            clearChildStatuses(par, state);
            resolveComposite(state, par);
        } else if (fail > (totalChildren - requiredSuccesses)) {
            log.log(agent.getId() + " " + par.getId() + " " + par.getType() + " FAILURE");
            state.getStatusCache().put(par.getId(), NodeStatus.FAILURE);
            // Clear child statuses when parallel completes
            clearChildStatuses(par, state);
            resolveComposite(state, par);
        } else {
            // Parallel node is not yet decided - keep child statuses for next tick
            // Don't put anything in cache for the parallel node itself
        }
        return null;
    }

    private void clearChildStatuses(BehaviorTreeNode parent, ExecuteState state) {
        // When a parallel node completes, clear its children's cached statuses
        for (BehaviorTreeNode child : parent.getChildren()) {
            state.getStatusCache().remove(child.getId());
            state.getOpenCompositeEntries().remove(child.getId()); // ADD THIS LINE
            if (child instanceof CompositeNode) {
                clearChildStatuses(child, state);
            }
        }
    }

    private String getLeafBehaviorName(LeafNode leaf) {
        String className = leaf.getBehavior().getClass().getSimpleName();

        // Convert first letter to lowercase for camelCase
        if (className.length() > 0) {
            return Character.toLowerCase(className.charAt(0)) + className.substring(1);
        }
        return className;
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
     * Resets the execution state for the given agent, clearing cached node statuses,
     * current pointer and open composite entries.
     *
     * @param agent the agent to reset
     */
    public void reset(Ladybug agent) {
        stateOf(agent).reset();
    }
}