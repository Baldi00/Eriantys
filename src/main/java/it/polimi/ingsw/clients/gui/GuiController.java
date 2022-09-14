package it.polimi.ingsw.clients.gui;

import it.polimi.ingsw.clients.ClientController;
import it.polimi.ingsw.clients.gui.view.components.frames.game_frame.GameFrame;
import it.polimi.ingsw.clients.gui.view.components.frames.game_frame.GameFrameListener;
import it.polimi.ingsw.clients.gui.view.components.frames.setup_frame.SetupFrame;
import it.polimi.ingsw.clients.gui.view.components.frames.setup_frame.SetupFrameListener;
import it.polimi.ingsw.clients.gui.view.utils.GuiUtils;
import it.polimi.ingsw.clients.gui.view.components.frames.setup_frame.SetupFrameState;
import it.polimi.ingsw.models.components.Assistant;
import it.polimi.ingsw.models.components.Tower;
import it.polimi.ingsw.models.components.Wizard;
import it.polimi.ingsw.models.state.GameState;
import it.polimi.ingsw.network.GsonManager;
import it.polimi.ingsw.network.JsonCommand;
import it.polimi.ingsw.network.messages.Command;
import it.polimi.ingsw.network.messages.Parameters;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public class GuiController extends ClientController
        implements SetupFrameListener, GameFrameListener {

    private final SetupFrame setupFrame;
    private GameFrame gameFrame;

    private final Logger logger = Logger.getGlobal();

    public GuiController() {
        super();
        enableOnlyRamCache();
        useNimbusLookAndFeel();
        setupFrame = new SetupFrame(this);
    }

    private void useNimbusLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | UnsupportedLookAndFeelException | IllegalAccessException |
                 InstantiationException e) {
            logger.warning("ERROR: Cannot set Nimbus look & feel");
        }
    }

    // improves assets loading times
    private void enableOnlyRamCache() {
        ImageIO.setUseCache(false);
    }

    @Override
    public void receiveIpAndPort(String ip, int port) {
        boolean connected = connectToServer(ip, port);
        if (!connected) {
            GuiUtils.alert("server unreachable");
        } else {
            startSendingBeatsToServer();
            updateServerBeatTimestamp();
            startCheckingIfServerIsUp();

            new Thread(receiveServerMessages).start();
        }
    }

    @Override
    public void receiveNickname(String nickname) {
        setNickname(nickname);
        sendNicknameToServer();
    }

    @Override
    public void receiveMatchMode(int numPlayers, boolean expertMode) {
        sendMatchTypeToServer(numPlayers, expertMode);
        setupFrame.setSetupState(SetupFrameState.TOWER_WIZARD_CHOICE);
    }

    @Override
    public void receiveWizardAndTower(Wizard wizard, Tower tower) {
        sendWizardAndTowerToServer(getNickname(), wizard, tower);
    }

    private final Runnable receiveServerMessages = () -> {
        BufferedReader stream = getInputStream();
        while (isClientRunning()) {
            try {
                // TODO: i think this read operation can be handled directly on ClientController.
                //       This class should only implement a method to handle the received message.
                String message;
                if ((message = stream.readLine()) != null) {
                    SwingUtilities.invokeLater(
                            () -> dispatch(message)
                    );
                } else {
                    setClientRunning(false);
                }
            } catch (IOException e) {
                setClientRunning(false);
            }
        }
    };

    // TODO: would be interesting to register lambdas just for the commands we want on
    //       the controller so we don't have this giant switch statement.
    private void dispatch(String message) {
        JsonCommand jsonMessage = JsonCommand.fromJson(message);
        Command command = jsonMessage.getCommand();
        switch (command) {
            case NICKNAME_ALREADY_PRESENT -> GuiUtils.alert("Nickname già usato. Scegli un altro nickname.");
            case ENTER_NICKNAME -> setupFrame.setSetupState(SetupFrameState.LOGIN);
            case LOGIN_SUCCESSFUL -> setupFrame.setSetupState(SetupFrameState.MATCH_SETUP);
            case JOIN_SUCCESSFUL -> setupFrame.setSetupState(SetupFrameState.WAITING);
            case CHOOSE_WIZARD_TOWER -> {
                String playerNameWhoMustChoose = jsonMessage.getParameter(Parameters.NICKNAME);
                GameState gameState = GsonManager.getInstance().fromJson(
                        jsonMessage.getParameter(Parameters.GAME_STATE), GameState.class);
                chooseWizardAndTower(playerNameWhoMustChoose, gameState);
            }
            case MOVE_DONE -> {
                GameState gameState = GsonManager.getInstance().fromJson(jsonMessage.getParameter(Parameters.GAME_STATE), GameState.class);
                JsonCommand lastMove = JsonCommand.fromJson(jsonMessage.getParameter(Parameters.LAST_MOVE));
                moveDone(gameState, lastMove);
            }
            case BEAT -> updateServerBeatTimestamp();
            case FORCE_END_MATCH -> exit(true);
            default -> System.out.println("IGNORE MESSAGE: " + command);
        }
    }

    private void moveDone(GameState gameState, JsonCommand lastMove) {
        setLastMoveFromServer(lastMove);
        if (!lastMove.getCommand().equals(Command.ILLEGAL_MOVE)) {
            setGameState(gameState);

            if (lastMove.getCommand().equals(Command.INITIALIZATION)) {
                closeSetupFrame();

                int playerId = gameState.getPlayerByName(getNickname()).getId();
                gameFrame = new GameFrame(gameState, playerId, this);
                gameFrame.updateView(gameState);
            } else {
                if (!isGameOver()) {
                    gameFrame.updateView(gameState);
                } else {
                    printGameOverAndWinner();
                    exit(false);
                }
            }
        } else {
            if (isMyTurn())
                GuiUtils.alert("Mossa proibita");
        }
    }

    private void printGameOverAndWinner() {
        Tower winner = getGameState().getWinner();
        if (winner != null) {
            GuiUtils.alert("Ha vinto la torre " + winner + "\n" +
                    "Partita conclusa");
        } else {
            GuiUtils.alert("La partita è terminata in pareggio");
        }
    }

    private void chooseWizardAndTower(String nickname, GameState gameState) {
        if (nickname.equals(getNickname())) {
            setupFrame.setSetupState(SetupFrameState.TOWER_WIZARD_CHOICE);
            setGameState(gameState);
            List<Wizard> availableWizards = getGameState().getAvailableWizards();
            List<Tower> availableTowers = getGameState().getAvailableTowers();

            for (Wizard wizard : availableWizards)
                setupFrame.enableWizard(wizard);

            for (Tower tower : availableTowers)
                setupFrame.enableTower(tower);
        }
    }

    // TODO: would be good to pass to exit() the reason of the exit like
    //       MATCH_ENDED, OPPONENT_DISCONNECTED, SERVER_UNREACHABLE...
    private void exit(boolean opponentDisconnected) {
        // stop before showing alerts to avoid repetitive alerts
        stopPeriodicTasks();

        if (opponentDisconnected)
            GuiUtils.alert("Un giocatore si è disconnesso, la partita verrà chiusa");

        closeSetupFrame();
        closeGameFrame();
        System.exit(0);
    }

    private void closeSetupFrame() {
        if (setupFrame != null)
            setupFrame.dispose();
    }

    private void closeGameFrame() {
        if (gameFrame != null)
            gameFrame.dispose();
    }

    @Override
    public void showServerUnreachableMessage() {
        GuiUtils.alert("Server non raggiungibile");
    }

    @Override
    public void receiveAssistant(Assistant assistant) {
        sendAssistantToServer(assistant);
    }

    @Override
    public void leaveMatch() {
        sendLogoutMessageToServer();
        exit(false);
    }

    @Override
    public void endTurn() {
        sendEndTurnToServer();
    }
}
