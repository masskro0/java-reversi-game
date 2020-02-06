package view_and_controller;

import model.Board;
import model.ReversiBoard;

import javax.swing.SwingUtilities;
import javax.swing.JFrame;

/**
 * {@code StartGui} initializes a model and a view and starts a new game.
 */
public final class StartGui {

    // Allow only one instance of the {@code StartGui} class.
    private StartGui() {
    }

    /**
     * Starts a new game in the Event Dispatcher Thread.
     *
     * @param args Passed arguments to the main method.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Board model = new ReversiBoard();
            JFrame mainFrame = new View(model);
            mainFrame.setVisible(true);
        });
    }
}
