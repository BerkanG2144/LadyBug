package bt;

/**
 * Contract for behaviors that want to provide a custom log name.
 * @author ujnaa
 * @version SS25
 */
public interface LogNameProvider {
    /**
     * Returns the custom name to use in log output.
     * @return a non-blank name for logging
     * */
    String logName();
}
