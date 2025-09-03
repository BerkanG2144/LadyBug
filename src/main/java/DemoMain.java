import bt.*;                 // BehaviorTreeNode, LeafNode, SequenceNode, FallbackNode, NodeStatus, NodeBehavior ...
import engine.TreeExecution; // deine Engine
import model.*;              // Board, Ladybug, Position, Direction

public class DemoMain {
    public static void main(String[] args) {
        // 1) Mini-Board direkt im Code ('.' leer, '#' Baum, '^','>','v','<' Ladybug-Richtung)
        // Ladybug startet bei (2,2) und schaut nach rechts ('>')
        char[][] grid = {
                {'.', '.', '.', '.', '.'},
                {'.', '>', '.', '#', '.'},
                {'.', '.', '.', '.', '.'},
                {'.', '.', '.', '.', '.'},
                {'.', '.', '.', '.', '.'},
        };
        Board board = new Board(grid);

        // 2) Käfer holen (ID 1, da Board ctor IDs ab 1 vergibt)
        Ladybug bug = board.getLadybugById(1)
                .orElseThrow(() -> new IllegalArgumentException("Keine Ladybug mit ID 1 gefunden"));

        // 3) Kleinen Behavior Tree zusammenbauen:
        // root = Fallback( Sequence( treeFront? -> turnLeft ), moveForward )
        FallbackNode root = new FallbackNode("root");

// 1) Wenn Baum vorne, dann links abbiegen
        SequenceNode seqTurnIfTree = new SequenceNode("seq-turn-if-tree");
        seqTurnIfTree.addChild(new LeafNode("cond-treeFront",
                new bt.TreeFront(), LeafNode.LeafKind.CONDITION));
        seqTurnIfTree.addChild(new LeafNode("act-turnLeft",
                new bt.TurnLeft(), LeafNode.LeafKind.ACTION));

// 2) Wenn am Rand, dann rechts abbiegen
        SequenceNode seqTurnIfEdge = new SequenceNode("seq-turn-if-edge");
        seqTurnIfEdge.addChild(new LeafNode("cond-atEdge",
                new bt.AtEdge(), LeafNode.LeafKind.CONDITION));
        seqTurnIfEdge.addChild(new LeafNode("act-turnRight",
                new bt.TurnRight(), LeafNode.LeafKind.ACTION));

// 3) Sonst vorwärts
        LeafNode actMove = new LeafNode("act-moveForward",
                new bt.Move(), LeafNode.LeafKind.ACTION);

        root.addChild(seqTurnIfTree);
        root.addChild(seqTurnIfEdge);
        root.addChild(actMove);
        // sonst: vorwärts

        // 4) Engine starten
        TreeExecution engine = new TreeExecution(root, System.out::println);

        // 5) Tick-Loop (führt jeweils die *nächste* Action aus)
        System.out.println("Startzustand:");
        board.print();

        int safety = 20; // Schleifenschutz
        while (safety-- > 0 && engine.tick(board, bug)) {
            System.out.println("\nNach Tick:");
            board.print();
        }
        System.out.println("\nFertig (keine Actions mehr).");
    }
}
