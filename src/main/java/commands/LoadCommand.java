package commands;

import bt.BehaviorTreeNode;
import exceptions.BoardException;
import exceptions.CommandArgumentException;
import exceptions.LadybugException;
import exceptions.TreeParsingException;
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
    public void execute(String[] args) throws BoardException,
            CommandArgumentException, TreeParsingException, LadybugException {
        if (args == null || args.length < 2) {
            throw new CommandArgumentException(getCommandName(), args, getUsage());
        }
        String sub = args[0].toLowerCase();

        switch (sub) {
            case "board" -> {
                if (args.length != 2) {
                    throw new CommandArgumentException(getCommandName(), args,
                            "Error, load board <path> | load trees <path...>");
                }
                loadBoard(args[1]);
            }
            case "trees" -> {
                if (args.length < 2) {
                    throw new CommandArgumentException(getCommandName(), args,
                            "Error, load board <path> | load trees <path...>");
                }
                loadTrees(args);
            }
            default -> throw new CommandArgumentException(getCommandName(), args,
                    "Error, unknown subcommand");
        }
    }

    private void loadBoard(String boardPath) throws BoardException, LadybugException {
        final Path p = Path.of(boardPath);
        if (!Files.exists(p)) {
            throw new BoardException("Error, Board file not found: " + p);
        }
        try {
            Board board = BoardParser.parse(p.toString());
            state.setBoard(board);
        } catch (IOException e) {
            throw new BoardException("Error, reading board file " + p + ": " + e.getMessage());
        }
    }

    private void loadTrees(String[] args)
            throws BoardException, CommandArgumentException, TreeParsingException, LadybugException {
        if (state.getBoard() == null) {
            throw new BoardException("Error, no board loaded");
        }
        // args: ["trees", <path1>, <path2>, ...]
        if (args.length < 2) {
            throw new CommandArgumentException(getCommandName(), args,
                    "Error, load board <path> | load trees <path...>");
        }

        String[] treePaths = new String[args.length - 1];
        System.arraycopy(args, 1, treePaths, 0, args.length - 1);

        // 1) Käfer-Positionen DEFENSIV kopieren (nicht die Live-Liste verwenden!)
        List<LadybugPosition> positionsSnapshot = new java.util.ArrayList<>(state.getBoard().getLadybugList());

        // 2) Anzahl prüfen
        if (treePaths.length > positionsSnapshot.size()) {
            throw new CommandArgumentException(getCommandName(), args,
                    "Error, too many trees for available ladybugs");
        }

        // 3) Erst ALLE Dateien einlesen/ausgeben/parsen/validieren
        BehaviorTreeNode[] parsed = new BehaviorTreeNode[treePaths.length];
        for (int i = 0; i < treePaths.length; i++) {
            final Path path = Path.of(treePaths[i]);
            if (!Files.exists(path)) {
                throw new TreeParsingException("Error, tree file not found", path.toString());
            }
            try {
                String content = Files.readString(path);
                System.out.println(content.trim()); // Verbatim ausgeben
                BehaviorTreeNode tree = new MermaidParser().parse(content);

                if (!hasActionNode(tree)) {
                    throw new TreeParsingException("Error, behavior tree must contain at least one action", path.toString());
                }
                parsed[i] = tree;
            } catch (IOException e) {
                throw new TreeParsingException("Error, cannot read tree file", path.toString());
            }
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