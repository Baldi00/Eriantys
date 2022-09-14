package it.polimi.ingsw.clients.gui.view.components.panels;

import it.polimi.ingsw.clients.gui.view.components.frames.setup_frame.SetupFrameListener;
import it.polimi.ingsw.clients.gui.view.utils.GuiUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ConnectionPanel extends JPanel implements ActionListener {
    private final JTextField ipInput;
    private final JTextField portInput;

    private final transient SetupFrameListener listener;

    public ConnectionPanel(SetupFrameListener listener) {
        this.listener = listener;

        JLabel ipLabel = new JLabel("Server IP");
        ipInput = new JTextField("localhost");

        JLabel portLabel = new JLabel("Server port");
        portInput = new JTextField("5000");

        JButton proceedButton = new JButton("Avanti");
        proceedButton.addActionListener(this);


        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(ipLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        ipInput.setPreferredSize(new Dimension(100, 25));
        add(ipInput, gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(portLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        portInput.setPreferredSize(new Dimension(100, 25));
        add(portInput, gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        add(proceedButton, gbc);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String ip;
        int port;

        ip = ipInput.getText().strip();
        if (ip == null || ip.isEmpty()) {
            GuiUtils.alert("Indirizzo IP non valido");
            return;
        }

        try {
            port = Integer.parseInt(portInput.getText());
        } catch (NumberFormatException ex) {
            GuiUtils.alert("Porta non valida");
            return;
        }

        listener.receiveIpAndPort(ip, port);
    }
}
