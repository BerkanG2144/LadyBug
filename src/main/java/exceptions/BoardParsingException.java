package exceptions;

import java.nio.file.Path;

/**
 * Exception thrown when a board file cannot be parsed.
 * @author u-Kürzel
 */
public class BoardParsingException extends BehaviorTreeException {

    private final Path filePath;

    /**
     * Constructs a new board parsing exception.
     * @param message the detail message
     * @param filePath the path of the board file that could not be parsed
     */
    public BoardParsingException(String message, Path filePath) {
        super(String.format("Error parsing board file '%s': %s", filePath, message));
        this.filePath = filePath;
    }

    /**
     * Constructs a new board parsing exception with a cause.
     * @param message the detail message
     * @param filePath the path of the board file that could not be parsed
     * @param cause the underlying cause
     */
    public BoardParsingException(String message, Path filePath, Throwable cause) {
        super(String.format("Error parsing board file '%s': %s", filePath, message), cause);
        this.filePath = filePath;
    }

    /**
     * Returns the path of the board file that could not be parsed.
     * @return the file path
     */
    public Path getFilePath() {
        return filePath;
    }
}