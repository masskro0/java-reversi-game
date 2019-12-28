package reversi.model;

/**
 * A node in the minimax algorithm tree. It knows its current board state,
 * children, local and global score.
 */
public class TreeNode {

    /**
     * A Reversi board object, which holds a specific game state.
     */
    private final ReversiBoard reversiBoard;

    /**
     * The local score of this board as calculated as in the score() method.
     */
    private double localScore;

    /**
     * The global score of this board as calculated as by the minimax
     * algorithm.
     */
    private double globalScore;

    /**
     * Children of {@code this} hold valid moves of this TreeNode's Reversi
     * board.
     */
    private TreeNode[] children;

    /**
     * Creates a TreeNode with a given game board and level. Represents a node
     * in the tree.
     *
     * @param reversiBoard A board of a specific game state.
     * @param level Level value of the given board.
     */
    TreeNode(ReversiBoard reversiBoard, int level) {
        this.reversiBoard = reversiBoard;
        localScore = 0;
        globalScore = 0;
        children = new TreeNode[Board.SIZE * Board.SIZE - 4];
    }

    /**
     * Sets the children of this node.
     *
     * @param children Children of {@code this} node.
     */
    void setChildren(TreeNode[] children) {
        this.children = children;
    }

    /**
     * Returns the game board of this node.
     *
     * @return A Reversi board of this node.
     */
    ReversiBoard getReversiBoard() {
        return reversiBoard;
    }

    /**
     * Sets the local score of the Reversi board of {@code this} node. The
     * value is the same as calculated in the score() method in the
     * ReversiBoard class.
     *
     * @param localScore Local score value of the current game board.
     */
    void setLocalScore(double localScore) {
        this.localScore = localScore;
    }

    /**
     * Creates a tree of Reversi boards and calculates the best move for a
     * certain level. A higher level results in a deeper tree. This algorithm
     * takes the best computer move and the worst human move, decided by
     * global scores. Leaves don't have children and have only a local score.
     *
     * @return ReversiBoard object with a executed move.
     */
    ReversiBoard minimaxAlgorithm() {
        if (children[0] == null) {
            globalScore = localScore;
            return reversiBoard;
        } else {
            if (reversiBoard.next() == Player.Computer) {
                double maxScore = Integer.MIN_VALUE;
                ReversiBoard bestBoard = null;
                for (TreeNode child: children) {
                    if (child == null) {
                        break;
                    }
                    child.minimaxAlgorithm();
                    if (maxScore < child.globalScore) {
                        maxScore = child.globalScore;
                        bestBoard = child.reversiBoard;
                    }
                }
                globalScore = localScore + maxScore;
                return bestBoard;
            } else {
                double minScore = Integer.MAX_VALUE;
                ReversiBoard worstBoard = null;
                for (TreeNode child: children) {
                    if (child == null) {
                        break;
                    }
                    child.minimaxAlgorithm();
                    if (minScore > child.globalScore) {
                        minScore = child.globalScore;
                        worstBoard = child.reversiBoard;
                    }
                }
                globalScore = localScore + minScore;
                return worstBoard;
            }
        }
    }
}
