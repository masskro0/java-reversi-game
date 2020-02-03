package model;

/**
 * An enum for all eight directions. Each direction contains a (y, x) or
 * (row, column) vector, which can be used to iterate in this direction on the
 * game board. The origin is on the upper left corner.
 */
public enum Directions {

    /**
     * North direction, the row index will be decreased.
     */
    NORTH(-1, 0),

    /**
     * North-east direction, the row index will be decreased and column index
     * increased.
     */
    NORTHEAST(-1, 1),

    /**
     * East direction, the column index will be increased.
     */
    EAST(0, 1),

    /**
     * South-east direction, the row and column index will be increased.
     */
    SOUTHEAST(1, 1),

    /**
     * South direction, the row index will be increased.
     */
    SOUTH(1, 0),

    /**
     * South-west direction, the row index will be increased and the column
     * index decreased.
     */
    SOUTHWEST(1, -1),

    /**
     * West direction, the column index will be decreased.
     */
    WEST(0, -1),

    /**
     * North-west direction, the row and column index will be decreased.
     */
    NORTHWEST(-1, -1);

    /**
     * Row index or the y-coordinate of a direction.
     */
    private final int row;

    /**
     * Column index or the x-coordinate of a direction.
     */
    private final int column;

    /**
     * Gets the row value of a direction.
     *
     * @return The row value of a direction.
     */
    int getRow() {
        return row;
    }

    /**
     * Gets the column value of a direction.
     *
     * @return The column value of a direction.
     */
    int getColumn() {
        return column;
    }

    /**
     * Creates a new direction.
     *
     * @param row Row value of a direction.
     * @param column Column value of a direction.
     */
    Directions(final int row, final int column) {
        this.row = row;
        this.column = column;
    }
}
