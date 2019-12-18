import java.security.Signature;

/**
 * This class implements the interface Board. This class represents the
 * Reversi board with all needed functionalities to communicate with the
 * Shell or the UI interface.
 */
// TODO evtl scoreberechnung in eigene klasse?
public class ReversiBoard implements Board {

    private PlayerTile[][] board;           // Matrix with player tiles
    private final int DEFAULT_LEVEL = 3;    // Default level is three
    private int level = DEFAULT_LEVEL;      // Set the default level
    private final Player firstPlayer;       // Player who makes the inital move
    private GameState gameState;            // Current game state
    // Stores the player who made the previous move
    private Player nextTurn;
    private double score;
    // Matrix with score values
    private final int[][] scoreBoard = initScoreBoard();
    private ReversiBoard[] children;

    /**
     * Creates a new game with a new Reversi board. This constructor lets the
     * human make the initial move.
     */
    public ReversiBoard() {
        board = new PlayerTile[SIZE][SIZE];
        firstPlayer = Player.Human;
        nextTurn = firstPlayer;
        initializeBoard();
        gameState = GameState.RUNNING;
        children = new ReversiBoard[SIZE * SIZE - 4];
    }

    /**
     * Creates a new game with a new Reversi board. This constructor lets a
     * given player make the first move.
     * @param firstPlayer Human or Computer
     */
    public ReversiBoard(final Player firstPlayer) {
         board = new PlayerTile[SIZE][SIZE];
         this.firstPlayer = firstPlayer;
         nextTurn = firstPlayer;
         initializeBoard();
         gameState = GameState.RUNNING;
    }

    /**
     * Initializes the scoreboard with the given point values.
     * @return matrix with point values
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
     * Return the board matrix. This is private since this is only needed for
     * the ReversiBoard class
     * @return board as two dimensional array
     */
    // TODO private und nachfragen was man machen kann
    PlayerTile[][] getBoard() {
        return board;
    }

    /**
     * Place the first four tiles on the board, 2 human and 2 computer tiles.
     * Depending on who starts, the structure varies
     */
    private void initializeBoard() {
        int i = SIZE / 2;
        int j = SIZE / 2 - 1;
        if (firstPlayer == Player.Human) {
            board[i][j] = new PlayerTile(i, j, Player.Human);
            board[j][i] = new PlayerTile(j, i, Player.Human);
            board[j][j] = new PlayerTile(j, j, Player.Computer);
            board[i][i] = new PlayerTile(i, i, Player.Computer);
        } else {
            board[i][j] = new PlayerTile(i, j, Player.Computer);
            board[j][i] = new PlayerTile(j, i, Player.Computer);
            board[j][j] = new PlayerTile(j, j, Player.Human);
            board[i][i] = new PlayerTile(i, i, Player.Human);
        }
    }

    // TODO dokumentieren aller scores, testen und auch nicht private...

    /**
     * This method calculates and returns the overall score of this board. It
     * calculates the scoreT, scoreM and scoreP to get the overall score.
     * @return score value of this board.
     */
    double score() {
        int tScoreComputer = 0;
        int tScoreHuman = 0;
        int mScoreComputer = 0;
        int mScoreHuman = 0;
        int pScoreHuman = 0;
        int pScoreComputer = 0;
        double occupiedFields = getNumberOfHumanTiles() * 1.0
                + getNumberOfMachineTiles() * 1.0;
        // Iterate over the whole board
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                // Get the necessary infos for tScore and pScore of all players
                if (getSlot(i, j) == Player.Human) {
                    tScoreHuman += scoreBoard[i][j];
                    pScoreHuman += countEmptyFieldsAroundTile(i, j);
                } else if (getSlot(i, j) == Player.Computer) {
                    tScoreComputer += scoreBoard[i][j];
                    pScoreComputer += countEmptyFieldsAroundTile(i, j);
                }
                // Get the necessary infos for mScore of all players
                if (possibleTurn(i, j, Player.Human)) {
                    mScoreHuman++;
                }
                if (possibleTurn(i, j, Player.Computer)) {
                    mScoreComputer++;
                }
            }
        }
        double scoreT = (tScoreComputer * 1.0) - 1.5 * (tScoreHuman * 1.0);
        double scoreM = (64.0 / occupiedFields) * (3.0 * mScoreComputer
                        - 4.0 * mScoreHuman);
        double scoreP = (64.0 / (2.0 * occupiedFields)) * (2.5 * pScoreHuman
                        - 3.0 * pScoreComputer);
        this.score = scoreT + scoreM + scoreP;
        return scoreT + scoreM + scoreP;
    }

    // TODO da muss ne bessere lösung her und Doku
    private int countEmptyFieldsAroundTile(int row, int column) {
        int counter = 0;
        if (row > 0 && getSlot(row - 1, column) == null) {
            counter++;
        }
        if (row > 0 && column < SIZE - 1
                && getSlot(row - 1, column + 1) == null) {
            counter++;
        }
        if (column < SIZE - 1 && getSlot(row, column + 1) == null) {
            counter++;
        }
        if (row < SIZE - 1 && column < SIZE - 1
                && getSlot(row + 1, column + 1) == null) {
            counter++;
        }
        if (row < SIZE - 1 && getSlot(row + 1, column) == null) {
            counter++;
        }
        if (column > 0 && row < SIZE - 1
                && getSlot(row + 1, column - 1) == null) {
            counter++;
        }
        if (column > 0 && getSlot(row, column - 1) == null) {
            counter++;
        }
        if (row > 0 && column > 0
                && getSlot(row - 1, column - 1) == null) {
            counter++;
        }
        return counter;
    }

    /**
     * This method checks if a player tile of the same player exists in the
     * positive y-axis direction, which is not his direct neighbour.
     * @param row Index of row position of a given reference tile
     * @param column Index of column position of a given reference tile
     * @param player Kind of player who owns the reference tile
     * @return player tile of the same player, who is not his direct neighbour
     * or null if he didn't one
     */
    private PlayerTile northDirection(int row, int column, Player player) {
        for (int r = row; r >= 0; r--) {
            // Did the searching algorithm reached the edge of the board, found
            // a empty field or a tile of the same player as the direct
            // neighbour in this direction?
            if (r == 0 || getSlot(r - 1, column) == null
                    || getSlot(row - 1, column) == player) {
                break;
            }
            // Found a player tile of the same player which is not the direct
            // neighbour?
            if (r < row && getSlot(r - 1, column) == player) {
                return board[r - 1][column];
            }
        }
        return null;
    }

    /**
     * This method checks if a player tile of the same player exists in the
     * positive x-axis and y-axis direction, which is not his direct neighbour.
     * @param row Index of row position of a given reference tile
     * @param column Index of column position of a given reference tile
     * @param player Kind of player who owns the reference tile
     * @return player tile of the same player, who is not his direct neighbour
     * or null if he didn't one
     */
    private PlayerTile northEastDirection(int row, int column, Player player) {
        int r = row;        // variable row shouldn't be changed
        int c = column;     // variable column shouldn't be changed
        while (r >= 0 && c < SIZE) {
            // Did the searching algorithm reached the edge of the board, found
            // a empty field or a tile of the same player as the direct
            // neighbour in this direction?
            if (r == 0 || c == SIZE - 1 || getSlot(r - 1, c + 1) == null
                    || getSlot(row - 1, column + 1) == player) {
                break;
            }
            // Found a player tile of the same player which is not the direct
            // neighbour?
            if (r < row && c > column && getSlot(r - 1, c + 1) == player) {
                return board[r - 1][c + 1];
            }
            r--;
            c++;
        }
        return null;
    }

    /**
     * This method checks if a player tile of the same player exists in the
     * positive x-axis direction, which is not his direct neighbour.
     * @param row Index of row position of a given reference tile
     * @param column Index of column position of a given reference tile
     * @param player Kind of player who owns the reference tile
     * @return player tile of the same player, who is not his direct neighbour
     * or null if he didn't one
     */
    private PlayerTile eastDirection(int row, int column, Player player) {
        for (int c = column; c < SIZE; c++) {
            // Did the searching algorithm reached the edge of the board, found
            // a empty field or a tile of the same player as the direct
            // neighbour in this direction?
            if (c == SIZE - 1 || getSlot(row, c + 1) == null
                    || getSlot(row, column + 1) == player) {
                break;
            }
            // Found a player tile of the same player which is not the direct
            // neighbour?
            if (c > column && getSlot(row, c + 1) == player) {
                return board[row][c + 1];
            }
        }
        return null;
    }

    /**
     * This method checks if a player tile of the same player exists in the
     * positive x-axis and negative y-axis direction, which is not his direct
     * neighbour.
     * @param row Index of row position of a given reference tile
     * @param column Index of column position of a given reference tile
     * @param player Kind of player who owns the reference tile
     * @return player tile of the same player, who is not his direct neighbour
     * or null if he didn't one
     */
    private PlayerTile southEastDirection(int row, int column, Player player) {
        int r = row;        // variable row shouldn't be changed
        int c = column;     // variable column shouldn't be changed
        while (r < SIZE && c < SIZE) {
            // Did the searching algorithm reached the edge of the board, found
            // a empty field or a tile of the same player as the direct
            // neighbour in this direction?
            if (r == SIZE - 1 || c == SIZE - 1 || getSlot(r + 1, c + 1) == null
                    || getSlot(row + 1, column + 1) == player) {
                break;
            }
            // Found a player tile of the same player which is not the direct
            // neighbour?
            if (r > row && c > column && getSlot(r + 1, c + 1) == player) {
                return board[r + 1][c + 1];
            }
            r++;
            c++;
        }
        return null;
    }

    /**
     * This method checks if a player tile of the same player exists in the
     * negative y-axis direction, which is not his direct neighbour.
     * @param row Index of row position of a given reference tile
     * @param column Index of column position of a given reference tile
     * @param player Kind of player who owns the reference tile
     * @return player tile of the same player, who is not his direct neighbour
     * or null if he didn't one
     */
    private PlayerTile southDirection(int row, int column, Player player) {
        for (int r = row; r < SIZE; r++) {
            // Did the searching algorithm reached the edge of the board, found
            // a empty field or a tile of the same player as the direct
            // neighbour in this direction?
            if (r == SIZE - 1 || getSlot(r + 1, column) == null
                    || getSlot(row + 1, column) == player) {
                break;
            }
            // Found a player tile of the same player which is not the direct
            // neighbour?
            if (r > row && getSlot(r + 1, column) == player) {
                return board[r + 1][column];
            }
        }
        return null;
    }

    /**
     * This method checks if a player tile of the same player exists in the
     * negative x-axis and y-axis direction, which is not his direct neighbour.
     * @param row Index of row position of a given reference tile
     * @param column Index of column position of a given reference tile
     * @param player Kind of player who owns the reference tile
     * @return player tile of the same player, who is not his direct neighbour
     * or null if he didn't one
     */
    private PlayerTile southWestDirection(int row, int column, Player player) {
        int r = row;        // variable row shouldn't be changed
        int c = column;     // variable column shouldn't be changed
        while (r < SIZE && c >= 0) {
            // Did the searching algorithm reached the edge of the board, found
            // a empty field or a tile of the same player as the direct
            // neighbour in this direction?
            if (r == SIZE - 1 || c == 0 || getSlot(r + 1, c - 1) == null
                    || getSlot(row + 1, column - 1) == player) {
                break;
            }
            // Found a player tile of the same player which is not the direct
            // neighbour?
            if (r > row && c < column && getSlot(r + 1, c - 1) == player) {
                return board[r + 1][c - 1];
            }
            r++;
            c--;
        }
        return null;
    }

    /**
     * This method checks if a player tile of the same player exists in the
     * negative x-axis direction, which is not his direct neighbour.
     * @param row Index of row position of a given reference tile
     * @param column Index of column position of a given reference tile
     * @param player Kind of player who owns the reference tile
     * @return player tile of the same player, who is not his direct neighbour
     * or null if he didn't one
     */
    private PlayerTile westDirection(int row, int column, Player player) {
        for (int c = column; c >= 0; c--) {
            // Did the searching algorithm reached the edge of the board, found
            // a empty field or a tile of the same player as the direct
            // neighbour in this direction?
            if (c == 0 || getSlot(row, c - 1) == null
                    || getSlot(row, column - 1) == player) {
                break;
            }
            // Found a player tile of the same player which is not the direct
            // neighbour?
            if (c < column && getSlot(row, c - 1) == player) {
                return board[row][c - 1];
            }
        }
        return null;
    }

    /**
     * This method checks if a player tile of the same player exists in the
     * negative x-axis and positive y-axis direction, which is not his direct
     * neighbour.
     * @param row Index of row position of a given reference tile
     * @param column Index of column position of a given reference tile
     * @param player Kind of player who owns the reference tile
     * @return player tile of the same player, who is not his direct neighbour
     * or null if he didn't one
     */
    private PlayerTile northWestDirection(int row, int column, Player player) {
        int r = row;        // variable row shouldn't be changed
        int c = column;     // variable column shouldn't be changed
        while (r >= 0 && c >= 0) {
            // Did the searching algorithm reached the edge of the board, found
            // a empty field or a tile of the same player as the direct
            // neighbour in this direction?
            if (r == 0 || c == 0 || getSlot(r - 1, c - 1) == null
                    || getSlot(row - 1, column - 1) == player) {
                break;
            }
            // Found a player tile of the same player which is not the direct
            // neighbour?
            if (r < row && c < column && getSlot(r - 1, c - 1) == player) {
                return board[r - 1][c - 1];
            }
            r--;
            c--;
        }
        return null;
    }

    /**
     * This method flips all possible tiles according to the rules. From a
     * initial tile, every direction is checked until another player's tile
     * is found which is not the direct neighbour. Between these two tiles are
     * all tiles flipped from the enemy. This method ensures that a tile was
     * found and primarily the smaller row index and secondary smaller column
     * index is deligated first before calling the flipTiles method.
     * @param row Row index of the initial tile
     * @param column Column index of the initial tile
     * @param player Human or Computer, who belongs the initial tile
     */
    // TODO Was ist mit Sichtbarkeit
    void flipAllTiles(int row, int column, Player player) {
        // Player tiles of all directions
        PlayerTile northTile = northDirection(row, column, player);
        PlayerTile northEastTile = northEastDirection(row, column, player);
        PlayerTile eastTile = eastDirection(row, column, player);
        PlayerTile southEastTile = southEastDirection(row, column, player);
        PlayerTile southTile = southDirection(row, column, player);
        PlayerTile southWestTile = southWestDirection(row, column, player);
        PlayerTile westTile = westDirection(row, column, player);
        PlayerTile northWestTile = northWestDirection(row, column, player);
        if (northTile != null) {
            flipTiles(northTile, board[row][column]);
        }
        if (northEastTile != null) {
            flipTiles(northEastTile, board[row][column]);
        }
        if (eastTile != null) {
            flipTiles(board[row][column], eastTile);
        }
        if (southEastTile != null) {
            flipTiles(board[row][column], southEastTile);
        }
        if (southTile != null) {
            flipTiles(board[row][column], southTile);
        }
        if (southWestTile != null) {
            flipTiles(board[row][column], southWestTile);
        }
        if (westTile != null) {
            flipTiles(westTile, board[row][column]);
        }
        if (northWestTile != null) {
            flipTiles(northWestTile, board[row][column]);
        }
    }

    /**
     * This method checks if a player can make a turn. It iterates in all
     * directions from a initial point.
     * @param row Row index of the initial point
     * @param column Column index of the initial point
     * @param player Player who makes a turn
     * @return Boolean whether the player can make a turn or not
     */
    boolean possibleTurn(int row, int column, Player player) {
        // Checking if a tiles exists for each direction
        boolean hasNorthTile = (northDirection(row, column, player) != null);
        boolean hasNorthEastTile =
                (northEastDirection(row, column, player) != null);
        boolean hasEastTile = (eastDirection(row, column, player) != null);
        boolean hasSouthEastTile =
                (southEastDirection(row, column, player) != null);
        boolean hasSouthTile = (southDirection(row, column, player) != null);
        boolean hasSouthWestTile =
                (southWestDirection(row, column, player) != null);
        boolean hasWestTile = (westDirection(row, column, player) != null);
        boolean hasNorthWestTile =
                (northWestDirection(row, column, player) != null);
        // Does any player tile exists in any direction and is the field empty?
        if ((hasNorthTile || hasNorthEastTile || hasEastTile
                || hasSouthEastTile || hasSouthTile || hasSouthWestTile
                || hasWestTile || hasNorthWestTile)
                && getSlot(row, column) == null) {
            return true;
        }
        return false;
    }

    /**
     * This method flips all tiles between two given tiles. This method
     * supposes that the two given tiles are from the same player and between
     * them are only enemy tiles as well as the first tile needs to have
     * primarily a smaller row index and secondly a smaller column index
     * (flipAllTiles method ensures all of these conditions). Only one
     * direction for each method invoke is considered.
     * @param first smaller player tile
     * @param second bigger player tile
     */
    private void flipTiles(PlayerTile first, PlayerTile second) {
        if (first.getRow() == second.getRow()) {
            // Same row
            for (int i = first.getColumn() + 1; i < second.getColumn(); i++) {
                board[first.getRow()][i].changeSide();
            }
        } else if (first.getColumn() == second.getColumn()) {
            // Same column
            for (int i = first.getRow() + 1; i < second.getRow(); i++) {
                board[i][first.getColumn()].changeSide();
            }
        } else if (first.getRow() < second.getRow()
                    && first.getColumn() < second.getColumn()) {
            // South-East direction
            int i = first.getRow() + 1;
            int j = first.getColumn() + 1;
            while (i < second.getRow() && j < second.getColumn()) {
                board[i][j].changeSide();
                i++;
                j++;
            }
        } else {
            // South-West direction
            int i = first.getRow() + 1;
            int j = first.getColumn() - 1;
            while (i < second.getRow() && j > second.getColumn()) {
                board[i][j].changeSide();
                i++;
                j--;
            }
        }
    }

    /**
     * Checks the whole board, if the human player can make a move.
     * @return True or False, if the human player can make a move
     */
    private boolean possibleHumanTurn() {
        // Row direction
        for (int i = 0; i < SIZE; i++) {
            // Column direction
            for (int j = 0; j < SIZE; j++) {
                // Can a tile of the human player be placed here?
                if (possibleTurn(i, j, Player.Human)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks the whole board, if the computer can make a move.
     * @return True or False, if the computer can make a move
     */
    private boolean possibleComputerTurn() {
        // Row direction
        for (int i = 0; i < SIZE; i++) {
            // Column direction
            for (int j = 0; j < SIZE; j++) {
                // Can a tile of the computer be placed here?
                if (possibleTurn(i, j, Player.Computer)) {
                    return true;
                }
            }
        }
        return false;
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
            // Initial board has two human and two computer tiles
            return firstPlayer;
        } else if (nextTurn == Player.Computer && possibleComputerTurn()) {
            return Player.Computer;
        } else if (nextTurn == Player.Human && possibleHumanTurn()) {
            return Player.Human;
        } else if (possibleHumanTurn() && !possibleComputerTurn()) {
            return Player.Human;
        }  else if (possibleComputerTurn() && !possibleHumanTurn()) {
            return Player.Computer;
        }
        // Nobody can make a turn
        gameState = GameState.OVER;
        return Player.Nobody;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReversiBoard move(int row, int col) {
        // Proceed with only valid position values
        try {
            if (getSlot(row, col) != null) {
                System.err.println("Error! There is already a tile on this "
                        + "position.");
            } else if (!possibleTurn(row, col, Player.Human)) {
                System.err.println("Error! This is not a possible turn.");
            } else if (nextTurn != Player.Human) {
                throw new IllegalMoveException("Error! Wait for your enemy's "
                        + "turn.");
            } else if (gameState == GameState.OVER) {
                throw new IllegalMoveException("Error! The game is over.");
            } else {
                ReversiBoard newBoard = clone();
                // Add new player tile to the field
                newBoard.board[row][col]
                        = new PlayerTile(row, col, Player.Human);
                // Flip all possible tiles, must be at least one
                newBoard.flipAllTiles(row, col, Player.Human);
                // Set human as the previous player
                newBoard.nextTurn = Player.Computer;
                return newBoard;
            }
            return null;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Error! Your position values"
                    + "must be between 0 and " + (SIZE - 1));
        }
    }

    private void setChildren(ReversiBoard reversiBoard, int depth) {
        if (depth < level) {
            int counter = 0;
            Player nextTurn = next();
            for (int i = 0; i < Board.SIZE; i++) {
                for (int j = 0; j < Board.SIZE; j++) {
                    if (reversiBoard.possibleTurn(i, j, nextTurn)) {
                        ReversiBoard newBoard = reversiBoard.clone();
                        newBoard.getBoard()[i][j] = new PlayerTile(i, j,
                                nextTurn);
                        newBoard.flipAllTiles(i, j, nextTurn);
                        if (nextTurn == Player.Human) {
                            newBoard.nextTurn = Player.Computer;
                        } else {
                            newBoard.nextTurn = Player.Human;
                        }
                        children[counter] = newBoard;
                        counter++;
                    }
                }
            }
            if (counter == 0) {
                if (nextTurn == Player.Human) {
                    nextTurn = Player.Computer;
                } else {
                    nextTurn = Player.Human;
                }
                for (int i = 0; i < Board.SIZE; i++) {
                    for (int j = 0; j < Board.SIZE; j++) {
                        if (reversiBoard.possibleTurn(i, j, nextTurn)) {
                            ReversiBoard newBoard = reversiBoard.clone();
                            newBoard.getBoard()[i][j] = new PlayerTile(i, j,
                                    nextTurn);
                            newBoard.flipAllTiles(i, j, nextTurn);
                            if (nextTurn == Player.Human) {
                                newBoard.nextTurn = Player.Computer;
                            } else {
                                newBoard.nextTurn = Player.Human;
                            }
                            children[counter] = newBoard;
                            counter++;
                        }
                    }
                }
            }
            if (counter != 0) {
                ++depth;
                for (ReversiBoard boardz: children) {
                    if (boardz != null) {
                        boardz.setChildren(boardz, depth);
                    }
                }
            }
        }
    }

    private ReversiBoard minmaxalg() {
        if (children[0] == null) {
            setScore(score());
            return this;
        } else {
            if (next() == Player.Computer) {
                double maxScore = Integer.MIN_VALUE;
                ReversiBoard maxNode = null;
                for (ReversiBoard board: children) {
                    if (board == null) {
                        break;
                    }
                    board.minmaxalg();
                    if (maxScore < board.getScore()) {
                        maxScore = board.getScore();
                        maxNode = board;
                    }
                }
                this.score = score() + maxScore;
                //System.out.println("mit kinder: " + score);
                return maxNode;
            } else {
                double minScore = Integer.MAX_VALUE;
                ReversiBoard minNode = null;
                for (ReversiBoard board: children) {
                    if (board == null) {
                        break;
                    }
                    board.minmaxalg();
                    if (minScore > board.getScore()) {
                        minScore = board.getScore();
                        minNode = board;
                    }
                }
                this.score = score() + minScore;
                //System.out.println("mit kinder: " + score);
                return minNode;
            }
        }
    }

    private void setScore(double score) {
        this.score = score;
    }

    private double getScore() {
        return score;
    }

    /**
     * {@inheritDoc}
     * TODO implementieren, dokumentieren und testen
     */
    @Override
    public ReversiBoard machineMove() {
        ReversiBoard newBoard = null;
        int counter = 0;
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (possibleTurn(i, j, Player.Computer)) {
                    newBoard = clone();
                    PlayerTile tile = new PlayerTile(i, j, Player.Computer);
                    newBoard.getBoard()[i][j] = tile;
                    newBoard.flipAllTiles(i, j, Player.Computer);
                    newBoard.nextTurn = Player.Human;
                    children[counter] = newBoard;
                    counter++;
                }
            }
        }
        for(ReversiBoard board: children) {
            if (board != null) {
                board.setChildren(board, 1);
            }
        }

        minmaxalg();

        /*
        for (ReversiBoard board: children) {
            if (board != null) {
                System.out.println(board.score);
                for (ReversiBoard board1 : board.children) {
                    if (board1 != null) {
                        System.out.println("\t" +  board1.score);
                        for (ReversiBoard board2 : board1.children) {
                            if (board2 != null) {
                                System.out.println("\t \t" + board2.score);
                            }
                        }
                    }
                }
            }
        }*/

        ReversiBoard bestBoard = null;
        double bestScore = Integer.MIN_VALUE;

        for (ReversiBoard board: children) {
            if (board != null) {
                if (board.score > bestScore) {
                    bestScore = board.score;
                    bestBoard = board;
                }
            }
        }
        if (bestBoard != null) {
            return bestBoard;
        } else {
            nextTurn = Player.Human;
            System.out.println("The bot has to miss a turn");
            return this;
        }




        /*
        ReversiBoard newBoard = null;
        TreeNode[] nodes = new TreeNode[SIZE * SIZE - 4];

        int counter = 0;
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (possibleTurn(i, j, Player.Computer)) {
                    newBoard = clone();
                    PlayerTile tile = new PlayerTile(i, j, Player.Computer);
                    newBoard.board[i][j] = tile;
                    newBoard.flipAllTiles(i, j, Player.Computer);
                    newBoard.nextTurn = Player.Human;
                    //System.out.println("(" + (i+1) + "," + (j+1) + ")" + newBoard.score());
                    counter++;
                    TreeNode node = new TreeNode(newBoard, level, 0,
                            Player.Human);

                    //System.out.println(toString());
                    //System.out.println(newBoard.toString());
                    node.minMaxAlgorithm();
                    System.out.println("(" + (i+1) + "," + (j+1) + ")" + node.getReversiBoard().score());
                    //System.out.println("mit Kinder: ");
                    for (TreeNode knoten: node.getChildren()) {
                        if (knoten != null) {
                            //System.out.println(knoten.getScore() + node.getReversiBoard().score());
                            //System.out.println(knoten.getReversiBoard());
                        }
                    }
                    nodes[counter] = node;
                    counter++;
                }
            }
        }
        /*for (TreeNode node: nodes) {
            if (node != null) {
                //System.out.println(node.getScore());
                //System.out.println(node.getReversiBoard());
                double minScore = Double.MAX_VALUE;
                //System.out.println("neues kind:");
                for (TreeNode nodechild : node.getChildren()) {
                    if (nodechild != null) {
                        //System.out.println(nodechild.getScore());
                        int retval = Double.compare(minScore,
                                nodechild.getScore());
                        if (retval > 0) {
                            minScore = nodechild.getScore();
                            node.setScore(minScore
                                    + node.getReversiBoard().score());
                        }
                    }
                }
            }
        }*/
        /*
        double maxScore = Integer.MIN_VALUE;
        int index = 0;
        for (int i = 0; i < nodes.length; i++) {
            if (nodes[i] == null) {
                break;
            }
            int retval = Double.compare(maxScore, nodes[i].getScore());

            if (retval < 0) {
                maxScore = nodes[i].getScore();
                index = i;
            }
        }

        // TODO gehört nicht hierhin...oder doch??
        if (nodes[0] == null) {
            System.out.println("<Bot muss aussetzen>");
            nextTurn = Player.Human;
            return this;
        }
        nodes[index].getReversiBoard().nextTurn = Player.Human;
        return nodes[index].getReversiBoard();*/
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
        return gameState == GameState.OVER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Player getWinner() {
        if (gameOver()) {
            int humanTiles = getNumberOfHumanTiles();
            int machineTiles = getNumberOfMachineTiles();
            if (humanTiles > machineTiles) {
                return Player.Human;
            } else if (humanTiles < machineTiles) {
                return Player.Computer;
            } else {
                return Player.Nobody;
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
                if (board[i][j] != null
                        && board[i][j].getPlayer() == Player.Human) {
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
                if (board[i][j] != null
                        && board[i][j].getPlayer() == Player.Computer) {
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
        // Position contains a tile?
        if (board[row][col] != null) {
            return board[row][col].getPlayer();
        }
        return null;
    }

    /**
     * {@inheritDoc}*/
    @Override
    public ReversiBoard clone() {
        // Create a new board
        ReversiBoard copyBoard = new ReversiBoard(firstPlayer);
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (board[i][j] != null) {
                    // Create a deep copy of this tile
                    PlayerTile copyTile = board[i][j].clone();
                    // Add this copied tile to the copied board
                    copyBoard.getBoard()[i][j] = copyTile;
                }
            }
        }
        copyBoard.children = new ReversiBoard[SIZE * SIZE - 4];
        copyBoard.setLevel(level);
        copyBoard.gameState = gameState;
        return copyBoard;
    }

    /**
     * {@inheritDoc}
     * // TODO ein whitespace abstand statt 2 am ende
     */
    @Override
    public String toString() {
        StringBuilder bob = new StringBuilder();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (board[i][j] == null) {
                    bob.append('.');
                } else if (board[i][j].getPlayer() == Player.Human) {
                    bob.append('X');
                } else {
                    bob.append('O');
                }
                // Leave a whitespace between the fields, except the last one
                if (j != board[i].length - 1) {
                    bob.append(" ");
                }
            }
            // Start a new line for the next row
            bob.append(System.getProperty("line.separator"));
        }
        return bob.toString();
    }
}
