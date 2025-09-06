package parser;

import model.Board;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

/**
 * Utility class for parsing board files into Board objects.
 *
 * This parser reads text files containing board layouts and validates
 * the format and symbols. The board must be rectangular and contain
 * only valid symbols representing empty spaces, trees, leaves, mushrooms,
 * and ladybugs with their directions.
 *
 * @author ujnaa
 */
public final class BoardParser {
    private static final Set<Character> VALID_SYMBOLS = Set.of('.', '#', '*', 'o', '^', '>', 'v', '<');

    private BoardParser() {
        //
    }


    /**
     * Parses a board file and creates a Board object.
     *
     * Reads the specified file, validates its format and content, then creates
     * a Board object from the parsed data. The file must contain a rectangular
     * grid of valid symbols. The content is also printed to standard output
     * for verification purposes.
     *
     * @param path the path to the board file to parse
     * @return a new Board object created from the file content
     * @throws IOException if an I/O error occurs while reading the file
     * @throws IllegalArgumentException if the path is null/empty, the file doesn't exist,
     *         the file is empty, the board has zero width, the board is not rectangular,
     *         or the file contains invalid characters
     */
    public static Board parse(String path) throws IOException {
        if (path == null || path.trim().isEmpty()) {
            throw new IllegalArgumentException("Error, file path cannot be null or empty");
        }
        if (!Files.exists(Paths.get(path))) {
            throw new IllegalArgumentException("Error, file does not exist: " + path);
        }

        String content = Files.readString(Paths.get(path));
        System.out.println(content);

        List<String> lines = content.replace("\r\n", "\n")
                .replace("\r", "\n").lines().toList();

        if (lines.isEmpty()) {
            throw new IllegalArgumentException("Error, file is empty");
        }
        int width = lines.get(0).length();
        if (width == 0) {
            throw new IllegalArgumentException("Error, board has zero width");
        }

        char[][] grid = new char[lines.size()][width];
        for (int y = 0; y < lines.size(); y++) {
            String line = lines.get(y);
            if (line.length() != width) {
                throw new IllegalArgumentException("Error, board must be rectangular");
            }
            for (int x = 0; x < width; x++) {
                char c = line.charAt(x);
                if (!VALID_SYMBOLS.contains(c)) {
                    throw new IllegalArgumentException("Error, invalid character: " + c);
                }
                grid[y][x] = c;
            }
        }
        return new Board(grid);
    }
}
