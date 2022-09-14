package it.polimi.ingsw.clients.gui.view.components.frames.setup_frame;

import it.polimi.ingsw.clients.gui.GuiController;
import it.polimi.ingsw.clients.gui.view.components.panels.*;
import it.polimi.ingsw.models.components.Tower;
import it.polimi.ingsw.models.components.Wizard;

import javax.swing.*;
import java.awt.*;

public class SetupFrame extends JFrame {
    private static final String CONNECTION_CARD = "conn";
    private static final String LOGIN_CARD = "login";
    private static final String MATCH_SETUP_CARD = "setup";
    private static final String TW_SETUP_CARD = "twsetup";
    private static final String WAIT_SETUP_CARD = "wait";

    private final JPanel cardContainer;
    private final ConnectionPanel connectionPanel;
    private final LoginPanel loginPanel;
    private final MatchSetupPanel matchSetupPanel;
    private final WaitingPanel waitingPanel;
    private final TowerWizardSetupPanel twSetupPanel;
    private final CardLayout layout;

    public SetupFrame(GuiController controller) throws HeadlessException {
        super("Eriantys");

        cardContainer = new JPanel();

        connectionPanel = new ConnectionPanel(controller);
        loginPanel = new LoginPanel(controller);
        matchSetupPanel = new MatchSetupPanel(controller);
        waitingPanel = new WaitingPanel();
        twSetupPanel = new TowerWizardSetupPanel(controller);

        layout = new CardLayout();
        cardContainer.setLayout(layout);

        // TODO: you can use SetupFrameState instead of custom strings
        // TODO: why panels are both first & second parameters
        cardContainer.add(connectionPanel, CONNECTION_CARD);
        cardContainer.add(loginPanel, LOGIN_CARD);
        cardContainer.add(MATCH_SETUP_CARD, matchSetupPanel);
        cardContainer.add(WAIT_SETUP_CARD, waitingPanel);
        cardContainer.add(TW_SETUP_CARD, twSetupPanel);

        getContentPane().add(cardContainer);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);
    }

    public void setSetupState(SetupFrameState state) {
        switch (state) {
            case CONNECTION -> layout.show(cardContainer, CONNECTION_CARD);
            case LOGIN -> layout.show(cardContainer, LOGIN_CARD);
            case MATCH_SETUP -> layout.show(cardContainer, MATCH_SETUP_CARD);
            case WAITING -> layout.show(cardContainer, WAIT_SETUP_CARD);
            case TOWER_WIZARD_CHOICE -> layout.show(cardContainer, TW_SETUP_CARD);
        }
    }

    public void enableWizard(Wizard wiz) {
        twSetupPanel.setWizardToPaint(wiz);
    }

    public void enableTower(Tower tower) {
        twSetupPanel.setTowerToPaint(tower);
    }

}
