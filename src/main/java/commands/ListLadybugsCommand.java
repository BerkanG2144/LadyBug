package commands;
import main.GameState;

import java.util.List;
import java.util.stream.Collectors;

public class ListLadybugsCommand extends AbstractCommand {

    public ListLadybugsCommand(GameState state) {
        super(state);
    }

    @Override
    protected void executeInternal(String[] args) throws Exception {
        requireLadybugs(); // Validation aus AbstractCommand

        List<Integer> ids = getBoard().listLadybugsIds();
        String result = ids.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(" "));
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
