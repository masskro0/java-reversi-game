import java.util.Scanner;
/**
 * A user interface who gets an input, executes the command and prints the
 * output.
 */
public final class Shell {

    // Allow only one instance of the Shell
    private Shell() { }

    /**
     * Main loop for user interaction.
     * @param args user input
     */
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        // Loop iteration variable needed to quit the loop
        boolean quit = false;
        ReversiBoard board = new ReversiBoard();

        // Main loop to get user input and execute it
        while (!quit) {
            System.out.print("othello> ");
            // Get next input
            String input = sc.nextLine();
            // Remove whitespace until the first char appears
            while (input.charAt(0) == ' ') {
                input = input.substring(1);
            }
            String[] tokens = input.split("\\s+");
            // Only proceed if there exists an user input
            if (tokens.length > 0 && !tokens[0].equals("") && input != null) {
                // First input must be a command
                String commandString = tokens[0].toUpperCase();
                // Safe it in a char
                char command = commandString.charAt(0);
                // 3 entered commands?
                if (tokens.length == 3) {
                    try {
                        // Allign the position to be executed.
                        int row = Integer.valueOf(tokens[1]);
                        int column = Integer.valueOf(tokens[2]);
                        switch (command) {
                            case 'M':
                                // Make a turn
                                // TODO
                                board = move(row, column, board);
                                break;
                            default:
                                System.err.println("Error! Invalid command");
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Error! You have to enter two "
                                + "integers!");
                    }
                } else if (tokens.length == 2) {
                    try {
                        int level = Integer.valueOf(tokens[1]);
                        switch (command) {
                            case 'L':
                                // Choose the difficulty. 1 is the easiest, 5
                                // the highest
                                if (level < 6 && level > 0) {
                                    board.setLevel(level);
                                } else {
                                    System.err.println("Error! Choose a level"
                                            + " between 1 and 5");
                                }
                                break;
                            default:
                                System.err.println("Error! Invalid command");
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Error! You have to enter a"
                                + " interger");
                    }
                } else {
                    // Execute single commands
                    switch (command) {
                        case 'N':
                            // NEW game will be created
                            board = new ReversiBoard(board.getFirstPlayer());
                            break;
                        case 'P':
                            // PRINT field as a matrix as row x column
                            System.out.println(board.toString());
                            break;
                        case 'S':
                            // SWITCH the player who starts the game and start
                            // a new game
                            board = switchPlayer(board);
                            break;
                        case 'H':
                            // HELP dialog
                            printHelp();
                            break;
                        case 'Q':
                            // QUIT loop
                            quit = true;
                            break;
                        default:
                            System.err.println("Error! Invalid command");
                    }
                }
            }
        }
        sc.close();
    }

 // TODO: else: Runtime error mit exception werfen aber wie
    private static ReversiBoard move(int row, int column, ReversiBoard board) {
        row--;
        column--;
        if (row < 0 || row >= Board.SIZE || column < 0
                || column >= Board.SIZE) {
            System.out.println("Error! Row and column index must be in the "
                    + "range between 0 and " + (Board.SIZE - 1));
            return board;
        }
        gameOver(board);
        if (board.next() == Player.Human) {
            ReversiBoard newBoardHuman = board.move(row, column);
            if (newBoardHuman != null) {
                ReversiBoard newBoardComputer = newBoardHuman.machineMove();
                gameOver(newBoardComputer);
                if (newBoardComputer != null) {
                    return newBoardComputer;
                } else {
                    System.out.println("<Bot muss aussetzen>");
                    return newBoardHuman;
                }
            }

        }
        return board;
    }

    private static boolean gameOver(ReversiBoard board) {
        if (board.getNumberOfMachineTiles()
                + board.getNumberOfHumanTiles() == 64) {
            if (board.getNumberOfHumanTiles()
                    > board.getNumberOfMachineTiles()) {
                System.out.println("You have won!");
            } else {
                System.out.println("Machine has won.");
            }
            return true;
        }
        return false;
    }

    /**
     * Start a new game and switch the player who starts the game
     * @param board old reversi board
     * @return new game with switched first player
     */
    private static ReversiBoard switchPlayer(ReversiBoard board) {
        if (board.getFirstPlayer() == Player.Human) {
            ReversiBoard newBoard = new ReversiBoard(Player.Computer);
            return newBoard.machineMove();
        }
        return new ReversiBoard(Player.Human);
    }

    /**
     * Called by the command 'H' to print the help dialog.
     */
    private static void printHelp() {
        System.out.println("'NEW' startet ein neues Spiel. \n"
                + "'MOVE row column' - Ein Zug kann durchgeführt werden, wenn "
                + "man dran ist. Row gibt die Zeile an, column die Spalte. "
                + "Der Ursprung ist links oben. \n"
                + "'LEVEL i' setzt den Schwierigkeitsgrad. 1 ist am"
                + " einfachsten und 7 am schwierigsten. \n"
                + "'SWITCH' wechselt den Spieler, der anfängt und startet"
                + "gleichzeitig ein neues Spiel. \n"
                + "'PRINT' Gibt das aktuelle Brett als Zeilen x Spalten aus."
                + " \n"
                + "'HELP' ruft diese Hilfestellung auf \n"
                + "'QUIT' beendet das Programm");
    }
}