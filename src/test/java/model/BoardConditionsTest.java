package model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class BoardConditionsTest {

    // ---- Helpers ----
    private static char[][] gridFrom(String... rows) {
        int h = rows.length, w = rows[0].length();
        char[][] g = new char[h][w];
        for (int y = 0; y < h; y++) {
            assertEquals(w, rows[y].length(), "Grid must be rectangular in test data");
            g[y] = rows[y].toCharArray();
        }
        return g;
    }

    private static Board boardFrom(String... rows) {
        return new Board(gridFrom(rows));
    }

    private static Ladybug spawnLadybug(Board board, int id, int x, int y, Direction dir) {
        Ladybug lb = new Ladybug(id, new Position(x, y), dir);
        board.addLadybug(lb);
        return lb;
    }

    // ---- Tests ----

    @Test
    @DisplayName("atEdge: true wenn Front außerhalb (oben an Kante, Blick nach oben)")
    void atEdge_true_topBoundary() {
        Board board = boardFrom(
                ".....",
                ".....",
                "....."
        );
        Ladybug lb = spawnLadybug(board, 1, 3, 1, Direction.UP); // y=1, Blick nach oben -> raus
        assertTrue(board.atEdge(lb));
        assertFalse(board.treeFront(lb));
        assertFalse(board.leafFront(lb));
        assertFalse(board.mushroomFront(lb));
    }

    @Test
    @DisplayName("atEdge: false wenn Front im Feld")
    void atEdge_false_inside() {
        Board board = boardFrom(
                ".....",
                ".....",
                "....."
        );
        Ladybug lb = spawnLadybug(board, 1, 2, 2, Direction.RIGHT);
        assertFalse(board.atEdge(lb));
    }

    @Test
    @DisplayName("treeFront: true wenn direkt vor der Ladybug ein Baum steht (#)")
    void treeFront_true() {
        Board board = boardFrom(
                "..#..",
                ".....",
                "....."
        );
        // Ladybug links vom Baum, schaut nach rechts
        Ladybug lb = spawnLadybug(board, 1, 2, 1, Direction.RIGHT);
        assertFalse(board.atEdge(lb));
        assertTrue(board.treeFront(lb));
        assertFalse(board.leafFront(lb));
        assertFalse(board.mushroomFront(lb));
    }

    @Test
    @DisplayName("leafFront: true wenn direkt vor der Ladybug ein Blatt liegt (*)")
    void leafFront_true() {
        Board board = boardFrom(
                ".....",
                ".*...",
                "....."
        );
        // Ladybug links vom Blatt, schaut nach rechts
        Ladybug lb = spawnLadybug(board, 1, 1, 2, Direction.RIGHT);
        assertFalse(board.atEdge(lb));
        assertTrue(board.leafFront(lb));
        assertFalse(board.treeFront(lb));
        assertFalse(board.mushroomFront(lb));
    }

    @Test
    @DisplayName("mushroomFront: true wenn direkt vor der Ladybug ein Pilz liegt (o)")
    void mushroomFront_true() {
        Board board = boardFrom(
                ".....",
                ".o...",
                "....."
        );
        // Ladybug links vom Pilz, schaut nach rechts
        Ladybug lb = spawnLadybug(board, 1, 1, 2, Direction.RIGHT);
        assertFalse(board.atEdge(lb));
        assertTrue(board.mushroomFront(lb));
        assertFalse(board.treeFront(lb));
        assertFalse(board.leafFront(lb));
    }

    @Test
    @DisplayName("Alle false, wenn vor der Ladybug ein freies Feld ist (.)")
    void allConditions_false_onEmptyFront() {
        Board board = boardFrom(
                ".....",
                ".....",
                "....."
        );
        Ladybug lb = spawnLadybug(board, 1, 2, 2, Direction.LEFT);
        // Links ist ein '.' innerhalb des Boards
        assertFalse(board.atEdge(lb));
        assertFalse(board.treeFront(lb));
        assertFalse(board.leafFront(lb));
        assertFalse(board.mushroomFront(lb));
    }

    @Test
    @DisplayName("atEdge: true an rechter Kante (Blick nach rechts)")
    void atEdge_true_rightBoundary() {
        Board board = boardFrom(
                ".....",
                ".....",
                "....."
        );
        Ladybug lb = spawnLadybug(board, 1, 5, 2, Direction.RIGHT); // x=5 am rechten Rand
        assertTrue(board.atEdge(lb));
    }

    @Test
    @DisplayName("Robustheit: Objekte vor der Ladybug werden korrekt unterschieden")
    void frontObject_disambiguation() {
        Board board = boardFrom(
                "...#.",
                "...*.",
                "...o.",
                "....."
        );
        // Reihe 1: Baum vor LB
        Ladybug lb1 = spawnLadybug(board, 1, 3, 1, Direction.RIGHT);
        assertTrue(board.treeFront(lb1));
        assertFalse(board.leafFront(lb1));
        assertFalse(board.mushroomFront(lb1));

        // Reihe 2: Blatt vor LB
        Ladybug lb2 = spawnLadybug(board, 2, 3, 2, Direction.RIGHT);
        assertTrue(board.leafFront(lb2));
        assertFalse(board.treeFront(lb2));
        assertFalse(board.mushroomFront(lb2));

        // Reihe 3: Pilz vor LB
        Ladybug lb3 = spawnLadybug(board, 3, 3, 3, Direction.RIGHT);
        assertTrue(board.mushroomFront(lb3));
        assertFalse(board.treeFront(lb3));
        assertFalse(board.leafFront(lb3));
    }
}
