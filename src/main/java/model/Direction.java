package model;

public enum Direction {
    UP('^', 0, -1),
    RIGHT('>', 1, 0),
    DOWN('v', 0, 1),
    LEFT('<', -1, 0);

    private final char symbol;
    private final int dx;
    private final int dy;

    Direction(char symbol, int dx, int dy) {
        this.symbol = symbol;
        this.dx = dx;
        this.dy = dy;
    }

    public char toSymbol() {
        return symbol;
    }

    public int getDx() {
        return dx;
    }

    public int getDy() {
        return dy;
    }

    //Symbol is not only symbol it has a direction so we assign the direction to each symbol
    //Later with Map<Character,Direction>
    public static Direction fromSymbol(char symbol) {
        for (Direction direction : values()) {
            if (direction.symbol == symbol) {
                return direction;
            }
        }
        throw new IllegalArgumentException("Error, Invalid direction symbol: " + symbol);
    }

    public Direction turnLeft() {
        return switch (this) { //this actual position
            case UP -> LEFT;
            case RIGHT -> UP;
            case DOWN -> RIGHT;
            case LEFT -> DOWN;
        };
    }

    public Direction turnRight() {
        return switch (this) { //this actual position
            case UP -> RIGHT;
            case RIGHT -> DOWN;
            case DOWN -> LEFT;
            case LEFT -> UP;
        };
    }

    public static Direction fromDelta(int dx, int dy) {
        if (Math.abs(dx) >= Math.abs(dy)) {
            return dx >= 0 ? RIGHT : LEFT;
        } else {
            return dy >= 0 ? DOWN : UP;
        }
    }

}
