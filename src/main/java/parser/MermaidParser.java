package parser;

import bt.*;
import exceptions.TreeParsingException;

import java.nio.file.Path;
import java.util.*;
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
    private static final Pattern P_PARALLEL =
            Pattern.compile("^([A-Za-z_][A-Za-z0-9_]*)\\[=\\s*([-+]?\\d+)\\s*>]$");
    private static final Pattern P_COND = Pattern.compile("^([A-Za-z_][A-Za-z0-9_]*)\\(\\[([^\\]]+)]\\)$");
    private static final Pattern P_ACT  = Pattern.compile("^([A-Za-z_][A-Za-z0-9_]*)\\[([^\\]]+)]$");
    private static final Pattern P_EDGE = Pattern.compile("^([A-Za-z_][A-Za-z0-9_]*)\\s*-->(?:\\|[^|]*\\|\\s*)?([A-Za-z_][A-Za-z0-9_]*)$");

    private static final Pattern P_COORD  = Pattern.compile("^(\\d+),(\\d+)$");
    private static final Pattern P_COORD2 = Pattern.compile("^(\\d+),(\\d+)\\s+(\\d+),(\\d+)$");

    /**
     * Parses Mermaid flowchart content into a behavior tree.
     *
     * @param mermaidContent Mermaid flowchart text to parse; must not be {@code null} or blank
     * @return the root {@link BehaviorTreeNode} of the parsed behavior tree
     * @throws TreeParsingException if the content is invalid or cannot be parsed
     * @see #parse(String, Path)
     */
    public BehaviorTreeNode parse(String mermaidContent) throws TreeParsingException {
        return parse(mermaidContent, null);
    }
    /**
     * Parses Mermaid flowchart content into a behavior tree.
     *
     * @param mermaidContent the Mermaid flowchart content to parse
     * @param filePath for the path
     * @return the root node of the parsed behavior tree
     * @throws TreeParsingException if the content is invalid or cannot be parsed0
     */
    public BehaviorTreeNode parse(String mermaidContent, Path filePath) throws TreeParsingException {
        if (mermaidContent == null || mermaidContent.isBlank()) {
            throw perr("Empty input", filePath);
        }

        List<String> lines = getStrings(mermaidContent); // deine Methode, liefert die relevanten Zeilen

        Map<String, BehaviorTreeNode> nodes = new LinkedHashMap<>();
        Map<String, List<String>> connections = new LinkedHashMap<>();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            int lineNo = i + 1; // 1-basiert für Fehlermeldungen

            String trimmedLine = line.trim();
            if (trimmedLine.isEmpty()) {
                continue;
            }

            parseLineForNodes(trimmedLine, lineNo, nodes);
            parseLineForConnections(trimmedLine, lineNo, connections);
        }

        buildTreeStructure(nodes, connections);
        return findRootNode(nodes, connections);
    }

    private static List<String> getStrings(String mermaidContent) throws TreeParsingException {
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
            throw perr("No 'flowchart' header found"); // wirft TreeParsingException
        }
        return lines.subList(flowIdx + 1, lines.size());
    }

    private void parseLineForNodes(String line, int lineNo,
                                   Map<String, BehaviorTreeNode> nodes) throws TreeParsingException {
        String cleanLine = stripLineComment(line);

        if (cleanLine.contains("-->")) {
            String[] raw = cleanLine.split("-->");
            for (int i = 0; i < raw.length; i++) {
                String token = (i == 0) ? raw[i].trim() : normalizeRightSide(raw[i]);
                if (!token.isEmpty()) {
                    parseSingleTokenAsNode(token, lineNo, nodes);
                }
            }
            return;
        }
        parseSingleTokenAsNode(cleanLine.trim(), lineNo, nodes);
    }
    private void parseLineForConnections(String line, int lineNo,
                                         Map<String, List<String>> connections) throws TreeParsingException {
        String cleanLine = stripLineComment(line);
        if (!cleanLine.contains("-->")) {
            return;
        }

        String[] raw = cleanLine.split("-->");
        List<String> parts = new ArrayList<>(raw.length);
        for (int i = 0; i < raw.length; i++) {
            String side = (i == 0) ? raw[i].trim() : normalizeRightSide(raw[i]);
            if (!side.isEmpty()) {
                parts.add(side);
            }
        }
        for (int i = 0; i + 1 < parts.size(); i++) {
            String parentId = extractId(parts.get(i), lineNo);
            String childId  = extractId(parts.get(i + 1), lineNo);
            connections.computeIfAbsent(parentId, k -> new ArrayList<>());
            List<String> list = connections.get(parentId);
            if (!list.contains(childId)) {
                list.add(childId);
            }
        }
    }
    private void putIfAbsentOrFail(Map<String, BehaviorTreeNode> nodes, String id, BehaviorTreeNode node, int lineNo)
            throws TreeParsingException {
        if (nodes.putIfAbsent(id, node) != null) {
            throw perr("duplicate node id '" + id + "' at line " + lineNo, lineNo);
        }
    }

    private void buildTreeStructure(Map<String, BehaviorTreeNode> nodes, Map<String, List<String>> connections)
            throws TreeParsingException {
        Map<String, Integer> parentCount = new HashMap<>();
        for (var e : connections.entrySet()) {
            String parentId = e.getKey();
            BehaviorTreeNode parent = nodes.get(parentId);
            if (parent == null) {
                throw perr("unknown parent id: " + parentId);
            }
            for (String childId : e.getValue()) {
                BehaviorTreeNode child = nodes.get(childId);
                if (child == null) {
                    throw perr("unknown child id: " + childId);
                }
                if (!(parent instanceof CompositeNode)) {
                    throw perr("node '" + parentId + "' is not composite but has children");
                }
                ((CompositeNode) parent).addChild(child);
                parentCount.put(childId, parentCount.getOrDefault(childId, 0) + 1);
                if (parentCount.get(childId) > 1) {
                    throw perr("node '" + childId + "' has multiple parents");
                }
            }
        }
    }

    private BehaviorTreeNode findRootNode(Map<String, BehaviorTreeNode> nodes, Map<String, List<String>> connections)
            throws TreeParsingException {
        if (nodes.isEmpty()) {
            throw perr("no nodes parsed");
        }

        if (connections.isEmpty() && nodes.size() == 1) {
            return nodes.values().iterator().next();
        }

        Set<String> children = new HashSet<>();
        for (List<String> childList : connections.values()) {
            children.addAll(childList);
        }

        List<String> roots = new ArrayList<>();
        for (String id : nodes.keySet()) {
            if (connections.containsKey(id) && !children.contains(id)) {
                roots.add(id);
            }
        }
        if (roots.isEmpty()) {
            throw perr("no root node found");
        }
        if (roots.size() > 1) {
            throw perr("multiple roots found: " + roots);
        }

        return nodes.get(roots.get(0));
    }

    private NodeBehavior createConditionBehavior(String conditionName, int lineNo) throws TreeParsingException {
        String trimmedName = conditionName.trim();
        switch (trimmedName) {
            case "treeFront": return new TreeFront();
            case "leafFront": return new LeafFront();
            case "mushroomFront": return new MushroomFront();
            case "atEdge": return new AtEdge();
            default:
                if (trimmedName.startsWith("existsPath ")) {
                    String args = trimmedName.substring("existsPath ".length()).trim();
                    Matcher m2 = P_COORD2.matcher(args);
                    if (m2.matches()) {
                        int x1 = Integer.parseInt(m2.group(1));
                        int y1 = Integer.parseInt(m2.group(2));
                        int x2 = Integer.parseInt(m2.group(3));
                        int y2 = Integer.parseInt(m2.group(4));
                        return new ExistsPathBetween(x1, y1, x2, y2);
                    }
                    Matcher m1 = P_COORD.matcher(args);
                    if (m1.matches()) {
                        int x = Integer.parseInt(m1.group(1));
                        int y = Integer.parseInt(m1.group(2));
                        return new ExistsPath(x, y);
                    }
                    throw perr("existsPath args invalid at line " + lineNo + ": '" + args + "'", lineNo);
                }
                throw perr("unknown condition at line " + lineNo + ": " + conditionName, lineNo);
        }
    }

    private NodeBehavior createActionBehavior(String actionName, int lineNo) throws TreeParsingException {
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
                    Matcher m = P_COORD.matcher(args);
                    if (!m.matches()) {
                        throw perr("fly args invalid at line " + lineNo + ": '" + args + "'", lineNo);
                    }
                    int x = Integer.parseInt(m.group(1));
                    int y = Integer.parseInt(m.group(2));
                    return new Fly(x, y);
                }
                throw perr("unknown action at line " + lineNo + ": " + actionName, lineNo);
        }
    }

    private void parseSingleTokenAsNode(String token, int lineNo, Map<String, BehaviorTreeNode> nodes) throws TreeParsingException {
        Matcher fallbackMatcher = P_FALLBACK.matcher(token);
        if (fallbackMatcher.matches()) {
            putIfAbsentOrFail(nodes, fallbackMatcher.group(1), new FallbackNode(fallbackMatcher.group(1)), lineNo);
            return;
        }
        Matcher sequenceMatcher = P_SEQUENCE.matcher(token);
        if (sequenceMatcher.matches()) {
            putIfAbsentOrFail(nodes, sequenceMatcher.group(1), new SequenceNode(sequenceMatcher.group(1)), lineNo);
            return;
        }
        Matcher parallelMatcher = P_PARALLEL.matcher(token);
        if (parallelMatcher.matches()) {
            String id = parallelMatcher.group(1);
            int m = Integer.parseInt(parallelMatcher.group(2));
            if (m <= 0) {
                // klare, testfreundliche Fehlermeldung
                throw perr("parallel threshold must be positive: " + m, lineNo);
            }
            putIfAbsentOrFail(nodes, id, new ParallelNode(id, m), lineNo);
            return;
        }
        Matcher condMatcher = P_COND.matcher(token);
        if (condMatcher.matches()) {
            String id = condMatcher.group(1);
            String conditionName = condMatcher.group(2);
            NodeBehavior behavior = createConditionBehavior(conditionName, lineNo);
            putIfAbsentOrFail(nodes, id, new LeafNode(id, behavior, LeafNode.LeafKind.CONDITION), lineNo);
            return;
        }
        Matcher actMatcher = P_ACT.matcher(token);
        if (actMatcher.matches()) {
            String id = actMatcher.group(1);
            String actionName = actMatcher.group(2);
            if (!"?".equals(actionName)
                    && !"->".equals(actionName)
                    && !actionName.matches("=\\s*[-+]?\\d+\\s*>")) {
                NodeBehavior behavior = createActionBehavior(actionName, lineNo);
                putIfAbsentOrFail(nodes, id, new LeafNode(id, behavior, LeafNode.LeafKind.ACTION), lineNo);
            }
            return;
        }
        if (token.matches("^[A-Za-z_][A-Za-z0-9_]*$")) {
            return; // nur referenziert; Definition erfolgt dort, wo ein Typ angegeben ist
        }
        throw perr("invalid node token at line " + lineNo + ": '" + token + "'", lineNo);
    }

    private String stripLineComment(String input) {
        int commentIndex = input.indexOf("%%");
        return (commentIndex >= 0) ? input.substring(0, commentIndex).trim() : input.trim();
    }

    private String normalizeRightSide(String input) {
        String s = input.trim();

        if (s.startsWith("|")) {
            int end = s.indexOf('|', 1);
            if (end >= 0 && end + 1 < s.length()) {
                s = s.substring(end + 1).trim();
            } else {
                return ""; // kaputtes Label -> ignorieren
            }
        }
        int comment = s.indexOf("%%");
        if (comment >= 0) {
            s = s.substring(0, comment).trim();
        }
        return s;
    }

    private String extractId(String token, int lineNo) throws TreeParsingException {
        Matcher m;
        m = P_FALLBACK.matcher(token);
        if (m.matches()) {
            return m.group(1);
        }
        m = P_SEQUENCE.matcher(token);
        if (m.matches()) {
            return m.group(1);
        }
        m = P_PARALLEL.matcher(token);
        if (m.matches()) {
            return m.group(1);
        }
        m = P_COND.matcher(token);
        if (m.matches()) {
            return m.group(1);
        }
        m = P_ACT.matcher(token);
        if (m.matches()) {
            return m.group(1);
        }
        if (token.matches("^[A-Za-z_][A-Za-z0-9_]*$")) {
            return token;
        }
        throw perr("invalid node token at line " + lineNo + ": '" + token + "'", lineNo);
    }

    private static TreeParsingException perr(String msg) {
        return new TreeParsingException(msg, (String) null);
    }

    private static TreeParsingException perr(String msg, int lineNo) {
        return new TreeParsingException(msg, /* invalidContent */ null, lineNo);
    }

    private static TreeParsingException perr(String msg, Path file) {
        return new TreeParsingException(msg, file);
    }
}