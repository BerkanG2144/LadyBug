import bt.*;
import parser.MermaidParser;

import java.nio.file.*;

public class ParseTreesMain {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("usage: ParseTreesMain <path-to-mermaid-file>");
            System.exit(1);
        }
        String content = Files.readString(Path.of(args[0]));
        System.out.println("=== RAW FILE ===");
        System.out.println(content);

        MermaidParser p = new MermaidParser();
        BehaviorTreeNode root = p.parse(content);

        System.out.println("\n=== PARSED TREE (preorder) ===");
        printTree(root, 0);
    }

    private static void printTree(BehaviorTreeNode node, int depth) {
        String indent = "  ".repeat(depth);
        System.out.print(indent + "- " + node.getClass().getSimpleName());
        // Falls du Namen/IDs hast:
        try {
            var m = node.getClass().getMethod("getName");
            Object name = m.invoke(node);
            System.out.print(" (" + name + ")");
        } catch (Exception ignored) {}
        // Leaf-Infos:
        if (node instanceof LeafNode leaf) {
            System.out.print(" [" + leaf.getKind() + "] -> " + leaf.getBehavior().getClass().getSimpleName());
        }
        System.out.println();

        if (node instanceof CompositeNode comp) {
            for (BehaviorTreeNode child : comp.getChildren()) {
                printTree(child, depth + 1);
            }
        }
    }
}
