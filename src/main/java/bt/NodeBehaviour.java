package bt;

import model.Board;
import model.Ladybug;

public interface NodeBehaviour {
    NodeStatus tick(Board board, Ladybug ladybug);
}
