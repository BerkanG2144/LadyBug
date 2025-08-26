package model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class DirectionTest {

    @Test
    void testFromSymbolValid() {
        assertEquals(Direction.UP, Direction.fromSymbol('^'));
        assertEquals(Direction.RIGHT, Direction.fromSymbol('>'));
        assertEquals(Direction.DOWN, Direction.fromSymbol('v'));
        assertEquals(Direction.LEFT, Direction.fromSymbol('<'));
    }


    @Test
    void testTurnLeft() {
        assertEquals(Direction.LEFT, Direction.UP.turnLeft());
        assertEquals(Direction.UP, Direction.RIGHT.turnLeft());
        assertEquals(Direction.RIGHT, Direction.DOWN.turnLeft());
        assertEquals(Direction.DOWN, Direction.LEFT.turnLeft());
    }

    @Test
    void testTurnRight() {
        assertEquals(Direction.RIGHT, Direction.UP.turnRight());
        assertEquals(Direction.DOWN, Direction.RIGHT.turnRight());
        assertEquals(Direction.LEFT, Direction.DOWN.turnRight());
        assertEquals(Direction.UP, Direction.LEFT.turnRight());
    }

    @Test
    void testTurnLeftAndRightInverse() {
        // Prüfen, dass turnLeft und turnRight sich aufheben
        for (Direction dir : Direction.values()) {
            assertEquals(dir, dir.turnLeft().turnRight());
            assertEquals(dir, dir.turnRight().turnLeft());
        }
    }
}
