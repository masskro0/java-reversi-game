package shell;

import reversi.model.*;

import java.util.Scanner;

/**
 * A user interface who gets an input, executes the command and prints the
 * output.
 */
public final class Shell {

    // Allow only one instance of the Shell.
    private Shell() {
    }

    /**
     * Main loop for user interaction.
     *
     * @param args The arguments a user entered.
     */
    public static void main(final String[] args) {
        Scanner sc = new Scanner(System.in);

        // Loop iteration variable needed to quit the loop.
        boolean quit = false;
        ReversiBoard board = new ReversiBoard();

        // Main loop to get user input and execute it.
        while (!quit) {
            System.out.print("othello> ");

            // Get the next input.
            String input = sc.nextLine();

            // Remove whitespace until the first char appears.
            input = input.trim();

            // Skip a loop iteration, if input is null.
            if (input == null) {
                continue;
            }
            String[] tokens = input.split("\\s+");

            // Only proceed if there exists an user input.
            if (tokens.length == 0 || tokens[0].isEmpty()) {
                continue;
            }

            // First input must be a command.
            String commandString = tokens[0].toUpperCase();
            char command = commandString.charAt(0);

            // 3 entered commands?
            if (tokens.length == 3) {
                try {
                    if (command == 'M') {
                        // Make a MOVE.
                        board = move(Integer.parseInt(tokens[1]),
                                Integer.parseInt(tokens[2]), board);
                    } else {
                        printError("Invalid command");
                    }
                } catch (NumberFormatException e) {
                    printError("You have to enter two integers");
                }
            } else if (tokens.length == 2) {
                try {
                    if (command == 'L') {
                        // Set the LEVEL.
                        board = setLevel(board, Integer.parseInt(tokens[1]));
                    } else {
                        printError("Invalid command");
                    }
                } catch (NumberFormatException e) {
                    printError("You have to enter an integer");
                }
            } else {

                // Execute single commands.
                switch (command) {
                case 'N':       // NEW game will be created.
                    board = createNewGame(board);
                    break;
                case 'P':       // PRINT board as a matrix.
                    System.out.println(board);
                    break;
                case 'S':       // SWITCH initial player and start a new game.
                    board = switchPlayer(board);
                    break;
                case 'H':       // Print the HELP dialog.
                    printHelp();
                    break;
                case 'Q':       // QUIT loop and exit the program code.
                    quit = true;
                    break;
                default:
                    printError("Invalid command");
                }
            }
        }
    }

    /**
     * Sets the level of the current Reversi board and checks whether the
     * user entered a valid level value.
     *
     * @param board The current board of a game session.
     * @param level A level value entered by the user.
     * @return The same board with a new set level.
     */
    private static ReversiBoard setLevel(ReversiBoard board, int level) {

        // Difficulty level between 1 and 5?
        if (level <= 5 && level >= 1) {
            board.setLevel(level);
        } else {
            printError("Enter a level between 1 and 5");
        }
        return board;
    }

    /**
     * Creates a new game and returns the newly created board with the same
     * settings as the current board.
     *
     * @param board The old game board.
     * @return The new created initial board.
     */
    private static ReversiBoard createNewGame(ReversiBoard board) {
        board = new ReversiBoard(board, board.getFirstPlayer());
        if (board.getFirstPlayer() == Player.Computer) {
            board = board.machineMove();
        }
        return board;
    }

    /**
     * Concatenates a given message and prints an error message.
     *
     * @param msg This message parts explains why an error occured.
     */
    private static void printError(String msg) {
        System.err.println("Error! " + msg);
    }

    // TODO: else: Runtime error mit exception werfen aber wie
    /**
     * Executes a human move and afterwards, if possible, a machine move, and
     * returns the new board.
     *
     * @param row The slot's row number where a tile of the human player should
     *        be placed on.
     * @param column The slot's column number where a tile of the human player
     *        should be placed on.
     * @param board The current game board where a move should be made.
     * @return A new board with the move/moves executed. If one of the moves is
     *         not valid, then the last changed board will be returned.
     */
    private static ReversiBoard move(int row, int column, ReversiBoard board) {
        if (board.gameOver()) {
            printResults(board);
            return board;
        }

        // Decrement indices because we start counting from 0.
        row--;
        column--;

        // Indices within board dimensions?
        if (row < 0 || row >= Board.SIZE || column < 0
                || column >= Board.SIZE) {
            printError("Row and column indices must be in the range between 1 "
                    + "and " + Board.SIZE);
            return board;
        }
        if (board.next() == Player.Human) {
            ReversiBoard newBoardHuman = board.move(row, column);
            if (newBoardHuman != null) {
                if (newBoardHuman.gameOver()) {
                    printResults(newBoardHuman);
                    return newBoardHuman;
                }
                if (newBoardHuman.next() == Player.Computer) {
                    ReversiBoard newBoardComp = newBoardHuman.machineMove();
                    if (newBoardComp.gameOver()) {
                        printResults(newBoardComp);
                    }
                    return newBoardComp;
                } else {
                    System.out.println("The bot has to miss a turn");
                    return newBoardHuman;
                }
            } else {
                printError("This is not a valid move");
                return board;
            }
        } else {
            System.out.println("You have to miss a turn");
            return board.machineMove();
        }
    }

    /**
     * Prints the winner if the game has ended.
     *
     * @param board The current board that needs to be checked.
     */
    private static void printResults(ReversiBoard board) {
        Player winner = board.getWinner();
        if (winner != null && winner != Player.Nobody) {
            if (winner == Player.Human) {
                System.out.println("You have won");
            } else {
                System.out.println("Machine has won");
            }
        }
    }

    /**
     * Starts a new game and switches the first player.
     *
     * @param board The current board that needs to be initialized.
     * @return A new game/board with the opposite first player.
     */
    private static ReversiBoard switchPlayer(ReversiBoard board) {
        if (board.getFirstPlayer() == Player.Human) {
            ReversiBoard newBoard = new ReversiBoard(board, Player.Computer);
            return createNewGame(newBoard);
        } else {
            ReversiBoard newBoard = new ReversiBoard(board, Player.Human);
            return createNewGame(newBoard);
        }
    }

    /**
     * Called by the command 'HELP' to print the help dialog.
     */
    private static void printHelp() {
        System.out.println("'NEW' \t\t\t\t startet ein neues Spiel. \n"
                + "'MOVE row column' \t Führt einen Zug aus. Row gibt die "
                + "Zeile an, column die Spalte. Der Ursprung ist links oben.\n"
                + "'LEVEL i' \t\t\t setzt den Schwierigkeitsgrad. 1 ist am"
                + " einfachsten und 5 am schwierigsten. \n"
                + "'SWITCH' \t\t\t wechselt den Spieler, der anfängt und "
                + "startet gleichzeitig ein neues Spiel. \n"
                + "'PRINT' \t\t\t Gibt das aktuelle Brett als Zeilen x "
                + "Spalten aus. \n"
                + "'HELP' \t\t\t\t ruft diese Hilfestellung auf. \n"
                + "'QUIT' \t\t\t\t beendet das Programm.");
    }
}