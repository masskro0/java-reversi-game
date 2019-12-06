public class TreeNode {

    private PlayerTile playerTile;
    private PlayerTile[] children;
    private TreeNode parent;
    private double score;

    TreeNode(PlayerTile playerTile) {
        this.playerTile = playerTile;
        children = new PlayerTile[Board.SIZE - 4];
        score = 0;
    }

    void setParent(TreeNode parent) {
        this.parent = parent;
    }

    public PlayerTile[] getChildren() {
        return children;
    }
}
