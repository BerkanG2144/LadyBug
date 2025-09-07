package commands;

import exceptions.*;

/**
 * Represents a CLI command in the Ladybug application.
 * <p>
 * Every command must implement this interface and provide:
 * <ul>
 *   <li>{@link #execute(String[])} to perform the action</li>
 *   <li>{@link #getCommandName()} to identify the command keyword</li>
 *   <li>{@link #getUsage()} to describe the expected usage format</li>
 * </ul>
 *
 * @author ujnaa
 */
public interface Command {

    /**
     * Executes the command with the given arguments.
     *
     * @param args the arguments passed from the CLI, never {@code null}
     * @throws BoardException           if a board-related error occurs
     * @throws LadybugNotFoundException if a referenced ladybug does not exist
     * @throws CommandArgumentException if the arguments are invalid
     * @throws TreeParsingException     if a behavior tree cannot be parsed
     * @throws LadybugException     if a behavior tree cannot be parsed
     */
    void execute(String[] args)
            throws BoardException,
            CommandArgumentException, TreeParsingException, LadybugException;

    /**
     * Returns the keyword that identifies this command.
     *
     * @return the command name, never {@code null}
     */
    String getCommandName();

    /**
     * Returns the usage string describing how this command should be invoked.
     *
     * @return usage description, never {@code null}
     */
    String getUsage();
}
