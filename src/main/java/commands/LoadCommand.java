package commands;

import bt.BehaviorTreeNode;
import exceptions.BoardException;
import exceptions.CommandArgumentException;
import exceptions.LadybugException;
import exceptions.TreeParsingException;
import main.GameState;
import model.Board;
import model.Ladybug;
import model.LadybugPosition;
import parser.BoardParser;
import parser.MermaidParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Command to load board files and behavior tree files with separated terrain/ladybug handling.
 * <p>
 * This command supports two subcommands with improved separation logic:
 * - load board &lt;path&gt;: Loads terrain and extracts ladybug positions into registry
 * - load trees &lt;path1&gt; [&lt;path2&gt; ...]: Creates ladybugs from registry and assigns trees
 *
 * The new approach ensures deterministic ladybug ID assignment and eliminates
 * "ghost arrow" problems by keeping terrain and ladybugs properly separated.
 * </p>
 *
 * @author ujnaa
 */
public class LoadCommand implements Command {
    private final GameState state;

    /**
     * Creates a new LoadCommand.
     *
     * @param state the game state to load content into
     */
    public LoadCommand(GameState state) {
        this.state = state;
    }

    @Override
    public void execute(String[] args) throws BoardException,
            CommandArgumentException, TreeParsingException, LadybugException {
        if (args == null || args.length < 2) {
            throw new CommandArgumentException(getCommandName(), args, getUsage());
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "board" -> {
                if (args.length != 2) {
                    throw new CommandArgumentException(getCommandName(), args,
                            "Error, load board <path> | load trees <path...>");
                }
                loadBoardWithSeparation(args[1]);
            }
            case "trees" -> {
                if (args.length < 2) {
                    throw new CommandArgumentException(getCommandName(), args,
                            "Error, load board <path> | load trees <path...>");
                }
                loadTreesWithRegistryLogic(args);
            }
            default -> throw new CommandArgumentException(getCommandName(), args,
                    "Error, unknown subcommand: " + subCommand);
        }
    }

    /**
     * Loads a board file with intelligent terrain/ladybug separation.
     * <p>
     * This method:
     * 1. Uses the new BoardParser to separate terrain from ladybugs
     * 2. Creates a Board with clean terrain and ladybug registry
     * 3. Clears any existing trees (clean slate for new board)
     * </p>
     *
     * @param boardPath path to the board file
     * @throws BoardException if board loading fails
     * @throws LadybugException if board creation fails
     */
    private void loadBoardWithSeparation(String boardPath) throws BoardException, LadybugException {
        Path path = Path.of(boardPath);
        if (!Files.exists(path)) {
            throw new BoardException("Error, Board file not found: " + path);
        }

        try {
            // Use new parser with separation logic
            BoardParser.ParseResult parseResult = BoardParser.parseFile(path.toString());

            // Create board with separated data
            Board board = new Board(parseResult.getTerrainGrid(), parseResult.getLadybugRegistry());

            // Set the new board (this clears existing trees automatically)
            state.setBoard(board);

        } catch (IOException e) {
            throw new BoardException("Error, reading board file " + path + ": " + e.getMessage());
        }
    }

    /**
     * Loads behavior trees with registry-based ladybug management.
     * <p>
     * This method implements the new deterministic approach:
     * 1. Validates that a board with registry is loaded
     * 2. Parses and validates all tree files
     * 3. Creates ladybugs from registry in sorted order (deterministic IDs!)
     * 4. Assigns trees to ladybugs in the same order
     * 5. Removes excess ladybugs if fewer trees than positions
     * </p>
     *
     * @param args command arguments starting with "trees"
     * @throws BoardException if no board is loaded or other board errors
     * @throws CommandArgumentException if arguments are invalid
     * @throws TreeParsingException if tree parsing fails
     * @throws LadybugException if ladybug operations fail
     */
    private void loadTreesWithRegistryLogic(String[] args)
            throws BoardException, CommandArgumentException, TreeParsingException, LadybugException {

        // Validate preconditions
        validateBoardLoaded();

        // Extract tree file paths
        String[] treePaths = extractTreePaths(args);

        // Validate tree count vs available positions
        validateTreeCount(treePaths);

        // Parse and validate all trees first (fail fast)
        BehaviorTreeNode[] parsedTrees = parseAllTrees(treePaths);

        // Now we're committed: create ladybugs and assign trees
        createLadybugsFromRegistryAndAssignTrees(parsedTrees);
    }

    /**
     * Validates that a board with registry is loaded.
     */
    private void validateBoardLoaded() throws BoardException {
        if (state.getBoard() == null) {
            throw new BoardException("Error, no board loaded");
        }

        // Additional check: ensure we have the registry (new boards should have this)
        if (state.getBoard().getLadybugRegistry() == null) {
            throw new BoardException("Error, board has no ladybug registry (load board first)");
        }
    }

    /**
     * Extracts tree file paths from command arguments.
     */
    private String[] extractTreePaths(String[] args) throws CommandArgumentException {
        if (args.length < 2) {
            throw new CommandArgumentException(getCommandName(), args,
                    "Error, load trees requires at least one tree file");
        }

        String[] treePaths = new String[args.length - 1];
        System.arraycopy(args, 1, treePaths, 0, args.length - 1);
        return treePaths;
    }

    /**
     * Validates that we don't have more trees than available ladybug positions.
     */
    private void validateTreeCount(String[] treePaths) throws CommandArgumentException {
        int availablePositions = state.getBoard().getLadybugRegistry().getCount();

        if (treePaths.length > availablePositions) {
            throw new CommandArgumentException(getCommandName(), treePaths,
                    String.format("Error, too many trees (%d) for available ladybug positions (%d)",
                            treePaths.length, availablePositions));
        }
    }

    /**
     * Parses all tree files and validates them.
     * This uses a fail-fast approach: if any tree is invalid, the entire operation fails.
     */
    private BehaviorTreeNode[] parseAllTrees(String[] treePaths)
            throws TreeParsingException, CommandArgumentException {

        BehaviorTreeNode[] parsedTrees = new BehaviorTreeNode[treePaths.length];

        for (int i = 0; i < treePaths.length; i++) {
            Path treePath = Path.of(treePaths[i]);

            if (!Files.exists(treePath)) {
                throw new TreeParsingException("Error, tree file not found", treePath.toString());
            }

            try {
                // Read and display file content (required by specification)
                String content = Files.readString(treePath);
                System.out.println(content.trim()); // Verbatim output

                // Parse the tree
                BehaviorTreeNode tree = new MermaidParser().parse(content);

                // Validate tree has at least one action
                if (!hasActionNode(tree)) {
                    throw new TreeParsingException(
                            "Error, behavior tree must contain at least one action",
                            treePath.toString());
                }

                parsedTrees[i] = tree;

            } catch (IOException e) {
                throw new TreeParsingException("Error, cannot read tree file",
                        treePath.toString());
            }
        }

        return parsedTrees;
    }

    /**
     * Creates ladybugs from registry and assigns trees using the new deterministic approach.
     * <p>
     * This method implements the core logic:
     * 1. Clear existing trees and ladybugs
     * 2. Get sorted positions from registry (deterministic order!)
     * 3. Create ladybugs with sequential IDs (1, 2, 3, ...)
     * 4. Assign trees to ladybugs in the same order
     * 5. Handle excess positions (more positions than trees)
     * </p>
     */
    private void createLadybugsFromRegistryAndAssignTrees(BehaviorTreeNode[] parsedTrees)
            throws LadybugException {

        Board board = state.getBoard();

        // Clear existing state for clean slate
        state.clearTrees();
        board.getLadybugManager().clearAllLadybugs();

        // Get sorted positions from registry (this is the key for deterministic IDs!)
        List<LadybugPosition> sortedPositions = board.getLadybugRegistry().getPositions();

        // Create ladybugs for trees only (not all positions)
        for (int i = 0; i < parsedTrees.length; i++) {
            LadybugPosition position = sortedPositions.get(i);

            // Create ladybug with sequential ID
            int ladybugId = i + 1;
            Ladybug ladybug = new Ladybug(ladybugId, position.getPosition(), position.getDirection());

            // Add to board
            board.addLadybug(ladybug);

            // Assign tree
            state.addTree(ladybugId, parsedTrees[i]);
        }
    }

    /**
     * Checks recursively if the tree contains at least one action node.
     *
     * @param node the node to check
     * @return true if the tree contains at least one action node
     */
    private boolean hasActionNode(BehaviorTreeNode node) {
        if (node instanceof bt.LeafNode leaf) {
            return leaf.isAction();
        }
        for (BehaviorTreeNode child : node.getChildren()) {
            if (hasActionNode(child)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getCommandName() {
        return "load";
    }

    @Override
    public String getUsage() {
        return "load board <path> | load trees <path1> [<path2> ...]";
    }
}