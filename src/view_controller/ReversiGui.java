package view_controller;

import model.Board;
import model.Player;
import model.ReversiBoard;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.LinkedList;
import javax.swing.*;
import javax.swing.border.*;

class ReversiGui {

    private final JPanel gui = new JPanel(new BorderLayout());
    private JPanel[][] slots = new JPanel[Board.SIZE][Board.SIZE];
    private JPanel gameBoard;
    private final String[] levels = {"1", "2", "3", "4", "5"};
    private final int DEFAULT_LEVEL = 3;
    private Board board = new ReversiBoard();
    private static LinkedList<Board> history = new LinkedList<>();

    ReversiGui() {
        initializeGui();
    }

    public final void initializeGui() {
        gui.setBorder(new EmptyBorder(5, 5, 5, 5));

        JPanel lowerBar = addLowerBar();
        gui.add(lowerBar, BorderLayout.SOUTH);

        gameBoard = new JPanel(new GridLayout(0, Board.SIZE));
        gui.add(gameBoard);

        for (int i = 0; i < slots.length; i++) {
            for (int j = 0; j < slots[i].length; j++) {
                JPanel slot = new Slot(new BorderLayout(),
                        board.getSlot(i, j));
                int finalI = i;
                int finalJ = j;
                slot.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        super.mouseClicked(e);
                        board = move(finalI, finalJ, board);
                        slots[finalI][finalJ] = new Slot(new BorderLayout(),
                                board.getSlot(finalI, finalJ));
                        //gameBoard.add(slots[finalI][finalJ]);
                        Slot slot = (Slot) slots[finalI][finalJ];
                        slot.revalidate();
                        slot.repaint();
                        gameBoard.revalidate();
                        gameBoard.repaint();
                    }
                });
                slots[i][j] = slot;
            }
        }

        JPanel horizonal = new JPanel();
        JPanel vertical = new JPanel();
        horizonal.setLayout(new BoxLayout(horizonal, BoxLayout.X_AXIS));
        vertical.setLayout(new BoxLayout(vertical, BoxLayout.Y_AXIS));
        for (int i = 0; i < Board.SIZE; ++i) {
            horizonal.add(Box.createHorizontalGlue());
            horizonal.add(new JLabel(String.valueOf(i + 1)));
            vertical.add(Box.createVerticalGlue());
            vertical.add(new JLabel(String.valueOf(i + 1)));
        }
        horizonal.add(Box.createHorizontalGlue());
        vertical.add(Box.createVerticalGlue());
        gui.add(horizonal, BorderLayout.NORTH);
        gui.add(vertical, BorderLayout.WEST);


        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                gameBoard.add(slots[i][j]);
            }
        }
    }

    public final JComponent getGui() {
        return gui;
    }

    private final JPanel addLowerBar() {
        JPanel container = new JPanel(new BorderLayout());

        JPanel buttonPanel = new JPanel();

        JLabel humanTiles = new JLabel(
                String.valueOf(board.getNumberOfHumanTiles()));
        humanTiles.setFont(humanTiles.getFont().deriveFont(30f));
        humanTiles.setForeground(Color.BLUE);

        JComboBox levelList = new JComboBox(levels);
        levelList.setSelectedIndex(DEFAULT_LEVEL - 1);
        levelList.addActionListener(actionEvent -> {
            String selection = (String) levelList.getSelectedItem();
            board.setLevel(Integer.parseInt(selection));
        });
        buttonPanel.add(levelList);

        JButton newButton = new JButton("New");
        newButton.setMnemonic(KeyEvent.VK_N);
        newButton.addActionListener(actionEvent -> createNewGame(board));
        buttonPanel.add(newButton);

        JButton switchButton = new JButton("Switch");
        switchButton.setMnemonic(KeyEvent.VK_S);
        switchButton.addActionListener(actionEvent -> switchPlayer(board));
        buttonPanel.add(switchButton);

        JButton undoButton = new JButton("Undo");
        undoButton.setMnemonic(KeyEvent.VK_U);
        undoButton.addActionListener(actionEvent -> {
            if (history.size() > 0) {
                board = history.pop();
            }
            System.out.println(history.size());
        });
        if (history.size() > 0) {
            undoButton.setEnabled(true);
        } else {
            undoButton.setEnabled(false);
        }
        buttonPanel.add(undoButton);

        JButton quitButton = new JButton("Quit");
        quitButton.setMnemonic(KeyEvent.VK_Q);
        quitButton.addActionListener(actionEvent -> System.exit(0));
        buttonPanel.add(quitButton);

        JLabel computerTiles = new JLabel(
                String.valueOf(board.getNumberOfMachineTiles()));
        computerTiles.setFont(humanTiles.getFont().deriveFont(30f));
        computerTiles.setForeground(Color.RED);

        container.add(humanTiles, BorderLayout.WEST);
        container.add(buttonPanel);
        container.add(computerTiles, BorderLayout.EAST);

        return container;
    }

    /**
     * Creates a new game and returns the newly created board with the same
     * settings as the current board.
     *
     * @param board The old game board.
     * @return The new created initial board.
     */
    private static Board createNewGame(Board board) {
        board = new ReversiBoard((ReversiBoard) board, board.getFirstPlayer());
        if (board.getFirstPlayer() == Player.Computer) {
            board = board.machineMove();
        }
        return board;
    }

    /**
     * Starts a new game and switches the first player.
     *
     * @param board The current board that needs to be initialized.
     * @return A new game/board with the opposite first player.
     */
    private static Board switchPlayer(Board board) {
        if (board.getFirstPlayer() == Player.Human) {
            Board newBoard = new ReversiBoard((ReversiBoard) board, Player.Computer);
            return createNewGame(newBoard);
        } else {
            Board newBoard = new ReversiBoard((ReversiBoard) board, Player.Human);
            return createNewGame(newBoard);
        }
    }

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
    private static Board move(int row, int column, Board board) {
        if (board.gameOver()) {
            // printResults(board);
            return board;
        }
        if (!board.equals(history.peekLast())) {
            history.add(board);
        }
        if (board.next() == Player.Human) {
            Board newBoard = board.move(row, column);
            if (newBoard != null) {
                if (newBoard.gameOver()) {
                    // printResults(newBoardHuman);
                    return newBoard;
                }
                if (newBoard.next() == Player.Computer) {
                    while (newBoard.next() == Player.Computer) {
                        newBoard = newBoard.machineMove();
                        if (newBoard.gameOver()) {
                            // printResults(newBoardComp);
                        }
                    }
                    return newBoard;
                } else {
                    //System.out.println("The bot has to miss a turn");
                    return newBoard;
                }
            } else {
                //printError("Invalid move at (" + (row + 1) + ", "
                //        + (column + 1) + ").");
                return board;
            }
        } else {
            //System.out.println("Human has to miss a turn");
            Board newBoard = board.clone();
            while (newBoard.next() == Player.Computer) {
                newBoard = newBoard.machineMove();
            }
            return newBoard;
        }
    }

}
