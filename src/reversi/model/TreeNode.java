package reversi.model;

public class TreeNode {

    private final ReversiBoard reversiBoard;
    private final TreeNode[] children;
    private final int level;
    private final int depth;
    private Player nextPlayer;
    private double score;

    TreeNode(ReversiBoard reversiBoard, int level, int depth, Player player) {
        // Only the reference is needed here
        this.reversiBoard = reversiBoard;
        children = new TreeNode[Board.SIZE * Board.SIZE - 4];
        this.level = level;
        this.depth = depth;
        this.nextPlayer = player;
        this.score = 0;
        if (depth < level - 1) {
            setChildren(reversiBoard);
        }
    }

    public TreeNode[] getChildren() {
        return children;
    }

    private void setChildren(ReversiBoard reversiBoard) {
    }
/*
    private boolean hasChildren() {
        return getChildren().length != 0;
    }

    public Player getNextPlayer() {
        return nextPlayer;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double scoreValue) {
        this.score = scoreValue;
    }

    ReversiBoard getReversiBoard() {
        return reversiBoard;
    }

    public TreeNode minMaxAlgorithm() {
    }*/
}
