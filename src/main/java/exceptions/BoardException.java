package exceptions;

/**
 * Exception type for errors related to the board.
 * <p>
 * This exception is thrown when invalid operations on the board occur,
 * such as invalid positions, illegal moves, or corrupted board data.
 * </p>
 *
 * @author ujnaa
 */
public class BoardException extends Exception {
    /**
     * Creates a new {@code BoardException} with the specified detail message.
     *
     * @param message a description of the error, must not be {@code null}
     */
    public BoardException(String message) {
        super(message);
    }
}
