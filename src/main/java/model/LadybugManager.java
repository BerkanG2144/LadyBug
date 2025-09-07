package model;

import exceptions.LadybugException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Collections;


/**
 * Manages ladybugs on the board.
 * @author ujnaa
 */
public class LadybugManager {
    private final List<Ladybug> ladybugs;
    private final BoardGrid grid;

    /**
     * Creates a new manager bound to the given grid and initializes
     * ladybugs from the grid's current state.
     *
     * @param grid the board grid backing this manager
     * @throws LadybugException if initializing ladybugs from the grid fails
     */
    public LadybugManager(BoardGrid grid) throws LadybugException {
        this.grid = grid;
        this.ladybugs = new ArrayList<>();
        initializeLadybugsFromGrid();
    }

    /**
     * Adds a ladybug to the manager.
     * @param ladybug the ladybug to add
     * @throws IllegalArgumentException if ladybug is invalid or position occupied
     */
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

    /**
     * Gets a ladybug by its ID.
     * @param id the ladybug ID
     * @return Optional containing the ladybug if found
     * @throws IllegalArgumentException if ID is invalid
     */
    public Optional<Ladybug> getLadybugById(int id) {
        if (id < 1) {
            throw new IllegalArgumentException("Error, invalid ladybug ID");
        }
        for (Ladybug lb : ladybugs) {
            if (lb.getId() == id) {
                return Optional.of(lb);
            }
        }
        return Optional.empty();
    }

    /**
     * Gets all ladybug IDs sorted.
     * @return list of sorted IDs
     */
    public List<Integer> listLadybugsIds() {
        List<Integer> result = new ArrayList<>();
        for (Ladybug ladybug : ladybugs) {
            result.add(ladybug.getId());
        }
        Collections.sort(result);
        return result;
    }

    /**
     * Gets all ladybug positions sorted by position.
     * @return list of ladybug positions
     */
    public List<LadybugPosition> getLadybugList() {
        List<LadybugPosition> result = new ArrayList<>();
        for (Ladybug lb : ladybugs) {
            result.add(new LadybugPosition(lb.getPosition(), lb.getDirection()));
        }
        result.sort(Comparator.comparingInt((LadybugPosition b) -> b.getPosition().y())
                .thenComparingInt(b -> b.getPosition().x()));
        return result;
    }

    /**
     * Moves a ladybug to an empty position.
     * @param ladybug the ladybug to move
     * @param newPosition the target position
     * @param newDirection the new direction
     * @throws IllegalArgumentException if the ladybug is {@code null}, the position is invalid,
     *                                  the id already exists, or the target cell is occupied
     * @throws LadybugException         if updating the grid or ladybug state fails
     */
    public void moveLadybugToEmpty(Ladybug ladybug, Position newPosition, Direction newDirection) throws LadybugException {
        validateLadybugMove(ladybug, newPosition);
        if (grid.getCell(newPosition) != '.') {
            throw new IllegalArgumentException("Error, target position occupied");
        }
        performMove(ladybug, newPosition, newDirection);
    }

    /**
     * Moves a ladybug to a mushroom position (replaces the mushroom).
     * @param ladybug the ladybug to move
     * @param mushroomPos the mushroom position
     * @param newDirection the new direction
     * @throws IllegalArgumentException if move is invalid
     * @throws LadybugException         if updating the grid or ladybug state fails
     */
    public void moveLadybugToMushroom(Ladybug ladybug, Position mushroomPos, Direction newDirection) throws LadybugException {
        validateLadybugMove(ladybug, mushroomPos);
        if (grid.getCell(mushroomPos) != 'o') {
            throw new IllegalArgumentException("Error, no mushroom to push");
        }
        performMove(ladybug, mushroomPos, newDirection);
    }

    /**
     * Sets the direction of a ladybug and updates the grid.
     * @param ladybug the ladybug
     * @param newDirection the new direction
     * @throws LadybugException if updating the grid or ladybug state fails
     */
    public void setLadybugDirection(Ladybug ladybug, Direction newDirection) throws LadybugException {
        ladybug.setDirection(newDirection);
        grid.setCell(ladybug.getPosition(), newDirection.toSymbol());
    }

    private void initializeLadybugsFromGrid() throws LadybugException {
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

                // Statt IllegalArgumentException zu fangen: vorher prüfen
                if (isLadybugSymbol(c)) {
                    Direction dir = Direction.fromSymbol(c);
                    ladybugPositions.add(new LadybugPosition(pos, dir));
                }
            }
        }
        ladybugPositions.sort(Comparator.comparingInt((LadybugPosition b) -> b.getPosition().y())
                .thenComparingInt(b -> b.getPosition().x()));
        return ladybugPositions;
    }

    private boolean isLadybugSymbol(char c) {
        return c == '^' || c == 'v' || c == '<' || c == '>';
    }

    private void validateLadybugMove(Ladybug ladybug, Position newPosition) {
        if (ladybug == null || !ladybugs.contains(ladybug)) {
            throw new IllegalArgumentException("Error, ladybug not found");
        }
        if (!grid.isValidPosition(newPosition)) {
            throw new IllegalArgumentException("Error, invalid position");
        }
    }

    private void performMove(Ladybug ladybug, Position newPosition, Direction newDirection) throws LadybugException {
        grid.setCell(ladybug.getPosition(), '.');
        ladybug.setPosition(newPosition);
        ladybug.setDirection(newDirection);
        grid.setCell(newPosition, newDirection.toSymbol());
    }

    /**
     * Removes all ladybugs from the grid and internal list,
     * resetting their cells to '.' without re-creating objects.
     * Intended for re-initialization via load commands.
     */
    public void clearAllLadybugs() {
        // Entferne alle Marienkäfer-Symbole vom Grid und setze auf '.' zurück
        for (Ladybug ladybug : ladybugs) {
            grid.setCell(ladybug.getPosition(), '.');
        }
        ladybugs.clear();
    }
}