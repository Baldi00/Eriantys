package it.polimi.ingsw.clients.gui.view.components.frames.game_frame;

import it.polimi.ingsw.clients.gui.view.assets.Sprite;
import it.polimi.ingsw.models.components.Assistant;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class ChooseAssistantFrame extends JFrame implements ActionListener, MouseListener {
    private static final int ASSISTANT_WIDTH = 148;
    private static final int ASSISTANT_HEIGHT = 225;

    private final List<JLabel> assistants;

    private JLabel previouslySelectedLabel;

    private final transient GameFrameListener listener;

    public ChooseAssistantFrame(
            List<Assistant> availableAssistants,
            boolean isActiveMode,
            GameFrameListener listener
    ) throws HeadlessException {
        this.listener = listener;

        assistants = new ArrayList<>();
        initAssistantsLabels();
        disableUnavailableAssistants(availableAssistants);

        JButton submitButton = createSubmitButton(isActiveMode);

        setTitle("Scegli una carta assistente");
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int k = 0;
        for (int i = 0; i < 2; i++) {
            gbc.gridy = i;
            for (int j = 0; j < 5; j++, k++) {
                gbc.gridx = j;
                add(assistants.get(k), gbc);
            }
        }

        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.gridwidth = 5;
        add(submitButton, gbc);
    }

    private static void selectAssistantLabel(JLabel label, boolean selected) {
        if (selected)
            label.setBorder(BorderFactory.createLineBorder(Color.BLUE));
        else
            label.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
    }

    private void initAssistantsLabels() {
        for (Assistant assistant : Assistant.values()) {
            BufferedImage buffImage = Sprite.getAssistant(assistant)
                    .getSprite(ASSISTANT_WIDTH, ASSISTANT_HEIGHT);
            ImageIcon imageIcon = new ImageIcon(buffImage);

            JLabel label = new JLabel(assistant.name(), imageIcon, SwingConstants.CENTER);
            label.setHorizontalTextPosition(SwingConstants.CENTER);
            label.setVerticalTextPosition(SwingConstants.BOTTOM);
            selectAssistantLabel(label, false);
            label.addMouseListener(this);

            assistants.add(label);
        }
    }

    private JButton createSubmitButton(boolean enabled) {
        JButton confirmButton = new JButton("Gioca assistente");
        confirmButton.addActionListener(this);
        confirmButton.setEnabled(enabled);
        return confirmButton;
    }

    private void disableUnavailableAssistants(List<Assistant> availableAssistants) {
        for (JLabel label : assistants) {
            Assistant assistant = Assistant.valueOf(label.getText());
            boolean enabled = availableAssistants.contains(assistant);
            label.setEnabled(enabled);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (previouslySelectedLabel != null) {
            Assistant assistant = Assistant.valueOf(previouslySelectedLabel.getText());
            listener.receiveAssistant(assistant);
            dispose();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        JLabel label = (JLabel) e.getComponent();
        if (label.isEnabled()) {
            if (previouslySelectedLabel != null)
                selectAssistantLabel(previouslySelectedLabel, false);
            selectAssistantLabel(label, true);
            previouslySelectedLabel = label;
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // unused
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // unused
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // unused
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // unused
    }
}
