package reversi.model;

public class TreeNode {

    private final ReversiBoard reversiBoard;
    private final PlayerTile[][] board;
    private ReversiBoard[] children;
    private double score;
    private int level;

    TreeNode(ReversiBoard reversiBoard, ReversiBoard[] children,
             double score, int level) {
        // Only the reference is needed here
        this.reversiBoard = reversiBoard;
        board = new PlayerTile[Board.SIZE][Board.SIZE];
        children = new ReversiBoard[Board.SIZE * Board.SIZE - 4];
        this.score = score;
        this.level = level;
    }

    void setChildren(ReversiBoard[] children) {
        this.children = children;
    }

    void setScore(double score) {
        this.score = score;
    }

    // TODO portieren
    ReversiBoard miniMaxAlgorithm() {
        if (children[0] != null) {
            if (reversiBoard.next() == Player.Computer) {
                double maxScore = Integer.MIN_VALUE;
                ReversiBoard maxNode = null;
                for (ReversiBoard board: children) {
                    if (board == null) {
                        break;
                    }
                    board.minmaxalg();
                    if (maxScore < score) {
                        maxScore = score;
                        maxNode = board;
                    }
                }
                this.score = score + maxScore;
                return maxNode;
            } else {
                double minScore = Integer.MAX_VALUE;
                ReversiBoard minNode = null;
                for (ReversiBoard board: children) {
                    if (board == null) {
                        break;
                    }
                    board.minmaxalg();
                    if (minScore > score) {
                        minScore = score;
                        minNode = board;
                    }
                }
                this.score = score + minScore;
                return minNode;
            }
        }
        return null;
    }
}
