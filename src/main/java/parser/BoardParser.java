package parser;

import exceptions.BoardException;
import exceptions.LadybugException;
import model.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Utility class for parsing board files with intelligent terrain/ladybug separation.
 * <p>
 * This parser reads text files containing mixed board layouts and automatically
 * separates terrain symbols from ladybug positions. The result is a clean terrain
 * grid and a registry of ladybug positions, enabling the new overlay-based rendering.
 * </p>
 *
 * @author ujnaa
 */
public final class BoardParser {
    private static final Set<Character> VALID_SYMBOLS = Set.of('.', '#', '*', 'o', '^', '>', 'v', '<');

    private BoardParser() {
        // Utility class - no instantiation
    }

    /**
     * Result of parsing a board file, containing separated terrain and ladybug data.
     * <p>
     * This class encapsulates the parsing result, making it easy to access both
     * the clean terrain grid and the extracted ladybug positions.
     * </p>
     */
    public static class ParseResult {
        private final char[][] terrainGrid;
        private final LadybugPositionRegistry ladybugRegistry;

        /**
         * Creates a parse result with terrain and ladybug data.
         *
         * @param terrainGrid clean terrain grid (no ladybug symbols)
         * @param ladybugRegistry registry containing extracted ladybug positions
         */
        public ParseResult(char[][] terrainGrid, LadybugPositionRegistry ladybugRegistry) {
            this.terrainGrid = terrainGrid;
            this.ladybugRegistry = ladybugRegistry;
        }

        /**
         * Gets the terrain grid containing only static elements.
         *
         * @return 2D char array with terrain symbols only
         */
        public char[][] getTerrainGrid() {
            return terrainGrid;
        }

        /**
         * Gets the ladybug registry containing extracted positions.
         *
         * @return registry with ladybug positions in sorted order
         */
        public LadybugPositionRegistry getLadybugRegistry() {
            return ladybugRegistry;
        }

        /**
         * Gets the number of ladybugs found during parsing.
         *
         * @return count of extracted ladybugs
         */
        public int getLadybugCount() {
            return ladybugRegistry.getCount();
        }

        @Override
        public String toString() {
            return String.format("ParseResult[%dx%d terrain, %d ladybugs]",
                    terrainGrid[0].length, terrainGrid.length, getLadybugCount());
        }
    }

    /**
     * Parses a board file and creates separated terrain and ladybug data.
     * <p>
     * This is the main parsing method that:
     * 1. Reads and validates the file
     * 2. Extracts ladybug positions into a registry
     * 3. Replaces ladybug symbols with empty terrain ('.')
     * 4. Returns clean separated data
     * </p>
     *
     * @param path the path to the board file to parse
     * @return ParseResult containing clean terrain grid and ladybug registry
     * @throws IOException if an I/O error occurs while reading the file
     * @throws BoardException if the file format is invalid
     * @throws LadybugException if ladybug data is invalid
     */
    public static ParseResult parseFile(String path) throws IOException, BoardException, LadybugException {
        validatePath(path);

        String content = readAndDisplayFile(path);
        List<String> lines = preprocessLines(content);
        validateBoardDimensions(lines);

        return extractTerrainAndLadybugs(lines);
    }

    /**
     * Parses a board file and creates a Board object (legacy compatibility method).
     * <p>
     * This method maintains backward compatibility while using the new separation logic.
     * It automatically creates a Board with the extracted terrain and ladybug registry.
     * </p>
     *
     * @param path the path to the board file to parse
     * @return new Board object with separated terrain and ladybug data
     * @throws IOException if an I/O error occurs while reading the file
     * @throws BoardException if board creation fails
     * @throws LadybugException if ladybug initialization fails
     */
    public static Board parse(String path) throws IOException, BoardException, LadybugException {
        ParseResult result = parseFile(path);
        return new Board(result.getTerrainGrid(), result.getLadybugRegistry());
    }

    // === Private helper methods ===

    /**
     * Validates that the file path is valid and the file exists.
     */
    private static void validatePath(String path) throws BoardException {
        if (path == null || path.trim().isEmpty()) {
            throw new BoardException("Error, file path cannot be null or empty");
        }
        if (!Files.exists(Paths.get(path))) {
            throw new BoardException("Error, file does not exist: " + path);
        }
    }

    /**
     * Reads the file and displays its content (as required by specification).
     */
    private static String readAndDisplayFile(String path) throws IOException {
        String content = Files.readString(Paths.get(path));
        System.out.println(content); // Required: verbatim output
        return content;
    }

    /**
     * Preprocesses file content into clean lines.
     */
    private static List<String> preprocessLines(String content) {
        String[] lineArray = content.replace("\r\n", "\n")
                .replace("\r", "\n").split("\n");

        List<String> lines = new ArrayList<>();
        for (String line : lineArray) {
            lines.add(line);
        }
        return lines;
    }

    /**
     * Validates board dimensions and basic format.
     */
    private static void validateBoardDimensions(List<String> lines) throws BoardException {
        if (lines.isEmpty()) {
            throw new BoardException("Error, file is empty");
        }

        int width = lines.get(0).length();
        if (width == 0) {
            throw new BoardException("Error, board has zero width");
        }

        // Check that all lines have the same width (rectangular)
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).length() != width) {
                throw new BoardException("Error, board must be rectangular");
            }
        }
    }

    /**
     * The core extraction logic: separates terrain from ladybugs.
     * <p>
     * This method implements the smart separation:
     * 1. Scan each character in the grid
     * 2. If it's a ladybug symbol (^>v<): extract position and replace with '.'
     * 3. If it's terrain: keep as-is
     * 4. Build clean terrain grid and populated registry
     * </p>
     */
    private static ParseResult extractTerrainAndLadybugs(List<String> lines) throws BoardException {
        int height = lines.size();
        int width = lines.get(0).length();

        char[][] terrainGrid = new char[height][width];
        LadybugPositionRegistry ladybugRegistry = new LadybugPositionRegistry();

        for (int y = 0; y < height; y++) {
            String line = lines.get(y);

            for (int x = 0; x < width; x++) {
                char c = line.charAt(x);

                // Validate character
                if (!VALID_SYMBOLS.contains(c)) {
                    throw new BoardException("Error, invalid character: " + c);
                }

                if (BoardGrid.isLadybugSymbol(c)) {
                    // Extract ladybug: get position and direction
                    Direction direction = Direction.fromSymbol(c);
                    Position position = new Position(x + 1, y + 1); // Convert to 1-based coordinates
                    ladybugRegistry.addPosition(position, direction);

                    // Replace with empty terrain in the grid
                    terrainGrid[y][x] = '.';

                } else {
                    // Regular terrain symbol: copy as-is
                    terrainGrid[y][x] = c;
                }
            }
        }

        return new ParseResult(terrainGrid, ladybugRegistry);
    }

    /**
     * Creates a board from raw grid data with automatic extraction (for testing).
     * <p>
     * This utility method is useful for unit tests that want to create boards
     * from inline grid data without file I/O.
     * </p>
     *
     * @param gridLines array of strings representing the board layout
     * @return Board object with separated terrain and ladybug data
     * @throws BoardException if the grid format is invalid
     * @throws LadybugException if ladybug initialization fails
     */
    public static Board createBoardFromLines(String... gridLines) throws BoardException, LadybugException {
        List<String> lines = List.of(gridLines);
        validateBoardDimensions(lines);

        ParseResult result = extractTerrainAndLadybugs(lines);
        return new Board(result.getTerrainGrid(), result.getLadybugRegistry());
    }

    /**
     * Parses grid data and returns the separation result (for testing/analysis).
     * <p>
     * This method allows external code to see exactly how the separation works
     * without creating a full Board object.
     * </p>
     *
     * @param gridLines array of strings representing the board layout
     * @return ParseResult showing the separated terrain and ladybug data
     * @throws BoardException if the grid format is invalid
     */
    public static ParseResult analyzeGridLines(String... gridLines) throws BoardException {
        List<String> lines = List.of(gridLines);
        validateBoardDimensions(lines);
        return extractTerrainAndLadybugs(lines);
    }
}