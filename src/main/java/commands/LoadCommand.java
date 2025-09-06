package commands;

import bt.BehaviorTreeNode;
import exceptions.BoardException;
import exceptions.CommandArgumentException;
import exceptions.FileParsingException;
import main.GameState;
import model.Board;
import model.Ladybug;
import model.LadybugPosition;
import parser.BoardParser;
import parser.MermaidParser;

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
    public void execute(String[] args) throws CommandArgumentException, BoardException, FileParsingException {
        if (args.length < 2) {
            throw new CommandArgumentException(getCommandName(), args, getUsage());
        }
        String sub = args[0].toLowerCase();

        switch (sub) {
            case "board": {
                Path p = Path.of(args[1]);
                if (!Files.exists(p)) {
                    throw new IllegalArgumentException("Board-Datei nicht gefunden: " + p);
                }

                Board board = BoardParser.parse(p.toString());
                state.setBoard(board);
                break;
            }
            case "trees": {
                if (state.getBoard() == null) {
                    throw new IllegalStateException("Error: no board loaded");
                }
                String[] treePaths = new String[args.length - 1];
                System.arraycopy(args, 1, treePaths, 0, args.length - 1);
                List<LadybugPosition> allLadybugPositions = state.getBoard().getLadybugList();

                state.clearTrees();
                state.getBoard().getLadybugManager().clearAllLadybugs();

                for (int i = 0; i < treePaths.length; i++) {
                    Path treePath = Path.of(treePaths[i]);
                    if (!Files.exists(treePath)) {
                        throw new IllegalArgumentException("Tree file not found: " + treePath);
                    }

                    String content = Files.readString(treePath);
                    System.out.println(content.trim());

                    try {
                        BehaviorTreeNode tree = MermaidParser.fromFile(treePath.toString());

                        if (!hasActionNode(tree)) {
                            throw new IllegalArgumentException("Error: behavior tree must contain at least one action");
                        }

                        LadybugPosition position = allLadybugPositions.get(i);
                        Ladybug ladybug = new Ladybug(i + 1, position.getPosition(), position.getDirection());
                        state.getBoard().addLadybug(ladybug);

                        state.addTree(i + 1, tree);

                    } catch (Exception e) {
                        throw new IllegalArgumentException("Error parsing tree file " + treePath + ": " + e.getMessage());
                    }
                }
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown subcommand: " + sub);
        }
    }

    /**
     * Prüft rekursiv, ob der Baum mindestens einen Action-Knoten enthält
     */
    private boolean hasActionNode(BehaviorTreeNode node) {
        if (node instanceof bt.LeafNode leaf) {
            return leaf.isAction();
        }

        // Für Composite-Knoten: prüfe alle Kinder
        for (BehaviorTreeNode child : node.getChildren()) {
            if (hasActionNode(child)) {
                return true;
            }
        }
        return false;
    }

    @Override public String getCommandName() {
        return "load";
    }
    @Override public String getUsage() {
        return "load board <path> | load trees <bugId> <path>";
    }
}
