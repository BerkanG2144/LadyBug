package model;

import exceptions.LadybugException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Comparator;


/**
 * Manages ladybugs on the board without manipulating the terrain grid.
 * <p>
 * This manager maintains a separate list of active ladybugs and handles
 * their movement, rotation, and lifecycle. The terrain grid remains untouched
 * and ladybugs are rendered as overlays during board display.
 * </p>
 *
 * @author ujnaa
 */
public class LadybugManager {
    private final List<Ladybug> ladybugs;
    private final BoardGrid grid;

    /**
     * Creates a new manager bound to the given grid.
     * <p>
     * Note: This constructor no longer automatically initializes ladybugs from the grid,
     * since the grid contains only terrain. Ladybugs must be added explicitly
     * via {@link #addLadybug(Ladybug)} or created from a registry.
     * </p>
     *
     * @param grid the board grid (used for position validation)
     * @throws LadybugException if initialization fails
     */
    public LadybugManager(BoardGrid grid) throws LadybugException {
        this.grid = grid;
        this.ladybugs = new ArrayList<>();
        // No longer initializes from grid - grid contains only terrain!
    }

    /**
     * Gets a copy of all managed ladybugs.
     *
     * @return list of ladybugs
     */
    public List<Ladybug> getLadybugs() {
        return new ArrayList<>(ladybugs);
    }

    /**
     * finds ladybug at position.
     *
     * @param pos for specific ID
     * @return in order
     */
    public Optional<Ladybug> findLadybugAt(Position pos) {
        for (Ladybug lb : ladybugs) {
            if (lb.getPosition().equals(pos)) {
                return Optional.of(lb);
            }
        }
        return Optional.empty();
    }

    /**
     * Removes a ladybug by ID.
     * <p>
     * Note: No grid manipulation needed since ladybugs are not stored in the grid.
     * </p>
     *
     * @param id the ladybug ID to remove
     * @return true if a ladybug was removed, false if not found
     */
    public boolean removeLadybugById(int id) {
        for (int i = 0; i < ladybugs.size(); i++) {
            Ladybug lb = ladybugs.get(i);
            if (lb.getId() == id) {
                ladybugs.remove(i);
                return true;
            }
        }
        return false;
    }

    /**
     * Adds a ladybug to the manager.
     * <p>
     * The ladybug's position must be valid and the target cell must be terrain-accessible.
     * No grid manipulation is performed - ladybugs exist only in the manager.
     * </p>
     *
     * @param ladybug the ladybug to add
     * @throws IllegalArgumentException if ladybug is invalid, position is invalid,
     *                                  or ladybug ID already exists
     */
    public void addLadybug(Ladybug ladybug) {
        if (ladybug == null || !grid.isValidPosition(ladybug.getPosition())) {
            throw new IllegalArgumentException("Error, invalid ladybug or position");
        }
        if (getLadybugById(ladybug.getId()).isPresent()) {
            throw new IllegalArgumentException("Error, ladybug already exists");
        }

        // Check if position is accessible (not a wall/tree)
        char terrain = grid.getCell(ladybug.getPosition());
        if (terrain == '#') {
            throw new IllegalArgumentException("Error, cannot place ladybug on wall/tree");
        }

        ladybugs.add(ladybug);
        // No grid manipulation - ladybug exists only in this manager!
    }

    /**
     * Gets a ladybug by its ID.
     *
     * @param id the ladybug ID
     * @return Optional containing the ladybug if found
     * @throws IllegalArgumentException if ID is invalid
     */
    public Optional<Ladybug> getLadybugById(int id) {
        if (id < 1) {
            throw new IllegalArgumentException("Error, invalid ladybug ID");
        }
        for (Ladybug lb : ladybugs) {
            if (lb.getId() == id) {
                return Optional.of(lb);
            }
        }
        return Optional.empty();
    }

    /**
     * Gets all ladybug IDs sorted.
     *
     * @return list of sorted IDs
     */
    public List<Integer> listLadybugsIds() {
        List<Integer> result = new ArrayList<>();
        for (Ladybug ladybug : ladybugs) {
            result.add(ladybug.getId());
        }
        Collections.sort(result);
        return result;
    }

    /**
     * Moves a ladybug to an empty position.
     * <p>
     * Only updates the ladybug object - no grid manipulation.
     * </p>
     *
     * @param ladybug the ladybug to move
     * @param newPosition the target position
     * @param newDirection the new direction
     * @throws IllegalArgumentException if the move is invalid
     * @throws LadybugException if updating the ladybug state fails
     */
    public void moveLadybugToEmpty(Ladybug ladybug, Position newPosition, Direction newDirection)
            throws LadybugException {
        validateLadybugMove(ladybug, newPosition);
        if (grid.getCell(newPosition) != '.') {
            throw new IllegalArgumentException("Error, target position not empty");
        }
        performMove(ladybug, newPosition, newDirection);
    }

    /**
     * Moves a ladybug to a mushroom position (conceptually replaces the mushroom).
     * <p>
     * The mushroom itself is handled by the board logic, this just moves the ladybug.
     * </p>
     *
     * @param ladybug the ladybug to move
     * @param mushroomPos the mushroom position
     * @param newDirection the new direction
     * @throws IllegalArgumentException if move is invalid
     * @throws LadybugException if updating the ladybug state fails
     */
    public void moveLadybugToMushroom(Ladybug ladybug, Position mushroomPos, Direction newDirection)
            throws LadybugException {
        validateLadybugMove(ladybug, mushroomPos);
        // Note: We don't check for mushroom here since board handles terrain changes
        performMove(ladybug, mushroomPos, newDirection);
    }

    /**
     * Sets the direction of a ladybug.
     * <p>
     * Only updates the ladybug object - no grid manipulation.
     * </p>
     *
     * @param ladybug the ladybug
     * @param newDirection the new direction
     * @throws LadybugException if updating the ladybug state fails
     */
    public void setLadybugDirection(Ladybug ladybug, Direction newDirection) throws LadybugException {
        ladybug.setDirection(newDirection);
        // No grid manipulation - direction changes are purely in-memory!
    }

    /**
     * Gets all ladybug positions from the current active ladybugs.
     * <p>
     * This replaces the old method that scanned the grid, since ladybugs
     * are no longer stored in the grid.
     * </p>
     *
     * @return list of current ladybug positions
     */
    public List<LadybugPosition> getLadybugPositionsFromGrid() {
        List<LadybugPosition> positions = new ArrayList<>();
        for (Ladybug ladybug : ladybugs) {
            positions.add(new LadybugPosition(ladybug.getPosition(), ladybug.getDirection()));
        }

        // Sort by position for consistency
        positions.sort(Comparator
                .comparingInt((LadybugPosition pos) -> pos.getPosition().y())
                .thenComparingInt(pos -> pos.getPosition().x()));

        return positions;
    }

    /**
     * Returns a list of all active ladybug positions and directions.
     * <p>
     * This is an alias for {@link #getLadybugPositionsFromGrid()} to maintain
     * compatibility with existing code.
     * </p>
     *
     * @return list of ladybug positions
     */
    public List<LadybugPosition> getLadybugList() {
        return getLadybugPositionsFromGrid();
    }

    /**
     * Gets all active ladybug positions sorted by position.
     * Only returns ladybugs that are currently managed by this manager.
     *
     * @return list of active ladybug positions
     */
    public List<LadybugPosition> getActiveLadybugList() {
        return getLadybugPositionsFromGrid(); // Same thing now
    }

    /**
     * Removes all ladybugs from the manager.
     * <p>
     * No grid manipulation needed since ladybugs are not stored in the grid.
     * </p>
     */
    public void clearAllLadybugs() {
        ladybugs.clear();
    }

    /**
     * Creates ladybugs from a registry and adds them to this manager.
     * <p>
     * This is the new way to initialize ladybugs, replacing the old
     * grid-scanning approach.
     * </p>
     *
     * @param registry the registry containing ladybug positions
     * @throws LadybugException if ladybug creation or addition fails
     */
    public void initializeFromRegistry(LadybugPositionRegistry registry) throws LadybugException {
        clearAllLadybugs(); // Start fresh

        List<LadybugPosition> positions = registry.getPositions();
        int id = 1;

        for (LadybugPosition position : positions) {
            Ladybug ladybug = new Ladybug(id++, position.getPosition(), position.getDirection());
            addLadybug(ladybug);
        }
    }

    // === Private helper methods ===

    private void validateLadybugMove(Ladybug ladybug, Position newPosition) {
        if (ladybug == null || !ladybugs.contains(ladybug)) {
            throw new IllegalArgumentException("Error, ladybug not found");
        }
        if (!grid.isValidPosition(newPosition)) {
            throw new IllegalArgumentException("Error, invalid position");
        }
    }

    private void performMove(Ladybug ladybug, Position newPosition, Direction newDirection)
            throws LadybugException {
        ladybug.setPosition(newPosition);
        ladybug.setDirection(newDirection);
        // No grid manipulation - movement is purely in ladybug objects!
    }
}