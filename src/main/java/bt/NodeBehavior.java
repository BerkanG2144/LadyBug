package bt;

import model.Board;
import model.Ladybug;

public interface NodeBehavior {
    NodeStatus tick(Board board, Ladybug ladybug);
}
