package it.polimi.ingsw.server;

import it.polimi.ingsw.models.GameManager;
import it.polimi.ingsw.models.components.*;
import it.polimi.ingsw.models.components.characters.Characters;
import it.polimi.ingsw.models.components.characters.effects.EffectArgs;
import it.polimi.ingsw.models.exceptions.IllegalMoveException;
import it.polimi.ingsw.models.state.GameState;
import it.polimi.ingsw.models.state.Stage;
import it.polimi.ingsw.network.GsonManager;
import it.polimi.ingsw.network.JsonCommand;
import it.polimi.ingsw.network.messages.Command;
import it.polimi.ingsw.network.messages.Parameters;
import it.polimi.ingsw.network.observers.JsonCommandChangeEvent;
import it.polimi.ingsw.network.observers.JsonCommandChangeListener;
import it.polimi.ingsw.server.modules.ClientHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Receives the requests from the clients through the client handlers
 * Requests to perform the changes on the gameState through the game manager
 * Sends the response to all clients through the client handlers
 * It refers to a single active match
 * Every client handler handles the communication with a single client connected to the current match
 */
public class ServerController implements JsonCommandChangeListener {

    private GameManager gameManager;
    private final List<ClientHandler> clientHandlers;
    private final List<String> nicknamesToBeAdded;

    /**
     * @param gameManager the model of the application used to perform changes on the gameState
     * @param nicknames   the list of the nicknames associated to this match
     */
    public ServerController(GameManager gameManager, List<String> nicknames) {
        this.gameManager = gameManager;
        this.clientHandlers = new ArrayList<>();
        nicknamesToBeAdded = new ArrayList<>(nicknames);
    }

    /**
     * Adds a list of client handler to the inner list
     * Client handlers are used to communicate with clients
     */
    public void addClientHandlers(List<ClientHandler> clientHandlers) {
        for (ClientHandler clientHandler : clientHandlers) {
            addClientHandler(clientHandler);
        }
    }

    private void addClientHandler(ClientHandler clientHandler) {
        clientHandlers.add(clientHandler);
        clientHandler.addMessageListener(this);
    }

    /**
     * Fired when a message from one client has been received
     */
    @Override
    public void jsonCommandChange(JsonCommandChangeEvent event) {
        if (event.getEventName().equals("messageReceived")) {
            JsonCommand request = event.getJsonCommand();
            JsonCommand response = handleRequest(request);
            sendResponseToClients(response);
        }
    }

    /**
     * Send the response to a previous request to all clients through the client handlers
     */
    public void sendResponseToClients(JsonCommand response) {
        for (ClientHandler clientHandler : clientHandlers) {
            clientHandler.sendMessageToClient(response.toJson());
        }
    }

    /**
     * Prepares and send the first request for the clients
     * The server says the clients to choose and send (one by one) the wizard and the tower
     */
    public void sendFirstWizardAndTowerRequestToClients() {
        JsonCommand response = prepareWizardAndTowerRequestForNextClient();
        sendResponseToClients(response);
    }

    /**
     * Receives the request from clients and dispatches it to the right function
     *
     * @return the response or the result that the request produced
     */
    JsonCommand handleRequest(JsonCommand request) {
        //Keeps a copy of the old gameState so in case of problems it's possible to rollback
        GameState oldGameState = getClonedGameState();

        JsonCommand response;
        try {
            response = switch (request.getCommand()) {
                case LOGOUT -> createForceEndMatchResponse();
                case PLAYER_MOVE_ADD_PLAYER -> handleAddPlayerRequest(request);
                default -> executeMove(request);
            };
        } catch (RuntimeException e) {
            rollbackGameState(oldGameState);
            JsonCommand lastMove = new JsonCommand(Command.ILLEGAL_MOVE)
                    .addParameter(Parameters.NICKNAME, gameManager.getGameState().getCurrentPlayer().getName(), true);
            response = new JsonCommand(Command.MOVE_DONE)
                    .addParameterSingleQuotes(Parameters.LAST_MOVE, lastMove.toJson());
        }

        return response;
    }

    /**
     * Adds a player to the match
     *
     * @return if all players have been added the initial gameState to send to all clients,
     * otherwise the new request to choose wizard and tower for the next client
     */
    private JsonCommand handleAddPlayerRequest(JsonCommand request) {
        addPlayer(request);
        if (nicknamesToBeAdded.isEmpty()) {
            prepareInitialGameState();
            String initialGameState = serializeGameStateJson();
            JsonCommand lastMove = new JsonCommand(Command.INITIALIZATION);
            return createMoveDoneResponse(initialGameState, lastMove);
        } else {
            return prepareWizardAndTowerRequestForNextClient();
        }
    }

    /**
     * @return the new request to choose wizard and tower for the next client
     */
    private JsonCommand prepareWizardAndTowerRequestForNextClient() {
        JsonCommand response;
        String gameState = serializeGameStateJson();
        response = new JsonCommand(Command.CHOOSE_WIZARD_TOWER)
                .addParameter(Parameters.NICKNAME, nicknamesToBeAdded.get(0), true)
                .addParameterSingleQuotes(Parameters.GAME_STATE, gameState);
        return response;
    }

    /**
     * Requests the game manager to perform the requested move form the client
     *
     * @return the response with the changed game state or with the old game state if something went wrong
     */
    private JsonCommand executeMove(JsonCommand move) {
        JsonCommand response;
        Command command = move.getCommand();
        Player currentPlayer = gameManager.getGameState().getCurrentPlayer();

        switch (command) {
            case PLAYER_MOVE_PLAY_ASSISTANT -> playAssistant(move);
            case PLAYER_MOVE_MOVE_STUDENT_FROM_ENTRANCE_TO_HALL -> moveStudentFromEntranceToHall(move);
            case PLAYER_MOVE_MOVE_STUDENT_FROM_ENTRANCE_TO_ISLAND -> moveStudentFromEntranceToIsland(move);
            case PLAYER_MOVE_MOVE_MOTHER_NATURE -> moveMotherNature(move);
            case PLAYER_MOVE_PICK_STUDENTS_FROM_CLOUD -> pickStudentsFromCloud(move);
            case PLAYER_MOVE_PLAY_CHARACTER -> playCharacter(move);
            case PLAYER_MOVE_END_TURN -> endTurn();
            default -> throw new IllegalMoveException("Client sent invalid command");
        }

        String changedGameState = serializeGameStateJson();
        move.addParameter(Parameters.NICKNAME, currentPlayer.getName(), true);
        response = createMoveDoneResponse(changedGameState, move);
        return response;
    }

    // REQUESTS TO PERFORM MOVES ON GAME STATE THROUGH THE GAME MANAGER

    private void prepareInitialGameState() {
        gameManager.preparation();
        gameManager.fillClouds();
    }

    private void addPlayer(JsonCommand jsonCommand) {
        String nickname = jsonCommand.getParameter(Parameters.NICKNAME);
        String wizardString = jsonCommand.getParameter(Parameters.WIZARD);
        String towerString = jsonCommand.getParameter(Parameters.TOWER);
        Wizard wizard = Wizard.valueOf(wizardString);
        Tower tower = Tower.valueOf(towerString);

        gameManager.addPlayer(nickname, wizard, tower);
        nicknamesToBeAdded.remove(nickname);
    }

    private void playAssistant(JsonCommand jsonCommand) {
        Assistant assistant = Assistant.valueOf(jsonCommand.getParameter(Parameters.ASSISTANT));
        gameManager.playAssistant(assistant);
    }

    private void moveStudentFromEntranceToHall(JsonCommand jsonCommand) {
        String studentColor = jsonCommand.getParameter(Parameters.STUDENT_COLOR);
        gameManager.moveStudentFromEntranceToHall(Student.valueOf(studentColor));
    }

    private void moveStudentFromEntranceToIsland(JsonCommand jsonCommand) {
        String studentColor = jsonCommand.getParameter(Parameters.STUDENT_COLOR);
        int islandId = Integer.parseInt(jsonCommand.getParameter(Parameters.ISLAND_ID));
        gameManager.moveStudentFromEntranceToIsland(Student.valueOf(studentColor), islandId);
    }

    private void moveMotherNature(JsonCommand jsonCommand) {
        int steps = Integer.parseInt(jsonCommand.getParameter(Parameters.STEPS));
        gameManager.moveMotherNature(steps);
    }

    private void pickStudentsFromCloud(JsonCommand jsonCommand) {
        int cloudId = Integer.parseInt(jsonCommand.getParameter(Parameters.CLOUD_ID));
        gameManager.pickStudentsFromCloud(cloudId);
    }

    private void playCharacter(JsonCommand jsonCommand) {
        List<Student> toExchangeFromStudent;
        List<Student> toExchangeToStudent;
        String characterType;
        int islandId;
        String studentColor;
        characterType = jsonCommand.getParameter(Parameters.CHARACTER_TYPE);
        EffectArgs.Builder effectArgsBuilder = new EffectArgs.Builder();
        effectArgsBuilder.setGameState(gameManager.getGameState());
        effectArgsBuilder.setCharacter(
                gameManager.getGameState().getExpertAttrs()
                        .getCharacterByType(Characters.fromString(characterType).getCharacterType()));
        if (jsonCommand.getParameter(Parameters.STUDENT_COLOR) != null) {
            studentColor = jsonCommand.getParameter(Parameters.STUDENT_COLOR);
            effectArgsBuilder.setStudent(Student.valueOf(studentColor));
        }
        if (jsonCommand.getParameter(Parameters.ISLAND_ID) != null) {
            islandId = Integer.parseInt(jsonCommand.getParameter(Parameters.ISLAND_ID));
            effectArgsBuilder.setIsland(gameManager.getGameState().getIslandByPosition(islandId));
        }
        if (jsonCommand.getParameter(Parameters.TO_EXCHANGE_FROM_NUMBER) != null) {
            int toExchangeFromNumber = Integer.parseInt(jsonCommand.getParameter(Parameters.TO_EXCHANGE_FROM_NUMBER));
            toExchangeFromStudent = new ArrayList<>();
            for (int i = 0; i < toExchangeFromNumber; i++) {
                String student = jsonCommand.getParameter(Parameters.TO_EXCHANGE_FROM + i);
                toExchangeFromStudent.add(Student.valueOf(student));
            }
            effectArgsBuilder.setSourceStudents(toExchangeFromStudent);
        }
        if (jsonCommand.getParameter(Parameters.TO_EXCHANGE_TO_NUMBER) != null) {
            int toExchangeToNumber = Integer.parseInt(jsonCommand.getParameter(Parameters.TO_EXCHANGE_TO_NUMBER));
            toExchangeToStudent = new ArrayList<>();
            for (int i = 0; i < toExchangeToNumber; i++) {
                String student = jsonCommand.getParameter(Parameters.TO_EXCHANGE_TO + i);
                toExchangeToStudent.add(Student.valueOf(student));
            }
            effectArgsBuilder.setDestStudents(toExchangeToStudent);
        }
        gameManager.playCharacter(Characters.fromString(characterType).getCharacterType(), effectArgsBuilder.build());
    }

    private void endTurn() {
        gameManager.endTurn();
        if (gameManager.getGameState().getStage().equals(Stage.ROUND_END)) {
            gameManager.nextRound();
            if (gameManager.getGameState().getStage().equals(Stage.PLANNING_FILL_CLOUDS)) {
                gameManager.fillClouds();
            }
        }
    }

    // UTILS

    /**
     * @return the string containing the game state in JSON format
     */
    private String serializeGameStateJson() {
        return GsonManager.getInstance().toJson(gameManager.getGameState(), GameState.class);
    }

    /**
     * @return the game state created by the given serialized game state string
     */
    private GameState deserializeGameStateJson(String serializedGameState) {
        return GsonManager.getInstance().fromJson(serializedGameState, GameState.class);
    }

    /**
     * @return the prepared response with the serialized game state and the information about the last performed move
     */
    private JsonCommand createMoveDoneResponse(String serializedGameState, JsonCommand lastMove) {
        return new JsonCommand(Command.MOVE_DONE)
                .addParameterSingleQuotes(Parameters.GAME_STATE, serializedGameState)
                .addParameterSingleQuotes(Parameters.LAST_MOVE, lastMove.toJson());
    }

    /**
     * @return the prepared response with the force end match command for the clients
     */
    private JsonCommand createForceEndMatchResponse() {
        return new JsonCommand(Command.FORCE_END_MATCH);
    }

    /**
     * @return the cloned game state
     */
    private GameState getClonedGameState() {
        return deserializeGameStateJson(serializeGameStateJson());
    }

    /**
     * Recreates the game manager based on the given gameState
     */
    private void rollbackGameState(GameState oldGameState) {
        gameManager = new GameManager(oldGameState);
    }
}
