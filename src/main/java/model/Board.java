package model;

import java.util.*;

public class Board {
    private final char[][] grid;
    private final int width;
    private final int height;
    private final List<Ladybug> ladybugs;
    private static final char[] VALID_SYMBOLS = { '#', '*', 'o', '.', '^', '>', 'v', '<' };

    public Board(char[][] grid) {
        if (grid == null || grid.length == 0 || grid[0] == null || grid[0].length == 0) {
            throw new IllegalArgumentException("Error, grid cannot be null or empty");
        }

        this.height = grid.length;
        this.width = grid[0].length;
        this.ladybugs = new ArrayList<>();
        //Validate Rectangular
        for (int y = 0; y < height; y++) {
            if (grid[y].length != width) {
                throw new IllegalArgumentException("Error, grid must be rectangular");
            }
            for (int x = 0; x < width; x++) {
                if (!isValidSymbol(grid[y][x])) {
                    throw new IllegalArgumentException("Error, invalid symbol");
                }
            }
        }
        this.grid = grid;
        //init ladybugs from grid
        int id = 1;
        for (LadybugPosition lp : getLadybugsFromGrid()) {
            ladybugs.add(new Ladybug(id++, lp.getPosition(), lp.getDirection()));
        }
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

    private boolean isValidSymbol(char character) {
        for (char valid : VALID_SYMBOLS) {
            if (character == valid) {
                return true;
            }
        }
        return false;
    }

    private boolean isValidPosition(Position pos) {
        return pos != null && pos.x() >= 1 && pos.x() <= width && pos.y() >= 1 && pos.y() <= height;
    }

    public List<LadybugPosition> getLadybugsFromGrid() {
        List<LadybugPosition> ladybugs = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                char c = grid[y][x]; //search for ladybugs in grid
                Direction dir = null;
                try {
                    dir = Direction.fromSymbol(c);
                } catch (IllegalArgumentException e) {
                    // Not a ladybug symbol, skip
                }
                if (dir != null) {
                    ladybugs.add(new LadybugPosition(new Position(x + 1, y + 1), dir));
                }
            }
        }
        ladybugs.sort(Comparator.comparingInt((LadybugPosition b) -> b.getPosition().y())
                .thenComparingInt((LadybugPosition b) -> b.getPosition().x()));
        return ladybugs;
    }

    public void addLadybug(Ladybug ladybug) {
        if (ladybug == null || !isValidPosition(ladybug.getPosition())) {
            throw new IllegalArgumentException("Error, invalid ladybug or position");
        }
        if (getLadybugById(ladybug.getId()).isPresent()) {
            throw new IllegalArgumentException("Error, ladybug already");
        }
        if (getCell(ladybug.getPosition()) != '.') {
            throw new IllegalArgumentException("Error, position already occupied");
        }
        ladybugs.add(ladybug);
        setCell(ladybug.getPosition(), ladybug.getDirection().toSymbol());
    }

    public List<LadybugPosition> getLadybugList() {
        List<LadybugPosition> result = new ArrayList<>();
        for (Ladybug ladybug : ladybugs) {
            result.add(new LadybugPosition(ladybug.getPosition(), ladybug.getDirection()));
        }
        result.sort(Comparator.comparingInt((LadybugPosition b) -> b.getPosition().y())
                .thenComparingInt((LadybugPosition b) -> b.getPosition().x()));
        return result;
    }

    public void moveLadybugToEmpty(Ladybug ladybug, Position newPosition, Direction newDirection) {
        if (ladybug == null || !ladybugs.contains(ladybug)) {
            throw new IllegalArgumentException("Error, ladybug not found");
        }
        if (!isValidPosition(newPosition)) {
            throw new IllegalArgumentException("Error, invalid position");
        }
        if (getCell(newPosition) != '.') {
            throw new IllegalArgumentException("Error, target position occupied");
        }

        setCell(ladybug.getPosition(), '.');
        ladybug.setPosition(newPosition);
        ladybug.setDirection(newDirection);
        setCell(newPosition, newDirection.toSymbol());
    }

    public void moveLadybugToMushroom(Ladybug ladybug, Position mushroomPos, Direction newDirection) {
        if (ladybug == null || !ladybugs.contains(ladybug)) {
            throw new IllegalArgumentException("Error, ladybug not found");
        }
        if (!isValidPosition(mushroomPos)) {
            throw new IllegalArgumentException("Error, invalid position");
        }
        if (getCell(mushroomPos) != 'o') {
            throw new IllegalArgumentException("Error, no mushroom to push");
        }

        setCell(ladybug.getPosition(), '.');
        ladybug.setPosition(mushroomPos);
        ladybug.setDirection(newDirection);
        setCell(mushroomPos, newDirection.toSymbol());
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

    // === Conditions, alle basierend auf getFrontPosition ===
    private Position getFrontPosition(Ladybug ladybug) {
        Direction dir = ladybug.getDirection();
        Position pos = ladybug.getPosition();

        int newX = pos.x() + dir.getDx();
        int newY = pos.y() + dir.getDy();

        if (newX < 1 || newX > width || newY < 1 || newY > height) {
            return null;
        }
        return new Position(newX, newY);
    }

    public boolean atEdge(Ladybug ladybug) {
        return getFrontPosition(ladybug) == null;
    }

    public boolean treeFront(Ladybug ladybug) {
        Position front = getFrontPosition(ladybug);
        return front != null && getCell(front) == '#';
    }

    public boolean leafFront(Ladybug ladybug) {
        Position front = getFrontPosition(ladybug);
        return front != null && getCell(front) == '*';
    }

    public boolean mushroomFront(Ladybug ladybug) {
        Position front = getFrontPosition(ladybug);
        return front != null && getCell(front) == 'o';
    }

    private boolean hasPath(Position start, Position end, boolean allowNonEmptyStart) {
        // Endziel MUSS leer sein (Absicherung – wichtig, falls jemand direkt hasPath aufruft)
        if (getCell(end) != '.') return false;

        // Start == End nur dann true, wenn End tatsächlich leer ist (oben geprüft)
        if (start.equals(end)) return true;

        // Wenn Start nicht leer sein darf (between-Fall), sofort prüfen
        if (!allowNonEmptyStart && getCell(start) != '.') return false;

        // BFS über 4er-Nachbarschaft nur auf '.'-Zellen
        boolean[][] visited = new boolean[height][width];
        ArrayDeque<Position> q = new ArrayDeque<>();
        q.add(start);
        visited[start.y() - 1][start.x() - 1] = true;

        // 4 Richtungen (N, S, W, O)
        int[] dx = { 0,  0, -1, 1};
        int[] dy = {-1,  1,  0, 0};

        while (!q.isEmpty()) {
            Position cur = q.pollFirst();

            for (int i = 0; i < 4; i++) {
                int nx = cur.x() + dx[i];
                int ny = cur.y() + dy[i];
                if (nx < 1 || nx > width || ny < 1 || ny > height) continue;

                if (visited[ny - 1][nx - 1]) continue;

                Position nxt = new Position(nx, ny);

                // nur leere Zellen betreten
                if (getCell(nxt) != '.') continue;

                if (nxt.equals(end)) return true;

                visited[ny - 1][nx - 1] = true;
                q.addLast(nxt);
            }
        }
        return false;
    }

    // --- A.4.1.5: vom Käfer zur Zielzelle (Start darf Nicht-'.' sein, Ziel MUSS '.') ---
    public boolean existsPath(Ladybug ladybug, int x, int y)   {
        Position start = ladybug.getPosition();
        Position end = new Position(x, y);
        if (!isValidPosition(end)) return false;
        // Ziel MUSS leer sein
        if (getCell(end) != '.') return false;
        return hasPath(start, end, /*allowNonEmptyStart=*/true);
    }

    // --- A.4.1.6: zwischen zwei Koordinaten (Start MUSS '.' sein, Ziel MUSS '.') ---
    public boolean existsPath(int x1, int y1, int x2, int y2) {
        Position start = new Position(x1, y1);
        Position end   = new Position(x2, y2);
        if (!isValidPosition(start) || !isValidPosition(end)) return false;
        // Beide Endpunkte müssen leer sein (Pfad über leere Felder)
        if (getCell(start) != '.' || getCell(end) != '.') return false;
        return hasPath(start, end, /*allowNonEmptyStart=*/false);
    }

    // ================ Actions ================
    public boolean turnLeft(Ladybug ladybug) {
        Direction newDir = ladybug.getDirection().turnLeft();
        setLadybugDirection(ladybug, newDir);
        return true;
    }

    public boolean turnRight(Ladybug ladybug) {
        Direction newDir = ladybug.getDirection().turnRight();
        setLadybugDirection(ladybug, newDir);
        return true;
    }

    public boolean placeLeaf(Ladybug ladybug) {
        if (atEdge(ladybug) || treeFront(ladybug) || mushroomFront(ladybug)) {
            return false;
        }

        Position front = getFrontPosition(ladybug);
        if (getCell(front) != '.') return false;
        setCell(front, '*');
        return true;
    }

    public boolean takeLeaf(Ladybug ladybug) {
        if (atEdge(ladybug) || !leafFront(ladybug)) return false;

        Position front = getFrontPosition(ladybug);
        setCell(front, '.');
        return true;
    }

    public boolean moveForward(Ladybug ladybug) {
        if (atEdge(ladybug) || treeFront(ladybug)) return false;

        Position front = getFrontPosition(ladybug);
        char c = getCell(front);
        Direction direction = ladybug.getDirection();

        if (c == '.') {
            moveLadybugToEmpty(ladybug, front, direction);
            return true;
        }

        if (c == 'o') {
            int bx = front.x() + direction.getDx();
            int by = front.y() + direction.getDy();
            if (bx < 1 || bx > width || by < 1 || by > height) return false;
            Position behind = new Position(bx, by);
            if (getCell(behind) == '.') {
                setCell(behind, 'o');
                moveLadybugToMushroom(ladybug, front, direction);
                return true;
            }
            return false;
        }

        return false;
    }

    public boolean flyTo(Ladybug ladybug, Position target) {
        if (target.x() < 1 || target.x() > width || target.y() < 1 ||
                target.y() > height || getCell(target) != '.') return false;

        Position current = ladybug.getPosition();
        int dx = Integer.compare(target.x() - current.x(), 0);
        int dy = Integer.compare(target.y() - current.y(), 0);
        Direction newDir = Direction.fromDelta(dx, dy);

        clearCell(current);
        ladybug.setPosition(target);
        ladybug.setDirection(newDir);
        setCell(target, newDir.toSymbol());
        return true;
    }

    private void setLadybugDirection(Ladybug ladybug, Direction newDir) {
        ladybug.setDirection(newDir);;
        setCell(ladybug.getPosition(), newDir.toSymbol());
    }

    private void clearCell(Position p) { setCell(p, '.'); }

    public List<Integer> listLadybugsIds() {
        List<Integer> ids = new ArrayList<>();
        for (Ladybug ladybug : ladybugs) {
            ids.add(ladybug.getId());
        }
        ids.sort(Integer::compare);
        return ids;
    }

    public Optional<Ladybug> getLadybugById(int id) {
        if (id < 1) {
            throw new IllegalArgumentException("Error, invalid ladybug ID");
        }
        for (Ladybug ladybug : ladybugs) {
            if (ladybug.getId() == id) {
                return Optional.of(ladybug);
            }
        }
        return Optional.empty();
    }

}
