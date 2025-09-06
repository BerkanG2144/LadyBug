package commands;

import bt.BehaviorTreeNode;
import exceptions.BoardParsingException;
import exceptions.CommandArgumentException;
import main.GameState;
import model.Board;
import model.Ladybug;
import model.LadybugPosition;
import parser.BoardParser;
import parser.MermaidParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Command to load board files and behavior tree files into the game.
 *
 * This command supports two subcommands:
 * - load board &lt;path&gt;: Loads a board from a file
 * - load trees &lt;path1&gt; [&lt;path2&gt; ...]: Loads behavior trees for ladybugs
 *
 * When loading trees, each tree file is assigned to a ladybug in order based on
 * their position on the board (row-wise, then column-wise).
 *
 * @author ujnaa
 */
public class LoadCommand implements Command {
    private final GameState state;

    /**
     * Creates a new LoadCommand.
     *
     * @param state the game state to load content into
     */
    public LoadCommand(GameState state) {
        this.state = state;
    }

    @Override
    public void execute(String[] args) throws CommandArgumentException, IOException, BoardParsingException {
        if (args.length < 2) {
            throw new CommandArgumentException(getCommandName(), args, getUsage());
        }
        String sub = args[0].toLowerCase();

        if ("board".equals(sub)) {
            loadBoard(args[1]);
        } else if ("trees".equals(sub)) {
            loadTrees(args);
        } else {
            throw new IllegalArgumentException("Unknown subcommand: " + sub);
        }
    }

    private void loadBoard(String boardPath) throws BoardParsingException, IOException {
        Path p = Path.of(boardPath);
        if (!Files.exists(p)) {
            throw new BoardParsingException("Board file not found", p);
        }

        Board board = BoardParser.parse(p.toString());
        state.setBoard(board);
    }

    private void loadTrees(String[] args) {
        if (state.getBoard() == null) {
            throw new IllegalStateException("Error: no board loaded");
        }

        String[] treePaths = new String[args.length - 1];
        System.arraycopy(args, 1, treePaths, 0, args.length - 1);
        List<LadybugPosition> allLadybugPositions = state.getBoard().getLadybugList();

        state.clearTrees();
        state.getBoard().getLadybugManager().clearAllLadybugs();

        for (int i = 0; i < treePaths.length; i++) {
            loadSingleTree(treePaths[i], i, allLadybugPositions);
        }
    }

    private void loadSingleTree(String treePath, int index, List<LadybugPosition> allLadybugPositions) {
        Path path = Path.of(treePath);
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("Tree file not found: " + path);
        }

        try {
            String content = Files.readString(path);
            System.out.println(content.trim());

            BehaviorTreeNode tree = MermaidParser.fromFile(path.toString());

            if (!hasActionNode(tree)) {
                throw new IllegalArgumentException("Error: behavior tree must contain at least one action");
            }

            LadybugPosition position = allLadybugPositions.get(index);
            Ladybug ladybug = new Ladybug(index + 1, position.getPosition(), position.getDirection());
            state.getBoard().addLadybug(ladybug);

            state.addTree(index + 1, tree);

        } catch (java.io.IOException e) {
            // checked Exception darfst du gezielt behandeln
            throw new IllegalArgumentException("Error reading tree file " + path + ": " + e.getMessage(), e);
        }
    }

    /**
     * Checks recursively if the tree contains at least one action node.
     *
     * @param node the node to check
     * @return true if the tree contains at least one action node
     */
    private boolean hasActionNode(BehaviorTreeNode node) {
        if (node instanceof bt.LeafNode leaf) {
            return leaf.isAction();
        }

        // For composite nodes: check all children
        for (BehaviorTreeNode child : node.getChildren()) {
            if (hasActionNode(child)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getCommandName() {
        return "load";
    }

    @Override
    public String getUsage() {
        return "load board <path> | load trees <bugId> <path>";
    }
}