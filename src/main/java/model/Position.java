package model;

/**
 * Immutable 2D position on the board.
 * <p>
 * A {@code Position} is represented using one-based coordinates (x, y),
 * where {@code (1,1)} refers to the top-left corner of the grid.
 * </p>
 *
 * <p>Instances of this class are immutable and can be safely reused.</p>
 *
 * @param x the horizontal coordinate, must be ≥ 1
 * @param y the vertical coordinate, must be ≥ 1
 *
 * @author ujnaa
 */
public record Position(int x, int y) {
    /**
     * Returns a new {@code Position} moved by the specified delta.
     */
    public Position {
        if (x < 1 || y < 1) {
            throw new IllegalArgumentException("Error, position coordinates must be positive");
        }
    }

    /**
     * Returns a new {@code Position} moved by the specified delta.
     *
     * @param dx the horizontal offset
     * @param dy the vertical offset
     * @return a new {@code Position} with updated coordinates
     */
    public Position add(int dx, int dy) {
        return new Position(x + dx, y + dy);
    }

    /**
     * Returns if position is Valid.
     *
     * @param maxX the horizontal offset
     * @param maxY the vertical offset
     * @return a new {@code Position} with updated coordinates
     */
    public boolean isValid(int maxX, int maxY) {
        return x >= 1 && x <= maxX && y >= 1 && y <= maxY;
    }

}
