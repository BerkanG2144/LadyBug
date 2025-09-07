package model;

import exceptions.LadybugException;

/**
 * Represents a ladybug with an ID, position, and direction.
 * @author ujnaa
 */
public class Ladybug {
    private final int id;
    private Position position;
    private Direction direction;


    /**
     * Creates a Ladybug.
     *
     * @param id the unique identifier (must be ≥ 1)
     * @param position the position on the board, must not be {@code null}
     * @param direction the direction the ladybug faces, must not be {@code null}
     * @throws LadybugException if {@code id < 1} or any argument is {@code null}
     */
    public Ladybug(int id, Position position, Direction direction) throws LadybugException {
        if (id < 1) {
            throw new LadybugException("Invalid ladybug ID: " + id);
        }
        if (position == null || direction == null) {
            throw new LadybugException("Null position or direction not allowed");
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
    /**
     * Sets a new Position.
     * @param position new position
     * @throws LadybugException if {@code position} is {@code null}
     */
    public void setPosition(Position position) throws LadybugException {
        if (position == null) {
            throw new LadybugException("Null position not allowed");
        }
        this.position = position;
    }

    /**
     * Sets a new Direction.
     * @param direction new direction.
     * @throws LadybugException if {@code direction} is {@code null}
     */
    public void setDirection(Direction direction) throws LadybugException {
        if (direction == null) {
            throw new LadybugException("Null direction not allowed");
        }
        this.direction = direction;
    }

    @Override
    public String toString() {
        return String.format("Ladybug[id=%d, pos=(%d,%d), dir=%s]", id, position.x(), position.y(), direction);
    }
}