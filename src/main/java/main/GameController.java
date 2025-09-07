package main;

import commands.*;
import exceptions.BoardException;
import exceptions.CommandArgumentException;
import exceptions.LadybugException;
import exceptions.TreeParsingException;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Central CLI controller for the Ladybug application.
 * <p>
 * Registers all commands, reads user input, dispatches commands, and
 * handles command-level exceptions for user-friendly feedback.
 * @author ujnaa
 */
public class GameController {
    private final GameState gameState = new GameState();
    private final Map<String, Command> commands = new HashMap<>();
    private final Scanner scanner = new Scanner(System.in);

    /**
     * Creates a controller instance and registers all available commands.
     */
    public GameController() {
        registerCommands();
    }

    /**
     * Registers all supported CLI commands and binds them to their keywords.
     */
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

    /**
     * Starts the interactive CLI loop, parsing lines and executing matching commands.
     * Handles known command exceptions and prints concise error messages.
     */
    public void run() {
        while (true) {
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) {
                continue;
            }

            String[] parts = line.split("\\s+");
            String name = parts[0].toLowerCase();
            String[] args = new String[Math.max(0, parts.length - 1)];
            if (args.length > 0) {
                System.arraycopy(parts, 1, args, 0, args.length);
            }

            Command cmd = commands.get(name);
            if (cmd == null) {
                System.out.println("Unknown command. Try 'help'.");
                continue;
            }
            try {
                cmd.execute(args);
            } catch (BoardException | CommandArgumentException | TreeParsingException
                     | LadybugException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

//    private void executeCommand(String input)
//            throws BoardException, LadybugNotFoundException,
//            CommandArgumentException, TreeParsingException, LadybugException {
//        String[] parts = input.split("\\s+");
//        if (parts.length == 0) {
//            return;
//        }
//
//        String commandName = parts[0].toLowerCase();
//        Command command = commands.get(commandName);
//
//        if (command == null) {
//            throw new CommandArgumentException(commandName, parts, "Unknown command: " + commandName);
//        }
//
//        String[] args = new String[Math.max(0, parts.length - 1)];
//        if (args.length > 0) {
//            System.arraycopy(parts, 1, args, 0, args.length);
//        }
//        command.execute(args);
//    }
}