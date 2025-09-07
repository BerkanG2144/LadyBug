package commands;

import bt.BehaviorTreeNode;
import bt.CompositeNode;
import exceptions.BoardException;
import exceptions.CommandArgumentException;
import exceptions.LadybugNotFoundException;
import exceptions.TreeParsingException;
import main.GameState;
import model.Ladybug;
import parser.MermaidParser;

import java.util.Optional;

/**
 * CLI command that adds a sibling node to an existing node in a ladybug's behavior tree.
 * <p>
 * Usage: {@code add sibling <ladybug> <id> <node>}
 * <ul>
 *   <li>{@code <ladybug>}: numeric id of the ladybug</li>
 *   <li>{@code <id>}: id of the existing target node</li>
 *   <li>{@code <node>}: single-node Mermaid definition to insert as the right sibling</li>
 * </ul>
 * @author ujnaa
 */
public class AddSiblingCommand extends AbstractCommand {

    /**
     * Creates a new {@link AddSiblingCommand}.
     *
     * @param state shared game state; must not be {@code null}
     */
    public AddSiblingCommand(GameState state) {
        super(state);
    }

    @Override
    protected void executeInternal(String[] args)
            throws BoardException, LadybugNotFoundException, CommandArgumentException {
        if (args.length != 4 || !"sibling".equals(args[0])) {
            throw new CommandArgumentException(getCommandName(), args,
                    "Usage: add sibling <ladybug> <id> <node>");
        }
        requireLadybugs();
        int ladybugId;
        try {
            ladybugId = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            throw new CommandArgumentException(getCommandName(), args,
                    "Error: invalid ladybug ID");
        }
        String nodeId = args[2];
        String nodeDefinition = args[3];

        Optional<Ladybug> ladybug = getBoard().getLadybugById(ladybugId);
        if (ladybug.isEmpty()) {
            throw new LadybugNotFoundException(ladybugId);
        }

        // Get behavior tree for this ladybug
        BehaviorTreeNode tree = gameState.getLadybugTrees().get(ladybugId);
        if (tree == null) {
            throw new CommandArgumentException(getCommandName(), args,
                    "Error: no tree loaded for ladybug " + ladybugId);
        }

        // Find the target node
        BehaviorTreeNode targetNode = findNodeById(tree, nodeId);
        if (targetNode == null) {
            throw new CommandArgumentException(getCommandName(), args,
                    "Error: node " + nodeId + " not found");
        }

        // Find parent of target node
        BehaviorTreeNode parentNode = findParentNode(tree, targetNode);
        if (parentNode == null) {
            throw new CommandArgumentException(getCommandName(), args,
                    "Error: cannot add sibling to root node");
        }

        if (!(parentNode instanceof CompositeNode)) {
            throw new CommandArgumentException(getCommandName(), args,
                    "Error: parent node is not composite");
        }

        // Parse the new node definition
        //BehaviorTreeNode newNode = parseNodeDefinition(nodeDefinition);
        BehaviorTreeNode newNode;
        try {
            newNode = parseNodeDefinition(nodeDefinition);
        } catch (TreeParsingException  e) {
            throw new CommandArgumentException(getCommandName(), args,
                    "Error: invalid node definition: " + e.getMessage());
        }

        // Add sibling to the right of target node
        addSiblingToParent((CompositeNode) parentNode, targetNode, newNode);
    }

    private BehaviorTreeNode findNodeById(BehaviorTreeNode root, String nodeId) {
        if (root.getId().equals(nodeId)) {
            return root;
        }

        for (BehaviorTreeNode child : root.getChildren()) {
            BehaviorTreeNode found = findNodeById(child, nodeId);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private BehaviorTreeNode findParentNode(BehaviorTreeNode root, BehaviorTreeNode target) {
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

//    private BehaviorTreeNode parseNodeDefinition(String nodeDefinition) throws TreeParsingException {
//        // Create a minimal mermaid structure to parse the single node
//        String mermaidContent = "flowchart TD\n    " + nodeDefinition;
//
//        try {
//            MermaidParser parser = new MermaidParser();
//            BehaviorTreeNode parsedTree = parser.parse(mermaidContent);
//
//            // The parsed tree should have exactly one node (the root)
//            // Since we're parsing a single node definition, return that node
//            return parsedTree;
//        } catch (Exception e) {
//            throw new TreeParsingException("Invalid node definition", nodeDefinition);
//        }
//    }

    private BehaviorTreeNode parseNodeDefinition(String nodeDefinition) throws TreeParsingException {
        String mermaidContent = "flowchart TD\n    " + nodeDefinition;
        MermaidParser parser = new MermaidParser();
        // Let TreeParsingException propagate; do not catch generic Exception.
        return parser.parse(mermaidContent);
    }

    private void addSiblingToParent(CompositeNode parent, BehaviorTreeNode targetNode, BehaviorTreeNode newNode)
            throws CommandArgumentException {
        // Get current children
        var children = parent.getChildren();

        // Find the index of the target node
        int targetIndex = -1;
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i) == targetNode) {
                targetIndex = i;
                break;
            }
        }

        if (targetIndex == -1) {
            throw new CommandArgumentException(getCommandName(),
                    "Error: target node not found in parent's children");
        }

        // Insert the new node right after the target (targetIndex + 1)
        parent.addChild(targetIndex + 1, newNode);
    }

    @Override
    public String getCommandName() {
        return "add";
    }

    @Override
    public String getUsage() {
        return "add sibling <ladybug> <id> <node>";
    }
}