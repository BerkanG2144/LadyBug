package exceptions;

import java.nio.file.Path;

/**
 * Exception thrown when a file cannot be parsed.
 * @author ujnaa
 */
public class FileParsingException extends BehaviorTreeException {

    private final Path filePath;

    /**
     * Constructs a new file parsing exception.
     * @param message the detail message
     * @param filePath the path of the file that could not be parsed
     */
    public FileParsingException(String message, Path filePath) {
        super(String.format("Error, parsing file '%s': %s", filePath, message));
        this.filePath = filePath;
    }

    /**
     * Constructs a new file parsing exception with a cause.
     * @param message the detail message
     * @param filePath the path of the file that could not be parsed
     * @param cause the underlying cause
     */
    public FileParsingException(String message, Path filePath, Throwable cause) {
        super(String.format("Error, parsing file '%s': %s", filePath, message), cause);
        this.filePath = filePath;
    }

    /**
     * Returns the path of the file that could not be parsed.
     * @return the file path
     */
    public Path getFilePath() {
        return filePath;
    }
}