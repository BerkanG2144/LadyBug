package bt;

/**
 * Defines the type of leaf node: either an action or a condition.
 * @author ujnaa
 * @version SS25
 */
public enum LeafKind {
    /** A leaf that performs an action and may change the world state. */
    ACTION,

    /** A leaf that evaluates a predicate and must not change state. */
    CONDITION
}
