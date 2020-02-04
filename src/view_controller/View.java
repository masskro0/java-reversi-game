package view_controller;

import model.Board;
import model.Player;
import model.ReversiBoard;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;

public class View extends JFrame {

    private static final int MAX_LEVEL = 5;
    private static final int DEFAULT_LEVEL = 3;
    private static final int MIN_FRAME_WIDTH = 400;
    private static final int MIN_FRAME_HEIGHT = 400;
    private static final Color HUMAN_TILE_COLOR = Color.BLUE;
    private static final Color MACHINE_TILE_COLOR = Color.RED;
    private static final Color GAME_PANEL_BACKGROUND = Color.GREEN;
    private static final Color MENU_PANEL_BORDER_COLOR = Color.BLACK;

    private final JPanel gamePanel;
    private final Slot[][] gameSlots;
    private int currentLevel = DEFAULT_LEVEL;
    private Board gameModel;
    private boolean isHumansTurn;
    private Thread machineThread;
    private LinkedList<Board> history;
    private ButtonModel undoButtonModel;
    private JLabel humanTiles;
    private JLabel machineTiles;


    View(Board gameModel) {
        isHumansTurn = gameModel.getFirstPlayer() == Player.HUMAN;
        this.gameModel = gameModel;
        gameSlots = new Slot[Board.SIZE][Board.SIZE];
        setTitle("Reversi");
        setMinimumSize(new Dimension(MIN_FRAME_WIDTH, MIN_FRAME_HEIGHT));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        gamePanel = getGamePanel();
        humanTiles = new JLabel(" " + gameModel.getNumberOfHumanTiles());
        machineTiles = new JLabel(gameModel.getNumberOfMachineTiles() + " ");
        addAxis();
        add(gamePanel, BorderLayout.CENTER);
        add(getMenuPanel(), BorderLayout.SOUTH);
        history = new LinkedList<>();

        pack();
    }

    private void addAxis() {
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
        add(horizonal, BorderLayout.NORTH);
        add(vertical, BorderLayout.WEST);
    }

    private void updateScores() {
        humanTiles.setText(" " + gameModel.getNumberOfHumanTiles());
        machineTiles.setText(gameModel.getNumberOfMachineTiles() + " ");
    }

    private JPanel getGamePanel() {
        GridLayout gridLayout = new GridLayout(Board.SIZE, Board.SIZE);
        JPanel gamePanel = new JPanel(gridLayout);
        gamePanel.setBackground(GAME_PANEL_BACKGROUND);

        MouseListener mouseListener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);

                if (isHumansTurn && !gameModel.gameOver()) {
                    executeHumanMove(((Slot) e.getSource()).row, ((Slot) e.getSource()).col);
                } else if (machineThread != null && machineThread.isAlive()
                        && !gameModel.gameOver()) {
                    JOptionPane.showMessageDialog(null,
                            "The machine is currently calculating.");
                } else if (!isHumansTurn && !gameModel.gameOver()) {
                    executeMachineMove();
                }
                gameOverNotifier();
            }
        };
        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                Slot slot = new Slot(row, col);
                slot.addMouseListener(mouseListener);

                // Add the slot to the array for later referencing.
                gameSlots[row][col] = slot;
                gamePanel.add(slot);
            }
        }
        return gamePanel;
    }

    private void gameOverNotifier() {
        if (gameModel.gameOver()) {
            if (gameModel.getWinner() == Player.HUMAN) {
                JOptionPane.showMessageDialog(null,
                        "Congratulations! You won.");
            } else if (gameModel.getWinner() == Player.COMPUTER) {
                JOptionPane.showMessageDialog(null,
                        "Sorry! The machine wins.");
            } else {
                JOptionPane.showMessageDialog(null,
                        "The game ends in a tie.");
            }
        }
    }

    private JPanel getMenuPanel() {
        JPanel container = new JPanel(new BorderLayout());

        JPanel menuPanel = new JPanel(new FlowLayout());
        createJComponentsOfMenu(menuPanel);

        humanTiles.setFont(humanTiles.getFont().deriveFont(25f));
        humanTiles.setForeground(Color.BLUE);

        machineTiles.setFont(humanTiles.getFont().deriveFont(25f));
        machineTiles.setForeground(Color.RED);

        container.add(humanTiles, BorderLayout.WEST);
        container.add(menuPanel);
        container.add(machineTiles, BorderLayout.EAST);

        return container;
    }

    private void executeHumanMove(int row, int col) {
        if (!gameModel.gameOver() && gameModel.move(row, col) == null) {
            JOptionPane.showMessageDialog(null,
                    "Invalid move.");
            return;
        }
        if (!gameModel.equals(history.peekLast())) {
            history.push(gameModel);
            undoButtonModel.setEnabled(true);
        }
        if (!gameModel.gameOver() && isHumansTurn) {
            gameModel = gameModel.move(row, col);
            gamePanel.repaint();
            isHumansTurn = false;
        } else {
            return;
        }
        if (!gameModel.gameOver() && !isHumansTurn) {
            executeMachineMove();
        }
    }

    private void executeMachineMove() {
        machineThread = new Thread(() -> {
            gameModel = gameModel.machineMove();
            gameOverNotifier();
            gamePanel.repaint();
            isHumansTurn = true;
            updateScores();

            /*
            Enables the game to recognize a level change while the
            machine is calculating. After the calculation has finished the
            new level takes effect.
            */
            gameModel.setLevel(currentLevel);
        });
        machineThread.start();
    }

    @SuppressWarnings("deprecation")
    private void stopMachineThread() {
        if (machineThread != null && machineThread.isAlive()) {
            machineThread.stop();
        }
    }

    private void createJComponentsOfMenu(JPanel menuPanel) {
        JComboBox levelBox = getLevelBox();
        levelBox.addActionListener(new ActionListener() {

            /**
             * Sets the game's level of difficulty to the number that was
             * selected.
             * @param e The {@code ActionEvent} being triggered by selecting a
             *          number.
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                if (levelBox.getSelectedItem() != null) {
                    currentLevel = (Integer) levelBox.getSelectedItem();
                    gameModel.setLevel(currentLevel);

                    /*
                    Prevents graphical uncleanliness after selecting a new
                    level in fullscreen mode.
                    */
                    gamePanel.repaint();
                }
            }
        });
        JButton newButton = new JButton("New");
        newButton.setMnemonic(KeyEvent.VK_N);
        newButton.addActionListener(new ActionListener() {

            /**
             * Starts a new game with the same starting player by clicking
             * the button.
             * @param e The {@code ActionEvent} being triggered by clicking
             *          the button.
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                isHumansTurn = gameModel.getFirstPlayer() == Player.HUMAN;
                stopMachineThread();
                gameModel = new ReversiBoard((ReversiBoard) gameModel, gameModel.getFirstPlayer());
                gameModel.setLevel(currentLevel);
                gamePanel.repaint();

                if (!isHumansTurn) {
                    executeMachineMove();
                }
            }
        });
        JButton switchButton = new JButton("Switch");
        switchButton.setMnemonic(KeyEvent.VK_S);
        switchButton.addActionListener(new ActionListener() {
            /**
             * Starts a new game and switches the beginning player by
             * clicking the button.
             * @param e The {@code ActionEvent} being triggered by clicking
             *          the button.
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                stopMachineThread();
                if (gameModel.getFirstPlayer() == Player.HUMAN) {
                    isHumansTurn = false;
                    gameModel = new ReversiBoard((ReversiBoard) gameModel, Player.COMPUTER);
                    gameModel.setLevel(currentLevel);
                    gamePanel.repaint();
                    executeMachineMove();
                } else {
                    isHumansTurn = true;
                    gameModel = new ReversiBoard();
                    gameModel.setLevel(currentLevel);
                    gamePanel.repaint();
                }
            }
        });
        JButton undoButton = new JButton("Undo");
        undoButton.setMnemonic(KeyEvent.VK_U);
        undoButton.setEnabled(false);
        setUndoButtonModel(undoButton.getModel());
        undoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (history.size() > 0) {
                    gameModel = history.pop();
                    gamePanel.repaint();
                    updateScores();
                    if (history.size() == 0) {
                        undoButtonModel.setEnabled(false);
                    }
                }
            }
        });

        JButton quitButton = new JButton("Quit");
        quitButton.setMnemonic(KeyEvent.VK_Q);
        quitButton.addActionListener(new ActionListener() {

            /**
             * Stops the running machine thread and closes the game.
             * @param e The {@code ActionEvent} being triggered by clicking
             *          the button.
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                stopMachineThread();
                dispose();
            }
        });
        menuPanel.add(levelBox);
        menuPanel.add(newButton);
        menuPanel.add(switchButton);
        menuPanel.add(undoButton);
        menuPanel.add(quitButton);
    }

    private void setUndoButtonModel(ButtonModel undoButtonModel) {
        this.undoButtonModel = undoButtonModel;
    }

    private JComboBox getLevelBox() {
        Integer[] levels = new Integer[MAX_LEVEL];

        for (int i = 0; i < MAX_LEVEL; i++) {
            levels[i] = i + 1;
        }
        JComboBox levelBox = new JComboBox<>(levels);
        levelBox.setSelectedItem(currentLevel);

        return levelBox;
    }

    /**
     * The {@code Slot} is the main component of the {@code gamePanel}, which
     * indicates if or by whom this position in the game-grid is already taken.
     */
    private final class Slot extends JPanel {

        private final int row;
        private final int col;

        /**
         * Constructs a new {@code Slot} and sets its {@code row} and {@code
         * col}umn. Initially the slot is no witness.
         *
         * @param row The row of the slot indicated at 0.
         * @param col The column of the slot indicated at 0.
         */
        private Slot(int row, int col) {
            this.row = row;
            this.col = col;
            setBackground(GAME_PANEL_BACKGROUND);
            setBorder(BorderFactory.createLineBorder(MENU_PANEL_BORDER_COLOR));
        }

        /**
         * Manages the appearance of the slot with respect to the gameModel's
         * game state.GameOver
         *
         * @param gr The object to draw onto.
         */
        @Override
        protected void paintComponent(Graphics gr) {
            super.paintComponent(gr);
            if (gameModel.getSlot(row, col) != Player.NOBODY) {
                int cntrX = getWidth() / 2;
                int cntrY = getHeight() / 2;
                int radius;
                if (cntrX < cntrY) {
                    radius = getWidth() / 2 - 3;
                } else {
                    radius = getHeight() / 2 - 3;
                }
                if (gameModel.getSlot(row, col) == Player.HUMAN) {
                    gr.setColor(HUMAN_TILE_COLOR);
                } else if (gameModel.getSlot(row, col) == Player.COMPUTER) {
                    gr.setColor(MACHINE_TILE_COLOR);
                }
                gr.fillOval(cntrX - radius, cntrY - radius, radius * 2,
                        radius * 2);
                gr.drawOval(cntrX - radius, cntrY - radius, radius * 2,
                        radius * 2);

            }
        }
    }
}
