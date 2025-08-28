package bt;

import model.Board;
import model.Ladybug;
import model.Position;

public class Fly implements NodeBehavior {
    private final int x;
    private final int y;

    public Fly(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public NodeStatus tick(Board board, Ladybug ladybug) {
        if (board == null || ladybug == null) {
            return NodeStatus.FAILURE;
        }
        Position target = new Position(x, y);
        return board.flyTo(ladybug, target) ? NodeStatus.SUCCESS : NodeStatus.FAILURE;
    }
}

