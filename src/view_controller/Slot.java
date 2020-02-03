package view_controller;

import javax.swing.*;
import java.awt.*;
import model.Player;

public class Slot extends JPanel {

    private Player owner;

    Slot(LayoutManager layout, boolean isDoubleBuffered, Player owner) {
        super(layout, isDoubleBuffered);
        setBackground(Color.GREEN);
        setBorder(BorderFactory.createLineBorder(Color.BLACK));
        this.owner = owner;
    }

    Slot(LayoutManager layout, Player owner) {
        this(layout, true, owner);
    }

    void setOwner(Player owner) {
        this.owner = owner;
    }

    /**
     * Change the owner of this tile.
     */
    void changeSide() {
        if (owner == Player.Computer) {
            owner = Player.Human;
        } else {
            owner = Player.Computer;
        }
    }

    public Player getPlayer() {
        return owner;
    }

    @Override
    protected void paintComponent(Graphics gr) {
        super.paintComponent(gr);
        if (owner != Player.Nobody) {
            int cntrX = getWidth() / 2;
            int cntrY = getHeight() / 2;
            int radius;
            if (cntrX < cntrY) {
                radius = getWidth() / 2 - 3;
            } else {
                radius = getHeight() / 2 - 3;
            }
            if (owner == Player.Human) {
                gr.setColor(Color.BLUE);
            } else if (owner == Player.Computer) {
                gr.setColor(Color.RED);
            }
            gr.fillOval(cntrX - radius, cntrY - radius, radius * 2,
                    radius * 2);
            gr.drawOval(cntrX - radius, cntrY - radius, radius * 2,
                    radius * 2);

        }
    }


}
