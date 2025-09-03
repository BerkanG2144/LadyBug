package parser;

import bt.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MermaidParser {

    private static final Pattern P_FALLBACK = Pattern.compile("^([A-Za-z_][A-Za-z0-9_]*)\\[\\?]$");
    private static final Pattern P_SEQUENCE = Pattern.compile("^([A-Za-z_][A-Za-z0-9_]*)\\[->]$");
    private static final Pattern P_PARALLEL = Pattern.compile("^([A-Za-z_][A-Za-z0-9_]*)\\[=(\\d+)>]$");
    private static final Pattern P_COND = Pattern.compile("^([A-Za-z_][A-Za-z0-9_]*)\\(\\[([^\\]]+)]\\)$"); // deine Syntax
    private static final Pattern P_ACT  = Pattern.compile("^([A-Za-z_][A-Za-z0-9_]*)\\[([^\\]]+)]$");
    private static final Pattern P_EDGE = Pattern.compile("^([A-Za-z_][A-Za-z0-9_]*)\\s*-->(?:\\|[^|]*\\|\\s*)?([A-Za-z_][A-Za-z0-9_]*)$");

    private static final Pattern P_COORD  = Pattern.compile("^(\\d+),(\\d+)$");
    private static final Pattern P_COORD2 = Pattern.compile("^(\\d+),(\\d+)\\s+(\\d+),(\\d+)$");


    public BehaviorTreeNode parse(String mermaidContent) {
        if (mermaidContent == null || mermaidContent.isBlank()) {
            throw new IllegalArgumentException("Empty Input");
        }

        List<String> raw = Arrays.asList(mermaidContent.split("\\R"));
        List<String> lines = new ArrayList<>();
        for (String s : raw) {
            s = s.trim();
            if (!s.isEmpty() && !s.startsWith("%%")) lines.add(s);
        }
        int flowIdx = -1;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).startsWith("flowchart")) { flowIdx = i; break; }
        }
        if (flowIdx < 0) throw new IllegalArgumentException("No 'flowchart' header found");
        lines = lines.subList(flowIdx + 1, lines.size());

        Map<String, BehaviorTreeNode> nodes = new LinkedHashMap<>();
        Map<String, List<String>> connections = new LinkedHashMap<>();

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            parseLineForNodes(line, nodes);
            parseLineForConnections(line, connections);
        }

        buildTreeStructure(nodes, connections);
        BehaviorTreeNode root = findRootNode(nodes, connections);
        return root;
    }

    private void parseLineForNodes(String line, Map<String, BehaviorTreeNode> nodes) {
        line = stripLineComment(line);

        if (line.contains("-->")) {
            String[] raw = line.split("-->");
            for (int i = 0; i < raw.length; i++) {
                String t = (i == 0) ? raw[i].trim() : normalizeRightSide(raw[i]);
                if (!t.isEmpty()) parseSingleTokenAsNode(t, nodes);
            }
            return;
        }
        parseSingleTokenAsNode(line.trim(), nodes);
    }


    private void parseLineForConnections(String line, Map<String, List<String>> connections) {
        line = stripLineComment(line);
        if (!line.contains("-->")) return;

        // Ketten A --> B --> C in Paare auflösen: (A->B), (B->C)
        String[] raw = line.split("-->");
        List<String> parts = new ArrayList<>(raw.length);
        for (int i = 0; i < raw.length; i++) {
            String side = (i == 0) ? raw[i].trim() : normalizeRightSide(raw[i]);
            if (!side.isEmpty()) parts.add(side);
        }
        for (int i = 0; i + 1 < parts.size(); i++) {
            String parentId = extractId(parts.get(i));
            String childId  = extractId(parts.get(i + 1));
            connections.computeIfAbsent(parentId, k -> new ArrayList<>());
            List<String> list = connections.get(parentId);
            if (!list.contains(childId)) list.add(childId); // Duplikate vermeiden
        }
    }


    private void putIfAbsentOrFail(Map<String, BehaviorTreeNode> nodes, String id, BehaviorTreeNode node) {
        if (nodes.putIfAbsent(id, node) != null) {
            throw new IllegalArgumentException("Duplicate node id: " + id);
        }
    }

    private void buildTreeStructure(Map<String, BehaviorTreeNode> nodes, Map<String, List<String>> connections) {
        Map<String,Integer> parentCount = new HashMap<>();
        for (var e : connections.entrySet()) {
            String parentId = e.getKey();
            BehaviorTreeNode parent = nodes.get(parentId);
            if (parent == null) throw new IllegalArgumentException("Unknown parent id: " + parentId);

            for (String childId : e.getValue()) {
                BehaviorTreeNode child = nodes.get(childId);
                if (child == null) throw new IllegalArgumentException("Unknown child id: " + childId);

                // nur Composite dürfen Kinder haben
                if (!(parent instanceof CompositeNode)) {
                    throw new IllegalArgumentException("Node '" + parentId + "' is not composite but has children");
                }
                ((CompositeNode) parent).addChild(child);

                parentCount.merge(childId, 1, Integer::sum);
                if (parentCount.get(childId) > 1)
                    throw new IllegalArgumentException("Node '" + childId + "' has multiple parents");
            }
        }
    }

    private BehaviorTreeNode findRootNode(Map<String, BehaviorTreeNode> nodes, Map<String, List<String>> connections) {
        if (nodes.isEmpty()) throw new IllegalArgumentException("No nodes parsed");

        if (connections.isEmpty() && nodes.size() == 1) {
            return nodes.values().iterator().next();
        }

        Set<String> children = new HashSet<>();
        connections.values().forEach(children::addAll);

        List<String> roots = new ArrayList<>();
        for (String id : nodes.keySet()) {
            if (connections.containsKey(id) && !children.contains(id)) roots.add(id);
        }
        if (roots.isEmpty()) throw new IllegalArgumentException("No root node found");
        if (roots.size() > 1) throw new IllegalArgumentException("Multiple roots found: " + roots);
        return nodes.get(roots.get(0));
    }

    private NodeBehavior createConditionBehavior(String conditionName) {
        String s = conditionName.trim();
        switch (s) {
            case "treeFront": return new TreeFront();
            case "leafFront": return new LeafFront();
            case "mushroomFront": return new MushroomFront();
            case "atEdge": return new AtEdge();
            default:
                if (s.startsWith("existsPath ")) {
                    String args = s.substring("existsPath ".length()).trim();
                    Matcher m2 = P_COORD2.matcher(args);
                    if (m2.matches()) {
                        int x1 = Integer.parseInt(m2.group(1)), y1 = Integer.parseInt(m2.group(2));
                        int x2 = Integer.parseInt(m2.group(3)), y2 = Integer.parseInt(m2.group(4));
                        return new ExistsPathBetween(x1,y1,x2,y2);
                    }
                    Matcher m1 = P_COORD.matcher(args);
                    if (m1.matches()) {
                        int x = Integer.parseInt(m1.group(1)), y = Integer.parseInt(m1.group(2));
                        return new ExistsPath(x,y);
                    }
                    throw new IllegalArgumentException("existsPath args invalid: '" + args + "'");
                }
                throw new IllegalArgumentException("Unknown condition: " + conditionName);
        }
    }

    private NodeBehavior createActionBehavior(String actionName) {
        String s = actionName.trim();
        switch (s) {
            case "move": return new Move();
            case "turnLeft": return new TurnLeft();
            case "turnRight": return new TurnRight();
            case "takeLeaf": return new TakeLeaf();
            case "placeLeaf": return new PlaceLeaf();
            default:
                if (s.startsWith("fly ")) {
                    String args = s.substring("fly ".length()).trim();
                    Matcher m = P_COORD.matcher(args);
                    if (!m.matches()) throw new IllegalArgumentException("fly args invalid: '" + args + "'");
                    int x = Integer.parseInt(m.group(1)), y = Integer.parseInt(m.group(2));
                    return new Fly(x,y);
                }
                throw new IllegalArgumentException("Unknown action: " + actionName);
        }
    }

    private void parseSingleTokenAsNode(String token, Map<String, BehaviorTreeNode> nodes) {
        Matcher m;
        if ((m = P_FALLBACK.matcher(token)).matches()) {
            putIfAbsentOrFail(nodes, m.group(1), new FallbackNode(m.group(1)));
            return;
        }
        if ((m = P_SEQUENCE.matcher(token)).matches()) {
            putIfAbsentOrFail(nodes, m.group(1), new SequenceNode(m.group(1)));
            return;
        }
        if ((m = P_PARALLEL.matcher(token)).matches()) {
            putIfAbsentOrFail(nodes, m.group(1), new ParallelNode(m.group(1), Integer.parseInt(m.group(2))));
            return;
        }
        if ((m = P_COND.matcher(token)).matches()) {
            String id = m.group(1), conditionName = m.group(2);
            NodeBehavior b = createConditionBehavior(conditionName);
            putIfAbsentOrFail(nodes, id, new LeafNode(id, b, LeafNode.LeafKind.CONDITION));
            return;
        }
        if ((m = P_ACT.matcher(token)).matches()) {
            String id = m.group(1), actionName = m.group(2);
            if (!"?".equals(actionName) && !"->".equals(actionName) && !actionName.matches("=\\d+>")) {
                NodeBehavior b = createActionBehavior(actionName);
                putIfAbsentOrFail(nodes, id, new LeafNode(id, b, LeafNode.LeafKind.ACTION));
            }
        }
    }

    // Entfernt trailing Kommentar %%...
    private String stripLineComment(String s) {
        int i = s.indexOf("%%");
        return (i >= 0) ? s.substring(0, i).trim() : s.trim();
    }

    // Macht aus " |label|  B[->] %% bla" -> "B[->]"
    private String normalizeRightSide(String t) {
        t = t.trim();
        if (t.startsWith("|")) {
            int idx = t.indexOf('|', 1);
            if (idx >= 0 && idx + 1 < t.length()) t = t.substring(idx + 1).trim();
            else return ""; // kaputtes Label -> ignorieren
        }
        int space = t.indexOf(' ');
        if (space > 0) t = t.substring(0, space).trim();
        return t;
    }

    // Holt die reine Node-ID aus einem Token wie "A[?]", "C([treeFront])", "B[->]" -> "A","C","B"
    private String extractId(String token) {
        Matcher m;
        if ((m = P_FALLBACK.matcher(token)).matches()) return m.group(1);
        if ((m = P_SEQUENCE.matcher(token)).matches()) return m.group(1);
        if ((m = P_PARALLEL.matcher(token)).matches()) return m.group(1);
        if ((m = P_COND.matcher(token)).matches()) return m.group(1);
        if ((m = P_ACT.matcher(token)).matches()) return m.group(1);
        // falls es nur eine rohe ID ist (z.B. "A")
        if (token.matches("^[A-Za-z_][A-Za-z0-9_]*$")) return token;
        throw new IllegalArgumentException("Invalid node token: '" + token + "'");
    }

    public static BehaviorTreeNode fromFile(String path) throws IOException {
        String content = Files.readString(Path.of(path));
        return new MermaidParser().parse(content);
    }

}
