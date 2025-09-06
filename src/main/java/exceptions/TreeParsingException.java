package exceptions;

/**
 * Exception thrown when a behavior tree cannot be parsed from Mermaid syntax.
 * @author u-Kürzel
 */
public class TreeParsingException extends exceptions.BehaviorTreeException {

    private final String invalidContent;
    private final int lineNumber;

    /**
     * Constructs a new tree parsing exception.
     * @param message the detail message
     * @param invalidContent the content that could not be parsed
     */
    public TreeParsingException(String message, String invalidContent) {
        super(message);
        this.invalidContent = invalidContent;
        this.lineNumber = -1;
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
}