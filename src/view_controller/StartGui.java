package view_controller;

import model.Board;
import model.ReversiBoard;

import javax.swing.*;

public class StartGui {

    private StartGui() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Board model = new ReversiBoard();
            JFrame mainFrame = new View(model);
            mainFrame.setVisible(true);
        });
    }
}
