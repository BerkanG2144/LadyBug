package engine;

import bt.*;
import exceptions.LadybugException;
import model.Board;
import model.Ladybug;

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
        NodeStatus rootStatus = stateManager.getCachedStatus(agent, root.getId());
        if (rootStatus != null) {
            logger.logCompositeExit(agent, root, rootStatus);
            return false;
        }
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
        if (node instanceof CompositeNode comp) {
            // 1) Sofort-Entscheidung?
            NodeStatus immediate = tryResolveCompositeNow(node, agent);
            if (immediate != null) {
                logger.logCompositeExit(agent, node, immediate);
                stateManager.updateNodeStatus(agent, node.getId(), immediate);
                return null;
            }
            boolean alreadyOpen = !stateManager.recordCompositeEntry(agent, node.getId()); // ← node
            boolean hasProgress = hasProgressInComposite(comp, agent);                    // ← comp bleibt ok
            if (!alreadyOpen && !hasProgress) {
                logger.logCompositeEntry(agent, node);                                    // ← node
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
    private NodeStatus tryResolveCompositeNow(BehaviorTreeNode node, Ladybug agent) {
        if (!(node instanceof CompositeNode comp)) {
            return null;
        }
        if (comp instanceof SequenceNode seq) {
            for (BehaviorTreeNode c : seq.getChildren()) {
                NodeStatus s = stateManager.getCachedStatus(agent, c.getId());
                if (s == null) {
                    return null;             // noch offen → nicht entscheidbar
                }
                if (s == NodeStatus.FAILURE) {
                    return NodeStatus.FAILURE; // Kurzschluss
                }
            }
            return NodeStatus.SUCCESS;                   // alle SUCCESS
        }
        if (comp instanceof FallbackNode fb) {
            for (BehaviorTreeNode c : fb.getChildren()) {
                NodeStatus s = stateManager.getCachedStatus(agent, c.getId());
                if (s == null) {
                    return null;
                }
                if (s == NodeStatus.SUCCESS) {
                    return NodeStatus.SUCCESS; // Kurzschluss
                }
            }
            return NodeStatus.FAILURE;                   // alle FAILURE
        }
        if (comp instanceof ParallelNode par) {
            int succ = 0;
            int fail = 0;
            int n = par.getChildren().size();
            int need = par.getRequiredSuccesses();
            for (BehaviorTreeNode c : par.getChildren()) {
                NodeStatus s = stateManager.getCachedStatus(agent, c.getId());
                if (s == NodeStatus.SUCCESS) {
                    succ++;
                } else if (s == NodeStatus.FAILURE) {
                    fail++;
                } else {
                    return null;                        // offen → nicht entscheidbar
                }
            }
            if (succ >= need) {
                return NodeStatus.SUCCESS;
            }
            if (fail > (n - need)) {
                return NodeStatus.FAILURE;
            }
            return null;
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
        return findNextActionForHead(root, board, agent);
    }

    private BehaviorTreeNode handleLeafNode(LeafNode leaf, Board board, Ladybug agent)
            throws LadybugException {
        if (stateManager.getCachedStatus(agent, leaf.getId()) != null) {
            return null;
        }
        if (leaf.isCondition()) {
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
        state.setCurrentNode(root);
    }

    private BehaviorTreeNode handleTreeCompletion(Board board, Ladybug agent, ExecuteState state)
            throws LadybugException {
        NodeStatus rootStatus = stateManager.getCachedStatus(agent, root.getId());
        if (rootStatus != null) {
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
                return null;
            }
            if (leaf.isCondition()) {
                NodeStatus result = leaf.getBehavior().tick(board, agent);
                stateManager.updateNodeStatus(agent, leaf.getId(), result);
                return null; // Composite entscheidet mit Cache weiter
            } else {
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
    private boolean hasProgressInComposite(CompositeNode node, Ladybug agent) {
        for (BehaviorTreeNode c : node.getChildren()) {
            if (stateManager.getCachedStatus(agent, c.getId()) != null) {
                return true;
            }
        }
        return false;
    }
}