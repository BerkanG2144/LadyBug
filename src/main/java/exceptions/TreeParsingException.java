package exceptions;

import java.nio.file.Path;

/**
 * Exception thrown when a behavior tree cannot be parsed from Mermaid syntax.
 * @author ujnaa
 */
public class TreeParsingException extends BehaviorTreeException {

    private final String invalidContent;
    private final int lineNumber;
    private final Path filePath;

    /**
     * Constructs a new tree parsing exception.
     * @param message the detail message
     * @param invalidContent the content that could not be parsed
     */
    public TreeParsingException(String message, String invalidContent) {
        super(message);
        this.invalidContent = invalidContent;
        this.lineNumber = -1;
        this.filePath = null;
    }

    /**
     * Constructs a new tree parsing exception with line number.
     * @param message the detail message
     * @param invalidContent the content that could not be parsed
     * @param lineNumber the line number where parsing failed
     */
    public TreeParsingException(String message, String invalidContent, int lineNumber) {
        super(message);
        this.invalidContent = invalidContent;
        this.lineNumber = lineNumber;
        this.filePath = null;
    }

    /**
     * Constructs a new tree parsing exception with file path.
     * @param message the detail message
     * @param filePath the path of the file that could not be parsed
     */
    public TreeParsingException(String message, Path filePath) {
        super(String.format("Error parsing tree file '%s': %s", filePath, message));
        this.invalidContent = filePath != null ? filePath.toString() : "";
        this.lineNumber = -1;
        this.filePath = filePath;
    }

    /**
     * Constructs a new tree parsing exception with file path and cause.
     * @param message the detail message
     * @param filePath the path of the file that could not be parsed
     * @param cause the underlying cause
     */
    public TreeParsingException(String message, Path filePath, Throwable cause) {
        super(String.format("Error parsing tree file '%s': %s", filePath, message), cause);
        this.invalidContent = filePath != null ? filePath.toString() : "";
        this.lineNumber = -1;
        this.filePath = filePath;
    }

    /**
     * Returns the content that could not be parsed.
     * @return the invalid content
     */
    public String getInvalidContent() {
        return invalidContent;
    }

    /**
     * Returns the line number where parsing failed.
     * @return the line number, or -1 if not available
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * Returns the path of the file that could not be parsed.
     * @return the file path, or null if not available
     */
    public Path getFilePath() {
        return filePath;
    }
}