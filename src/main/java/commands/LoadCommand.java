package commands;

import main.GameState;
import model.Board;
import parser.BoardParser;

import java.nio.file.Files;
import java.nio.file.Path;

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
                if (!Files.exists(p)) throw new IllegalArgumentException("Board-Datei nicht gefunden: " + p);
                Board board = BoardParser.parse(p.toString());
                state.setBoard(board);
                System.out.println("Board geladen: " + p.toAbsolutePath());
                break;
            }
            case "trees": {
                //
            }
            default:
                throw new IllegalArgumentException("Unknown subcommand: " + sub);
        }
    }

    @Override public String getCommandName() { return "load"; }
    @Override public String getUsage() { return "load board <path> | load trees <bugId> <path>"; }
}
