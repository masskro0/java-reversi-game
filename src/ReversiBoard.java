/**
 * This class implements the interface Board. This class represents the
 * Reversi board with all needed functionalities to communicate with the
 * Shell or the UI interface.
 */
public class ReversiBoard implements Board {

    private PlayerTile[][] board;           // Matrix with player tiles
    private final int DEFAULT_LEVEL = 3;    // Default level is three
    private int level = DEFAULT_LEVEL;      // Set the default level
    private final Player firstPlayer;       // Player who makes the inital move
    private GameState gameState;            // Current game state
    // Stores the player who can make the next move
    private Player currentTurn;


    /**
     * Creates a new game with a new Reversi board. This constructor lets the
     * human make the initial move.
     */
    public ReversiBoard() {
        board = new PlayerTile[SIZE][SIZE];
        firstPlayer = Player.Human;
        currentTurn = firstPlayer;
        initializeBoard();
        gameState = GameState.RUNNING;
    }

    /**
     * Creates a new game with a new Reversi board. This constructor lets a
     * given player make the first move.
     * @param firstPlayer Human or Computer
     */
    public ReversiBoard(final Player firstPlayer) {
         board = new PlayerTile[SIZE][SIZE];
         this.firstPlayer = firstPlayer;
         currentTurn = firstPlayer;
         initializeBoard();
         gameState = GameState.RUNNING;
    }

    /**
     * Return the board matrix. This is private since this is only needed for
     * the ReversiBoard class
     * @return board as two dimensional array
     */
    private PlayerTile[][] getBoard() {
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

    // TODO alles dokumentieren (alle richtungen)
    private PlayerTile northDirection(int row, int column, Player player) {
        for (int r = row; r >= 0; r--) {
            if (r == 0 || getSlot(r - 1, column) == null
                    || getSlot(row - 1, column) == player) {
                break;
            }
            if (r < row && getSlot(r - 1, column) == player) {
                return board[r][column];
            }
        }
        return null;
    }

    private PlayerTile northEastDirection(int row, int column, Player player) {
        int r = row;
        int c = column;
        while (r >= 0 && c < SIZE) {
            if (r == 0 || c == SIZE - 1 || getSlot(r - 1, c + 1) == null
                    || getSlot(row - 1, column + 1) == player) {
                break;
            }
            if (r < row && c > column && getSlot(r - 1, c + 1) == player) {
                return board[r][c];
            }
            r--;
            c++;
        }
        return null;
    }

    private PlayerTile eastDirection(int row, int column, Player player) {
        for (int c = column; c < SIZE; c++) {
            if (c == SIZE - 1 || getSlot(row, c + 1) == null
                    || getSlot(row, column + 1) == player) {
                break;
            }
            if (c > column && getSlot(row, c + 1) == player) {
                return board[row][c];
            }
        }
        return null;
    }

    private PlayerTile southEastDirection(int row, int column, Player player) {
        int r = row;
        int c = column;
        while (r < SIZE && c < SIZE) {
            if (r == SIZE - 1 || c == SIZE - 1 || getSlot(r + 1, c + 1) == null
                    || getSlot(row + 1, column + 1) == player) {
                break;
            }
            if (r > row && c > column && getSlot(r + 1, c + 1) == player) {
                return board[r][c];
            }
            r++;
            c++;
        }
        return null;
    }

    private PlayerTile southDirection(int row, int column, Player player) {
        for (int r = row; r < SIZE; r++) {
            if (r == SIZE - 1 || getSlot(r + 1, column) == null
                    || getSlot(row + 1, column) == player) {
                break;
            }
            if (r > row && getSlot(r + 1, column) == player) {
                return board[r][column];
            }
        }
        return null;
    }

    private PlayerTile southWestDirection(int row, int column, Player player) {
        int r = row;
        int c = column;
        while (r < SIZE && c >= 0) {
            if (r == SIZE - 1 || c == 0 || getSlot(r + 1, c - 1) == null
                    || getSlot(row + 1, column - 1) == player) {
                break;
            }
            if (r > row && c < column && getSlot(r + 1, c - 1) == player) {
                return board[r][c];
            }
            r++;
            c--;
        }
        return null;
    }

    private PlayerTile westDirection(int row, int column, Player player) {
        for (int c = column; c >= 0; c--) {
            if (c == 0 || getSlot(row, c - 1) == null
                    || getSlot(row, column - 1) == player) {
                break;
            }
            if (c < column && getSlot(row, c - 1) == player) {
                return board[row][c];
            }
        }
        return null;
    }

    private PlayerTile northWestDirection(int row, int column, Player player) {
        int r = row;
        int c = column;
        while (r >= 0 && c >= 0) {
            if (r == 0 || c == 0 || getSlot(r - 1, c - 1) == null
                    || getSlot(row - 1, column - 1) == player) {
                break;
            }
            if (r < row && c < column && getSlot(r - 1, c - 1) == player) {
                return board[r][c];
            }
            r--;
            c--;
        }
        return null;
    }

    // TODO dokumentieren und evtl refactoren
    // TODO MAJOR TESTING
    private void flipAllTiles(int row, int column, Player player) {
        if (northDirection(row, column, player) != null) {
            flipTiles(northDirection(row, column, player), board[row][column],
                    player);
        }
        if (northEastDirection(row, column, player) != null) {
            flipTiles(northEastDirection(row, column, player),
                    board[row][column], player);
        }
        if (eastDirection(row, column, player) != null) {
            flipTiles(board[row][column], eastDirection(row, column, player),
                    player);
        }
        if (southEastDirection(row, column, player) != null) {
            flipTiles(board[row][column],
                    southEastDirection(row, column, player), player);
        }
        if (southDirection(row, column, player) != null) {
            flipTiles(board[row][column], southDirection(row, column, player),
                    player);
        }
        if (southWestDirection(row, column, player) != null) {
            flipTiles(board[row][column],
                    southWestDirection(row, column, player), player);
        }
        if (westDirection(row, column, player) != null) {
            flipTiles(westDirection(row, column, player), board[row][column],
                    player);
        }
        if (northWestDirection(row, column, player) != null) {
            flipTiles(northWestDirection(row, column, player),
                    board[row][column], player);
        }
    }

    // TODO Mögliches Duplikat, Lösung dazu einfallen lassen!
    private boolean possibleTurn(int row, int column, Player player) {
        int r = row;
        int c = column;
        // North direction
        for (r = row; r >= 0; r--) {
            if (r == 0 || getSlot(r - 1, column) == null
                    || getSlot(row - 1, column) == player) {
                break;
            }
            if (r < row && getSlot(r - 1, column) == player) {
                return true;
            }
        }
        r = row;
        // North-East direction
        while (r >= 0 && c < SIZE) {
            if (r == 0 || c == SIZE - 1 || getSlot(r - 1, c + 1) == null
                    || getSlot(row - 1, column + 1) == player) {
                break;
            }
            if (r < row && c > column && getSlot(r - 1, c + 1) == player) {
                return true;
            }
            r--;
            c++;
        }
        // East direction
        for (c = column; c < SIZE; c++) {
            if (c == SIZE - 1 || getSlot(row, c + 1) == null
                    || getSlot(row, column + 1) == player) {
                break;
            }
            if (c > column && getSlot(row, c + 1) == player) {
                return true;
            }
        }
        r = row;
        c = column;
        // South-East direction
        while (r < SIZE && c < SIZE) {
            if (r == SIZE - 1 || c == SIZE - 1 || getSlot(r + 1, c + 1) == null
                    || getSlot(row + 1, column + 1) == player) {
                break;
            }
            if (r > row && c > column && getSlot(r + 1, c + 1) == player) {
                return true;
            }
            r++;
            c++;
        }
        // South direction
        for (r = row; r < SIZE; r++) {
            if (r == SIZE - 1 || getSlot(r + 1, column) == null
                    || getSlot(row + 1, column) == player) {
                break;
            }
            if (r > row && getSlot(r + 1, column) == player) {
                return true;
            }
        }
        r = row;
        c = column;
        // South-West direction
        while (r < SIZE && c >= 0) {
            if (r == SIZE - 1 || c == 0 || getSlot(r + 1, c - 1) == null
                    || getSlot(row + 1, column - 1) == player) {
                break;
            }
            if (r > row && c < column && getSlot(r + 1, c - 1) == player) {
                return true;
            }
            r++;
            c--;
        }
        // West direction
        for (c = column; c >= 0; c--) {
            if (c == 0 || getSlot(row, c - 1) == null
                    || getSlot(row, column - 1) == player) {
                break;
            }
            if (c < column && getSlot(row, c - 1) == player) {
                return true;
            }
        }
        r = row;
        c = column;
        // North-West direction
        while (r >= 0 && c >= 0) {
            if (r == 0 || c == 0 || getSlot(r - 1, c - 1) == null
                    || getSlot(row - 1, column - 1) == player) {
                break;
            }
            if (r < row && c < column && getSlot(r - 1, c - 1) == player) {
                return true;
            }
            r--;
            c--;
        }
        return false;
    }

    // TODO dokumentieren und testen
    // Ich gehe davon aus, dass der kleinere zuerst kommt, an erster stelle row muss kleiner sein
    private void flipTiles(PlayerTile first, PlayerTile second,
                           Player player) {
        if (first.getRow() == second.getRow()) {
            for (int i = first.getColumn() + 1; i < second.getColumn(); i++) {
                board[first.getRow()][i] = new PlayerTile(first.getRow(), i,
                        player);
            }
        } else if (first.getColumn() == second.getColumn()) {
            for (int i = first.getRow() + 1; i < second.getRow(); i++) {
                board[i][first.getColumn()] = new PlayerTile(i,
                        first.getColumn(), player);
            }
        } else if (first.getRow() < second.getRow()
                    && first.getColumn() < second.getColumn()){
            int i = first.getRow() + 1;
            int j = first.getColumn() + 1;
            while (i < second.getRow() && j < second.getColumn()) {
                board[i][j] = new PlayerTile(i, j, player);
                i++;
                j++;
            }
        } else {
            int i = first.getRow() + 1;
            int j = first.getColumn() - 1;
            while (i < second.getRow() && j > second.getColumn()) {
                board[i][j] = new PlayerTile(i, j, player);
                i++;
                j--;
            }
        }
    }

    // TODO Doku
    private boolean humanTurn() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (possibleTurn(i, j, Player.Human)) {
                    return true;
                }
            }
        }
        return false;
    }

    // TODO Doku
    private boolean computerTurn() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
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
     * TODO Testen und dokumentieren
     */
    @Override
    public Player next() {
        if (currentTurn == Player.Human && computerTurn()) {
            return Player.Computer;
        } else if (currentTurn == Player.Human && !computerTurn()) {
            return Player.Human;
        } else if (currentTurn == Player.Computer && humanTurn()) {
            return Player.Human;
        } else if (currentTurn == Player.Computer && !humanTurn()) {
            return Player.Computer;
        }
        gameState = GameState.OVER;
        return Player.Nobody;
    }

    /**
     * {@inheritDoc}
     * TODO dokumentieren
     */
    @Override
    public Board move(int row, int col) {
        try {
            if (getSlot(row, col) != null) {
                System.err.println("Error! There is already a tile on this "
                        + "position.");
            } else if (!possibleTurn(row, col, Player.Human)) {
                System.err.println("Error! This is not a possible turn.");
            } else if (currentTurn != Player.Human) {
                throw new IllegalMoveException("Error! Wait for your enemy's "
                        + "turn.");
            } else if (gameState == GameState.OVER) {
                throw new IllegalMoveException("Error! The game is over.");
            } else {
                ReversiBoard newBoard = (ReversiBoard) clone();
                newBoard.board[row][col]
                        = new PlayerTile(row, col, Player.Human);
                newBoard.flipAllTiles(row, col, Player.Human);
                currentTurn = Player.Computer;
                return newBoard;
            }
            return null;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Error! Your position values"
                    + "must be between 0 and " + (SIZE - 1));
        }
    }

    /**
     * {@inheritDoc}
     * TODO
     */
    @Override
    public Board machineMove() {
        return null;
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
     * TODO Task
     */
    @Override
    public Player getSlot(final int row, final int col) {
        // TODO Verhalten untersuchen
        // Decrement, since the game counts from 1 to 8
        //row--;
        //col--;
        if (board[row][col] != null) {
            return board[row][col].getPlayer();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Board clone() {
        // Create a new board
        ReversiBoard copyBoard = new ReversiBoard(firstPlayer);
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                // Assign every element of the current board
                copyBoard.getBoard()[i][j] = board[i][j];
            }
        }
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
