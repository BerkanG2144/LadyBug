package model;

/**
 * Represents the four cardinal directions used on the board.
 * <p>
 * Each direction is associated with:
 * <ul>
 *   <li>a character symbol (e.g. {@code '^'} for up)</li>
 *   <li>a delta in x-direction ({@code dx})</li>
 *   <li>a delta in y-direction ({@code dy})</li>
 * </ul>
 * These values can be used to update positions and determine orientation.
 * </p>
 *
 * @author ujnaa
 */
public enum Direction {
    /** Upward direction, symbol {@code '^'}. */
    UP('^', 0, -1),

    /** Rightward direction, symbol {@code '>'}. */
    RIGHT('>', 1, 0),

    /** Downward direction, symbol {@code 'v'}. */
    DOWN('v', 0, 1),

    /** Leftward direction, symbol {@code '<'}. */
    LEFT('<', -1, 0);

    private final char symbol;
    private final int dx;
    private final int dy;

    Direction(char symbol, int dx, int dy) {
        this.symbol = symbol;
        this.dx = dx;
        this.dy = dy;
    }

    /**
     * Gets the character symbol for this direction.
     *
     * @return the symbol
     */
    public char toSymbol() {
        return symbol;
    }

    /**
     * Gets the horizontal movement delta ({@code dx}) for this direction.
     *
     * @return the horizontal delta
     */
    public int getDx() {
        return dx;
    }

    /**
     * Gets the vertical movement delta ({@code dy}) for this direction.
     *
     * @return the vertical delta
     */
    public int getDy() {
        return dy;
    }

    /**
     * Resolves a {@code Direction} from a symbol.
     *
     * @param symbol the direction symbol
     * @return the corresponding {@code Direction}
     * @throws IllegalArgumentException if the symbol does not match any direction
     */
    public static Direction fromSymbol(char symbol) {
        for (Direction direction : values()) {
            if (direction.symbol == symbol) {
                return direction;
            }
        }
        throw new IllegalArgumentException("Error, Invalid direction symbol: " + symbol);
    }

    /**
     * Turns left relative to the current direction.
     *
     * @return the new direction after turning left
     */
    public Direction turnLeft() {
        return switch (this) { //this actual position
            case UP -> LEFT;
            case RIGHT -> UP;
            case DOWN -> RIGHT;
            case LEFT -> DOWN;
        };
    }

    /**
     * Turns right relative to the current direction.
     *
     * @return the new direction after turning right
     */
    public Direction turnRight() {
        return switch (this) { //this actual position
            case UP -> RIGHT;
            case RIGHT -> DOWN;
            case DOWN -> LEFT;
            case LEFT -> UP;
        };
    }

    /**
     * Resolves a {@code Direction} from a movement delta.
     * <p>
     * If the horizontal movement ({@code dx}) has a greater or equal absolute value
     * than the vertical movement ({@code dy}), a horizontal direction is returned.
     * Otherwise, a vertical direction is chosen.
     * </p>
     *
     * @param dx horizontal delta
     * @param dy vertical delta
     * @return the closest matching {@code Direction}
     */
    public static Direction fromDelta(int dx, int dy) {
        if (Math.abs(dx) >= Math.abs(dy)) {
            return dx >= 0 ? RIGHT : LEFT;
        } else {
            return dy >= 0 ? DOWN : UP;
        }
    }

}
