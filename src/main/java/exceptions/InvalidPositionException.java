package exceptions;

import model.Position;

/**
 * Exception thrown when an invalid position is accessed on the board.
 * @author u-Kürzel
 */
public class InvalidPositionException extends BoardException {

    private final Position position;
    private final int maxX;
    private final int maxY;

    /**
     * Constructs a new invalid position exception.
     * @param position the invalid position
     * @param maxX the maximum valid X coordinate
     * @param maxY the maximum valid Y coordinate
     */
    public InvalidPositionException(Position position, int maxX, int maxY) {
        super(String.format("Position (%d,%d) is invalid. Valid range: (1,1) to (%d,%d)",
                position.x(), position.y(), maxX, maxY));
        this.position = position;
        this.maxX = maxX;
        this.maxY = maxY;
    }

    /**
     * Returns the invalid position.
     * @return the invalid position
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Returns the maximum valid X coordinate.
     * @return the maximum X coordinate
     */
    public int getMaxX() {
        return maxX;
    }

    /**
     * Returns the maximum valid Y coordinate.
     * @return the maximum Y coordinate
     */
    public int getMaxY() {
        return maxY;
    }
}