package reversi.model;

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
    private PlayerTile[][] board;

    /**
     * The initial set level when a game is started for the first time.
     */
    private final int DEFAULT_LEVEL = 3;

    /**
     * The game difficulty.
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
    public Player nextTurn;

    /**
     * The current score of this board, calculated with the score method and
     * the min-max-algorihm.
     */
    private double score;

    /**
     * A two-dimensional matrix containing score values for each field of a
     * Reversi board.
     */
    private final int[][] scoreBoard = initScoreBoard();

    /**
     * All possible next moves of this board.
     */
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
        level = DEFAULT_LEVEL;
    }

    /**
     * Creates a new game with a new Reversi board. This constructor lets a
     * given player make the first move.
     *
     * @param firstPlayer Player to make the initial move, Computer or Human.
     */
    public ReversiBoard(ReversiBoard oldBoard, final Player firstPlayer) {
         board = new PlayerTile[SIZE][SIZE];
         this.firstPlayer = firstPlayer;
         nextTurn = firstPlayer;
         initializeBoard();
         gameState = GameState.RUNNING;
         children = new ReversiBoard[SIZE * SIZE - 4];
         setLevel(oldBoard.level);
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
     *
     * @return score value of this board.
     */
    private double score() {
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
                if (validMove(i, j, Player.Human)) {
                    mScoreHuman++;
                }
                if (validMove(i, j, Player.Computer)) {
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

    private int countEmptyFieldsAroundTile(int row, int column) {
        int counter = 0;
        for (Directions direction: Directions.values()) {
            int rowDir = row + direction.row;
            int columnDir = column + direction.column;
            boolean validRowDir = (rowDir >= 0 && rowDir < SIZE);
            boolean validColumnDir = (columnDir >= 0 && columnDir < SIZE);
            if (validRowDir && validColumnDir
                    && getSlot(rowDir, columnDir) == null) {
                counter++;
            }
        }
        return counter;
    }

    private PlayerTile[] validTiles(int row, int column, Player player,
                                    boolean flip) {
        PlayerTile[] validTiles = new PlayerTile[Directions.values().length];
        int counter = 0;
        for (Directions direction: Directions.values()) {
            boolean validTile = false;
            int rowDir = row + direction.row;
            int columnDir = column + direction.column;
            boolean validRowDir = (rowDir >= 0 && rowDir < SIZE);
            boolean validColumnDir = (columnDir >= 0 && columnDir < SIZE);
            if (!validRowDir || !validColumnDir
                    || getSlot(rowDir, columnDir) == null
                    || getSlot(rowDir, columnDir) == player) {
                continue;
            }
            while (rowDir >= 0 && rowDir < SIZE && columnDir < SIZE
                    && columnDir >= 0  && getSlot(rowDir, columnDir) != null) {
                if (getSlot(rowDir, columnDir) == player
                        && (Math.abs(rowDir - (row)) > 1
                        || Math.abs(columnDir - (column)) > 1)) {
                    validTiles[counter] = board[rowDir][columnDir];
                    counter++;
                    validTile = true;
                    break;
                }
                rowDir += direction.row;
                columnDir += direction.column;
            }
            if (flip && validTile) {
                rowDir -= direction.row;
                columnDir -= direction.column;
                while (getSlot(rowDir, columnDir) != player) {
                    board[rowDir][columnDir].changeSide();
                    rowDir -= direction.row;
                    columnDir -= direction.column;
                }
            }
        }
        return validTiles;
    }

    /**
     * This method checks if a player can make any moves. It iterates in all
     * directions from a initial point.
     *
     * @param row Row index of the initial point
     * @param column Column index of the initial point
     * @param player Player who makes a turn
     * @return Boolean whether the player can make a turn or not
     */
    boolean validMove(int row, int column, Player player) {
        return getSlot(row, column) == null
                && validTiles(row, column, player, false)[0] != null;
    }

    /**
     * Checks the whole board, if the human player can even make a move.
     * @return True or False, if the human player can make a move
     */
    private boolean hasMoves(Player player) {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {

                // Can a tile of the player be placed here?
                if (validMove(i, j, player)) {
                    return true;
                }
            }
        }
        return false;
    }

    ReversiBoard minmaxalg() {
        if (children[0] == null) {
            score();
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
                    if (maxScore < board.score) {
                        maxScore = board.score;
                        maxNode = board;
                    }
                }
                this.score = score() + maxScore;
                return maxNode;
            } else {
                double minScore = Integer.MAX_VALUE;
                ReversiBoard minNode = null;
                for (ReversiBoard board: children) {
                    if (board == null) {
                        break;
                    }
                    board.minmaxalg();
                    if (minScore > board.score) {
                        minScore = board.score;
                        minNode = board;
                    }
                }
                this.score = score() + minScore;
                return minNode;
            }
        }
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
        } else if (nextTurn == Player.Computer && hasMoves(Player.Computer)) {
            return Player.Computer;
        } else if (nextTurn == Player.Human && hasMoves(Player.Human)) {
            return Player.Human;
        } else if (hasMoves(Player.Human) && !hasMoves(Player.Computer)) {
            return Player.Human;
        }  else if (hasMoves(Player.Computer) && !hasMoves(Player.Human)) {
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
        /*if (nextTurn != Player.Human) {
            throw new IllegalMoveException("Error! Wait for your enemy's "
                    + "turn.");
        } else if (gameState == GameState.OVER) {
            throw new IllegalMoveException("Error! The game is over.");
        }*/
        if (getSlot(row, col) == null && next() == Player.Human
                && validMove(row, col, Player.Human)) {
            ReversiBoard newBoard = clone();
            newBoard.board[row][col] = new PlayerTile(row, col, Player.Human);
            newBoard.validTiles(row, col, Player.Human, true);
            newBoard.nextTurn = Player.Computer;
            return newBoard;
        }
        return null;
    }

    // TODO irgendwas und dokumentieren
    private void setChildren(ReversiBoard reversiBoard, int depth) {
        if (depth < level) {
            int counter = 0;
            Player nextTurn = next();
            for (int i = 0; i < Board.SIZE; i++) {
                for (int j = 0; j < Board.SIZE; j++) {
                    if (reversiBoard.validMove(i, j, nextTurn)) {
                        ReversiBoard newBoard = reversiBoard.clone();
                        newBoard.board[i][j] = new PlayerTile(i, j,
                                nextTurn);
                        newBoard.validTiles(i, j, nextTurn, true);
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
                        if (reversiBoard.validMove(i, j, nextTurn)) {
                            ReversiBoard newBoard = reversiBoard.clone();
                            newBoard.board[i][j] = new PlayerTile(i, j,
                                    nextTurn);
                            newBoard.validTiles(i, j, nextTurn, true);
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

    /**
     * {@inheritDoc}
     * TODO dokumentieren
     */
    @Override
    public ReversiBoard machineMove() {
        setChildren(this, 0);
        minmaxalg();
/*
        // TODO FOR DEBUGGING -> DELETE ME
        for (ReversiBoard board1: children) {
            if (board1 != null) {
                System.out.println(board1.score);
                for (ReversiBoard board2: board1.children) {
                    if (board2 != null) {
                        //System.out.println("\t" + board2.score);
                        for (ReversiBoard board3: board2.children) {
                            if (board3 != null) {
                                //System.out.println("\t\t" + board3.score);
                                //System.out.println(board3);
                            }
                        }
                    }
                }
            }
        }*/

        ReversiBoard bestBoard = null;
        double bestScore = Integer.MIN_VALUE;

        for (ReversiBoard boardq: children) {
            if (boardq != null) {
                if (boardq.score > bestScore) {
                    bestScore = boardq.score;
                    bestBoard = boardq;
                }
            }
        }
        if (bestBoard != null) {
            return bestBoard;
        } else {
            nextTurn = Player.Human;
            return this;
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
                || (!hasMoves(Player.Human) && !hasMoves(Player.Computer));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Player getWinner() {
        if (gameOver()) {
            if (getNumberOfHumanTiles() > getNumberOfMachineTiles()) {
                return Player.Human;
            } else if (getNumberOfHumanTiles() < getNumberOfMachineTiles()) {
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
        if (board[row][col] != null) {
            return board[row][col].getPlayer();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReversiBoard clone() {
        ReversiBoard copyBoard = new ReversiBoard(this, firstPlayer);
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (board[i][j] != null) {

                    // Create a deep copy of every tile.
                    PlayerTile copyTile = board[i][j].clone();
                    copyBoard.board[i][j] = copyTile;
                }
            }
        }
        copyBoard.setLevel(level);
        copyBoard.children = new ReversiBoard[SIZE * SIZE - 4];
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
                if (board[i][j] == null) {
                    bob.append('.');
                } else if (board[i][j].getPlayer() == Player.Human) {
                    bob.append('X');
                } else {
                    bob.append('O');
                }
                if (j != board[i].length - 1) {
                    bob.append(" ");
                }
            }

            // Start a new line for the next row.
            bob.append(System.getProperty("line.separator"));
        }
        return bob.toString();
    }
}