package model;

/**
 * Represents a two-dimensional grid for the board.
 * <p>
 * The grid consists of valid symbols that represent different objects
 * or states on the board. This class provides methods for validating,
 * accessing, modifying, and printing the grid.
 * </p>
 *
 * Valid symbols include:
 * <ul>
 *   <li>{@code '#'} - Wall</li>
 *   <li>{@code '*'} - Flower</li>
 *   <li>{@code 'o'} - Leaf</li>
 *   <li>{@code '.'} - Empty space</li>
 *   <li>{@code '^'} - Ladybug facing up</li>
 *   <li>{@code '>'} - Ladybug facing right</li>
 *   <li>{@code 'v'} - Ladybug facing down</li>
 *   <li>{@code '<'} - Ladybug facing left</li>
 * </ul>
 *
 * The grid is immutable size (width and height) after creation,
 * but cell contents can be modified using {@link #setCell(Position, char)}.
 *
 * @author ujnaa
 */
public class BoardGrid {
    private static final char[] VALID_SYMBOLS = { '#', '*', 'o', '.', '^', '>', 'v', '<' };
    private final char[][] grid;
    private final int width;
    private final int height;

    /**
     * Creates a new {@code BoardGrid} with the given character matrix.
     *
     * @param grid the initial grid data, must not be {@code null},
     *             must be rectangular, and must contain only valid symbols
     * @throws IllegalArgumentException if the grid is {@code null}, empty,
     *                                  not rectangular, or contains invalid symbols
     */
    public BoardGrid(char[][] grid) {
        validateGrid(grid);
        this.height = grid.length;
        this.width = grid[0].length;
        this.grid = copyGrid(grid);
    }

    /**
     * Returns the symbol at the specified position.
     *
     * @param pos the position to query, must not be {@code null}
     * @return the symbol at the given position
     * @throws IllegalArgumentException if the position is invalid
     */
    public char getCell(Position pos) {
        if (!isValidPosition(pos)) {
            throw new IllegalArgumentException("Error, invalid position: " + pos);
        }
        return grid[pos.y() - 1][pos.x() - 1];
    }

    /**
     * Updates the symbol at the specified position.
     *
     * @param pos    the position to update, must not be {@code null}
     * @param symbol the new symbol, must be a valid board symbol
     * @throws IllegalArgumentException if the position or symbol is invalid
     */
    public void setCell(Position pos, char symbol) {
        if (!isValidPosition(pos)) {
            throw new IllegalArgumentException("Error, invalid position: " + pos);
        }
        if (!isValidSymbol(symbol)) {
            throw new IllegalArgumentException("Error, invalid symbol: " + symbol);
        }
        grid[pos.y() - 1][pos.x() - 1] = symbol;
    }

    /**
     * Checks if the given position is within the grid boundaries.
     *
     * @param pos the position to check
     * @return {@code true} if the position is valid, {@code false} otherwise
     */
    public boolean isValidPosition(Position pos) {
        return pos != null && pos.x() >= 1 && pos.x() <= width && pos.y() >= 1 && pos.y() <= height;
    }

    private boolean isValidSymbol(char character) {
        for (char valid : VALID_SYMBOLS) {
            if (character == valid) {
                return true;
            }
        }
        return false;
    }

    /**
     * Prints the grid to the console with a border.
     */
    public void print() {
        StringBuilder border = new StringBuilder("+");
        border.append("-".repeat(width)).append("+");
        System.out.println(border);
        for (char[] row : grid) {
            System.out.print("|");
            for (char c : row) {
                System.out.print(c);
            }
            System.out.println("|");
        }
        System.out.println(border);
    }

    private void validateGrid(char[][] grid) {
        if (grid == null || grid.length == 0 || grid[0] == null || grid[0].length == 0) {
            throw new IllegalArgumentException("Error, grid cannot be null or empty");
        }

        int expectedWidth = grid[0].length;
        for (int y = 0; y < grid.length; y++) {
            if (grid[y].length != expectedWidth) {
                throw new IllegalArgumentException("Error, grid must be rectangular");
            }
            for (int x = 0; x < expectedWidth; x++) {
                if (!isValidSymbol(grid[y][x])) {
                    throw new IllegalArgumentException("Error, invalid symbol: " + grid[y][x]);
                }
            }
        }
    }

    private char[][] copyGrid(char[][] original) {
        char[][] copy = new char[original.length][];
        for (int i = 0; i < original.length; i++) {
            copy[i] = original[i].clone();
        }
        return copy;
    }

    /**
     * Gets the grid width.
     * @return the width
     */
    public int getWidth() {
        return width;
    }

    /**
     * Gets the grid height.
     * @return the height
     */
    public int getHeight() {
        return height;
    }
}

