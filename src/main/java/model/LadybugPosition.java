package model;

/**
 * Represents a ladybug's position and direction on the board.
 * @author ujnaa
 */
public class LadybugPosition {
    private final Position position;
    private final Direction direction;

    /**
     * Creates a LadybugPosition.
     * @param position the position on the board
     * @param direction the direction the ladybug faces
     */
    public LadybugPosition(Position position, Direction direction) {
        this.position = position;
        this.direction = direction;
    }

    /**
     * Returns the position.
     * @return the position
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Returns the direction.
     * @return the direction
     */
    public Direction getDirection() {
        return direction;
    }
}