package view_controller;

import javax.swing.*;

public class StartGui {

    public static void main(String[] args) {
        Runnable r = () -> {
            ReversiGui reversiGui = new ReversiGui();

            JFrame frame = new JFrame("Reversi");
            frame.setSize(500, 500);
            frame.getContentPane().add(reversiGui.getGui());
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setLocationByPlatform(true);
            frame.setVisible(true);
        };
        SwingUtilities.invokeLater(r);
    }
}
