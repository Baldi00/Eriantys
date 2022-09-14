package it.polimi.ingsw.clients.gui.view.components.panels;

import it.polimi.ingsw.clients.gui.view.assets.Sprite;
import it.polimi.ingsw.clients.gui.view.components.frames.setup_frame.SetupFrameListener;
import it.polimi.ingsw.models.components.Tower;
import it.polimi.ingsw.models.components.Wizard;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

public class TowerWizardSetupPanel extends JPanel implements ActionListener {
    private JRadioButton blackTowerRadio1;
    private JRadioButton blackTowerRadio2;
    private JRadioButton whiteTowerRadio1;
    private JRadioButton whiteTowerRadio2;
    private JRadioButton greyTowerRadio;

    private JLabel blackTowerLabel1;
    private JLabel blackTowerLabel2;
    private JLabel whiteTowerLabel1;
    private JLabel whiteTowerLabel2;
    private JLabel greyTowerLabel;

    private JRadioButton kingRadio;
    private JRadioButton witchRadio;
    private JRadioButton sageRadio;
    private JRadioButton druidRadio;

    private JLabel kingLabel;
    private JLabel witchLabel;
    private JLabel sageLabel;
    private JLabel druidLabel;

    private JButton proceedButton;

    private Tower selectedTower;
    private Wizard selectedWizard;

    private final transient SetupFrameListener listener;

    public TowerWizardSetupPanel(SetupFrameListener listener) {
        this.listener = listener;

        setupProceedButton();
        setupTowers();
        setupWizards();

        setupThisPanel();
    }

    private void setupProceedButton() {
        proceedButton = new JButton("Inizia partita");
        proceedButton.addActionListener(this);
    }

    private void setupTowers() {
        int size = 100;
        BufferedImage blackTower = Sprite.BLACK_TOWER.getSprite(size, size);
        BufferedImage whiteTower = Sprite.WHITE_TOWER.getSprite(size, size);
        BufferedImage greyTower = Sprite.GREY_TOWER.getSprite(size, size);

        blackTowerLabel1 = new JLabel(new ImageIcon(blackTower));
        blackTowerLabel2 = new JLabel(new ImageIcon(blackTower));
        whiteTowerLabel1 = new JLabel(new ImageIcon(whiteTower));
        whiteTowerLabel2 = new JLabel(new ImageIcon(whiteTower));
        greyTowerLabel = new JLabel(new ImageIcon(greyTower));
        disableTowersLabels();

        blackTowerRadio1 = createTowerRadioButton("BLACK");
        blackTowerRadio2 = createTowerRadioButton("BLACK");
        whiteTowerRadio1 = createTowerRadioButton("WHITE");
        whiteTowerRadio2 = createTowerRadioButton("WHITE");
        greyTowerRadio = createTowerRadioButton("GREY");
        disableTowersRadioButtons();

        ButtonGroup towerGroup = new ButtonGroup();
        towerGroup.add(blackTowerRadio1);
        towerGroup.add(blackTowerRadio2);
        towerGroup.add(whiteTowerRadio1);
        towerGroup.add(whiteTowerRadio2);
        towerGroup.add(greyTowerRadio);
    }

    private JRadioButton createTowerRadioButton(String name) {
        JRadioButton radioButton = new JRadioButton(name);
        radioButton.addActionListener(new TowerSelectionListener());
        radioButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        radioButton.setHorizontalTextPosition(SwingConstants.CENTER);
        radioButton.setHorizontalAlignment(SwingConstants.CENTER);
        return radioButton;
    }

    private void setupWizards() {
        BufferedImage king = Sprite.WIZARD_KING.getSprite(120, 182);
        BufferedImage witch = Sprite.WIZARD_WITCH.getSprite(120, 182);
        BufferedImage sage = Sprite.WIZARD_SAGE.getSprite(120, 182);
        BufferedImage druid = Sprite.WIZARD_DRUID.getSprite(120, 182);

        kingLabel = new JLabel(new ImageIcon(king));
        witchLabel = new JLabel(new ImageIcon(witch));
        sageLabel = new JLabel(new ImageIcon(sage));
        druidLabel = new JLabel(new ImageIcon(druid));
        disableWizardsLabels();

        kingRadio = createWizardRadioButton("MARIO");
        witchRadio = createWizardRadioButton("LUIGI");
        sageRadio = createWizardRadioButton("YOSHI");
        druidRadio = createWizardRadioButton("PEACH");
        kingRadio.setName("KING");
        witchRadio.setName("WITCH");
        sageRadio.setName("SAGE");
        druidRadio.setName("DRUID");
        disableWizardsRadioButtons();

        ButtonGroup wizardGroup = new ButtonGroup();
        wizardGroup.add(kingRadio);
        wizardGroup.add(witchRadio);
        wizardGroup.add(sageRadio);
        wizardGroup.add(druidRadio);
    }

    private JRadioButton createWizardRadioButton(String name) {
        JRadioButton radioButton = new JRadioButton(name);
        radioButton.addActionListener(new WizardSelectionListener());
        radioButton.setHorizontalTextPosition(SwingConstants.CENTER);
        radioButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        radioButton.setHorizontalAlignment(SwingConstants.CENTER);
        return radioButton;
    }

    private void setupThisPanel() {
        setLayout(new GridBagLayout());
        addTowerPanel();
        addWizardPanel();
        addProceedButton();
    }

    private void addTowerPanel() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(getTowerPanel(), gbc);
    }

    private void addWizardPanel() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(getWizardPanel(), gbc);
    }

    private void addProceedButton() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(proceedButton, gbc);
    }

    private JPanel getWizardPanel() {
        JPanel wizardPanel = new JPanel(new GridLayout(1, 0));

        JPanel kingSubPanel = getWizardSubPanel(kingLabel, kingRadio);
        JPanel witchSubPanel = getWizardSubPanel(witchLabel, witchRadio);
        JPanel sageSubPanel = getWizardSubPanel(sageLabel, sageRadio);
        JPanel druidSubPanel = getWizardSubPanel(druidLabel, druidRadio);

        wizardPanel.add(kingSubPanel);
        wizardPanel.add(witchSubPanel);
        wizardPanel.add(sageSubPanel);
        wizardPanel.add(druidSubPanel);

        return wizardPanel;
    }

    private JPanel getWizardSubPanel(JLabel label, JRadioButton radioButton) {
        JPanel subPanel = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        gbc.gridy = 0;
        subPanel.add(label, gbc);

        gbc.gridy = 1;
        subPanel.add(radioButton, gbc);

        return subPanel;
    }

    private JPanel getTowerPanel() {
        JPanel towerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        JPanel blackTowerSubPanel1 = getTowerSubPanel(blackTowerLabel1, blackTowerRadio1);
        JPanel blackTowerSubPanel2 = getTowerSubPanel(blackTowerLabel2, blackTowerRadio2);
        JPanel whiteTowerSubPanel1 = getTowerSubPanel(whiteTowerLabel1, whiteTowerRadio1);
        JPanel whiteTowerSubPanel2 = getTowerSubPanel(whiteTowerLabel2, whiteTowerRadio2);
        JPanel greyTowerSubPanel = getTowerSubPanel(greyTowerLabel, greyTowerRadio);

        towerPanel.add(blackTowerSubPanel1);
        towerPanel.add(blackTowerSubPanel2);
        towerPanel.add(whiteTowerSubPanel1);
        towerPanel.add(whiteTowerSubPanel2);
        towerPanel.add(greyTowerSubPanel);

        return towerPanel;
    }

    private JPanel getTowerSubPanel(JLabel label, JRadioButton radioButton) {
        JPanel subPanel = new JPanel(new GridLayout(2, 1));
        subPanel.add(label);
        subPanel.add(radioButton);
        return subPanel;
    }

    public void setWizardToPaint(Wizard toPaint) {
        switch (toPaint) {
            case KING -> {
                kingRadio.setEnabled(true);
                kingLabel.setEnabled(true);
            }
            case WITCH -> {
                witchRadio.setEnabled(true);
                witchLabel.setEnabled(true);
            }
            case SAGE -> {
                sageRadio.setEnabled(true);
                sageLabel.setEnabled(true);
            }
            case DRUID -> {
                druidRadio.setEnabled(true);
                druidLabel.setEnabled(true);
            }
        }
    }

    public void setTowerToPaint(Tower toPaint) {
        switch (toPaint) {
            case BLACK -> {
                if (!blackTowerLabel1.isEnabled()) {
                    blackTowerRadio1.setEnabled(true);
                    blackTowerLabel1.setEnabled(true);
                } else {
                    blackTowerRadio2.setEnabled(true);
                    blackTowerLabel2.setEnabled(true);
                }
            }
            case WHITE -> {
                if (!whiteTowerLabel1.isEnabled()) {
                    whiteTowerRadio1.setEnabled(true);
                    whiteTowerLabel1.setEnabled(true);
                } else {
                    whiteTowerRadio2.setEnabled(true);
                    whiteTowerLabel2.setEnabled(true);
                }
            }
            case GREY -> {
                greyTowerRadio.setEnabled(true);
                greyTowerLabel.setEnabled(true);
            }
        }
    }

    private void disableComponents() {
        proceedButton.setEnabled(false);

        disableTowersLabels();
        disableTowersRadioButtons();
        disableWizardsLabels();
        disableWizardsRadioButtons();
    }

    private void disableTowersLabels() {
        blackTowerLabel1.setEnabled(false);
        blackTowerLabel2.setEnabled(false);
        whiteTowerLabel1.setEnabled(false);
        whiteTowerLabel2.setEnabled(false);
        greyTowerLabel.setEnabled(false);
    }

    private void disableTowersRadioButtons() {
        blackTowerRadio1.setEnabled(false);
        blackTowerRadio2.setEnabled(false);
        whiteTowerRadio1.setEnabled(false);
        whiteTowerRadio2.setEnabled(false);
        greyTowerRadio.setEnabled(false);
    }

    private void disableWizardsLabels() {
        kingLabel.setEnabled(false);
        witchLabel.setEnabled(false);
        sageLabel.setEnabled(false);
        druidLabel.setEnabled(false);
    }

    private void disableWizardsRadioButtons() {
        kingRadio.setEnabled(false);
        witchRadio.setEnabled(false);
        sageRadio.setEnabled(false);
        druidRadio.setEnabled(false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (selectedWizard != null && selectedTower != null) {
            listener.receiveWizardAndTower(selectedWizard, selectedTower);
            proceedButton.setText("Attendi gli altri giocatori");
            disableComponents();
        }
    }

    private class WizardSelectionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            selectedWizard = Wizard.valueOf(((JRadioButton)e.getSource()).getName());
        }
    }

    private class TowerSelectionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            selectedTower = Tower.valueOf(e.getActionCommand());
        }
    }
}
