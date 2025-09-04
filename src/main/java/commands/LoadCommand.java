package commands;

import bt.BehaviorTreeNode;
import main.GameState;
import model.Board;
import model.Ladybug;
import model.LadybugPosition;
import parser.BoardParser;
import parser.MermaidParser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLOutput;
import java.util.List;

public class LoadCommand implements Command {
    private final GameState state;

    public LoadCommand(GameState state) { this.state = state; }

    @Override
    public void execute(String[] args) throws Exception {
        if (args.length < 2) {
            throw new IllegalArgumentException(getUsage());
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "board": {
                // Erwartet: load board <path>
                Path p = Path.of(args[1]);
                if (!Files.exists(p)) {
                    throw new IllegalArgumentException("Board-Datei nicht gefunden: " + p);
                }

                Board board = BoardParser.parse(p.toString());
                state.setBoard(board);
                break;
            }
            case "trees": {
                // Erwartet: load trees <path1> [<path2> ...]
                if (state.getBoard() == null) {
                    throw new IllegalStateException("Error: no board loaded");
                }

                // Sammle alle Pfade (ab args[1])
                String[] treePaths = new String[args.length - 1];
                System.arraycopy(args, 1, treePaths, 0, args.length - 1);

                //hole alle verfügbaren pos
                List<LadybugPosition> allLadybugPositions = state.getBoard().getLadybugList();

                state.clearTrees();
                state.getBoard().getLadybugManager().clearAllLadybugs();

                // Lade jeden Baum sukzessive
                for (int i = 0; i < treePaths.length; i++) {
                    Path treePath = Path.of(treePaths[i]);
                    if (!Files.exists(treePath)) {
                        throw new IllegalArgumentException("Tree file not found: " + treePath);
                    }

                    // Gib Inhalt verbatim aus (für Tests)
                    String content = Files.readString(treePath);
                    System.out.println(content.trim());


                    // Parse den Baum
                    try {
                        BehaviorTreeNode tree = MermaidParser.fromFile(treePath.toString());

                        // Validiere dass mindestens eine Action vorhanden ist
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

    @Override public String getCommandName() { return "load"; }
    @Override public String getUsage() { return "load board <path> | load trees <bugId> <path>"; }
}
