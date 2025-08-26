package model;

import org.junit.jupiter.api.*;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class BoardConditionsActionsTest {

    // ---------- kleine Helfer ----------

    private static char[][] rows(String... r) {
        int h = r.length, w = r[0].length();
        char[][] g = new char[h][w];
        for (int y = 0; y < h; y++) g[y] = r[y].toCharArray();
        return g;
    }

    private static Board board(String... r) {
        return new Board(rows(r));
    }

    private static Ladybug addLB(Board b, int id, int x, int y, Direction dir) {
        Ladybug lb = new Ladybug(id, new Position(x, y), dir);
        b.addLadybug(lb);
        return lb;
    }

    private static void set(Board b, int x, int y, char c) {
        b.setCell(new Position(x, y), c);
    }

    private static char get(Board b, int x, int y) {
        return b.getCell(new Position(x, y));
    }

    // Wir verwenden eine globale ID-Zählung für Ladybugs in den Tests
    private final AtomicInteger ids = new AtomicInteger(1);

    // ---------- CONDITIONS ----------

    @Nested
    class Conditions {

        @Test
        void atEdge_true_whenFrontIsOutside() {
            Board b = board(
                    "....",
                    "....",
                    "...."
            );
            // Käfer an x=1, Blick nach LINKS -> vor ihm ist x=0 (außerhalb)
            Ladybug lb = addLB(b, ids.getAndIncrement(), 1, 2, Direction.LEFT);

            assertTrue(b.atEdge(lb));
            assertFalse(b.treeFront(lb));
            assertFalse(b.leafFront(lb));
            assertFalse(b.mushroomFront(lb));
        }

        @Test
        void treeFront_true_whenHashInFront() {
            Board b = board(
                    "....",
                    ".#..",
                    "...."
            );
            Ladybug lb = addLB(b, ids.getAndIncrement(), 1, 2, Direction.RIGHT);
            assertTrue(b.treeFront(lb));
            assertFalse(b.leafFront(lb));
            assertFalse(b.mushroomFront(lb));
            assertFalse(b.atEdge(lb));
        }

        @Test
        void leafFront_true_whenStarInFront() {
            Board b = board(
                    "....",
                    ".*..",
                    "...."
            );
            Ladybug lb = addLB(b, ids.getAndIncrement(), 1, 2, Direction.RIGHT);
            assertTrue(b.leafFront(lb));
        }

        @Test
        void mushroomFront_true_whenMushroomInFront() {
            Board b = board(
                    "....",
                    ".o..",
                    "...."
            );
            Ladybug lb = addLB(b, ids.getAndIncrement(), 1, 2, Direction.RIGHT);
            assertTrue(b.mushroomFront(lb));
        }
    }

    // ---------- ACTIONS: turnLeft / turnRight ----------

    @Nested
    class Turning {

        @Test
        void turnLeft_updatesBoardSymbol() {
            Board b = board("...");
            Ladybug lb = addLB(b, ids.getAndIncrement(), 2, 1, Direction.RIGHT);

            assertTrue(b.turnLeft(lb)); // RIGHT -> UP
            assertEquals(Direction.UP.toSymbol(), get(b, 2, 1));

            assertTrue(b.turnLeft(lb)); // UP -> LEFT
            assertEquals(Direction.LEFT.toSymbol(), get(b, 2, 1));
        }

        @Test
        void turnRight_updatesBoardSymbol() {
            Board b = board("...");
            Ladybug lb = addLB(b, ids.getAndIncrement(), 2, 1, Direction.UP);

            assertTrue(b.turnRight(lb)); // UP -> RIGHT
            assertEquals(Direction.RIGHT.toSymbol(), get(b, 2, 1));

            assertTrue(b.turnRight(lb)); // RIGHT -> DOWN
            assertEquals(Direction.DOWN.toSymbol(), get(b, 2, 1));
        }
    }

    // ---------- ACTIONS: placeLeaf / takeLeaf ----------

    @Nested
    class LeafActions {

        @Test
        void placeLeaf_onEmpty_frontCellGetsStar() {
            Board b = board(
                    "....",
                    "...."
            );
            Ladybug lb = addLB(b, ids.getAndIncrement(), 2, 2, Direction.RIGHT);
            assertTrue(b.placeLeaf(lb));
            assertEquals('*', get(b, 3, 2));
        }

        @Test
        void placeLeaf_blocked_byTree_orMushroom_orEdge() {
            // Baum blockiert
            Board t = board("..#.");
            Ladybug lb1 = addLB(t, ids.getAndIncrement(), 2, 1, Direction.RIGHT);
            assertFalse(t.placeLeaf(lb1));

            // Pilz blockiert
            Board m = board("..o.");
            Ladybug lb2 = addLB(m, ids.getAndIncrement(), 2, 1, Direction.RIGHT);
            assertFalse(m.placeLeaf(lb2));

            // Rand blockiert
            Board e = board("....");
            Ladybug lb3 = addLB(e, ids.getAndIncrement(), 1, 1, Direction.LEFT);
            assertFalse(e.placeLeaf(lb3));
        }

        @Test
        void takeLeaf_whenPresent_removesIt() {
            Board b = board(
                    "....",
                    "...."
            );
            Ladybug lb = addLB(b, ids.getAndIncrement(), 2, 2, Direction.RIGHT);
            set(b, 3, 2, '*');

            assertTrue(b.takeLeaf(lb));
            assertEquals('.', get(b, 3, 2));
        }

        @Test
        void takeLeaf_whenAbsent_returnsFalse() {
            Board b = board("....");
            Ladybug lb = addLB(b, ids.getAndIncrement(), 2, 1, Direction.RIGHT);

            assertFalse(b.takeLeaf(lb));
        }
    }

    // ---------- ACTION: moveForward ----------

    @Nested
    class MoveForwardAction {

        @Test
        void moveForward_toEmpty_movesOneCell() {
            Board b = board("....");
            Ladybug lb = addLB(b, ids.getAndIncrement(), 2, 1, Direction.RIGHT);

            assertTrue(b.moveForward(lb));
            // (2,1) -> (3,1)
            assertEquals('.', get(b, 2, 1));
            assertEquals(Direction.RIGHT.toSymbol(), get(b, 3, 1));
        }

        @Test
        void moveForward_blocked_byTree_returnsFalse() {
            Board b = board("..#.");
            Ladybug lb = addLB(b, ids.getAndIncrement(), 2, 1, Direction.RIGHT);

            assertFalse(b.moveForward(lb));
            assertEquals(Direction.RIGHT.toSymbol(), get(b, 2, 1)); // bleibt stehen
        }

        @Test
        void moveForward_atEdge_returnsFalse() {
            Board b = board("....");
            Ladybug lb = addLB(b, ids.getAndIncrement(), 1, 1, Direction.LEFT);

            assertFalse(b.moveForward(lb));
            assertEquals(Direction.LEFT.toSymbol(), get(b, 1, 1));
        }

        @Test
        void moveForward_pushMushroom_success_whenBehindIsEmpty() {
            Board b = board("....");
            // Pilz vor LB (3,1), dahinter leer (4,1)
            set(b, 3, 1, 'o');

            Ladybug lb = addLB(b, ids.getAndIncrement(), 2, 1, Direction.RIGHT);

            assertTrue(b.moveForward(lb));
            // Pilz nach hinten geschoben (4,1)
            assertEquals('o', get(b, 4, 1));
            // LB ist auf das ehemalige Pilzfeld (3,1) gerückt
            assertEquals(Direction.RIGHT.toSymbol(), get(b, 3, 1));
            // Startzelle leer
            assertEquals('.', get(b, 2, 1));
        }

        @Test
        void moveForward_pushMushroom_blocked_whenBehindNotEmpty() {
            Board b = board("....");
            // Pilz vor LB (3,1), dahinter Baum (4,1)
            set(b, 3, 1, 'o');
            set(b, 4, 1, '#');

            Ladybug lb = addLB(b, ids.getAndIncrement(), 2, 1, Direction.RIGHT);

            assertFalse(b.moveForward(lb));
            // Nichts verschoben
            assertEquals('o', get(b, 3, 1));
            assertEquals('#', get(b, 4, 1));
            assertEquals(Direction.RIGHT.toSymbol(), get(b, 2, 1));
        }

        @Test
        void moveForward_pushMushroom_blocked_whenBehindOutOfBounds() {
            Board b = board(".."); // width=2
            // Pilz auf (2,1), dahinter wäre (3,1)=OOB
            set(b, 2, 1, 'o');

            Ladybug lb = addLB(b, ids.getAndIncrement(), 1, 1, Direction.RIGHT);

            assertFalse(b.moveForward(lb));
            // Nichts verschoben
            assertEquals('o', get(b, 2, 1));
            assertEquals(Direction.RIGHT.toSymbol(), get(b, 1, 1));
        }
    }

    // ---------- ACTION: flyTo ----------

    @Nested
    class FlyToAction {

        @Test
        void flyTo_valid_target_inside_board() {
            Board b = board(
                    "....",
                    "...."
            );
            Ladybug lb = addLB(b, ids.getAndIncrement(), 1, 1, Direction.RIGHT);

            assertTrue(b.flyTo(lb, new Position(3, 2)));
            assertEquals('.', get(b, 1, 1));
            assertEquals(lb.getDirection().toSymbol(), get(b, 3, 2));
        }

        @Test
        void flyTo_outOfBounds_returnsFalse() {
            Board b = board("....");
            Ladybug lb = addLB(b, ids.getAndIncrement(), 2, 1, Direction.RIGHT);

            assertFalse(b.flyTo(lb, new Position(99, 99)));
            // bleibt stehen
            assertEquals(Direction.RIGHT.toSymbol(), get(b, 2, 1));
        }
    }
}
