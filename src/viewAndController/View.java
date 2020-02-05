package viewAndController;

import model.Board;
import model.Player;
import model.ReversiBoard;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.ButtonModel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.BoxLayout;
import javax.swing.Box;
import javax.swing.JOptionPane;
import javax.swing.JComboBox;
import javax.swing.BorderFactory;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.awt.event.MouseListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.Graphics;

import java.util.LinkedList;

public class View extends JFrame {

    private final int MAX_LEVEL = 5;
    private final int DEFAULT_LEVEL = 3;
    private final int MIN_FRAME_WIDTH = 500;
    private final int MIN_FRAME_HEIGHT = 500;
    private final Color HUMAN_TILE_COLOR = Color.BLUE;
    private final Color MACHINE_TILE_COLOR = Color.RED;
    private final Color GAME_PANEL_BACKGROUND = Color.GREEN;
    private final Color BORDER_COLOR = Color.BLACK;

    /**
     * Contains the slots and represents the game board.
     */
    private final JPanel gamePanel;

    /**
     * Contains every single slot to simply add to the {@code gamePanel}.
     */
    private final Slot[][] gameSlots;

    /**
     * Stores the chosen difficulty of the current session. Initially on 3.
     */
    private int currentLevel = DEFAULT_LEVEL;

    /**
     * The game model which interacts with the interface.
     */
    private Board model;

    /**
     * {@code true} if and only if it is the human's turn.
     */
    private boolean isHumansTurn;

    /**
     * A separate thread to execute machine moves, so a human can still
     * interact with the UI.
     */
    private Thread machineThread;

    /**
     * A stack which stores {@code Board} objects of previous moves.
     */
    private LinkedList<Board> history;

    /**
     * The model of the undo button to access its features globally.
     */
    private ButtonModel undoButtonModel;

    /**
     * Text field containing the number of human tiles.
     */
    private JLabel humanTiles;

    /**
     * Text field containing the number of machine tiles.
     */
    private JLabel machineTiles;

    /**
     * Initializes a new {@code JFrame} main frame where all components are
     * stored.
     *
     * @param model The game board.
     */
    public View(Board model) {
        isHumansTurn = model.getFirstPlayer() == Player.HUMAN;
        this.model = model;
        gameSlots = new Slot[Board.SIZE][Board.SIZE];
        setTitle("Reversi");
        setMinimumSize(new Dimension(MIN_FRAME_WIDTH, MIN_FRAME_HEIGHT));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        gamePanel = getGamePanel();
        humanTiles = new JLabel(" " + model.getNumberOfHumanTiles());
        machineTiles = new JLabel(model.getNumberOfMachineTiles() + " ");
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
            horizonal.setBackground(Color.YELLOW);
            vertical.add(Box.createVerticalGlue());
            vertical.add(new JLabel(String.valueOf(i + 1)));
            vertical.setBackground(Color.GRAY);
        }
        horizonal.add(Box.createHorizontalGlue());
        vertical.add(Box.createVerticalGlue());
        add(horizonal, BorderLayout.NORTH);
        add(vertical, BorderLayout.WEST);
    }

    private void updateScores() {
        humanTiles.setText(" " + model.getNumberOfHumanTiles());
        machineTiles.setText(model.getNumberOfMachineTiles() + " ");
    }

    private JPanel getGamePanel() {
        GridLayout gridLayout = new GridLayout(Board.SIZE, Board.SIZE);
        JPanel gamePanel = new JPanel(gridLayout);
        gamePanel.setBackground(GAME_PANEL_BACKGROUND);

        MouseListener mouseListener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);

                if (isHumansTurn && !model.gameOver()) {
                    executeHumanMove(((Slot) e.getSource()).row, ((Slot) e.getSource()).col);
                } else if (machineThread != null && machineThread.isAlive()
                        && !model.gameOver()) {
                    JOptionPane.showMessageDialog(null,
                            "The machine is currently calculating.");
                } else if (!isHumansTurn && !model.gameOver()) {
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
        if (model.gameOver()) {
            if (model.getWinner() == Player.HUMAN) {
                JOptionPane.showMessageDialog(null,
                        "Congratulations! You won.");
            } else if (model.getWinner() == Player.COMPUTER) {
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
        if (!model.gameOver() && model.move(row, col) == null) {
            JOptionPane.showMessageDialog(null,
                    "Invalid move.");
            return;
        }
        if (!model.equals(history.peekLast())) {
            history.push(model);
            undoButtonModel.setEnabled(true);
        }
        if (!model.gameOver() && isHumansTurn) {
            model = model.move(row, col);
            gamePanel.repaint();
            isHumansTurn = false;
        } else {
            return;
        }
        if (!model.gameOver() && !isHumansTurn) {
            executeMachineMove();
        }
    }

    private void executeMachineMove() {
        machineThread = new Thread(() -> {
            model = model.machineMove();
            gameOverNotifier();
            gamePanel.repaint();
            isHumansTurn = true;
            updateScores();

            /*
            Enables the game to recognize a level change while the
            machine is calculating. After the calculation has finished the
            new level takes effect.
            */
            model.setLevel(currentLevel);
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
                    model.setLevel(currentLevel);

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
                isHumansTurn = model.getFirstPlayer() == Player.HUMAN;
                stopMachineThread();
                model = new ReversiBoard((ReversiBoard) model, model.getFirstPlayer());
                model.setLevel(currentLevel);
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
                history.clear();
                undoButtonModel.setEnabled(false);
                if (model.getFirstPlayer() == Player.HUMAN) {
                    isHumansTurn = false;
                    model = new ReversiBoard((ReversiBoard) model, Player.COMPUTER);
                    model.setLevel(currentLevel);
                    gamePanel.repaint();
                    executeMachineMove();
                } else {
                    isHumansTurn = true;
                    model = new ReversiBoard();
                    model.setLevel(currentLevel);
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
                    model = history.pop();
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
            setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
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
            if (model.getSlot(row, col) != Player.NOBODY) {
                int cntrX = getWidth() / 2;
                int cntrY = getHeight() / 2;
                int radius;
                if (cntrX < cntrY) {
                    radius = getWidth() / 2 - 3;
                } else {
                    radius = getHeight() / 2 - 3;
                }
                if (model.getSlot(row, col) == Player.HUMAN) {
                    gr.setColor(HUMAN_TILE_COLOR);
                } else if (model.getSlot(row, col) == Player.COMPUTER) {
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
