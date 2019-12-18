/**
 * This class represents one player tile on the board. It has a defined
 * position and a player side (or colour)
 */

public class PlayerTile implements Cloneable{
    private final int row;           // Row position
    private final int column;        // Column position
    private Player player;           // Human or Computer side (blue or red)

    /**
     * Create a new player tile object on the field.
     * @param row position
     * @param column position
     * @param player imitates the colour. Can be Human or Computer
     */
    PlayerTile(final int row, final int column, final Player player) {
        this.row = row;
        this.column = column;
        this.player = player;
    }

    /**
     * Returns the row index.
     * @return row index
     */
    public int getRow() {
        return row;
    }

    /**
     * Returns the column index.
     * @return column index
     */
    public int getColumn() {
        return column;
    }

    /**
     * Returns the player who belongs this tile.
     * @return Human or Computer
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Change the player side to the opposite side.
     */
    public void changeSide() {
        if (player == Player.Computer) {
            player = Player.Human;
        } else {
            player = Player.Computer;
        }
    }

    /**
     * This method creates and returns a deep copy of this player tile.
     * @return A clone.
     */
    public PlayerTile clone() {
        PlayerTile cloneTile;
        try {
            cloneTile = (PlayerTile) super.clone();
            return cloneTile;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
