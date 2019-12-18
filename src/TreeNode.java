// TODO ALLES testen, dokumentieren, in 2 Klassen unterteilen?
// TODO keine board methoden, alles in board berechnen und alles setten und getten
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
        int counter = 0;
        for (int i = 0; i < Board.SIZE; i++) {
            for (int j = 0; j < Board.SIZE; j++) {
                if (reversiBoard.possibleTurn(i, j, nextPlayer)) {
                    ReversiBoard newBoard = reversiBoard.clone();
                    newBoard.getBoard()[i][j] = new PlayerTile(i, j,
                            nextPlayer);
                    newBoard.flipAllTiles(i, j, nextPlayer);
                    TreeNode child;
                    if (nextPlayer == Player.Human) {
                        child = new TreeNode(newBoard, level, depth + 1,
                                Player.Computer);
                    } else {
                        child = new TreeNode(newBoard, level, depth + 1,
                                Player.Human);
                    }
                    children[counter] = child;
                    counter++;
                }
            }
        }
        if (counter == 0) {
            if (nextPlayer == Player.Human) {
                nextPlayer = Player.Computer;
            } else {
                nextPlayer = Player.Human;
            }
            for (int i = 0; i < Board.SIZE; i++) {
                for (int j = 0; j < Board.SIZE; j++) {
                    if (reversiBoard.possibleTurn(i, j, nextPlayer)) {
                        ReversiBoard newBoard = reversiBoard.clone();
                        newBoard.getBoard()[i][j] = new PlayerTile(i, j,
                                nextPlayer);
                        newBoard.flipAllTiles(i, j, nextPlayer);
                        TreeNode child;
                        if (nextPlayer == Player.Human) {
                            child = new TreeNode(newBoard, level, depth + 1,
                                    Player.Computer);
                        } else {
                            child = new TreeNode(newBoard, level, depth + 1,
                                    Player.Human);
                        }
                        children[counter] = child;
                        counter++;
                    }
                }
            }
        }
    }

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
        if (!hasChildren() || getChildren()[0] == null) {
            this.score = reversiBoard.score();
            return this;
        } else {
            if (reversiBoard.next() == Player.Computer) {
                double maxScore = Integer.MIN_VALUE;
                TreeNode maxNode = null;
                for (TreeNode node: getChildren()) {
                    if (node == null) {
                        break;
                    }
                    node.minMaxAlgorithm();
                    if (maxScore < node.getScore()) {
                        maxScore = node.getScore();
                        maxNode = node;
                    }
                }
                this.score = reversiBoard.score() + maxScore;
                //System.out.println("mit kinder: " + score);
                return maxNode;
            } else {
                double minScore = Integer.MAX_VALUE;
                TreeNode minNode = null;
                for (TreeNode node: getChildren()) {
                    if (node == null) {
                        break;
                    }
                    node.minMaxAlgorithm();
                    if (minScore > node.getScore()) {
                        minScore = node.getScore();
                        minNode = node;
                    }
                }
                score = reversiBoard.score() + minScore;
                //System.out.println("mit kinder: " + score);
                return minNode;
            }
        }
    }
}
