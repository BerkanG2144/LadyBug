package commands;

import engine.TreeExecution;
import main.GameState;
import model.Ladybug;

import java.util.List;
import java.util.Optional;

public class NextActionCommand extends AbstractCommand{

    public NextActionCommand(GameState state) {
        super(state);
    }

    @Override
    protected void executeInternal(String[] args) throws Exception {
        if (args.length != 0) {
            throw new IllegalArgumentException("Usage: next action");
        }

        requireLadybugs();

        List<Integer> ladybugIds = getBoard().listLadybugsIds();

        for (int ladybugId : ladybugIds) {
            Optional<Ladybug> ladybug = getBoard().getLadybugById(ladybugId);
            if (ladybug.isEmpty()) {
                continue;
            }

            TreeExecution execution = gameState.getExecutions().get(ladybugId);
            if (execution == null) {
                System.out.println("Error: no tree loaded for ladybug " + ladybugId);
                continue;
            }

            boolean actionExecuted = execution.tick(getBoard(), ladybug.get());

            if (actionExecuted) {
                getBoard().print();
            }
        }
    }

    @Override
    public String getCommandName() {
        return "next";
    }

    @Override
    public String getUsage() {
        return "next action";
    }
}
