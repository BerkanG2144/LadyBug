package exceptions;

/**
 * Signals that the program should be terminated by user request.
 * @author ujnaa
 */
public class QuitException extends BoardException  {

    /**
     * Creates a new QuitException with a default message.
     */
    public QuitException() {
        super("Program terminated by user");
    }
}
