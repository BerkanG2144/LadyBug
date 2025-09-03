package commands;

import main.GameState;
import model.Board;

import java.util.List;

public class PrintPositionCommand implements Command {
    private final GameState state;

    public PrintPositionCommand(GameState state) {
        this.state = state;
    }

    @Override
    public void execute(String[] args) throws Exception {
        Board board = state.getBoard();
        List<Integer> ids = board.listLadybugsIds();

        if (ids.isEmpty()) {
            System.out.println("Error: no ladybugs found");
            return;
        }

        board.getLadybugsFromGrid();
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
