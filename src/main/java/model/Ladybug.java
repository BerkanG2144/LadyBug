package model;

/**
 * Represents a ladybug with an ID, position, and direction.
 * @author u-Kürzel
 */
public class Ladybug {
    private final int id;
    private Position position;
    private Direction direction;


    /**
     * Creates a Ladybug.
     * @param id the unique identifier
     * @param position the position on the board
     * @param direction the direction the ladybug faces
     */
    public Ladybug(int id, Position position, Direction direction) {
        if (id < 1) {
            throw new IllegalArgumentException("Error, invalid ladybug ID");
        }
        if (position == null || direction == null) {
            throw new IllegalArgumentException("Error, null position or direction");
        }
        this.id = id;
        this.position = position;
        this.direction = direction;
    }

    /**
     * Returns the ID.
     * @return the ID
     */
    public int getId() {
        return id;
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

    public void setPosition(Position position) {
        if (position == null) throw new IllegalArgumentException("Error, null position");
        this.position = position;
    }

    public void setDirection(Direction direction) {
        if (direction == null) throw new IllegalArgumentException("Error, null direction");
        this.direction = direction;
    }

    @Override
    public String toString() {
        return String.format("Ladybug[id=%d, pos=(%d,%d), dir=%s]", id, position.x(), position.y(), direction);
    }
}