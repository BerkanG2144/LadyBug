package model;

import java.util.List;
import java.util.Optional;

/**
 * Represents the game board and aggregates access to the underlying grid,
 * ladybug management and path finding. Most operations are delegated to
 * the respective components.
 *
 * <p>No behavior was changed; only Javadoc was added.</p>
 *
 * @author ujnaa
 */
public class Board {
    private final BoardGrid grid;
    private final LadybugManager ladybugManager;
    private final PathFinder pathFinder;

    /**
     * Creates a new board from the provided grid data.
     *
     * @param gridData two-dimensional character array describing the field
     */
    public Board(char[][] gridData) {
        this.grid = new BoardGrid(gridData);
        this.ladybugManager = new LadybugManager(grid);
        this.pathFinder = new PathFinder(grid);
    }

    // === Component access methods (for GameState) ===

    /**
     * Returns the underlying {@link BoardGrid}.
     *
     * @return the grid
     */
    public BoardGrid getGrid() {
        return grid;
    }

    /**
     * Returns the {@link LadybugManager}.
     *
     * @return the ladybug manager
     */
    public LadybugManager getLadybugManager() {
        return ladybugManager;
    }

    /**
     * Returns the {@link PathFinder}.
     *
     * @return the path finder
     */
    public PathFinder getPathFinder() {
        return pathFinder;
    }

    // === Grid operations ===

    /**
     * Gets the symbol at the given position.
     *
     * @param pos position in the grid (1-based)
     * @return the character at that position
     */
    public char getCell(Position pos) {
        return grid.getCell(pos);
    }

    /**
     * Sets a symbol at the given position.
     *
     * @param pos    position in the grid (1-based)
     * @param symbol character to set
     */
    public void setCell(Position pos, char symbol) {
        grid.setCell(pos, symbol);
    }

    /**
     * Prints the current grid to standard output.
     */
    public void print() {
        grid.print();
    }

    // === Ladybug operations (delegation) ===

    /**
     * Adds a ladybug to the board.
     *
     * @param ladybug ladybug to add
     */
    public void addLadybug(Ladybug ladybug) {
        ladybugManager.addLadybug(ladybug);
    }

    /**
     * Looks up a ladybug by its id.
     *
     * @param id unique id
     * @return {@link Optional} containing the ladybug if present
     */
    public Optional<Ladybug> getLadybugById(int id) {
        return ladybugManager.getLadybugById(id);
    }

    /**
     * Lists all ladybug ids.
     *
     * @return list of ids
     */
    public List<Integer> listLadybugsIds() {
        return ladybugManager.listLadybugsIds();
    }

    /**
     * Returns a snapshot of all ladybugs including position and direction.
     *
     * @return list of ladybug positions
     */
    public List<LadybugPosition> getLadybugList() {
        return ladybugManager.getLadybugList();
    }

    // === Pathfinding operations (delegation) ===

    /**
     * Checks if a path exists from the given ladybug's current position to the target cell.
     *
     * @param ladybug start ladybug
     * @param x       target x (1-based)
     * @param y       target y (1-based)
     * @return {@code true} if a path exists; otherwise {@code false}
     */
    public boolean existsPath(Ladybug ladybug, int x, int y) {
        return pathFinder.existsPath(ladybug, x, y);
    }

    /**
     * Checks if a path exists between two cells.
     *
     * @param x1 start x (1-based)
     * @param y1 start y (1-based)
     * @param x2 target x (1-based)
     * @param y2 target y (1-based)
     * @return {@code true} if a path exists; otherwise {@code false}
     */
    public boolean existsPath(int x1, int y1, int x2, int y2) {
        return pathFinder.existsPath(x1, y1, x2, y2);
    }

    // === Conditions ===

    /**
     * Checks whether the given ladybug is on any edge of the board.
     *
     * @param ladybug ladybug to check
     * @return {@code true} if on an edge; otherwise {@code false}
     */
    public boolean atEdge(Ladybug ladybug) {
        Position pos = ladybug.getPosition();
        int x = pos.x();
        int y = pos.y();

        // at any border cell
        return x == 1 || x == grid.getWidth() || y == 1 || y == grid.getHeight();
    }

    /**
     * Checks whether there is a tree ('#') in front of the ladybug.
     *
     * @param ladybug ladybug
     * @return {@code true} if a tree is directly ahead; otherwise {@code false}
     */
    public boolean treeFront(Ladybug ladybug) {
        Position front = getFrontPosition(ladybug);
        return front != null && getCell(front) == '#';
    }

    /**
     * Checks whether there is a leaf ('*') in front of the ladybug.
     *
     * @param ladybug ladybug
     * @return {@code true} if a leaf is directly ahead; otherwise {@code false}
     */
    public boolean leafFront(Ladybug ladybug) {
        Position front = getFrontPosition(ladybug);
        return front != null && getCell(front) == '*';
    }

    /**
     * Checks whether there is a mushroom ('o') in front of the ladybug.
     *
     * @param ladybug ladybug
     * @return {@code true} if a mushroom is directly ahead; otherwise {@code false}
     */
    public boolean mushroomFront(Ladybug ladybug) {
        Position front = getFrontPosition(ladybug);
        return front != null && getCell(front) == 'o';
    }

    // === Actions ===

    /**
     * Turns the ladybug to the left.
     *
     * @param ladybug ladybug
     * @return always {@code true}
     */
    public boolean turnLeft(Ladybug ladybug) {
        Direction newDir = ladybug.getDirection().turnLeft();
        ladybugManager.setLadybugDirection(ladybug, newDir);
        return true;
    }

    /**
     * Turns the ladybug to the right.
     *
     * @param ladybug ladybug
     * @return always {@code true}
     */
    public boolean turnRight(Ladybug ladybug) {
        Direction newDir = ladybug.getDirection().turnRight();
        ladybugManager.setLadybugDirection(ladybug, newDir);
        return true;
    }

    /**
     * Places a leaf ('*') on the cell in front of the ladybug if it is empty ('.').
     *
     * @param ladybug ladybug
     * @return {@code true} if the leaf was placed; otherwise {@code false}
     */
    public boolean placeLeaf(Ladybug ladybug) {
        Position front = getFrontPosition(ladybug);
        if (front == null) {
            return false;
        }
        char c = getCell(front);
        if (c != '.') {
            return false;
        }
        setCell(front, '*');
        return true;
    }

    /**
     * Removes a leaf ('*') from the cell in front of the ladybug and empties it ('.').
     *
     * @param ladybug ladybug
     * @return {@code true} if a leaf was removed; otherwise {@code false}
     */
    public boolean takeLeaf(Ladybug ladybug) {
        Position front = getFrontPosition(ladybug);
        if (front == null) {
            return false;
        }
        if (getCell(front) != '*') {
            return false;
        }
        setCell(front, '.');
        return true;
    }

    /**
     * Moves the ladybug one cell forward. Mushrooms ('o') are pushed if possible;
     * trees ('#') block movement.
     *
     * @param ladybug ladybug
     * @return {@code true} if movement was performed; otherwise {@code false}
     */
    public boolean moveForward(Ladybug ladybug) {
        Position front = getFrontPosition(ladybug);
        if (front == null) {
            return false;
        }
        if (treeFront(ladybug)) {
            return false;
        }
        char c = getCell(front);
        Direction direction = ladybug.getDirection();

        if (c == '.') {
            ladybugManager.moveLadybugToEmpty(ladybug, front, direction);
            return true;
        }

        if (c == 'o') {
            Position behind = calculateBehindPosition(front, direction);
            if (behind == null || getCell(behind) != '.') {
                return false;
            }
            setCell(behind, 'o');
            ladybugManager.moveLadybugToMushroom(ladybug, front, direction);
            return true;
        }

        return false;
    }

    /**
     * Lets the ladybug fly to a free target cell and orients it according to the
     * flight direction.
     *
     * @param ladybug ladybug
     * @param target  target position (must be valid and empty)
     * @return {@code true} if the flight succeeded; otherwise {@code false}
     */
    public boolean flyTo(Ladybug ladybug, Position target) {
        if (!grid.isValidPosition(target) || getCell(target) != '.') {
            return false;
        }

        Position current = ladybug.getPosition();
        int dx = Integer.compare(target.x() - current.x(), 0);
        int dy = Integer.compare(target.y() - current.y(), 0);
        Direction newDir = Direction.fromDelta(dx, dy);

        setCell(current, '.');
        ladybug.setPosition(target);
        ladybug.setDirection(newDir);
        setCell(target, newDir.toSymbol());
        return true;
    }

    // === Helper methods ===

    /**
     * Checks whether coordinates are within the grid bounds.
     *
     * @param x x-coordinate (1-based)
     * @param y y-coordinate (1-based)
     * @return {@code true} if valid; otherwise {@code false}
     */
    private boolean isValidCoordinate(int x, int y) {
        return x >= 1 && x <= grid.getWidth() && y >= 1 && y <= grid.getHeight();
    }

    /**
     * Computes the position directly in front of the ladybug.
     *
     * @param ladybug ladybug
     * @return front position or {@code null} if outside the grid
     */
    private Position getFrontPosition(Ladybug ladybug) {
        Direction dir = ladybug.getDirection();
        Position pos = ladybug.getPosition();

        int newX = pos.x() + dir.getDx();
        int newY = pos.y() + dir.getDy();

        if (!isValidCoordinate(newX, newY)) {
            return null;
        }

        return new Position(newX, newY);
    }

    /**
     * Computes the cell behind a given front position in the same direction,
     * if that cell is valid.
     *
     * @param front     cell in front of the ladybug
     * @param direction facing direction
     * @return valid position behind or {@code null} if invalid
     */
    private Position calculateBehindPosition(Position front, Direction direction) {
        int behindX = front.x() + direction.getDx();
        int behindY = front.y() + direction.getDy();

        Position behind = new Position(behindX, behindY);
        return grid.isValidPosition(behind) ? behind : null;
    }
}
