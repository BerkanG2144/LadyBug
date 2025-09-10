package engine;

import bt.*;
import exceptions.LadybugException;
import model.Board;
import model.Ladybug;

import java.util.ArrayList;
import java.util.List;

/**
 * Refactored TreeExecution that delegates responsibilities to specialized components.
 * This class now focuses on orchestration rather than implementation details.
 *
 * @author ujnaa
 */
public class TreeExecution {
    private final ExecutionStateManager stateManager;
    private final TreeExecutionLogger logger;
    private final NodeTraversalHandler traversalHandler;
    private final BehaviorTreeNode root;

    /**
     * Creates a new TreeExecutionRefactored for the given behavior tree.
     *
     * @param root the root node of the behavior tree (must not be null)
     * @param log optional logger; if null, logging is disabled
     */
    public TreeExecution(BehaviorTreeNode root, Logger log) {
        this.root = root;
        this.stateManager = new ExecutionStateManager(root);
        this.logger = new TreeExecutionLogger(log);
        this.traversalHandler = new NodeTraversalHandler(stateManager, logger);
    }

    /**
     * Returns the execution state for an agent.
     *
     * @param agent the agent whose state to obtain
     * @return the ExecuteState for the agent
     */
    public ExecuteState stateOf(Ladybug agent) {
        return stateManager.getOrCreateState(agent);
    }

    /**
     * Advances the behavior tree for the given agent by finding and executing the next action.
     *
     * @param board the game board
     * @param agent the agent to tick
     * @return true if an action was executed, false otherwise
     * @throws LadybugException if execution fails
     */
    public boolean tick(Board board, Ladybug agent) throws LadybugException {
        ExecuteState state = stateManager.getOrCreateState(agent);
        BehaviorTreeNode currentNode = state.getCurrentNode();
        if (currentNode == null) {
            currentNode = root;
            state.setCurrentNode(currentNode);
        }
        BehaviorTreeNode action = findNextAction(currentNode, board, agent);
        if (action == null) {
            action = handleTreeCompletion(board, agent, state);
            if (action == null) {
                return false;
            }
        }
        executeAction(action, board, agent, state);
        return true;
    }
    /**
     * Jumps to a specific node in the tree.
     *
     * @param agent the agent
     * @param nodeId the target node ID
     * @return true if successful, false if node not found
     */
    public boolean jumpTo(Ladybug agent, String nodeId) {
        ExecuteState state = stateOf(agent);
        BehaviorTreeNode targetNode = state.findNodeById(nodeId);

        if (targetNode == null) {
            return false;
        }
        state.getStatusCache().clear();
        state.getOpenCompositeEntries().clear();
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
        state.setCurrentNode(targetNode);
        return true;
    }
    /**
     * Resets the execution state for an agent.
     *
     * @param agent the agent to reset
     */
    public void reset(Ladybug agent) {
        stateManager.resetState(agent);
    }
    private BehaviorTreeNode findNextAction(BehaviorTreeNode node, Board board, Ladybug agent)
            throws LadybugException {
        if (!(node instanceof LeafNode)) {
            if (stateManager.recordCompositeEntry(agent, node.getId())) {
                logger.logCompositeEntry(agent, node);
            }
        }
        if (node instanceof LeafNode leaf) {
            return handleLeafNode(leaf, board, agent);
        } else if (node instanceof SequenceNode seq) {
            return traversalHandler.processSequenceNode(seq, board, agent, this::findNextAction);
        } else if (node instanceof FallbackNode fb) {
            return traversalHandler.processFallbackNode(fb, board, agent, this::findNextAction);
        } else if (node instanceof ParallelNode par) {
            return traversalHandler.processParallelNode(par, board, agent, this::findNextAction);
        }

        return null;
    }

    /**
     * Finds next actionable leaf for head command.
     * @param board the game board
     * @param agent the agent
     * @return next action node or null
     * @throws LadybugException if evaluation fails
     */
    public BehaviorTreeNode findNextActionNode(Board board, Ladybug agent) throws LadybugException {
        ExecuteState state = stateOf(agent);

        // Wenn Root im Cache ist: neuer Zyklus wie in tick() -> Cache und Open-Entries leeren
        NodeStatus rootStatus = stateManager.getCachedStatus(agent, root.getId());
        if (rootStatus != null) {
            stateManager.clearStatusCache(agent);
            state.getOpenCompositeEntries().clear();
            state.setCurrentNode(root);
        }

        BehaviorTreeNode start = state.getCurrentNode();
        BehaviorTreeNode next = findNextActionForHead(start != null ? start : root, board, agent);
        if (next != null) {
            return next;
        }

        // Falls dennoch nichts gefunden: einmal hart auf Root starten (robust ggü. inkonsistentem state)
        return findNextActionForHead(root, board, agent);
    }


    private BehaviorTreeNode handleLeafNode(LeafNode leaf, Board board, Ladybug agent)
            throws LadybugException {

        // Check if already executed
        if (stateManager.getCachedStatus(agent, leaf.getId()) != null) {
            return null;
        }

        if (leaf.isCondition()) {
            // Execute condition immediately
            NodeStatus result = leaf.getBehavior().tick(board, agent);
            logger.logLeafExecution(agent, leaf, result);

            ExecuteState state = stateManager.getOrCreateState(agent);
            state.setLastExecutedLeaf(leaf);
            stateManager.updateNodeStatus(agent, leaf.getId(), result);
            return null;
        } else {
            // Return action for execution
            return leaf;
        }
    }

    private void executeAction(BehaviorTreeNode action, Board board, Ladybug agent, ExecuteState state)
            throws LadybugException {

        LeafNode leaf = (LeafNode) action;
        NodeStatus result = leaf.getBehavior().tick(board, agent);
        logger.logLeafExecution(agent, leaf, result);

        state.setLastExecutedLeaf(leaf);
        stateManager.updateNodeStatus(agent, leaf.getId(), result);

        // Prepare for next tick
        state.setCurrentNode(root);
    }

    private BehaviorTreeNode handleTreeCompletion(Board board, Ladybug agent, ExecuteState state)
            throws LadybugException {

        NodeStatus rootStatus = stateManager.getCachedStatus(agent, root.getId());
        if (rootStatus != null) {
            // Tree complete, restart
            state.setCurrentNode(root);
            stateManager.clearStatusCache(agent);
            state.getOpenCompositeEntries().clear();

            return findNextAction(root, board, agent);
        }
        return null;
    }

    private BehaviorTreeNode findNextActionForHead(BehaviorTreeNode node, Board board, Ladybug agent)
            throws LadybugException {

        if (node instanceof LeafNode leaf) {
            NodeStatus cached = stateManager.getCachedStatus(agent, leaf.getId());
            if (cached != null) {
                // Bereits bewertet: Aktion ist damit in diesem Zyklus "verbraucht"
                return null;
            }

            if (leaf.isCondition()) {
                // Bedingung still (ohne Log) bewerten und cachen
                NodeStatus result = leaf.getBehavior().tick(board, agent);
                stateManager.updateNodeStatus(agent, leaf.getId(), result);
                return null; // Composite entscheidet mit Cache weiter
            } else {
                // Aktion noch nicht ausgeführt -> das ist die nächste Aktion
                return leaf;
            }
        }

        if (node instanceof SequenceNode seq) {
            return findNextInSequence(seq, board, agent);
        } else if (node instanceof FallbackNode fb) {
            return findNextInFallback(fb, board, agent);
        } else if (node instanceof ParallelNode par) {
            return findNextInParallel(par, board, agent);
        }

        return null;
    }


    private BehaviorTreeNode findNextInSequence(SequenceNode seq, Board board, Ladybug agent)
            throws LadybugException {

        for (BehaviorTreeNode child : seq.getChildren()) {
            NodeStatus cached = stateManager.getCachedStatus(agent, child.getId());
            if (cached != null) {
                if (cached == NodeStatus.FAILURE) {
                    return null; // ganze Sequence fällt
                }
                continue; // SUCCESS -> nächstes Kind
            }

            BehaviorTreeNode next = findNextActionForHead(child, board, agent);
            if (next != null) {
                return next;
            }

            // Eventuell wurde gerade eine Bedingung gecached:
            cached = stateManager.getCachedStatus(agent, child.getId());
            if (cached != null) {
                if (cached == NodeStatus.FAILURE) {
                    return null;
                } else {
                    continue; // SUCCESS -> nächstes Kind
                }
            }
        }
        return null;
    }

    private BehaviorTreeNode findNextInFallback(FallbackNode fb, Board board, Ladybug agent)
            throws LadybugException {

        for (BehaviorTreeNode child : fb.getChildren()) {
            NodeStatus cached = stateManager.getCachedStatus(agent, child.getId());
            if (cached != null) {
                if (cached == NodeStatus.SUCCESS) {
                    BehaviorTreeNode deeper = findNextActionForHead(child, board, agent);
                    return deeper; // kann null sein -> dann gibt es in diesem Zweig gerade keine Aktion
                }
                continue; // FAILURE -> probiere nächstes Kind
            }

            BehaviorTreeNode next = findNextActionForHead(child, board, agent);
            if (next != null) {
                return next;
            }
            cached = stateManager.getCachedStatus(agent, child.getId());
            if (cached != null && cached == NodeStatus.SUCCESS) {
                BehaviorTreeNode deeper = findNextActionForHead(child, board, agent);
                return deeper;
            }
            // Bei FAILURE -> nächstes Kind
        }
        return null;
    }
    private BehaviorTreeNode findNextInParallel(ParallelNode par, Board board, Ladybug agent)
            throws LadybugException {

        for (BehaviorTreeNode child : par.getChildren()) {
            if (stateManager.getCachedStatus(agent, child.getId()) == null) {
                BehaviorTreeNode next = findNextActionForHead(child, board, agent);
                if (next != null) {
                    return next;
                }


            }
        }
        return null;
    }

    private void setupJumpState(ExecuteState state, BehaviorTreeNode targetNode) {
        // Find path to target
        List<BehaviorTreeNode> pathToTarget = findPathToNode(root, targetNode);
        if (pathToTarget == null) {
            return;
        }

        // Mark ancestors as open
        for (BehaviorTreeNode ancestor : pathToTarget) {
            if (ancestor instanceof CompositeNode && ancestor != targetNode) {
                state.getOpenCompositeEntries().add(ancestor.getId());
            }
        }

        // Handle skipped siblings
        BehaviorTreeNode parent = findParent(root, targetNode);
        if (parent != null && parent instanceof CompositeNode) {
            handleSkippedSiblings(parent, targetNode, state);
        }
    }

    private void handleSkippedSiblings(BehaviorTreeNode parent, BehaviorTreeNode target, ExecuteState state) {
        List<BehaviorTreeNode> children = parent.getChildren();
        int targetIndex = children.indexOf(target);

        for (int i = 0; i < targetIndex; i++) {
            BehaviorTreeNode skipped = children.get(i);
            NodeStatus skipStatus = determineSkipStatus(parent);
            state.getStatusCache().put(skipped.getId(), skipStatus);
        }
    }

    private NodeStatus determineSkipStatus(BehaviorTreeNode parent) {
        if (parent instanceof FallbackNode || parent instanceof ParallelNode) {
            return NodeStatus.FAILURE;
        } else if (parent instanceof SequenceNode) {
            return NodeStatus.SUCCESS;
        }
        return NodeStatus.FAILURE;
    }

    private List<BehaviorTreeNode> findPathToNode(BehaviorTreeNode current, BehaviorTreeNode target) {
        if (current == target) {
            List<BehaviorTreeNode> path = new ArrayList<>();
            path.add(current);
            return path;
        }

        for (BehaviorTreeNode child : current.getChildren()) {
            List<BehaviorTreeNode> childPath = findPathToNode(child, target);
            if (childPath != null) {
                List<BehaviorTreeNode> path = new ArrayList<>();
                path.add(current);
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
}