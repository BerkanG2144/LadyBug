package model;

import java.util.Optional;

/**
 * Renders the board with terrain and ladybugs.
 * @author ujnaa
 */
public final class BoardRenderer {
    private final Board board;

    /**
     * Creates a new renderer bound to the given {@link Board}.
     *
     * @param board the board to render; must not be {@code null}
     */
    public BoardRenderer(Board board) {
        this.board = board;
    }

    /**
     * Prints the current board state with ladybugs rendered as overlays on terrain.
     * <p>
     * This method combines the static terrain with dynamic ladybug positions,
     * showing ladybug direction symbols over the terrain where they are located.
     * </p>
     */
    public void print() {
        BoardGrid grid = board.getGrid();
        StringBuilder border = new StringBuilder("+");
        border.append("-".repeat(grid.getWidth())).append("+");
        System.out.println(border);

        for (int y = 1; y <= grid.getHeight(); y++) {
            System.out.print("|");
            for (int x = 1; x <= grid.getWidth(); x++) {
                Position pos = new Position(x, y);
                System.out.print(displayCharAt(pos));
            }
            System.out.println("|");
        }
        System.out.println(border);
    }

    /**
     * Gets the display character at a position, with ladybugs rendered as overlay.
     * <p>
     * This method implements the core overlay logic:
     * 1. Check if there's a ladybug at this position
     * 2. If yes, show the ladybug's direction symbol
     * 3. If no, show the underlying terrain
     * </p>
     *
     * @param pos the position to check
     * @return the character to display (terrain or ladybug symbol)
     */
    private char displayCharAt(Position pos) {
        Optional<Ladybug> ladybugOpt = board.getLadybugManager().findLadybugAt(pos);
        return ladybugOpt.map(lb -> lb.getDirection().toSymbol())
                .orElse(board.getCell(pos));
    }
}
