package reversi.model;

/**
 * This class represents one player tile on the Reversi board. It has a defined
 * position and an owner.
 */

public class PlayerTile implements Cloneable {

    /**
     * Defines the row index of this tile on the Reversi board.
     */
    private final int row;

    /**
     * Defines the column index of this tile on the Reversi board.
     */
    private final int column;

    /**
     * Specifies the owner of this tile, can be computer or human.
     */
    private Player player;

    /**
     * Create a new player tile object on the board.
     *
     * @param row Row index on the board.
     * @param column Column index on the board.
     * @param player Can be Human or Computer.
     */
    // TODO private
    public PlayerTile(final int row, final int column, final Player player) {
        this.row = row;
        this.column = column;
        this.player = player;
    }

    /**
     * Returns the row index.
     *
     * @return Row index of this tile.
     */
    public int getRow() {
        return row;
    }

    /**
     * Returns the column index.
     *
     * @return Column index of this tile.
     */
    public int getColumn() {
        return column;
    }

    /**
     * Returns the player who belongs this tile.
     *
     * @return Can be Human or Computer.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Change the owner of this tile.
     */
    public void changeSide() {
        if (player == Player.Computer) {
            player = Player.Human;
        } else {
            player = Player.Computer;
        }
    }

    /**
     * This method creates and returns a deep copy of this PlayerTile object.
     *
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
