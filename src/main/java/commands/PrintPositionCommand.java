package commands;

import main.GameState;
import model.Board;
import model.Ladybug;
import model.Position;

import java.util.List;
import java.util.Optional;

public class PrintPositionCommand implements Command {
    private final GameState state;

    public PrintPositionCommand(GameState state) {
        this.state = state;
    }

    @Override
    public void execute(String[] args) throws Exception {
        if (args.length < 2) {
            throw new IllegalArgumentException("Usage: print position <ladybug>");
        }

        Board board = state.getBoard();
        if (board == null) {
            System.out.println("Error: no board loaded");
            return;
        }

        int ladybugId = Integer.parseInt(args[1]);
        Optional<Ladybug> ladybug = board.getLadybugById(ladybugId);

        if (ladybug.isEmpty()) {
            System.out.println("Error: ladybug not found");
            return;
        }

        Position pos = ladybug.get().getPosition();
        System.out.println("(" + pos.x() + ", " + pos.y() + ")");
    }

    @Override
    public String getCommandName() {
        return "print";
    }

    @Override
    public String getUsage() {
        return "print position";
    }
}
