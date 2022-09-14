package it.polimi.ingsw.clients.gui.view.components.panels;

import it.polimi.ingsw.clients.gui.view.components.frames.setup_frame.SetupFrameListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MatchSetupPanel extends JPanel implements ActionListener {
    private final JLabel numPlayersLabel;
    private final JLabel expertModeLabel;
    private final JCheckBox expertModeCheck;
    private final JComboBox<Integer> numPlayersCombo;
    private final DefaultComboBoxModel<Integer> model;
    private final JButton proceedButton;

    private final transient SetupFrameListener listener;

    public MatchSetupPanel(SetupFrameListener listener) {
        this.listener = listener;

        numPlayersLabel = new JLabel("Numero giocatori");
        expertModeLabel = new JLabel("Modalità esperto?");
        expertModeCheck = new JCheckBox();
        proceedButton = new JButton("START");
        proceedButton.addActionListener(this);
        model = new DefaultComboBoxModel<>();
        model.addElement(2);
        model.addElement(3);
        model.addElement(4);
        model.setSelectedItem(2);
        numPlayersCombo = new JComboBox<>(model);

        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();

        setLayout(layout);

        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(numPlayersLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        numPlayersCombo.setPreferredSize(new Dimension(75, 25));
        add(numPlayersCombo, gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 1;
        expertModeLabel.setPreferredSize(new Dimension(200, 25));
        add(expertModeLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        add(expertModeCheck, gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        add(proceedButton, gbc);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int numPlayers = (int) model.getSelectedItem();
        boolean expertMode = expertModeCheck.isSelected();

        listener.receiveMatchMode(numPlayers, expertMode);

    }
}
