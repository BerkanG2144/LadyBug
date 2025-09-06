package parser;

import bt.BehaviorTreeNode;
import bt.CompositeNode;
import bt.SequenceNode;
import bt.FallbackNode;
import bt.ParallelNode;
import bt.ExistsPath;
import bt.AtEdge;
import bt.TakeLeaf;
import bt.TreeFront;
import bt.ExistsPathBetween;
import bt.MushroomFront;
import bt.PlaceLeaf;
import bt.TurnLeft;
import bt.TurnRight;
import bt.Fly;
import bt.LeafFront;
import bt.LeafNode;
import bt.NodeBehavior;
import bt.Move;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for Mermaid flowchart syntax to create behavior trees.
 *
 * This parser reads Mermaid flowchart syntax and converts it into a behavior tree
 * structure with nodes and connections. Supports fallback, sequence, parallel,
 * condition, and action nodes.
 *
 * @author ujnaa
 */
public class MermaidParser {

    private static final Pattern P_FALLBACK = Pattern.compile("^([A-Za-z_][A-Za-z0-9_]*)\\[\\?]$");
    private static final Pattern P_SEQUENCE = Pattern.compile("^([A-Za-z_][A-Za-z0-9_]*)\\[->]$");
    private static final Pattern P_PARALLEL = Pattern.compile("^([A-Za-z_][A-Za-z0-9_]*)\\[=(\\d+)>]$");
    private static final Pattern P_COND = Pattern.compile("^([A-Za-z_][A-Za-z0-9_]*)\\(\\[([^\\]]+)]\\)$");
    private static final Pattern P_ACT  = Pattern.compile("^([A-Za-z_][A-Za-z0-9_]*)\\[([^\\]]+)]$");
    private static final Pattern P_EDGE = Pattern.compile("^([A-Za-z_][A-Za-z0-9_]*)\\s*-->(?:\\|[^|]*\\|\\s*)?([A-Za-z_][A-Za-z0-9_]*)$");

    private static final Pattern P_COORD  = Pattern.compile("^(\\d+),(\\d+)$");
    private static final Pattern P_COORD2 = Pattern.compile("^(\\d+),(\\d+)\\s+(\\d+),(\\d+)$");

    /**
     * Parses Mermaid flowchart content into a behavior tree.
     *
     * @param mermaidContent the Mermaid flowchart content to parse
     * @return the root node of the parsed behavior tree
     * @throws IllegalArgumentException if the content is invalid or cannot be parsed
     */
    public BehaviorTreeNode parse(String mermaidContent) {
        if (mermaidContent == null || mermaidContent.isBlank()) {
            throw new IllegalArgumentException("Empty Input");
        }

        List<String> raw = Arrays.asList(mermaidContent.split("\\R"));
        List<String> lines = new ArrayList<>();
        for (String line : raw) {
            String trimmedLine = line.trim();
            if (!trimmedLine.isEmpty() && !trimmedLine.startsWith("%%")) {
                lines.add(trimmedLine);
            }
        }

        int flowIdx = -1;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).startsWith("flowchart")) {
                flowIdx = i;
                break;
            }
        }
        if (flowIdx < 0) {
            throw new IllegalArgumentException("No 'flowchart' header found");
        }
        lines = lines.subList(flowIdx + 1, lines.size());

        Map<String, BehaviorTreeNode> nodes = new LinkedHashMap<>();
        Map<String, List<String>> connections = new LinkedHashMap<>();

        for (String line : lines) {
            String trimmedLine = line.trim();
            if (trimmedLine.isEmpty()) {
                continue;
            }

            parseLineForNodes(trimmedLine, nodes);
            parseLineForConnections(trimmedLine, connections);
        }

        buildTreeStructure(nodes, connections);
        BehaviorTreeNode root = findRootNode(nodes, connections);
        return root;
    }

    private void parseLineForNodes(String line, Map<String, BehaviorTreeNode> nodes) {
        String cleanLine = stripLineComment(line);

        if (cleanLine.contains("-->")) {
            String[] raw = cleanLine.split("-->");
            for (int i = 0; i < raw.length; i++) {
                String token = (i == 0) ? raw[i].trim() : normalizeRightSide(raw[i]);
                if (!token.isEmpty()) {
                    parseSingleTokenAsNode(token, nodes);
                }
            }
            return;
        }
        parseSingleTokenAsNode(cleanLine.trim(), nodes);
    }

    private void parseLineForConnections(String line, Map<String, List<String>> connections) {
        String cleanLine = stripLineComment(line);
        if (!cleanLine.contains("-->")) {
            return;
        }

        // Ketten A --> B --> C in Paare auflösen: (A->B), (B->C)
        String[] raw = cleanLine.split("-->");
        List<String> parts = new ArrayList<>(raw.length);
        for (int i = 0; i < raw.length; i++) {
            String side = (i == 0) ? raw[i].trim() : normalizeRightSide(raw[i]);
            if (!side.isEmpty()) {
                parts.add(side);
            }
        }
        for (int i = 0; i + 1 < parts.size(); i++) {
            String parentId = extractId(parts.get(i));
            String childId  = extractId(parts.get(i + 1));
            connections.computeIfAbsent(parentId, k -> new ArrayList<>());
            List<String> list = connections.get(parentId);
            if (!list.contains(childId)) {
                list.add(childId); // Duplikate vermeiden
            }
        }
    }

    private void putIfAbsentOrFail(Map<String, BehaviorTreeNode> nodes, String id, BehaviorTreeNode node) {
        if (nodes.putIfAbsent(id, node) != null) {
            throw new IllegalArgumentException("Duplicate node id: " + id);
        }
    }

    private void buildTreeStructure(Map<String, BehaviorTreeNode> nodes, Map<String, List<String>> connections) {
        Map<String, Integer> parentCount = new HashMap<>();
        for (var e : connections.entrySet()) {
            String parentId = e.getKey();
            BehaviorTreeNode parent = nodes.get(parentId);
            if (parent == null) {
                throw new IllegalArgumentException("Unknown parent id: " + parentId);
            }

            for (String childId : e.getValue()) {
                BehaviorTreeNode child = nodes.get(childId);
                if (child == null) {
                    throw new IllegalArgumentException("Unknown child id: " + childId);
                }

                // nur Composite dürfen Kinder haben
                if (!(parent instanceof CompositeNode)) {
                    throw new IllegalArgumentException("Node '" + parentId + "' is not composite but has children");
                }
                ((CompositeNode) parent).addChild(child);

                parentCount.merge(childId, 1, Integer::sum);
                if (parentCount.get(childId) > 1) {
                    throw new IllegalArgumentException("Node '" + childId + "' has multiple parents");
                }
            }
        }
    }

    private BehaviorTreeNode findRootNode(Map<String, BehaviorTreeNode> nodes, Map<String, List<String>> connections) {
        if (nodes.isEmpty()) {
            throw new IllegalArgumentException("No nodes parsed");
        }

        if (connections.isEmpty() && nodes.size() == 1) {
            return nodes.values().iterator().next();
        }

        Set<String> children = new HashSet<>();
        connections.values().forEach(children::addAll);

        List<String> roots = new ArrayList<>();
        for (String id : nodes.keySet()) {
            if (connections.containsKey(id) && !children.contains(id)) {
                roots.add(id);
            }
        }
        if (roots.isEmpty()) {
            throw new IllegalArgumentException("No root node found");
        }
        if (roots.size() > 1) {
            throw new IllegalArgumentException("Multiple roots found: " + roots);
        }
        return nodes.get(roots.get(0));
    }

    private NodeBehavior createConditionBehavior(String conditionName) {
        String trimmedName = conditionName.trim();
        switch (trimmedName) {
            case "treeFront": return new TreeFront();
            case "leafFront": return new LeafFront();
            case "mushroomFront": return new MushroomFront();
            case "atEdge": return new AtEdge();
            default:
                if (trimmedName.startsWith("existsPath ")) {
                    String args = trimmedName.substring("existsPath ".length()).trim();
                    Matcher coordMatcher2 = P_COORD2.matcher(args);
                    if (coordMatcher2.matches()) {
                        int x1 = Integer.parseInt(coordMatcher2.group(1));
                        int y1 = Integer.parseInt(coordMatcher2.group(2));
                        int x2 = Integer.parseInt(coordMatcher2.group(3));
                        int y2 = Integer.parseInt(coordMatcher2.group(4));
                        return new ExistsPathBetween(x1, y1, x2, y2);
                    }
                    Matcher coordMatcher1 = P_COORD.matcher(args);
                    if (coordMatcher1.matches()) {
                        int x = Integer.parseInt(coordMatcher1.group(1));
                        int y = Integer.parseInt(coordMatcher1.group(2));
                        return new ExistsPath(x, y);
                    }
                    throw new IllegalArgumentException("existsPath args invalid: '" + args + "'");
                }
                throw new IllegalArgumentException("Unknown condition: " + conditionName);
        }
    }

    private NodeBehavior createActionBehavior(String actionName) {
        String trimmedName = actionName.trim();
        switch (trimmedName) {
            case "move": return new Move();
            case "turnLeft": return new TurnLeft();
            case "turnRight": return new TurnRight();
            case "takeLeaf": return new TakeLeaf();
            case "placeLeaf": return new PlaceLeaf();
            default:
                if (trimmedName.startsWith("fly ")) {
                    String args = trimmedName.substring("fly ".length()).trim();
                    Matcher flyMatcher = P_COORD.matcher(args);
                    if (!flyMatcher.matches()) {
                        throw new IllegalArgumentException("fly args invalid: '" + args + "'");
                    }
                    int x = Integer.parseInt(flyMatcher.group(1));
                    int y = Integer.parseInt(flyMatcher.group(2));
                    return new Fly(x, y);
                }
                throw new IllegalArgumentException("Unknown action: " + actionName);
        }
    }

    private void parseSingleTokenAsNode(String token, Map<String, BehaviorTreeNode> nodes) {
        Matcher fallbackMatcher = P_FALLBACK.matcher(token);
        if (fallbackMatcher.matches()) {
            putIfAbsentOrFail(nodes, fallbackMatcher.group(1), new FallbackNode(fallbackMatcher.group(1)));
            return;
        }

        Matcher sequenceMatcher = P_SEQUENCE.matcher(token);
        if (sequenceMatcher.matches()) {
            putIfAbsentOrFail(nodes, sequenceMatcher.group(1), new SequenceNode(sequenceMatcher.group(1)));
            return;
        }

        Matcher parallelMatcher = P_PARALLEL.matcher(token);
        if (parallelMatcher.matches()) {
            putIfAbsentOrFail(nodes, parallelMatcher.group(1),
                    new ParallelNode(parallelMatcher.group(1), Integer.parseInt(parallelMatcher.group(2))));
            return;
        }

        Matcher condMatcher = P_COND.matcher(token);
        if (condMatcher.matches()) {
            String id = condMatcher.group(1);
            String conditionName = condMatcher.group(2);
            NodeBehavior behavior = createConditionBehavior(conditionName);
            putIfAbsentOrFail(nodes, id, new LeafNode(id, behavior, LeafNode.LeafKind.CONDITION));
            return;
        }

        Matcher actMatcher = P_ACT.matcher(token);
        if (actMatcher.matches()) {
            String id = actMatcher.group(1);
            String actionName = actMatcher.group(2);
            if (!"?".equals(actionName) && !"->".equals(actionName) && !actionName.matches("=\\d+>")) {
                NodeBehavior behavior = createActionBehavior(actionName);
                putIfAbsentOrFail(nodes, id, new LeafNode(id, behavior, LeafNode.LeafKind.ACTION));
            }
        }
    }

    // Entfernt trailing Kommentar %%...
    private String stripLineComment(String input) {
        int commentIndex = input.indexOf("%%");
        return (commentIndex >= 0) ? input.substring(0, commentIndex).trim() : input.trim();
    }

    // Macht aus " |label|  B[->] %% bla" -> "B[->]"
    private String normalizeRightSide(String input) {
        String normalized = input.trim();
        if (normalized.startsWith("|")) {
            int endIndex = normalized.indexOf('|', 1);
            if (endIndex >= 0 && endIndex + 1 < normalized.length()) {
                normalized = normalized.substring(endIndex + 1).trim();
            } else {
                return ""; // kaputtes Label -> ignorieren
            }
        }
        int spaceIndex = normalized.indexOf(' ');
        if (spaceIndex > 0) {
            normalized = normalized.substring(0, spaceIndex).trim();
        }
        return normalized;
    }

    // Holt die reine Node-ID aus einem Token wie "A[?]", "C([treeFront])", "B[->]" -> "A","C","B"
    private String extractId(String token) {
        Matcher fallbackMatcher = P_FALLBACK.matcher(token);
        if (fallbackMatcher.matches()) {
            return fallbackMatcher.group(1);
        }

        Matcher sequenceMatcher = P_SEQUENCE.matcher(token);
        if (sequenceMatcher.matches()) {
            return sequenceMatcher.group(1);
        }

        Matcher parallelMatcher = P_PARALLEL.matcher(token);
        if (parallelMatcher.matches()) {
            return parallelMatcher.group(1);
        }

        Matcher condMatcher = P_COND.matcher(token);
        if (condMatcher.matches()) {
            return condMatcher.group(1);
        }

        Matcher actMatcher = P_ACT.matcher(token);
        if (actMatcher.matches()) {
            return actMatcher.group(1);
        }

        // falls es nur eine rohe ID ist (z.B. "A")
        if (token.matches("^[A-Za-z_][A-Za-z0-9_]*$")) {
            return token;
        }
        throw new IllegalArgumentException("Invalid node token: '" + token + "'");
    }

    /**
     * Creates a behavior tree from a Mermaid file.
     *
     * @param path the path to the Mermaid file
     * @return the root node of the behavior tree
     * @throws IOException if the file cannot be read
     */
    public static BehaviorTreeNode fromFile(String path) throws IOException {
        String content = Files.readString(Path.of(path));
        return new MermaidParser().parse(content);
    }
}