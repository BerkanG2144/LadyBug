package commands;

import bt.BehaviorTreeNode;
import engine.TreeExecution;
import main.GameState;
import model.Ladybug;

import java.util.Optional;

public class HeadCommand extends AbstractCommand {

    public HeadCommand(GameState state) {
        super(state);
    }

    @Override
    protected void executeInternal(String[] args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException("Usage: head <ladybug>");
        }

        requireLadybugs(); // Prüft ob Board und Marienkäfer existieren

        int ladybugId;
        try {
            ladybugId = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Error: invalid ladybug ID");
        }

        Optional<Ladybug> ladybug = getBoard().getLadybugById(ladybugId);
        if (ladybug.isEmpty()) {
            throw new IllegalArgumentException("Error: ladybug not found");
        }

        // Prüfe ob TreeExecution für diesen Marienkäfer existiert
        TreeExecution execution = gameState.getExecutions().get(ladybugId);
        if (execution == null) {
            throw new IllegalStateException("Error: no tree loaded for ladybug " + ladybugId);
        }

        var execState = execution.stateOf(ladybug.get());   // <-- public Getter in TreeExecution (siehe unten)
        if (execState != null && execState.getLastExecutedLeaf() != null) {
            System.out.println(execState.getLastExecutedLeaf().getId());
            return;
        }

        // Hole aktuellen Knoten (nutzt das erweiterte System)
        BehaviorTreeNode currentNode = execution.getCurrentNode(ladybug.get());

        if (currentNode == null) {
            // Fallback: verwende Root-Knoten
            BehaviorTreeNode tree = gameState.getLadybugTrees().get(ladybugId);
            if (tree == null) {
                throw new IllegalStateException("Error: no tree found for ladybug " + ladybugId);
            }
            currentNode = tree;
        }

        System.out.println(currentNode.getId());
    }

    @Override
    public String getCommandName() {
        return "head";
    }

    @Override
    public String getUsage() {
        return "head <ladybug>";
    }
}