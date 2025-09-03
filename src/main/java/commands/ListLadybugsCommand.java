// commands/ListLadybugsCommand.java
package commands;

import main.GameState;
import model.Board;

import java.util.List;

public class ListLadybugsCommand implements Command {
    private final GameState state;

    public ListLadybugsCommand(GameState state) {
        this.state = state;
    }

    @Override
    public void execute(String[] args) {
        Board board = state.getBoard();
        if (board == null) {
            System.out.println("Error: no board loaded");
            return;
        }

        List<Integer> ids = board.listLadybugsIds(); // Methode in Board, die IDs sammelt
        if (ids.isEmpty()) {
            System.out.println("Error: no ladybugs found");
            return;
        }

        // IDs mit Leerzeichen getrennt in einer Zeile ausgeben
        String result = ids.stream()
                .map(String::valueOf)
                .reduce((a, b) -> a + " " + b)
                .orElse("");
        System.out.println(result);
    }

    @Override
    public String getCommandName() {
        return "list";
    }

    @Override
    public String getUsage() {
        return "list ladybugs";
    }
}
