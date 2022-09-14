package it.polimi.ingsw.clients.gui.view.components.panels;

import it.polimi.ingsw.clients.gui.view.components.frames.setup_frame.SetupFrameListener;
import it.polimi.ingsw.clients.gui.view.exceptions.MissingFieldsException;
import it.polimi.ingsw.clients.gui.view.utils.GuiUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginPanel extends JPanel implements ActionListener {
    private final JLabel usernameLabel;
    private final JTextField usernameInput;
    private final JButton proceedButton;

    private final transient SetupFrameListener listener;

    public LoginPanel(SetupFrameListener listener) {
        this.listener = listener;

        usernameLabel = new JLabel("Nickname");
        usernameInput = new JTextField();
        proceedButton = new JButton("Avanti");
        proceedButton.addActionListener(this);

        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();

        setLayout(layout);

        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(usernameLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        usernameInput.setPreferredSize(new Dimension(100, 25));
        add(usernameInput, gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        add(proceedButton, gbc);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String nickname = usernameInput.getText().strip();
        if (nickname.isEmpty()) {
            GuiUtils.alert("Campi mancanti");
            return;
        }
        listener.receiveNickname(nickname);
    }
}
