package reversi.model;

public enum Directions {
    NORTH       (-1, 0),
    NORTHEAST   (-1, 1),
    EAST        (0, 1),
    SOUTHEAST   (1, 1),
    SOUTH       (1, 0),
    SOUTHWEST   (1, -1),
    WEST        (0, -1),
    NORTHWEST   (-1, -1);

    final int row;
    final int column;

    private Directions(int row, int column) {
        this.row = row;
        this.column = column;
    }
}
