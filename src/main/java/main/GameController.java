package main;

import commands.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;


public class GameController {
    private final GameState gameState = new GameState();
    private final Map<String, Command> commands = new HashMap<>();
    private final Scanner scanner = new Scanner(System.in);

    public GameController() {
        registerCommands();
    }

    private void registerCommands() {
        LoadCommand loadCommand = new LoadCommand(gameState);
        commands.put("load", loadCommand);
        commands.put("list", new ListLadybugsCommand(gameState));
        commands.put("print", new PrintPositionCommand(gameState));
        commands.put("reset", new ResetTreeCommand(gameState));
        commands.put("head", new HeadCommand(gameState));
        commands.put("next", new NextActionCommand(gameState));
        commands.put("add", new AddSiblingCommand(gameState));
        commands.put("quit", new QuitCommand(gameState));
    }

    public void run() {
        System.out.println("LadyBug");
        while (true) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split("\\s+");
            String name = parts[0].toLowerCase();
            String[] args = new String[Math.max(0, parts.length - 1)];
            if (args.length > 0) System.arraycopy(parts, 1, args, 0, args.length);

            Command cmd = commands.get(name);
            if (cmd == null) {
                System.out.println("Unknown command. Try 'help'.");
                continue;
            }
            try {
                cmd.execute(args);
            } catch (QuitCommand.QuitException e) {
                // Quit gracefully without System.exit()
                break;
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void executeCommand(String input) throws Exception {
        String[] parts = input.split("\\s+");
        if (parts.length == 0) return;

        String commandName = parts[0];
        Command command = commands.get(commandName);

        if (command == null) {
            throw new IllegalArgumentException("Unknown command: " + commandName);
        }

        command.execute(parts);
    }
}
