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
    private Player nextTurn;

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
     * Return the board matrix. This is private since this is only needed for
     * the ReversiBoard class
     * @return board as two dimensional array
     */
    // TODO private
    public PlayerTile[][] getBoard() {
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
     *
     * @return score value of this board.
     */
    // TODO private
    public double score() {
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
                    //System.out.println(possibleTurn(i,j,Player.Human) + " (" + (i+1) + "," + (j+1) + ")" + " Human");
                }
                if (possibleTurn(i, j, Player.Computer)) {
                    mScoreComputer++;
                    //System.out.println(possibleTurn(i,j,Player.Computer) + " (" + (i+1) + "," + (j+1) + ")" + " Computer");
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
            if (!validRowDir || !validColumnDir
                    || getSlot(rowDir, columnDir) != null) {
                continue;
            } else {
                counter++;
            }
        }
        return counter;
    }

    // Wenn was falsch ist dann hier
    private PlayerTile[] validTiles(int row, int column, Player player) {
        PlayerTile[] validTiles = new PlayerTile[Directions.values().length];
        int counter = 0;
        for (Directions direction: Directions.values()) {
            int rowDir = row + direction.row;
            int columnDir = column + direction.column;
            boolean validRowDir = (rowDir >= 0 && rowDir < SIZE);
            boolean validColumnDir = (columnDir >= 0 && columnDir < SIZE);
            if (!validRowDir || !validColumnDir
                    || getSlot(rowDir, columnDir) == null
                    || getSlot(rowDir, columnDir) == player) {
                continue;
            }
            while (rowDir >= 0 && rowDir < SIZE && columnDir >= 0
                    && columnDir < SIZE) {
                if (getSlot(rowDir, columnDir) == player) {
                    validTiles[counter] = board[rowDir][columnDir];
                    counter++;
                    break;
                }
                rowDir += direction.row;
                columnDir += direction.column;
            }
        }
        return validTiles;
    }

    /**
     * This method flips all possible tiles according to the rules. From a
     * initial tile, every direction is checked until another player's tile
     * is found which is not the direct neighbour. Between these two tiles are
     * all tiles flipped from the enemy. This method ensures that a tile was
     * found and primarily the smaller row index and secondary smaller column
     * index is deligated first before calling the flipTiles method.
     *
     * @param row Row index of the initial tile
     * @param column Column index of the initial tile
     * @param player Human or Computer, who belongs the initial tile
     */
    // TODO PRivate
    public void flipAllTiles(int row, int column, Player player) {
        for (PlayerTile tile: validTiles(row, column, player)) {
            if (tile != null) {
                //System.out.println(tile.getRow() + " " +  tile.getColumn() + " " + tile.getPlayer());
                flipTiles(board[row][column], tile);
            }
        }
    }

    /**
     * This method checks if a player can make a turn. It iterates in all
     * directions from a initial point.
     *
     * @param row Row index of the initial point
     * @param column Column index of the initial point
     * @param player Player who makes a turn
     * @return Boolean whether the player can make a turn or not
     */
    public boolean possibleTurn(int row, int column, Player player) {
        return getSlot(row, column) == null
                && validTiles(row, column, player)[0] != null;
    }

    // TODO NICHT MEHR SICHERGESTELLT DASS KLEINERER WERT ZUERST KOMMT! IN DER METHODE SICHERSTELLEN!
    /**
     * This method flips all tiles between two given tiles. This method
     * supposes that the two given tiles are from the same player and between
     * them are only enemy tiles as well as the first tile needs to have
     * primarily a smaller row index and secondly a smaller column index
     * (flipAllTiles method ensures all of these conditions). Only one
     * direction for each method invoke is considered.
     *
     * @param first smaller player tile
     * @param second bigger player tile
     */
    private void flipTiles(PlayerTile first, PlayerTile second) {
        PlayerTile smaller;
        PlayerTile bigger;
        if (first.getRow() < second.getRow()) {
            smaller = first;
            bigger = second;
        } else if (first.getRow() == second.getRow()
                && first.getColumn() < second.getColumn()) {
            smaller = first;
            bigger = second;
        } else {
            smaller = second;
            bigger = first;
        }
        if (smaller.getRow() == bigger.getRow()) {
            // Same row
            for (int i = smaller.getColumn() + 1; i < bigger.getColumn(); i++) {
                board[smaller.getRow()][i].changeSide();
            }
        } else if (smaller.getColumn() == bigger.getColumn()) {
            // Same column
            for (int i = smaller.getRow() + 1; i < bigger.getRow(); i++) {
                board[i][smaller.getColumn()].changeSide();
            }
        } else if (smaller.getRow() < bigger.getRow()
                    && smaller.getColumn() < bigger.getColumn()) {
            // South-East direction
            int i = smaller.getRow() + 1;
            int j = smaller.getColumn() + 1;
            while (i < bigger.getRow() && j < bigger.getColumn()) {
                board[i][j].changeSide();
                i++;
                j++;
            }
        } else {
            // South-West direction
            int i = smaller.getRow() + 1;
            int j = smaller.getColumn() - 1;
            while (i < bigger.getRow() && j > bigger.getColumn()) {
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
        if (nextTurn != Player.Human) {
            throw new IllegalMoveException("Error! Wait for your enemy's "
                    + "turn.");
        } else if (gameState == GameState.OVER) {
            throw new IllegalMoveException("Error! The game is over.");
        }
        if (getSlot(row, col) == null && nextTurn == Player.Human
                && possibleTurn(row, col, Player.Human)) {
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
    }

    // TODO irgendwas und dokumentieren
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

    // TODO irgendwas und dokumentieren
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
     * TODO dokumentieren
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

        for (ReversiBoard board1: children) {
            if (board1 != null) {
                System.out.println(board1.score);
                for (ReversiBoard board2: board1.children) {
                    if (board2 != null) {
                        System.out.println("\t" + board2.score);
                        for (ReversiBoard board3: board2.children) {
                            if (board3 != null) {
                                System.out.println("\t\t" + board3.score);
                            }
                        }
                    }
                }
            }
        }

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
        if (gameState == GameState.OVER
                || (!possibleHumanTurn() && !possibleComputerTurn())) {
            return true;
        }
        return false;
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
     * {@inheritDoc}
     */
    @Override
    public ReversiBoard clone() {
        // Create a new board
        ReversiBoard copyBoard = new ReversiBoard(this, firstPlayer);
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
                    bob.append("  ");
                }
            }
            // Start a new line for the next row
            bob.append(System.getProperty("line.separator"));
        }
        return bob.toString();
    }
}