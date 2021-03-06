package model;

/**
 * This class implements the interface Board and represents a Reversi board
 * with all needed functionalities to play a game and to communicate with the
 * Shell or the UI interface.
 */
public class ReversiBoard implements Board {

    /**
     * A two-dimensional matrix containing all PlayerTile objects and can be
     * seen as the board surface.
     */
    private PlayerTile[][] field;

    /**
     * The game's difficulty. Initial value is 3.
     */
    private int level;

    /**
     * The player who makes the first move in the game.
     */
    private final Player firstPlayer;

    /**
     * The game state of this board.
     */
    private GameState gameState;

    /**
     * The player who should make the next turn. It is always set as the
     * opposite side of the player who made the previous turn.
     */
    private Player nextTurn;

    /**
     * A two-dimensional matrix containing score values for each field of a
     * Reversi board.
     */
    private final int[][] scoreBoard;

    /**
     * Creates a new game with a new Reversi board. This constructor lets the
     * human make the initial move.
     */
    public ReversiBoard() {
        field = new PlayerTile[SIZE][SIZE];
        firstPlayer = Player.HUMAN;
        nextTurn = firstPlayer;
        initializeBoard();
        gameState = GameState.RUNNING;
        level = 3;
        scoreBoard = initScoreBoard();
    }

    /**
     * Creates a new game with a new Reversi board. This constructor lets a
     * given player make the first move.
     *
     * @param oldBoard Previous used ReversiBoard object.
     * @param firstPlayer Player to make the initial move, Computer or Human.
     */
    public ReversiBoard(ReversiBoard oldBoard, final Player firstPlayer) {
        field = new PlayerTile[SIZE][SIZE];
        this.firstPlayer = firstPlayer;
        nextTurn = firstPlayer;
        initializeBoard();
        gameState = GameState.RUNNING;
        setLevel(oldBoard.level);
        scoreBoard = initScoreBoard();
    }

    /**
     * Initializes the scoreboard with the given score values.
     *
     * @return Matrix with score values.
     */
    private int[][] initScoreBoard() {
        return new int[][]{
                {9999, 5, 500, 200, 200, 500, 5, 9999},
                {5, 1, 50, 150, 150, 50, 1, 5},
                {500, 50, 250, 100, 100, 250, 50, 500},
                {200, 150, 100, 50, 50, 100, 150, 200},
                {200, 150, 100, 50, 50, 100, 150, 200},
                {500, 50, 250, 100, 100, 250, 50, 500},
                {5, 1, 50, 150, 150, 50, 1, 5},
                {9999, 5, 500, 200, 200, 500, 5, 9999}
                };
    }

    /**
     * Place the first four tiles on the board, 2 human and 2 computer tiles.
     * Depending on who starts, the structure varies.
     */
    private void initializeBoard() {
        int i = SIZE / 2;
        int j = SIZE / 2 - 1;
        if (firstPlayer == Player.HUMAN) {
            field[i][j] = new PlayerTile(i, j, Player.HUMAN);
            field[j][i] = new PlayerTile(j, i, Player.HUMAN);
            field[j][j] = new PlayerTile(j, j, Player.COMPUTER);
            field[i][i] = new PlayerTile(i, i, Player.COMPUTER);
        } else {
            field[i][j] = new PlayerTile(i, j, Player.COMPUTER);
            field[j][i] = new PlayerTile(j, i, Player.COMPUTER);
            field[j][j] = new PlayerTile(j, j, Player.HUMAN);
            field[i][i] = new PlayerTile(i, i, Player.HUMAN);
        }
    }

    /**
     * Calculates the scoreT, scoreM and scoreP and returns the local score of
     * {@code this}. ScoreT measures the importance of occupied fields, scoreM
     * assesses the number of possible moves and scoreP a board state to
     * achieve a large number of possible moves in future game rounds.
     *
     * @return Local score value of this board.
     */
    private double score() {
        int tScoreComputer = 0;
        int tScoreHuman = 0;
        int mScoreComputer = 0;
        int mScoreHuman = 0;
        int pScoreHuman = 0;
        int pScoreComputer = 0;
        int occupiedFields = getNumberOfHumanTiles()
                + getNumberOfMachineTiles();

        // Iterate over the board to get the values for score calculation.
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (getSlot(i, j) == Player.HUMAN) {
                    tScoreHuman += scoreBoard[i][j];
                    pScoreHuman += countEmptyFieldsAroundTile(i, j);
                } else if (getSlot(i, j) == Player.COMPUTER) {
                    tScoreComputer += scoreBoard[i][j];
                    pScoreComputer += countEmptyFieldsAroundTile(i, j);
                }
                if (validMove(i, j, Player.HUMAN)) {
                    mScoreHuman++;
                }
                if (validMove(i, j, Player.COMPUTER)) {
                    mScoreComputer++;
                }
            }
        }
        double scoreT = (tScoreComputer * 1.0) - 1.5 * (tScoreHuman * 1.0);
        double scoreM = (64.0 / occupiedFields) * (3.0 * mScoreComputer
                        - 4.0 * mScoreHuman);
        double scoreP = (64.0 / (2.0 * occupiedFields)) * (2.5 * pScoreHuman
                        - 3.0 * pScoreComputer);
        return scoreT + scoreM + scoreP;
    }

    /**
     * Counts and returns the number of not occupied fields around a tile.
     *
     * @param row The row of the tile in the game grid.
     * @param column The column of the tile in the game grid.
     * @return The number of not occupied fields.
     */
    private int countEmptyFieldsAroundTile(int row, int column) {
        int counter = 0;
        for (Directions direction: Directions.values()) {
            int rowDir = row + direction.getRow();
            int columnDir = column + direction.getColumn();
            boolean validRowIndex = (rowDir >= 0 && rowDir < SIZE);
            boolean validColumnIndex = (columnDir >= 0 && columnDir < SIZE);
            if (validRowIndex && validColumnIndex
                    && getSlot(rowDir, columnDir) == Player.NOBODY) {
                counter++;
            }
        }
        return counter;
    }

    /**
     * Collects and returns all tiles, which belong to the same player, are not
     * a direct neighbour to a given tile and are reachable in any of the eight
     * direction. By setting the flip value to true, all enemy tiles between
     * the found and given tile can be flipped.
     *
     * @param row The row of a given tile in the game grid.
     * @param column The column of a given tile in the game grid.
     * @param player The player who belongs a given tile.
     * @param flip {@code true} if tiles should be flipped.
     * @return Array of reachable tiles of the same player.
     */
    private PlayerTile[] validTiles(int row, int column, Player player,
                                    boolean flip) {
        PlayerTile[] validTiles = new PlayerTile[Directions.values().length];
        int counter = 0;
        for (Directions direction: Directions.values()) {
            boolean validTile = false;
            int rowDir = row + direction.getRow();
            int columnDir = column + direction.getColumn();
            boolean validRowIndex = (rowDir >= 0 && rowDir < SIZE);
            boolean validColumnIndex = (columnDir >= 0 && columnDir < SIZE);
            if (!validRowIndex || !validColumnIndex
                    || getSlot(rowDir, columnDir) == Player.NOBODY
                    || getSlot(rowDir, columnDir) == player) {
                continue;
            }
            while (validRowIndex && validColumnIndex
                    && getSlot(rowDir, columnDir) != Player.NOBODY) {
                // Tile belongs to the same player and is not direct neighbour?
                if (getSlot(rowDir, columnDir) == player
                        && (Math.abs(rowDir - (row)) > 1
                        || Math.abs(columnDir - (column)) > 1)) {
                    validTiles[counter] = field[rowDir][columnDir];
                    counter++;
                    validTile = true;
                    break;
                }
                rowDir += direction.getRow();
                columnDir += direction.getColumn();
                validRowIndex = (rowDir >= 0 && rowDir < SIZE);
                validColumnIndex = (columnDir >= 0 && columnDir < SIZE);
            }
            if (flip && validTile) {
                rowDir -= direction.getRow();
                columnDir -= direction.getColumn();

                // Flip all enemy tiles between the given and found tile.
                while (getSlot(rowDir, columnDir) != player) {
                    field[rowDir][columnDir].changeSide();
                    rowDir -= direction.getRow();
                    columnDir -= direction.getColumn();
                }
            }
        }
        return validTiles;
    }

    /**
     * This method checks if a player can make a specific move. The given slot
     * must be empty and at least one tile must be flipped.
     *
     * @param row Row index of the potential move.
     * @param column Column index of the potential move.
     * @param player A player who wants to make this move.
     * @return {@code true} if and only if the move is valid.
     */
    private boolean validMove(int row, int column, Player player) {
        return getSlot(row, column) == Player.NOBODY
                && validTiles(row, column, player, false)[0] != null;
    }

    /**
     * Checks, if a player can make any valid moves.
     *
     * @param player A player who needs to be checked for valid moves.
     * @return {@code true} if and only if a player has valid moves.
     */
    private boolean hasMoves(Player player) {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (validMove(i, j, player)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Captures all possible moves of a specific player for a Reversi board
     * of a TreeNode and stores them in a TreeNode array as children. This
     * method is recursively called, until the depth of the tree is as big as
     * the level.
     *
     * @param depth The actual depth of the tree.
     */
    private void setChildren(TreeNode node, int depth) {
        TreeNode[] children = new TreeNode[SIZE * SIZE - 4];
        if (depth < level) {
            int counter = 0;
            Player nextTurn = next();

            // Check each field for a valid move and store it.
            for (int i = 0; i < Board.SIZE; i++) {
                for (int j = 0; j < Board.SIZE; j++) {
                    if (validMove(i, j, nextTurn)) {
                        ReversiBoard newBoard = clone();
                        newBoard.field[i][j] = new PlayerTile(i, j,
                                nextTurn);
                        newBoard.validTiles(i, j, nextTurn, true);
                        if (nextTurn == Player.HUMAN) {
                            newBoard.nextTurn = Player.COMPUTER;
                        } else {
                            newBoard.nextTurn = Player.HUMAN;
                        }
                        TreeNode child = new TreeNode(newBoard, level);
                        children[counter] = child;
                        counter++;
                    }
                }
            }
            if (counter != 0) {
                ++depth;
                for (TreeNode child: children) {
                    if (child != null) {
                        child.getReversiBoard().setChildren(child, depth);
                    }
                }
                node.setChildren(children);
            }
        }
        node.setLocalScore(score());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Player getFirstPlayer() {
        return firstPlayer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Player next() {
        if (getNumberOfHumanTiles() + getNumberOfMachineTiles() == 4) {
            // Initial board has two human and two computer tiles.
            return firstPlayer;
        } else if (nextTurn == Player.COMPUTER && hasMoves(Player.COMPUTER)) {
            return Player.COMPUTER;
        } else if (nextTurn == Player.HUMAN && hasMoves(Player.HUMAN)) {
            return Player.HUMAN;
        } else if (hasMoves(Player.HUMAN) && !hasMoves(Player.COMPUTER)) {
            return Player.HUMAN;
        }  else if (hasMoves(Player.COMPUTER) && !hasMoves(Player.HUMAN)) {
            return Player.COMPUTER;
        }

        // Nobody can make a turn.
        gameState = GameState.OVER;
        return Player.NOBODY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReversiBoard move(int row, int col) {
        // Indices within board dimensions?
        if (row < 0 || row >= Board.SIZE || col < 0 || col >= Board.SIZE) {
            throw new IllegalArgumentException("Row and column indices must be"
                    + " in the range between 1 and " + Board.SIZE);
        }
        if (gameState == GameState.OVER || next() != Player.HUMAN) {
            throw new IllegalMoveException("It's not your turn!");
        } else {
            if (getSlot(row, col) == Player.NOBODY && next() == Player.HUMAN
                    && validMove(row, col, Player.HUMAN)) {
                ReversiBoard newBoard = clone();
                newBoard.field[row][col]
                        = new PlayerTile(row, col, Player.HUMAN);
                newBoard.validTiles(row, col, Player.HUMAN, true);
                newBoard.nextTurn = Player.COMPUTER;
                return newBoard;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReversiBoard machineMove() {
        if (gameState == GameState.OVER || next() != Player.COMPUTER) {
            throw new IllegalMoveException("Machine made a move twice.");
        } else {
            TreeNode root = new TreeNode(this, level);
            setChildren(root, 0);
            ReversiBoard bestBoard = root.minimaxAlgorithm();
            if (bestBoard != null) {
                return bestBoard;
            } else {
                nextTurn = Player.HUMAN;
                return this;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLevel(int level) {
        this.level = level;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean gameOver() {
        return gameState == GameState.OVER
                || (!hasMoves(Player.HUMAN) && !hasMoves(Player.COMPUTER));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Player getWinner() {
        if (gameOver()) {
            if (getNumberOfHumanTiles() > getNumberOfMachineTiles()) {
                return Player.HUMAN;
            } else if (getNumberOfHumanTiles() < getNumberOfMachineTiles()) {
                return Player.COMPUTER;
            } else {
                return Player.NOBODY;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfHumanTiles() {
        int numberOfTiles = 0;
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (field[i][j] != null
                        && field[i][j].getPlayer() == Player.HUMAN) {
                    numberOfTiles++;
                }
            }
        }
        return numberOfTiles;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfMachineTiles() {
        int numberOfTiles = 0;
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (field[i][j] != null
                        && field[i][j].getPlayer() == Player.COMPUTER) {
                    numberOfTiles++;
                }
            }
        }
        return numberOfTiles;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Player getSlot(final int row, final int col) {
        if (field[row][col] != null) {
            return field[row][col].getPlayer();
        } else {
            return Player.NOBODY;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReversiBoard clone() {
        ReversiBoard copyBoard = new ReversiBoard(this, firstPlayer);
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (field[i][j] != null) {

                    // Create a deep copy of every tile.
                    PlayerTile copyTile = field[i][j].clone();
                    copyBoard.field[i][j] = copyTile;
                }
            }
        }
        copyBoard.setLevel(level);
        copyBoard.nextTurn = nextTurn;
        copyBoard.gameState = gameState;
        return copyBoard;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder bob = new StringBuilder();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (field[i][j] == null) {
                    bob.append('.');
                } else if (field[i][j].getPlayer() == Player.HUMAN) {
                    bob.append('X');
                } else {
                    bob.append('O');
                }
                if (j != field[i].length - 1) {
                    bob.append(" ");
                }
            }

            // Start a new line for the next row.
            bob.append(System.getProperty("line.separator"));
        }
        return bob.toString();
    }
}