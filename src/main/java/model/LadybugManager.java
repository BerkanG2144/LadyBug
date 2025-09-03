package model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class LadybugManager {
    private final List<Ladybug> ladybugs;
    private final BoardGrid grid;

    public LadybugManager(BoardGrid grid) {
        this.grid = grid;
        this.ladybugs = new ArrayList<>();
    }

    public void addLadybug(Ladybug ladybug) {
        if (ladybug == null || !grid.isValidPosition(ladybug.getPosition())) {
            throw new IllegalArgumentException("Error, invalid ladybug or position");
        }
        if (getLadybugById(ladybug.getId()).isPresent()) {
            throw new IllegalArgumentException("Error, ladybug already exists");
        }
        if (grid.getCell(ladybug.getPosition()) != '.') {
            throw new IllegalArgumentException("Error, position already occupied");
        }
        ladybugs.add(ladybug);
        grid.setCell(ladybug.getPosition(), ladybug.getDirection().toSymbol());
    }

    public Optional<Ladybug> getLadybugById(int id) {
        if (id < 1) {
            throw new IllegalArgumentException("Error, invalid ladybug ID");
        }
        return ladybugs.stream().filter(lb -> lb.getId() == id).findFirst();
    }

    public List<Integer> listLadyBugsIds() {
        return ladybugs.stream().map(Ladybug::getId).sorted().toList();
    }

    public List<LadybugPosition> getLadybugList() {
        return ladybugs.stream()
                .map(lb -> new LadybugPosition(lb.getPosition(), lb.getDirection()))
                .sorted(Comparator.comparingInt((LadybugPosition b) -> b.getPosition().y())
                        .thenComparingInt(b -> b.getPosition().x()))
                .toList();
    }

    public void moveLadybugToEmpty(Ladybug ladybug, Position newPosition, Direction newDirection) {
        validateLadybugMove(ladybug, newPosition);
        if (grid.getCell(newPosition) != '.') {
            throw new IllegalArgumentException("Error, target position occupied");
        }
        performMove(ladybug, newPosition, newDirection);
    }

    public void moveLadybugToMushroom(Ladybug ladybug, Position mushroomPos, Direction newDirection) {
        validateLadybugMove(ladybug, mushroomPos);
        if (grid.getCell(mushroomPos) != 'o') {
            throw new IllegalArgumentException("Error, no mushroom to push");
        }
        performMove(ladybug, mushroomPos, newDirection);
    }

    public void setLadybugDirection(Ladybug ladybug, Direction newDirection) {
        ladybug.setDirection(newDirection);
        grid.setCell(ladybug.getPosition(), newDirection.toSymbol());
    }

    private void initializeLadybugsFromGrid() {
        List<LadybugPosition> positions = getLadybugsFromGrid();
        int id = 1;
        for (LadybugPosition lp : positions) {
            ladybugs.add(new Ladybug(id++, lp.getPosition(), lp.getDirection()));
        }
    }

    private List<LadybugPosition> getLadybugsFromGrid() {
        List<LadybugPosition> ladybugPositions = new ArrayList<>();
        for (int y = 1; y <= grid.getHeight(); y++) {
            for (int x = 1; x <= grid.getWidth(); x++) {
                Position pos = new Position(x, y);
                char c = grid.getCell(pos);
                try {
                    Direction dir = Direction.fromSymbol(c);
                    ladybugPositions.add(new LadybugPosition(pos, dir));
                } catch (IllegalArgumentException e) {
                    // Not a ladybug symbol, skip
                }
            }
        }
        ladybugPositions.sort(Comparator.comparingInt((LadybugPosition b) -> b.getPosition().y())
                .thenComparingInt(b -> b.getPosition().x()));
        return ladybugPositions;
    }

    private void validateLadybugMove(Ladybug ladybug, Position newPosition) {
        if (ladybug == null || !ladybugs.contains(ladybug)) {
            throw new IllegalArgumentException("Error, ladybug not found");
        }
        if (!grid.isValidPosition(newPosition)) {
            throw new IllegalArgumentException("Error, invalid position");
        }
    }

    private void performMove(Ladybug ladybug, Position newPosition, Direction newDirection) {
        grid.setCell(ladybug.getPosition(), '.');
        ladybug.setPosition(newPosition);
        ladybug.setDirection(newDirection);
        grid.setCell(newPosition, newDirection.toSymbol());
    }
}
