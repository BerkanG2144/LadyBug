package model;

import java.util.ArrayDeque;

/**
 * Provides pathfinding utilities for navigating the board.
 * <p>
 * The {@code PathFinder} uses the grid from {@link BoardGrid} to check
 * valid moves and compute the shortest paths between positions. It supports
 * movement based on valid cells and avoids obstacles such as walls.
 * </p>
 *
 * <h2>Main responsibilities:</h2>
 * <ul>
 *   <li>Validate if a position can be moved into.</li>
 *   <li>Find reachable neighbors for a given position.</li>
 *   <li>Compute the shortest paths (e.g., using BFS) between two positions.</li>
 * </ul>
 *
 * <p>Instances of this class are tied to a single {@link BoardGrid} and
 * will update automatically when the grid changes.</p>
 *
 * @author ujnaa
 */
public class PathFinder {
    private static final int[] DX = { 0,  0, -1, 1};
    private static final int[] DY = {-1,  1,  0, 0};
    private final BoardGrid grid;

    /**
     * Creates a new {@code PathFinder} for the given grid.
     *
     * @param grid the board grid to operate on, must not be {@code null}
     */
    public PathFinder(BoardGrid grid) {
        this.grid = grid;
    }

    /**
     * Checks if a path exists from ladybug position to target coordinates.
     * @param ladybug the starting ladybug
     * @param targetX target X coordinate
     * @param targetY target Y coordinate
     * @return true if path exists
     */
    public boolean existsPath(Ladybug ladybug, int targetX, int targetY) {
        Position start = ladybug.getPosition();
        Position end = new Position(targetX, targetY);

        if (!grid.isValidPosition(end)) {
            return false;
        }
        // Target must be empty
        if (grid.getCell(end) != '.') {
            return false;
        }

        return hasPath(start, end, true);
    }

    /**
     * Checks if a path exists between two coordinates.
     * @param x1 start X coordinate
     * @param y1 start Y coordinate
     * @param x2 end X coordinate
     * @param y2 end Y coordinate
     * @return true if path exists
     */
    public boolean existsPath(int x1, int y1, int x2, int y2) {
        Position start = new Position(x1, y1);
        Position end = new Position(x2, y2);

        if (!grid.isValidPosition(start) || !grid.isValidPosition(end)) {
            return false;
        }
        // Both endpoints must be empty for path between coordinates
        if (grid.getCell(start) != '.' || grid.getCell(end) != '.') {
            return false;
        }

        return hasPath(start, end, false);
    }

    /**
     * Performs BFS to find if a path exists between two positions.
     * @param start starting position
     * @param end ending position
     * @param allowNonEmptyStart whether start position can be non-empty
     * @return true if path exists
     */
    private boolean hasPath(Position start, Position end, boolean allowNonEmptyStart) {
        // End goal must always be empty
        if (grid.getCell(end) != '.') {
            return false;
        }

        // If start equals end, path exists if end is empty (checked above)
        if (start.equals(end)) {
            return true;
        }

        // Check if start must be empty (for between-coordinates case)
        if (!allowNonEmptyStart && grid.getCell(start) != '.') {
            return false;
        }

        // BFS over empty cells only
        boolean[][] visited = new boolean[grid.getHeight()][grid.getWidth()];
        ArrayDeque<Position> queue = new ArrayDeque<>();
        queue.add(start);
        visited[start.y() - 1][start.x() - 1] = true;

        while (!queue.isEmpty()) {
            Position current = queue.pollFirst();

            // Check all 4 directions
            for (int i = 0; i < 4; i++) {
                int newX = current.x() + DX[i];
                int newY = current.y() + DY[i];

                // Check bounds
                if (newX < 1 || newX > grid.getWidth() || newY < 1 || newY > grid.getHeight()) {
                    continue;
                }

                // Check if already visited
                if (visited[newY - 1][newX - 1]) {
                    continue;
                }

                Position next = new Position(newX, newY);

                // Only move through empty cells
                if (grid.getCell(next) != '.') {
                    continue;
                }

                // Found target
                if (next.equals(end)) {
                    return true;
                }

                visited[newY - 1][newX - 1] = true;
                queue.addLast(next);
            }
        }

        return false;
    }
}