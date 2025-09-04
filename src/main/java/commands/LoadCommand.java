package commands;

import bt.BehaviorTreeNode;
import main.GameState;
import model.Board;
import parser.BoardParser;
import parser.MermaidParser;

import java.nio.file.Files;
import java.nio.file.Path;
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
                System.out.println("Board geladen: " + p.toAbsolutePath());
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

                // Prüfe ob genug Marienkäfer vorhanden sind
                List<Integer> ladybugIds = state.getBoard().listLadybugsIds();
                if (treePaths.length > ladybugIds.size()) {
                    throw new IllegalArgumentException("Error: more trees than ladybugs on board");
                }

                // Lösche vorherige Trees
                state.clearTrees();

                // Lade jeden Baum sukzessive
                for (int i = 0; i < treePaths.length; i++) {
                    Path treePath = Path.of(treePaths[i]);
                    if (!Files.exists(treePath)) {
                        throw new IllegalArgumentException("Tree file not found: " + treePath);
                    }

                    // Gib Inhalt verbatim aus (für Tests)
                    String content = Files.readString(treePath);
                    System.out.print(content); // Ohne zusätzliche Newline

                    // Parse den Baum
                    try {
                        BehaviorTreeNode tree = MermaidParser.fromFile(treePath.toString());

                        // Validiere dass mindestens eine Action vorhanden ist
                        if (!hasActionNode(tree)) {
                            throw new IllegalArgumentException("Error: behavior tree must contain at least one action");
                        }

                        // Marienkäfer-ID ist i+1 (beginnend bei 1)
                        int ladybugId = i + 1;
                        state.addTree(ladybugId, tree);

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
