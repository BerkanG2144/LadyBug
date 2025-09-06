package model;

import java.util.List;
import java.util.Optional;

public class Board {
    private final BoardGrid grid;
    private final LadybugManager ladybugManager;
    private final PathFinder pathFinder;

    public Board(char[][] gridData) {
        this.grid = new BoardGrid(gridData);
        this.ladybugManager = new LadybugManager(grid);
        this.pathFinder = new PathFinder(grid);
    }

    // === Component access methods (für GameState) ===
    public BoardGrid getGrid() {
        return grid;
    }

    public LadybugManager getLadybugManager() {
        return ladybugManager;
    }

    public PathFinder getPathFinder() {
        return pathFinder;
    }

    // === Grid operations ===
    public char getCell(Position pos) {
        return grid.getCell(pos);
    }

    public void setCell(Position pos, char symbol) {
        grid.setCell(pos, symbol);
    }

    public void print() {
        grid.print();
    }

    // === Ladybug operations (Delegation) ===
    public void addLadybug(Ladybug ladybug) {
        ladybugManager.addLadybug(ladybug);
    }

    public Optional<Ladybug> getLadybugById(int id) {
        return ladybugManager.getLadybugById(id);
    }

    public List<Integer> listLadybugsIds() {
        return ladybugManager.listLadybugsIds();
    }

    public List<LadybugPosition> getLadybugList() {
        return ladybugManager.getLadybugList();
    }

    // === Pathfinding operations (Delegation) ===
    public boolean existsPath(Ladybug ladybug, int x, int y) {
        return pathFinder.existsPath(ladybug, x, y);
    }

    public boolean existsPath(int x1, int y1, int x2, int y2) {
        return pathFinder.existsPath(x1, y1, x2, y2);
    }

    // === Conditions ===
    public boolean atEdge(Ladybug ladybug) {
        Position pos = ladybug.getPosition();
        int x = pos.x();
        int y = pos.y();

        // Prüfe ob Marienkäfer an irgendeinem Rand steht
        return x == 1 || x == grid.getWidth() || y == 1 || y == grid.getHeight();
    }
    public boolean treeFront(Ladybug ladybug) {
        Position front = getFrontPosition(ladybug);
        return front != null && getCell(front) == '#';
    }

    public boolean leafFront(Ladybug ladybug) {
        Position front = getFrontPosition(ladybug);
        return front != null && getCell(front) == '*';
    }

    public boolean mushroomFront(Ladybug ladybug) {
        Position front = getFrontPosition(ladybug);
        return front != null && getCell(front) == 'o';
    }

    // === Actions ===
    public boolean turnLeft(Ladybug ladybug) {
        Direction newDir = ladybug.getDirection().turnLeft();
        ladybugManager.setLadybugDirection(ladybug, newDir);
        return true;
    }

    public boolean turnRight(Ladybug ladybug) {
        Direction newDir = ladybug.getDirection().turnRight();
        ladybugManager.setLadybugDirection(ladybug, newDir);
        return true;
    }

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
    private boolean isValidCoordinate(int x, int y) {
        return x >= 1 && x <= grid.getWidth() && y >= 1 && y <= grid.getHeight();
    }

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

    private Position calculateBehindPosition(Position front, Direction direction) {
        int behindX = front.x() + direction.getDx();
        int behindY = front.y() + direction.getDy();

        Position behind = new Position(behindX, behindY);
        return grid.isValidPosition(behind) ? behind : null;
    }

    // === Backwards compatibility ===
    @Deprecated
    public List<LadybugPosition> getLadybugsFromGrid() {
        return getLadybugList();
    }
}