package model;

public record Position (int x, int y) {

    public Position {
        if (x < 1 || y < 1) {
            throw new IllegalArgumentException("Error, position coordinates must be positive");
        }
    }

    public Position add(int dx, int dy) {
        return new Position(x + dx, y + dy);
    }

    public boolean isValid(int maxX, int maxY) {
        return x >= 1 && x <= maxX && y >= 1 && y <= maxY;
    }

}
