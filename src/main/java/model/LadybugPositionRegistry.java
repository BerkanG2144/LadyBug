package model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Registry that stores the initial positions of ladybugs found during board parsing.
 * <p>
 * This class separates the concern of tracking where ladybugs should be placed
 * from the actual terrain grid. During board parsing, ladybug symbols are
 * extracted and stored here, while the grid contains only terrain.
 * </p>
 *
 * <p>The registry maintains the positions in sorted order (row-wise, then column-wise)
 * to ensure deterministic ladybug ID assignment when trees are loaded.</p>
 *
 * @author ujnaa
 */
public class LadybugPositionRegistry {
    private final List<LadybugPosition> initialPositions;

    /**
     * Creates a new empty registry.
     */
    public LadybugPositionRegistry() {
        this.initialPositions = new ArrayList<>();
    }

    /**
     * Creates a registry with the given initial positions.
     * The positions are automatically sorted by row, then column.
     *
     * @param positions the initial ladybug positions
     */
    public LadybugPositionRegistry(List<LadybugPosition> positions) {
        this.initialPositions = new ArrayList<>(positions);
        sortPositions();
    }

    /**
     * Adds a ladybug position to the registry.
     * The registry will be automatically re-sorted to maintain order.
     *
     * @param position the position to add
     * @param direction the direction the ladybug faces
     */
    public void addPosition(Position position, Direction direction) {
        initialPositions.add(new LadybugPosition(position, direction));
        sortPositions();
    }

    /**
     * Returns all registered ladybug positions in sorted order.
     * The list is sorted first by row (y-coordinate), then by column (x-coordinate).
     *
     * @return a copy of the sorted position list
     */
    public List<LadybugPosition> getPositions() {
        return new ArrayList<>(initialPositions);
    }

    /**
     * Returns the number of registered ladybug positions.
     *
     * @return the count of positions
     */
    public int getCount() {
        return initialPositions.size();
    }

    /**
     * Checks if the registry is empty.
     *
     * @return true if no positions are registered
     */
    public boolean isEmpty() {
        return initialPositions.isEmpty();
    }

    /**
     * Clears all registered positions.
     */
    public void clear() {
        initialPositions.clear();
    }

    /**
     * Sorts the positions by row first, then column.
     * This ensures deterministic ordering for ladybug ID assignment.
     */
    private void sortPositions() {
        initialPositions.sort(Comparator
                .comparingInt((LadybugPosition pos) -> pos.getPosition().y())
                .thenComparingInt(pos -> pos.getPosition().x()));
    }
}