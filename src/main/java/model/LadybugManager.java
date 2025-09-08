package model;

import exceptions.LadybugException;

import java.util.*;



/**
 * Manages ladybugs on the board.
 * @author ujnaa
 */
public class LadybugManager {
    private final List<Ladybug> ladybugs;
    private final BoardGrid grid;

    /**
     * Creates a new manager bound to the given grid and initializes
     * ladybugs from the grid's current state.
     *
     * @param grid the board grid backing this manager
     * @throws LadybugException if initializing ladybugs from the grid fails
     */
    public LadybugManager(BoardGrid grid) throws LadybugException {
        this.grid = grid;
        this.ladybugs = new ArrayList<>();
        initializeLadybugsFromGrid();
    }

    /**
     * Gets ladybugs.
     *
     * @return ladybugs
     * */
    public List<Ladybug> getLadybugs() {
        return new ArrayList<>(ladybugs);
    }

    /**
     * Removes a ladybug by id and clears its cell on the grid.
     * @param id for ka
     * @return true or false
     * */
    public boolean removeLadybugById(int id) {
        for (int i = 0; i < ladybugs.size(); i++) {
            Ladybug lb = ladybugs.get(i);
            if (lb.getId() == id) {
                // Grid-Zelle leeren
                grid.setCell(lb.getPosition(), '.');
                ladybugs.remove(i);
                return true;
            }
        }
        return false;
    }

    /**
     * Adds a ladybug to the manager.
     * @param ladybug the ladybug to add
     * @throws IllegalArgumentException if ladybug is invalid or position occupied
     */
    public void addLadybug(Ladybug ladybug) {
        if (ladybug == null || !grid.isValidPosition(ladybug.getPosition())) {
            throw new IllegalArgumentException("Error, invalid ladybug or position");
        }
        if (getLadybugById(ladybug.getId()).isPresent()) {
            throw new IllegalArgumentException("Error, ladybug already exists");
        }
        if (grid.getCell(ladybug.getPosition()) != '.') {
            throw new IllegalArgumentException("Error, position already occupied");
        }
        ladybugs.add(ladybug);
        grid.setCell(ladybug.getPosition(), ladybug.getDirection().toSymbol());
    }

    private void initializeLadybugsFromGrid() throws LadybugException {
        if (!ladybugs.isEmpty()) {
            return; // schon initialisiert
        }
        List<LadybugPosition> positions = getLadybugPositionsFromGrid();
        int id = 1;
        for (LadybugPosition lp : positions) {
            Ladybug lb = new Ladybug(id++, lp.getPosition(), lp.getDirection());
            ladybugs.add(lb);
            // Grid-Symbol bleibt wie es ist
        }
    }

    /**
     * Gets a ladybug by its ID.
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
     * @param ladybug the ladybug to move
     * @param newPosition the target position
     * @param newDirection the new direction
     * @throws IllegalArgumentException if the ladybug is {@code null}, the position is invalid,
     *                                  the id already exists, or the target cell is occupied
     * @throws LadybugException         if updating the grid or ladybug state fails
     */
    public void moveLadybugToEmpty(Ladybug ladybug, Position newPosition, Direction newDirection) throws LadybugException {
        validateLadybugMove(ladybug, newPosition);
        if (grid.getCell(newPosition) != '.') {
            throw new IllegalArgumentException("Error, target position occupied");
        }
        performMove(ladybug, newPosition, newDirection);
    }

    /**
     * Moves a ladybug to a mushroom position (replaces the mushroom).
     * @param ladybug the ladybug to move
     * @param mushroomPos the mushroom position
     * @param newDirection the new direction
     * @throws IllegalArgumentException if move is invalid
     * @throws LadybugException         if updating the grid or ladybug state fails
     */
    public void moveLadybugToMushroom(Ladybug ladybug, Position mushroomPos, Direction newDirection) throws LadybugException {
        validateLadybugMove(ladybug, mushroomPos);
        if (grid.getCell(mushroomPos) != 'o') {
            throw new IllegalArgumentException("Error, no mushroom to push");
        }
        performMove(ladybug, mushroomPos, newDirection);
    }

    /**
     * Sets the direction of a ladybug and updates the grid.
     * @param ladybug the ladybug
     * @param newDirection the new direction
     * @throws LadybugException if updating the grid or ladybug state fails
     */
    public void setLadybugDirection(Ladybug ladybug, Direction newDirection) throws LadybugException {
        ladybug.setDirection(newDirection);
        grid.setCell(ladybug.getPosition(), newDirection.toSymbol());
    }

    /**
     * Gets all ladybug positions from the grid (for tree loading).
     * This method scans the grid and returns positions where ladybugs are marked,
     * but does NOT create actual Ladybug objects.
     * @return list of ladybug positions found in the grid
     */
    public List<LadybugPosition> getLadybugPositionsFromGrid() {
        List<LadybugPosition> ladybugPositions = new ArrayList<>();
        for (int y = 1; y <= grid.getHeight(); y++) {
            for (int x = 1; x <= grid.getWidth(); x++) {
                Position pos = new Position(x, y);
                char c = grid.getCell(pos);

                if (isLadybugSymbol(c)) {
                    Direction dir = Direction.fromSymbol(c);
                    ladybugPositions.add(new LadybugPosition(pos, dir));
                }
            }
        }
        ladybugPositions.sort(Comparator.comparingInt((LadybugPosition b) -> b.getPosition().y())
                .thenComparingInt(b -> b.getPosition().x()));
        return ladybugPositions;
    }

    /**
     * Returns a list of all ladybug positions and directions found on the grid.
     * <p>
     * This is a convenience method that simply delegates to
     * {@link #getLadybugPositionsFromGrid()}, allowing callers to obtain a
     * snapshot of the board's ladybug state without activating them in the
     * manager. The returned list is sorted by row and then column.
     * </p>
     *
     * @return list of ladybug positions from the grid
     */
    public List<LadybugPosition> getLadybugList() {
        return getLadybugPositionsFromGrid();
    }



    private boolean isLadybugSymbol(char c) {
        return c == '^' || c == 'v' || c == '<' || c == '>';
    }

    /**
     * Gets all active ladybug positions sorted by position.
     * Only returns ladybugs that have been added via addLadybug() (i.e., when trees are loaded).
     * @return list of active ladybug positions
     */
    public List<LadybugPosition> getActiveLadybugList() {
        List<LadybugPosition> result = new ArrayList<>();
        for (Ladybug lb : ladybugs) {
            result.add(new LadybugPosition(lb.getPosition(), lb.getDirection()));
        }
        result.sort(Comparator.comparingInt((LadybugPosition b) -> b.getPosition().y())
                .thenComparingInt(b -> b.getPosition().x()));
        return result;
    }

    private void validateLadybugMove(Ladybug ladybug, Position newPosition) {
        if (ladybug == null || !ladybugs.contains(ladybug)) {
            throw new IllegalArgumentException("Error, ladybug not found");
        }
        if (!grid.isValidPosition(newPosition)) {
            throw new IllegalArgumentException("Error, invalid position");
        }
    }

    private void performMove(Ladybug ladybug, Position newPosition, Direction newDirection) throws LadybugException {
        grid.setCell(ladybug.getPosition(), '.');
        ladybug.setPosition(newPosition);
        ladybug.setDirection(newDirection);
        grid.setCell(newPosition, newDirection.toSymbol());
    }

    /**
     * Removes all ladybugs from the grid and internal list,
     * resetting their cells to '.'. Intended for re-initialization.
     */
    public void clearAllLadybugs() {
        for (Ladybug ladybug : ladybugs) {
            grid.setCell(ladybug.getPosition(), '.');
        }
        ladybugs.clear();
    }
}