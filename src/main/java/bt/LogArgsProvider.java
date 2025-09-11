package bt;

/**
 * Contract for behaviors that want to provide formatted arguments for logging.
 * @author ujnaa
 * @version SS25
 * */
public interface LogArgsProvider {
    /**
     * Returns the arguments to append to the log after the leaf name.
     * @return a formatted argument string
     */
    String logArgs();
}
