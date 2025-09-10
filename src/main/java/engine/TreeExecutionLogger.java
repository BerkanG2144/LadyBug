package engine;

import bt.BehaviorTreeNode;
import bt.LeafNode;
import bt.NodeStatus;
import model.Ladybug;

/**
 * Handles logging for behavior tree execution.
 * Formats and outputs execution events according to specification.
 *
 * @author ujnaa
 */
public class TreeExecutionLogger {
    private final Logger logger;

    /**
     * Creates a new TreeExecutionLogger.
     *
     * @param logger the logger to use, or null to disable logging
     */
    public TreeExecutionLogger(Logger logger) {
        this.logger = logger != null ? logger : message -> { };
    }

    /**
     * Logs a composite node entry.
     *
     * @param agent the agent
     * @param node the node being entered
     */
    public void logCompositeEntry(Ladybug agent, BehaviorTreeNode node) {
        if (!(node instanceof LeafNode)) {
            log(formatCompositeEntry(agent.getId(), node.getId(), node.getType()));
        }
    }

    /**
     * Logs a composite node exit with status.
     *
     * @param agent the agent
     * @param node the node exiting
     * @param status the exit status
     */
    public void logCompositeExit(Ladybug agent, BehaviorTreeNode node, NodeStatus status) {
        log(formatNodeResult(agent.getId(), node.getId(), node.getType(), status));
    }

    /**
     * Logs a leaf node execution.
     *
     * @param agent the agent
     * @param leaf the leaf node
     * @param status the execution status
     */
    public void logLeafExecution(Ladybug agent, LeafNode leaf, NodeStatus status) {
        String name = leaf.getLogNameOrDefault();
        String args = leaf.getLogArgsOrEmpty();
        log(formatLeafResult(agent.getId(), leaf.getId(), name, args, status));
    }

    /**
     * Formats a composite entry message.
     */
    private String formatCompositeEntry(int agentId, String nodeId, String nodeType) {
        return agentId + " " + nodeId + " " + nodeType + " ENTRY";
    }

    /**
     * Formats a node result message.
     */
    private String formatNodeResult(int agentId, String nodeId, String nodeType, NodeStatus status) {
        return agentId + " " + nodeId + " " + nodeType + " " + status;
    }

    /**
     * Formats a leaf result message.
     */
    private String formatLeafResult(int agentId, String nodeId, String name, String args, NodeStatus status) {
        return agentId + " " + nodeId + " " + name + args + " " + status;
    }

    /**
     * Outputs a log message.
     */
    private void log(String message) {
        logger.log(message);
    }
}