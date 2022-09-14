package it.polimi.ingsw.clients;

import it.polimi.ingsw.models.components.Island;
import it.polimi.ingsw.models.components.Student;
import it.polimi.ingsw.models.components.Tower;
import it.polimi.ingsw.models.components.Wizard;
import it.polimi.ingsw.models.components.Assistant;
import it.polimi.ingsw.models.components.characters.CharacterType;
import it.polimi.ingsw.models.state.GameState;
import it.polimi.ingsw.models.state.Stage;
import it.polimi.ingsw.network.JsonCommand;
import it.polimi.ingsw.network.SocketStreamUtils;
import it.polimi.ingsw.network.messages.Command;
import it.polimi.ingsw.network.messages.Parameters;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Implements the common functionalities of the cli/gui controllers.
 */
public abstract class ClientController {

    private static final long TIME_BETWEEN_BEATS = 1000;
    private static final long TIME_BETWEEN_SERVER_DOWN_CHECKS = 2000;
    private static final long SERVER_DOWN_MILLIS_THRESHOLD = 3000;

    private final ScheduledThreadPoolExecutor executor;

    private String nickname;
    private JsonCommand lastMoveFromServer;
    private GameState gameState;
    private BufferedReader inputStream;
    private PrintWriter outputStream;
    private long lastBeatFromServerTimestamp;

    private boolean clientRunning;

    protected ClientController() {
        setClientRunning(true);
        executor = new ScheduledThreadPoolExecutor(2);
    }

    public boolean connectToServer(String ip, int port) {
        try {
            Socket clientSocket = new Socket(ip, port);
            inputStream = SocketStreamUtils.getInputStream(clientSocket);
            if (inputStream == null)
                return false;
            outputStream = SocketStreamUtils.getOutputStream(clientSocket);
            return outputStream != null;
        } catch (IOException | IllegalArgumentException e) {
            return false;
        }
    }

    public boolean isClientRunning() {
        return clientRunning;
    }

    public void setClientRunning(boolean clientRunning) {
        this.clientRunning = clientRunning;
    }

    // SEND MESSAGES TO SERVER

    public void sendNicknameToServer() {
        JsonCommand jsonCommand = (new JsonCommand(Command.LOGIN)).addParameter(Parameters.NICKNAME, nickname, true);
        outputStream.println(jsonCommand);
    }

    public void sendMatchTypeToServer(Integer numPlayers, boolean expertMatch) {
        JsonCommand jsonCommand = (new JsonCommand(Command.JOIN_MATCH))
                .addParameter(Parameters.NUM_PLAYERS, numPlayers.toString(), true)
                .addParameter(Parameters.EXPERT_MATCH, "" + expertMatch, true);
        outputStream.println(jsonCommand);
    }

    public void sendWizardAndTowerToServer(String nickname, Wizard wizard, Tower tower) {
        JsonCommand jsonCommand = (new JsonCommand(Command.PLAYER_MOVE_ADD_PLAYER))
                .addParameter(Parameters.NICKNAME, nickname, true)
                .addParameter(Parameters.WIZARD, wizard.toString(), true)
                .addParameter(Parameters.TOWER, tower.toString(), true);
        outputStream.println(jsonCommand);
    }

    public void sendAssistantToServer(Assistant assistant) {
        JsonCommand jsonCommand = new JsonCommand(Command.PLAYER_MOVE_PLAY_ASSISTANT)
                .addParameter(Parameters.ASSISTANT, assistant.name(), true);
        outputStream.println(jsonCommand);
    }

    public void sendStudentToMoveFromEntranceToHallToServer(Student student) {
        JsonCommand jsonCommand = new JsonCommand(Command.PLAYER_MOVE_MOVE_STUDENT_FROM_ENTRANCE_TO_HALL)
                .addParameter(Parameters.STUDENT_COLOR, "" + student, true);
        outputStream.println(jsonCommand);
    }

    public void sendStudentToMoveFromEntranceToIslandToServer(Student student, int islandId) {
        JsonCommand jsonCommand = new JsonCommand(Command.PLAYER_MOVE_MOVE_STUDENT_FROM_ENTRANCE_TO_ISLAND)
                .addParameter(Parameters.STUDENT_COLOR, "" + student, true)
                .addParameter(Parameters.ISLAND_ID, "" + islandId, true);
        outputStream.println(jsonCommand);
    }

    public void sendMotherNatureStepsToServer(int steps) {
        JsonCommand jsonCommand = new JsonCommand(Command.PLAYER_MOVE_MOVE_MOTHER_NATURE)
                .addParameter(Parameters.STEPS, "" + steps, true);
        outputStream.println(jsonCommand);
    }

    public void sendCloudToServer(int cloudId) {
        JsonCommand jsonCommand = new JsonCommand(Command.PLAYER_MOVE_PICK_STUDENTS_FROM_CLOUD)
                .addParameter(Parameters.CLOUD_ID, "" + cloudId, true);
        outputStream.println(jsonCommand);
    }

    public void sendCharacterToServer(CharacterType characterType, Student student, Island island, List<Student> toExchangeFrom, List<Student> toExchangeTo) {
        JsonCommand jsonCommand = new JsonCommand(Command.PLAYER_MOVE_PLAY_CHARACTER)
                .addParameter(Parameters.CHARACTER_TYPE, "" + characterType, true);

        if (student != null) {
            jsonCommand.addParameter(Parameters.STUDENT_COLOR, student.toString(), true);
        }
        if (island != null) {
            jsonCommand.addParameter(Parameters.ISLAND_ID, "" + island.getPosition(), true);
        }
        if (toExchangeFrom != null) {
            jsonCommand.addParameter(Parameters.TO_EXCHANGE_FROM_NUMBER, "" + toExchangeFrom.size(), true);
            for (int i = 0; i < toExchangeFrom.size(); i++) {
                jsonCommand.addParameter(Parameters.TO_EXCHANGE_FROM + i, toExchangeFrom.get(i).toString(), true);
            }
        }
        if (toExchangeTo != null) {
            jsonCommand.addParameter(Parameters.TO_EXCHANGE_TO_NUMBER, "" + toExchangeTo.size(), true);
            for (int i = 0; i < toExchangeTo.size(); i++) {
                jsonCommand.addParameter(Parameters.TO_EXCHANGE_TO + i, toExchangeTo.get(i).toString(), true);
            }
        }

        outputStream.println(jsonCommand);
    }

    public void sendEndTurnToServer() {
        JsonCommand jsonCommand = new JsonCommand(Command.PLAYER_MOVE_END_TURN);
        outputStream.println(jsonCommand);
    }

    public void sendLogoutMessageToServer() {
        stopPeriodicTasks();
        JsonCommand jsonCommand = new JsonCommand(Command.LOGOUT);
        outputStream.println(jsonCommand);
    }

    /**
     * Launches a thread that sends beats to the server periodically
     * When the server receives a beat from the client it means the client is still alive
     * If the server doesn't receive a beat from the client it considers the client down
     */
    public void startSendingBeatsToServer() {
        executor.scheduleAtFixedRate(sendBeatToServer, 0, TIME_BETWEEN_BEATS, TimeUnit.MILLISECONDS);
    }

    private final Runnable sendBeatToServer = () -> {
        JsonCommand beat = new JsonCommand(Command.BEAT);
        if (outputStream != null) {
            outputStream.println(beat);
        }
    };

    // UTILS, GETTERS AND SETTERS

    /**
     * The client periodically checks if it has received a beat from the server
     * If, when the client checks, the last beat from the server was received too much time ago,
     * the client considers the server down and disconnects
     */
    public void startCheckingIfServerIsUp() {
        executor.scheduleAtFixedRate(disconnectWhenServerIsUnreachable, 0, TIME_BETWEEN_SERVER_DOWN_CHECKS, TimeUnit.MILLISECONDS);
    }

    private final Runnable disconnectWhenServerIsUnreachable = () -> {
        long elapsedMillis = System.currentTimeMillis() - lastBeatFromServerTimestamp;
        if (elapsedMillis > SERVER_DOWN_MILLIS_THRESHOLD) {
            showServerUnreachableMessage();
            System.exit(0);
        }
    };

    public abstract void showServerUnreachableMessage();

    public void stopPeriodicTasks() {
        executor.shutdown();
    }

    /** Set the time of the last beat received from server */
    public void updateServerBeatTimestamp() {
        lastBeatFromServerTimestamp = System.currentTimeMillis();
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setLastMoveFromServer(JsonCommand lastMoveFromServer) {
        this.lastMoveFromServer = lastMoveFromServer;
    }

    public JsonCommand getLastMoveFromServer() {
        return lastMoveFromServer;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getNickname() {
        return nickname;
    }

    public BufferedReader getInputStream() {
        return inputStream;
    }

    public boolean isGameOver() {
        return gameState.getStage().equals(Stage.GAME_OVER);
    }

    public boolean isMyTurn() {
        return gameState.getCurrentPlayer().getName().equals(nickname);
    }
}
