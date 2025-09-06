package exceptions;

/**
 * Exception thrown when an action cannot be executed.
 * @author u-Kürzel
 */
public class ActionExecutionException extends BehaviorTreeException {

    private final String actionName;
    private final String reason;

    /**
     * Constructs a new action execution exception.
     * @param actionName the name of the action that failed
     * @param reason the reason why the action failed
     */
    public ActionExecutionException(String actionName, String reason) {
        super(String.format("Action '%s' failed: %s", actionName, reason));
        this.actionName = actionName;
        this.reason = reason;
    }

    /**
     * Returns the name of the action that failed.
     * @return the action name
     */
    public String getActionName() {
        return actionName;
    }

    /**
     * Returns the reason why the action failed.
     * @return the reason
     */
    public String getReason() {
        return reason;
    }
}