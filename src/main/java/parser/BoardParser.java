package parser;

import model.Board;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

public class BoardParser {

    private static final Set<Character> VALID_SYMBOLS = Set.of('.', '#', '*', 'o', '^', '>', 'v', '<');

    public Board parse (String path) throws IOException {
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
