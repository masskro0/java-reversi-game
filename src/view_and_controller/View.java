package view_and_controller;

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
import java.awt.event.MouseListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Toolkit;

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
        this.model = model;
        gameSlots = new Slot[Board.SIZE][Board.SIZE];
        setTitle("Reversi");
        setMinimumSize(new Dimension(MIN_FRAME_WIDTH, MIN_FRAME_HEIGHT));

        // Opens the window in the middle of the screen.
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        gamePanel = getGamePanel();
        humanTiles = new JLabel(" " + model.getNumberOfHumanTiles());
        machineTiles = new JLabel(model.getNumberOfMachineTiles() + " ");
        addAxis();
        add(gamePanel, BorderLayout.CENTER);
        add(getLowerPanel(), BorderLayout.SOUTH);
        history = new LinkedList<>();
        pack();
    }

    /**
     * Adds the vertical and horizontal numeric axis to the main frame.
     */
    private void addAxis() {
        JPanel vertical = new JPanel(new GridLayout(Board.SIZE, 1));

        JPanel horizonal = new JPanel(new GridLayout(1, Board.SIZE));
        JPanel horizontalContainer = new JPanel();
        horizontalContainer.setLayout(new BoxLayout(horizontalContainer,
                BoxLayout.X_AXIS));

        // Add numbers to the JPanel.
        for (int i = 0; i < Board.SIZE; ++i) {
            JLabel horizontalLabel = new JLabel(String.valueOf(i + 1));
            horizontalLabel.setHorizontalAlignment((int) CENTER_ALIGNMENT);
            horizonal.add(horizontalLabel);
            vertical.add(new JLabel(String.valueOf(i + 1)));
        }
        add(vertical, BorderLayout.WEST);

        // Call pack to set the size for the vertical axis.
        pack();
        horizontalContainer.add(Box.createHorizontalStrut(
                vertical.getWidth()));
        horizontalContainer.add(horizonal);
        add(horizontalContainer, BorderLayout.NORTH);
    }

    /**
     * Updates the number of tiles of both players.
     */
    private void updateScores() {
        humanTiles.setText(" " + model.getNumberOfHumanTiles());
        machineTiles.setText(model.getNumberOfMachineTiles() + " ");
    }

    /**
     * Initializes the game panel with slots and adds behaviour for each of
     * them.
     *
     * @return Configured game panel.
     */
    private JPanel getGamePanel() {
        // Use a grid layout for an equal distribution of the slots.
        JPanel gamePanel = new JPanel(new GridLayout(Board.SIZE, Board.SIZE));
        gamePanel.setBackground(GAME_PANEL_BACKGROUND);

        MouseListener mouseListener = new MouseAdapter() {
            /**
             * Executes a human move and afterwards machine moves when a slot
             * was clicked. Also checks, if the machine thread is busy.
             *
             * @param e The mouse event.
             */
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                if (model.next() == Player.HUMAN && !model.gameOver()) {
                    humanMove(((Slot) e.getSource()).row,
                            ((Slot) e.getSource()).column);
                } else if (machineThread != null && machineThread.isAlive()
                        && !model.gameOver()) {
                    JOptionPane.showMessageDialog(null,
                            "The machine is currently calculating.");
                } else if (model.next() == Player.COMPUTER
                        && !model.gameOver()) {
                    machineMove();
                }
                gameOverChecker();
            }
        };

        // Create new slots and assign the upper {@code MouseListener} to them.
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

    /**
     * Checks whether the game is over or not and displays a message dialog
     * when it is.
     */
    private void gameOverChecker() {
        if (model.gameOver()) {
            if (model.getWinner() == Player.HUMAN) {
                JOptionPane.showMessageDialog(null,
                        "You have won!");
            } else if (model.getWinner() == Player.COMPUTER) {
                JOptionPane.showMessageDialog(null,
                        "The computer has won!");
            } else {
                JOptionPane.showMessageDialog(null,
                        "Draw!");
            }
        }
    }

    /**
     * Creates and returns the lower panel which contains buttons, the level
     * combo box and the scores values of both players.
     *
     * @return The {@code JPanel} object of the lower panel.
     */
    private JPanel getLowerPanel() {
        // A container panel containing the score values, buttons and box.
        JPanel container = new JPanel(new BorderLayout());

        // Contains buttons and box.
        JPanel menuPanel = getMenuPanel();

        // JLabel of the human's score.
        humanTiles.setFont(humanTiles.getFont().deriveFont(25f));
        humanTiles.setForeground(HUMAN_TILE_COLOR);

        // JLabel of the machine's score.
        machineTiles.setFont(humanTiles.getFont().deriveFont(25f));
        machineTiles.setForeground(MACHINE_TILE_COLOR);

        container.add(humanTiles, BorderLayout.WEST);
        container.add(menuPanel);
        container.add(machineTiles, BorderLayout.EAST);
        return container;
    }

    /**
     * Executes a human move and repaints the GUI.
     *
     * @param row Row index.
     * @param column Column index.
     */
    private void humanMove(int row, int column) {
        if (!model.gameOver() && model.move(row, column) == null) {
            // Plays a sound in case of a incorrect move.
            Toolkit.getDefaultToolkit().beep();
            return;
        }

        // Was a new move executed?
        if (!model.equals(history.peekLast())) {
            history.push(model);
            undoButtonModel.setEnabled(true);
        }
        if (!model.gameOver() && model.next() == Player.HUMAN) {
            model = model.move(row, column);
            updateScores();
            gamePanel.repaint();
        } else {
            return;
        }
        if (!model.gameOver() && model.next() == Player.HUMAN) {
            JOptionPane.showMessageDialog(null,
                    "The machine has to miss a turn.");
            return;
        }
        if (!model.gameOver() && model.next() == Player.COMPUTER) {
            machineMove();
        }
    }

    /**
     * Executes a machine move and repaints the GUI.
     */
    private void machineMove() {
        // Start the machine move in a new thread.
        machineThread = new Thread(() -> {
            model = model.machineMove();
            updateScores();
            while (model.next() == Player.COMPUTER) {
                JOptionPane.showMessageDialog(null,
                        "You have to miss a turn.");
                model = model.machineMove();
                updateScores();
                gameOverChecker();
                gamePanel.repaint();
            }
            gameOverChecker();
            gamePanel.repaint();

            /**
             * Keeps the right level when the user changes it while the
             * machine thread is busy.
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

    /**
     * Creates and configures the menu panel where all the buttons and the
     * level box are included.
     *
     * @return The configured menu panel as a {@code JPanel} object.
     */
    private JPanel getMenuPanel() {
        JPanel menuPanel = new JPanel(new FlowLayout());
        JComboBox levelBox = getLevelBox();
        levelBox.addActionListener(new ActionListener() {
            /**
             * Sets the level to the one that was selected.
             *
             * @param actionEvent The {@code ActionEvent} after selecting a
             *                   level.
             */
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                currentLevel = (Integer) levelBox.getSelectedItem();
                model.setLevel(currentLevel);
            }
        });
        JButton newButton = new JButton("New");
        newButton.setMnemonic(KeyEvent.VK_N);
        newButton.addActionListener(new ActionListener() {
            /**
             * Starts a new game with the same settings.
             *
             * @param actionEvent The {@code ActionEvent} after clicking on the
             *                   new button.
             */
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                stopMachineThread();
                history.clear();
                model = new ReversiBoard((ReversiBoard) model,
                        model.getFirstPlayer());
                updateScores();
                gamePanel.repaint();
                if (model.next() == Player.COMPUTER) {
                    machineMove();
                }
            }
        });
        JButton switchButton = new JButton("Switch");
        switchButton.setMnemonic(KeyEvent.VK_S);
        switchButton.addActionListener(new ActionListener() {
            /**
             * Starts a new game and switches the first player.
             *
             * @param actionEvent The {@code ActionEvent} after clicking on the
             *                   switch button.
             */
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                stopMachineThread();
                history.clear();
                undoButtonModel.setEnabled(false);
                if (model.getFirstPlayer() == Player.HUMAN) {
                    model = new ReversiBoard((ReversiBoard) model,
                            Player.COMPUTER);
                    machineMove();
                } else {
                    model = new ReversiBoard((ReversiBoard) model,
                            Player.HUMAN);
                }
                updateScores();
                gamePanel.repaint();
            }
        });
        JButton undoButton = new JButton("Undo");
        undoButton.setMnemonic(KeyEvent.VK_U);

        // Make the undo button initially unusable.
        undoButton.setEnabled(false);

        // Pass the undo button's model to control its usability globally.
        setUndoButtonModel(undoButton.getModel());
        undoButton.addActionListener(new ActionListener() {
            /**
             * Undos the last move and repaints the gui.
             *
             * @param actionEvent The {@code ActionEvent} after clicking on the
             *                    undo button.
             */
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (history.size() > 0) {
                    model = history.pop();
                    updateScores();
                    if (history.size() == 0) {
                        undoButtonModel.setEnabled(false);
                    }
                    gamePanel.repaint();
                }
            }
        });

        JButton quitButton = new JButton("Quit");
        quitButton.setMnemonic(KeyEvent.VK_Q);
        quitButton.addActionListener(new ActionListener() {
            /**
             * Stops the machine thread and disposes the GUI.
             *
             * @param actionEvent The {@code ActionEvent} being triggered by
             *                    clicking the button.
             */
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                stopMachineThread();
                dispose();
            }
        });
        menuPanel.add(levelBox);
        menuPanel.add(newButton);
        menuPanel.add(switchButton);
        menuPanel.add(undoButton);
        menuPanel.add(quitButton);
        return menuPanel;
    }

    /**
     * Sets the undo button's model to control its usability globally.
     *
     * @param undoButtonModel The model of the undo button.
     */
    private void setUndoButtonModel(ButtonModel undoButtonModel) {
        this.undoButtonModel = undoButtonModel;
    }

    /**
     * Creates and configures the drop down box for the level configuration.
     *
     * @return The {@code JComboBox} of the level box.
     */
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
     * This class represents a slot on the {@code gamePanel}. It is analogue to
     * a player tile of the game board model.
     */
    private final class Slot extends JPanel {

        /**
         * The row index of this slot.
         */
        private final int row;

        /**
         * The column index of this slot.
         */
        private final int column;

        /**
         * Creates a new {@code Slot} object.
         *
         * @param row Row index of this slot.
         * @param column Column index of this slot.
         */
        private Slot(int row, int column) {
            this.row = row;
            this.column = column;
            setBackground(GAME_PANEL_BACKGROUND);

            // Divide each slot by a border.
            setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        }

        /**
         * Paints a single slot on the {@code gamePanel} object to visualize
         * its appearance.
         *
         * @param gr The graphic object.
         */
        @Override
        protected void paintComponent(Graphics gr) {
            super.paintComponent(gr);
            if (model.getSlot(row, column) != Player.NOBODY) {
                // Get the coordinates of the center of the circle.
                int cntrX = getWidth() / 2;
                int cntrY = getHeight() / 2;
                int radius;

                // Get the radius dependent on the smaller size.
                if (cntrX < cntrY) {
                    // Leave a little bit space so the circles won't overlap.
                    radius = getWidth() / 2 - 2;
                } else {
                    radius = getHeight() / 2 - 2;
                }
                if (model.getSlot(row, column) == Player.HUMAN) {
                    gr.setColor(HUMAN_TILE_COLOR);
                } else if (model.getSlot(row, column) == Player.COMPUTER) {
                    gr.setColor(MACHINE_TILE_COLOR);
                }

                // Draw the circles.
                gr.fillOval(cntrX - radius, cntrY - radius, radius * 2,
                        radius * 2);
                gr.drawOval(cntrX - radius, cntrY - radius, radius * 2,
                        radius * 2);

            }
        }
    }
}
