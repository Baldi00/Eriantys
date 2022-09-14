package it.polimi.ingsw.clients.gui.view.components.panels;

import javax.swing.*;
import java.awt.*;

public class WaitingPanel extends JPanel {
    public WaitingPanel() {
        setLayout(new GridBagLayout());

        JLabel waitMessage = new JLabel("Attendi che altri giocatori si connettano...");

        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(waitMessage);
    }
}
