package model;

public class BoardGrid {
    private static final char[] VALID_SYMBOLS = { '#', '*', 'o', '.', '^', '>', 'v', '<' };
    private final char[][] grid;
    private final int width;
    private final int height;

    public BoardGrid(char[][] grid) {
        validateGrid(grid);
        this.height = grid.length;
        this.width = grid[0].length;
        this.grid = copyGrid(grid);
    }

    public char getCell(Position pos) {
        if (!isValidPosition(pos)) {
            throw new IllegalArgumentException("Error, invalid position: " + pos);
        }
        return grid[pos.y() - 1][pos.x() - 1];
    }

    public void setCell(Position pos, char symbol) {
        if (!isValidPosition(pos)) {
            throw new IllegalArgumentException("Error, invalid position: " + pos);
        }
        if (!isValidSymbol(symbol)) {
            throw new IllegalArgumentException("Error, invalid symbol: " + symbol);
        }
        grid[pos.y() - 1][pos.x() - 1] = symbol;
    }

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

