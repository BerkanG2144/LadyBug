package commands;

import com.sun.source.tree.Tree;
import engine.TreeExecution;
import main.GameState;
import model.Ladybug;

import java.util.Optional;

public class ResetTreeCommand extends AbstractCommand{

    public ResetTreeCommand(GameState state) {
        super(state);
    }

    @Override
    protected void executeInternal(String[] args) throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException("Error: reset tree <ladybug>");
        }

        requireLadybugs();

        int ladybugId;
        try {
            ladybugId = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Error, invalid ladybug ID");
        }

        Optional<Ladybug> ladybug = getBoard().getLadybugById(ladybugId);
        if (ladybug.isEmpty()) {
            throw new IllegalArgumentException("Error: ladybug not found");
        }

        TreeExecution execution = gameState.getExecutions().get(ladybugId);
        if (execution == null) {
            throw new IllegalArgumentException("Error: no tree loaded for ladybug " + ladybugId);
        }

        execution.reset(ladybug.get());
    }

    @Override
    public String getCommandName() {
        return "reset";
    }

    @Override
    public String getUsage() {
        return "reset tree <ladybug>";
    }
}
