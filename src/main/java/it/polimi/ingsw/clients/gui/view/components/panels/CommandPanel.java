package it.polimi.ingsw.clients.gui.view.components.panels;

import it.polimi.ingsw.clients.gui.view.assets.Sprite;
import it.polimi.ingsw.clients.gui.view.components.frames.game_frame.ChooseAssistantFrame;
import it.polimi.ingsw.clients.gui.view.components.frames.game_frame.GameFrameListener;
import it.polimi.ingsw.clients.gui.view.utils.GuiUtils;
import it.polimi.ingsw.models.components.*;
import it.polimi.ingsw.models.state.GameState;
import it.polimi.ingsw.models.state.Stage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.*;

public class CommandPanel extends JPanel {
    private final JButton assistantsButton;
    private final JButton leaveMatchButton;
    private final JButton endTurnButton;
    private final Map<String, PlayerBoardInfoPanel> indicators;
    private final JLabel currentAssistantCard;
    private final JLabel yourWizard;
    private final JLabel matchStatusInfo;

    private transient GameState gameState;

    private final int myId;
    private boolean isActiveMode;

    private final transient GameFrameListener listener;

    public CommandPanel(GameState gs, int id, GameFrameListener listener) {
        this.listener = listener;

        myId = id;
        gameState = gs;

        indicators = new HashMap<>();
        assistantsButton = new JButton("Gioca carta assistente");
        endTurnButton = new JButton("Termina turno");
        leaveMatchButton = new JButton("Abbandona");

        currentAssistantCard = new JLabel();
        Wizard wizard = gameState.getPlayerById(myId).getWizard();

        BufferedImage buffImage = Sprite.getWizard(wizard)
                .getSprite(100, 150);
        yourWizard = new JLabel(new ImageIcon(buffImage));
        matchStatusInfo = new JLabel(GuiUtils.infoMessageFromStage(gameState.getStage()), SwingConstants.CENTER);
        matchStatusInfo.setFont(matchStatusInfo.getFont().deriveFont(20f));

        for (Player p : gameState.getPlayers()) {
            addIndicatorPanel(p.getName(), p.getBoard().getTowerType());
        }

        highlightMe();
        highlightMatchStatusInfoIfIsMyTurn();

        assistantsButton.setPreferredSize(new Dimension(300, 40));
        assistantsButton.addActionListener(new SpawnAssistantsListener());

        endTurnButton.setEnabled(false);
        endTurnButton.addActionListener(new EndTurnListener(listener));

        leaveMatchButton.addActionListener(new LeaveMatchListener(listener));

        toggleButtonsStatus();
        highlightPlayer(gameState.getCurrentPlayer().getName());

        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridy = 0;
        add(matchStatusInfo, gbc);

        int i = 0;
        for (Map.Entry<String, PlayerBoardInfoPanel> t : indicators.entrySet()) {
            gbc.gridy = ++i;
            add(t.getValue(), gbc);
        }

        gbc.gridy = indicators.size() + 1;
        add(currentAssistantCard, gbc);

        gbc.gridy++;
        add(yourWizard, gbc);

        gbc.gridy++;

        gbc.gridy++;
        add(assistantsButton, gbc);

        gbc.gridy++;
        add(endTurnButton, gbc);

        gbc.gridy++;
        add(leaveMatchButton, gbc);
    }

    /**
     * called by the controller to trigger a content refresh
     *
     * @param gameState state of the game
     */
    public void updateView(GameState gameState) {
        setGameState(gameState);
        updateRoutine();
    }

    private void updateRoutine() {
        resetIndicators();
        setLastMoveInfo();
        setCurrentlyHighlightedPlayer();
        toggleButtonsStatus();
        setLastPlayedAssistants();
        setBoardStatus();
        setOwnedProfessors();
        highlightMatchStatusInfoIfIsMyTurn();

        if (gameState.isExpertMatch())
            setPlayersCoins();
    }

    /**
     * toggles buttons based on the current turn
     */
    private void toggleButtonsStatus() {
        if (gameState.getCurrentPlayer().getId() != myId) {
            setButtonsInViewOnlyMode();
            if (endTurnButton.isEnabled())
                endTurnButton.setEnabled(false);
        } else {
            if (gameState.getStage().equals(Stage.PLANNING_PLAY_ASSISTANTS))
                setButtonsInActiveMode();
            else if (gameState.getStage().equals(Stage.ACTION_END_TURN))
                endTurnButton.setEnabled(true);
            else
                setButtonsInViewOnlyMode();
        }
    }

    private void setButtonsInViewOnlyMode() {
        assistantsButton.setText("Visualizza elenco assistenti");
        isActiveMode = false;
    }

    private void setPlayersCoins() {
        for (Player p : gameState.getPlayers()) {
            String playerName = p.getName();
            indicators.get(playerName).setPlayerCoins(p.getNumCoins());
        }
    }

    private void setButtonsInActiveMode() {
        assistantsButton.setText("Gioca carta assistente");
        isActiveMode = true;
    }

    private void resetIndicators() {
        for (Map.Entry<String, PlayerBoardInfoPanel> t : indicators.entrySet())
            t.getValue().highlight(false);
    }

    private void setLastPlayedAssistants() {
        for (Player p : gameState.getPlayers()) {
            Assistant lastPlayedAssistant = gameState.getPlayerById(p.getId()).getLastPlayedAssistant();
            if (lastPlayedAssistant != null) {
                indicators.get(p.getName()).setLastPlayedAssistantName(lastPlayedAssistant);
            }
        }
    }

    private void setBoardStatus() {
        for (Player p : gameState.getPlayers()) {
            Map<Student, Integer> howManyHall = new EnumMap<>(Student.class);
            Map<Student, Integer> howManyEntrance = new EnumMap<>(Student.class);
            for (Student color : Student.values()) {
                int amountHall = gameState.getPlayerById(p.getId()).getBoard().getHall().getNumStudentsByColor(color);
                int amountEntrance = gameState.getPlayerById(p.getId()).getBoard().getEntrance().getNumStudentsByColor(color);
                howManyHall.put(color, amountHall);
                howManyEntrance.put(color, amountEntrance);
            }
            indicators.get(p.getName()).setHallStatus(howManyHall);
            indicators.get(p.getName()).setEntranceStatus(howManyEntrance);
        }
    }

    private void setOwnedProfessors() {
        for (Player p : gameState.getPlayers()) {
            List<Student> ownerships = gameState.getPlayerProfessors(p);
            indicators.get(p.getName()).setOwnedProfessors(ownerships);
        }
    }

    private void setLastMoveInfo() {
        String mex = GuiUtils.infoMessageFromStage(gameState.getStage());
        if (gameState.getStage().equals(Stage.ACTION_MOVE_STUDENTS))
            mex += " (ancora " + gameState.getStudentsToMove() + ")";
        matchStatusInfo.setText(mex);
    }

    private void setCurrentlyHighlightedPlayer() {
        highlightPlayer(gameState.getCurrentPlayer().getName());
    }

    private void highlightMe() {
        indicators.get(gameState.getPlayerById(myId).getName()).highlightPlayerName();
    }

    private void highlightMatchStatusInfoIfIsMyTurn() {
        if (gameState.getCurrentPlayer().getId() == myId) {
            matchStatusInfo.setForeground(Color.RED);
        } else {
            matchStatusInfo.setForeground(Color.GRAY);
        }
    }

    private void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    private void addIndicatorPanel(String username, Tower color) {
        indicators.put(username, new PlayerBoardInfoPanel(username, color, gameState.isExpertMatch()));
    }

    private void highlightPlayer(String nickname) {
        indicators.get(nickname).highlight(true);
    }

    private class SpawnAssistantsListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // remaining assistants
            List<Assistant> availableAssistants = gameState.getPlayerById(myId).getPlayableAssistants();

            ChooseAssistantFrame chooseAssistantFrame = new ChooseAssistantFrame(availableAssistants, isActiveMode, listener);
            chooseAssistantFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            chooseAssistantFrame.pack();
            chooseAssistantFrame.setLocationRelativeTo(null);
            chooseAssistantFrame.setAlwaysOnTop(true);
            chooseAssistantFrame.setResizable(false);
            chooseAssistantFrame.setVisible(true);
        }
    }

    private record EndTurnListener(GameFrameListener listener) implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            listener.endTurn();
        }
    }

    private record LeaveMatchListener(GameFrameListener listener) implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int choice = GuiUtils.confirm("Sei sicuro di voler uscire?");
            if (choice == JOptionPane.YES_OPTION)
                listener.leaveMatch();
        }
    }
}
