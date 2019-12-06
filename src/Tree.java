public class Tree {

    private PlayerTile root;
    private final int MAXCHILDREN = Board.SIZE - 4;
    private PlayerTile[] children;
    private PlayerTile parent;

    Tree (PlayerTile root) {
        this.root = root;
        children = new PlayerTile[MAXCHILDREN];
    }

    void setChild(TreeNode parentNode, TreeNode childNode) {
        int index = 0;
        while (parentNode.children[index] != null) {
            ++index;
        }
        parentNode.child[index] = childNode;
        childNode.parent = parentNode;
    }
}
