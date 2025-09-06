package exceptions;

/**
 * Exception thrown when command arguments are invalid or malformed.
 * @author ujnaa
 */
public class CommandArgumentException extends BehaviorTreeException {

    private final String commandName;
    private final String[] arguments;

    /**
     * Constructs a new command argument exception.
     * @param commandName the name of the command
     * @param message the error message
     */
    public CommandArgumentException(String commandName, String message) {
        super(message);
        this.commandName = commandName;
        this.arguments = null;
    }

    /**
     * Constructs a new command argument exception with arguments.
     * @param commandName the name of the command
     * @param arguments the invalid arguments
     * @param message the error message
     */
    public CommandArgumentException(String commandName, String[] arguments, String message) {
        super(message);
        this.commandName = commandName;
        this.arguments = arguments != null ? arguments.clone() : null;
    }

    /**
     * Returns the command name.
     * @return the command name
     */
    public String getCommandName() {
        return commandName;
    }

    /**
     * Returns the invalid arguments.
     * @return copy of the arguments array, or null if not available
     */
    public String[] getArguments() {
        return arguments != null ? arguments.clone() : null;
    }
}