package reversi.model;

/**
 * An enum for all eight directions. Each direction contains a (y, x) or
 * (row, column) vector, which can be used to iterate in this direction on the
 * game board.
 */
public enum Directions {
    NORTH       (-1, 0),
    NORTHEAST   (-1, 1),
    EAST        (0, 1),
    SOUTHEAST   (1, 1),
    SOUTH       (1, 0),
    SOUTHWEST   (1, -1),
    WEST        (0, -1),
    NORTHWEST   (-1, -1);

    /**
     * Row index or the y-coordinate of a direction.
     */
    final int row;

    /**
     * Column index or the x-coordinate of a direction.
     */
    final int column;

    private Directions(int row, int column) {
        this.row = row;
        this.column = column;
    }
}
