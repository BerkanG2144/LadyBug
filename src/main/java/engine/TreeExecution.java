package engine;

import bt.BehaviorTreeNode;
import bt.LeafNode;
import bt.NodeStatus;
import bt.SequenceNode;
import bt.ParallelNode;
import bt.CompositeNode;
import bt.FallbackNode;

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
        this.log = log != null ? log : s -> { };
    }

    public ExecuteState stateOf(Ladybug agent) {
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
        log.accept(agent.getId() + " " + leaf.getId() + " " + actionName + " " + result);
        state.setLastExecutedLeaf(leaf);
        state.getStatusCache().put(leaf.getId(), result);
        prepareNextState(state, action);
        return true;
    }

    private void cleanupCompletedNodeCache(ExecuteState state) {
        // Only clear cache for nodes that are definitely complete
        // Keep cache for parallel nodes that might still have unexecuted children
        Map<String, NodeStatus> cache = state.getStatusCache();
        Map<String, NodeStatus> newCache = new HashMap<>();

        // Keep statuses that might still be needed
        for (Map.Entry<String, NodeStatus> entry : cache.entrySet()) {
            String nodeId = entry.getKey();
            BehaviorTreeNode node = state.findNodeById(nodeId);

            // Keep leaf node results within parallel nodes
            if (node instanceof LeafNode) {
                BehaviorTreeNode parent = findParentNode(state.getRootNode(), node);
                if (parent instanceof ParallelNode) {
                    newCache.put(nodeId, entry.getValue());
                }
            }
        }

        cache.clear();
        cache.putAll(newCache);
    }

    private BehaviorTreeNode findParentNode(BehaviorTreeNode root, BehaviorTreeNode target) {
        if (root == null || target == null) {
            return null;
        }

        for (BehaviorTreeNode child : root.getChildren()) {
            if (child == target) {
                return root;
            }
            BehaviorTreeNode parent = findParentNode(child, target);
            if (parent != null) {
                return parent;
            }
        }
        return null;
    }

    private void prepareNextState(ExecuteState state, BehaviorTreeNode executedAction) {
        // After an Action: go back to root for next tick
        state.setCurrentNode(state.getRootNode());
    }

    private BehaviorTreeNode findNextAction(BehaviorTreeNode node, Board board, Ladybug agent, ExecuteState state) {
        if (!(node instanceof LeafNode)) {
            logCompositeEntryOnce(agent, state, node);
        }

        if (node instanceof LeafNode leaf) {
            if (leaf.isCondition()) {
                // Check if this condition was already evaluated in current parallel context
                if (state.getStatusCache().containsKey(leaf.getId())) {
                    // Already evaluated, don't re-evaluate
                    return null;
                }

                NodeStatus result = leaf.getBehavior().tick(board, agent);
                String conditionName = getLeafBehaviorName(leaf);
                log.accept(agent.getId() + " " + leaf.getId() + " " + conditionName + " " + result);
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

        if (node instanceof SequenceNode seq) {
            for (BehaviorTreeNode child : seq.getChildren()) {
                // Check if child already has a cached result
                NodeStatus cached = state.getStatusCache().get(child.getId());
                if (cached != null) {
                    if (cached == NodeStatus.FAILURE) {
                        log.accept(agent.getId() + " " + node.getId() + " " + node.getType() + " FAILURE");
                        state.getStatusCache().put(node.getId(), NodeStatus.FAILURE);
                        resolveComposite(state, node);
                        return null;
                    }
                    // SUCCESS -> continue to next child
                    continue;
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
                    log.accept(agent.getId() + " " + node.getId() + " " + node.getType() + " FAILURE");
                    state.getStatusCache().put(node.getId(), NodeStatus.FAILURE);
                    resolveComposite(state, node); // ← NEU
                    return null;
                }
            }
            log.accept(agent.getId() + " " + node.getId() + " " + node.getType() + " SUCCESS");
            state.getStatusCache().put(node.getId(), NodeStatus.SUCCESS);
            return null;
        }

        if (node instanceof FallbackNode fb) {
            for (BehaviorTreeNode child : fb.getChildren()) {
                // Check if child already has a cached result
                NodeStatus cached = state.getStatusCache().get(child.getId());
                if (cached != null) {
                    if (cached == NodeStatus.SUCCESS) {
                        log.accept(agent.getId() + " " + node.getId() + " " + node.getType() + " SUCCESS");
                        state.getStatusCache().put(node.getId(), NodeStatus.SUCCESS);
                        resolveComposite(state, node); // ← NEU
                        return null;
                    }
                    // FAILURE -> continue to next child
                    continue;
                }

                BehaviorTreeNode next = findNextAction(child, board, agent, state);
                if (next != null) {
                    return next;
                }

                NodeStatus res = state.getStatusCache().getOrDefault(child.getId(), NodeStatus.FAILURE);
                if (res == NodeStatus.SUCCESS) {
                    log.accept(agent.getId() + " " + node.getId() + " " + node.getType() + " SUCCESS");
                    state.getStatusCache().put(node.getId(), NodeStatus.SUCCESS);
                    resolveComposite(state, node); // ← NEU
                    return null;
                }
            }
            log.accept(agent.getId() + " " + node.getId() + " " + node.getType() + " FAILURE");
            state.getStatusCache().put(node.getId(), NodeStatus.FAILURE);
            return null;
        }

        if (node instanceof ParallelNode par) {
            int succ = 0;
            int fail = 0;
            var children = par.getChildren();
            int M = children.size();
            int N = par.getRequiredSuccesses();

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
            if (succ >= N) {
                log.accept(agent.getId() + " " + node.getId() + " " + node.getType() + " SUCCESS");
                state.getStatusCache().put(node.getId(), NodeStatus.SUCCESS);
                // Clear child statuses when parallel completes
                clearChildStatuses(node, state);
                resolveComposite(state, node); // ← NEU
            } else if (fail > (M - N)) {
                log.accept(agent.getId() + " " + node.getId() + " " + node.getType() + " FAILURE");
                state.getStatusCache().put(node.getId(), NodeStatus.FAILURE);
                // Clear child statuses when parallel completes
                clearChildStatuses(node, state);
                resolveComposite(state, node); // ← NEU
            } else {
                // Parallel node is not yet decided - keep child statuses for next tick
                // Don't put anything in cache for the parallel node itself
            }
            return null;
        }

        return null;
    }

    private void clearChildStatuses(BehaviorTreeNode parent, ExecuteState state) {
        // When a parallel node completes, clear its children's cached statuses
        for (BehaviorTreeNode child : parent.getChildren()) {
            state.getStatusCache().remove(child.getId());
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
            log.accept(agent.getId() + " " + node.getId() + " " + node.getType() + " ENTRY");
        }
    }

    private void resolveComposite(ExecuteState state, BehaviorTreeNode node) {
        state.getOpenCompositeEntries().remove(node.getId());
    }

    public void reset(Ladybug agent) {
        stateOf(agent).reset();
    }
}