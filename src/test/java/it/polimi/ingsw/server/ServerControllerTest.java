package it.polimi.ingsw.server;

import it.polimi.ingsw.network.GsonManager;
import it.polimi.ingsw.models.GameManager;
import it.polimi.ingsw.models.components.*;
import it.polimi.ingsw.models.components.characters.Character;
import it.polimi.ingsw.models.components.characters.CharacterType;
import it.polimi.ingsw.models.components.characters.Characters;
import it.polimi.ingsw.models.constants.GameConstants;
import it.polimi.ingsw.models.state.GameState;
import it.polimi.ingsw.models.state.Stage;
import it.polimi.ingsw.network.JsonCommand;
import it.polimi.ingsw.network.messages.Command;
import it.polimi.ingsw.network.messages.Parameters;
import it.polimi.ingsw.network.observers.JsonCommandChangeEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServerControllerTest {
    ServerController serverController;
    GameManager gameManager;

    @BeforeEach
    void setup() {
        gameManager = new GameManager(3, true);
        gameManager.addPlayer("a", Wizard.KING, Tower.BLACK);
        gameManager.addPlayer("b", Wizard.WITCH, Tower.GREY);
        gameManager.addPlayer("c", Wizard.SAGE, Tower.WHITE);
        gameManager.preparation();
        gameManager.fillClouds();
        serverController = new ServerController(gameManager, List.of("a", "b", "c"));
    }

    @Test
    void shouldAddPlayer() {
        gameManager = new GameManager(2, true);
        serverController = new ServerController(gameManager, List.of("a", "b", "c"));
        JsonCommand jsonCommand = (new JsonCommand(Command.PLAYER_MOVE_ADD_PLAYER))
                .addParameter(Parameters.NICKNAME, "a", true)
                .addParameter(Parameters.WIZARD, Wizard.KING.toString(), true)
                .addParameter(Parameters.TOWER, Tower.BLACK.toString(), true);
        serverController.handleRequest(jsonCommand);

        assertEquals("a", gameManager.getGameState().getPlayers().get(0).getName());
        assertEquals(Wizard.KING, gameManager.getGameState().getPlayers().get(0).getWizard());
        assertEquals(Tower.BLACK, gameManager.getGameState().getPlayers().get(0).getBoard().getTowerType());
    }

    @Test
    void shouldForceEndMatchAfterReceivingLogoutMessage() {
        JsonCommand jsonCommand = (new JsonCommand(Command.LOGOUT));
        JsonCommand response = serverController.handleRequest(jsonCommand);
        assertEquals(Command.FORCE_END_MATCH, response.getCommand());
    }

    @Test
    void shouldPrepareTheInitialGameState() {
        gameManager = new GameManager(3, true);
        gameManager.addPlayer("a", Wizard.KING, Tower.BLACK);
        gameManager.addPlayer("b", Wizard.WITCH, Tower.GREY);
        serverController = new ServerController(gameManager, List.of("c"));
        JsonCommand jsonCommand = (new JsonCommand(Command.PLAYER_MOVE_ADD_PLAYER))
                .addParameter(Parameters.NICKNAME, "c", true)
                .addParameter(Parameters.WIZARD, Wizard.SAGE.toString(), true)
                .addParameter(Parameters.TOWER, Tower.WHITE.toString(), true);
        JsonCommand response = serverController.handleRequest(jsonCommand);


        JsonCommand expectedResponse;
        String initialGameState = GsonManager.getInstance().toJson(gameManager.getGameState(), GameState.class);
        JsonCommand lastMove = new JsonCommand(Command.INITIALIZATION);
        expectedResponse = new JsonCommand(Command.MOVE_DONE)
                .addParameterSingleQuotes(Parameters.GAME_STATE, initialGameState)
                .addParameterSingleQuotes(Parameters.LAST_MOVE, lastMove.toJson());

        assertEquals(expectedResponse.getCommand(), response.getCommand());
        assertEquals(expectedResponse.getParameter(Parameters.GAME_STATE), response.getParameter(Parameters.GAME_STATE));
        assertEquals(expectedResponse.getParameter(Parameters.LAST_MOVE), response.getParameter(Parameters.LAST_MOVE));
    }

    @Test
    void shouldHaveBeenPlayedTheCorrectAssistant() {
        Player currentPlayer = gameManager.getGameState().getCurrentPlayer();

        JsonCommand jsonCommand = new JsonCommand(Command.PLAYER_MOVE_PLAY_ASSISTANT)
                .addParameter(Parameters.ASSISTANT, "SNAKE", true);
        serverController.handleRequest(jsonCommand);

        assertEquals(5, currentPlayer.getLastPlayedAssistant().getValue());
    }

    @Test
    void shouldHaveBeenMovedStudentToTheHall() {
        gameManager.getGameState().setStage(Stage.ACTION_MOVE_STUDENTS);
        Entrance currentPlayerEntrance = gameManager.getGameState().getCurrentPlayer().getBoard().getEntrance();
        currentPlayerEntrance.removeStudent(Student.RED);
        currentPlayerEntrance.removeStudent(Student.GREEN);
        currentPlayerEntrance.removeStudent(Student.YELLOW);
        currentPlayerEntrance.removeStudent(Student.CYAN);
        currentPlayerEntrance.removeStudent(Student.PINK);

        currentPlayerEntrance.receiveStudent(Student.RED);

        JsonCommand jsonCommand = new JsonCommand(Command.PLAYER_MOVE_MOVE_STUDENT_FROM_ENTRANCE_TO_HALL)
                .addParameter(Parameters.STUDENT_COLOR, Student.RED.toString(), true);
        serverController.handleRequest(jsonCommand);

        Player currentPlayer = gameManager.getGameState().getCurrentPlayer();
        assertEquals(1, currentPlayer.getBoard().getHall().getNumStudentsByColor(Student.RED));
    }

    @Test
    void shouldHaveBeenMovedStudentToTheIslandWithMotherNature() {
        gameManager.getGameState().setStage(Stage.ACTION_MOVE_STUDENTS);
        Entrance currentPlayerEntrance = gameManager.getGameState().getCurrentPlayer().getBoard().getEntrance();
        currentPlayerEntrance.removeStudent(Student.RED);
        currentPlayerEntrance.removeStudent(Student.GREEN);
        currentPlayerEntrance.removeStudent(Student.YELLOW);
        currentPlayerEntrance.removeStudent(Student.CYAN);
        currentPlayerEntrance.removeStudent(Student.PINK);

        currentPlayerEntrance.receiveStudent(Student.RED);

        JsonCommand jsonCommand = new JsonCommand(Command.PLAYER_MOVE_MOVE_STUDENT_FROM_ENTRANCE_TO_ISLAND)
                .addParameter(Parameters.STUDENT_COLOR, Student.RED.toString(), true)
                .addParameter(Parameters.ISLAND_ID, "" + gameManager.getGameState().getMotherNaturePosition(), true);
        serverController.handleRequest(jsonCommand);

        Island island = gameManager.getGameState().getIslandByPosition(gameManager.getGameState().getMotherNaturePosition());
        assertEquals(1, island.getNumStudent(Student.RED));
    }

    @Test
    void shouldHaveBeenMovedMotherNature() {
        gameManager.getGameState().setStage(Stage.PLANNING_PLAY_ASSISTANTS);
        int initialMotherNaturePosition = gameManager.getGameState().getMotherNaturePosition();

        gameManager.playAssistant(Assistant.LEOPARD);
        gameManager.playAssistant(Assistant.CAT);
        gameManager.playAssistant(Assistant.FOX);

        gameManager.getGameState().setStage(Stage.ACTION_MOVE_MOTHER_NATURE);

        JsonCommand jsonCommand = new JsonCommand(Command.PLAYER_MOVE_MOVE_MOTHER_NATURE)
                .addParameter(Parameters.STEPS, "2", true);
        serverController.handleRequest(jsonCommand);

        assertEquals((initialMotherNaturePosition + 2) % GameConstants.NUMBER_OF_ISLANDS,
                gameManager.getGameState().getMotherNaturePosition());
    }

    @Test
    void shouldHavePickedStudentsFromCloud() {
        gameManager.getGameState().setStage(Stage.ACTION_TAKE_STUDENTS_FROM_CLOUD);

        Player currentPlayer = gameManager.getGameState().getCurrentPlayer();
        for (Student student : Student.values()) {
            int numStudents = currentPlayer.getBoard().getEntrance().getNumStudentsByColor(student);
            for (int i = 0; i < numStudents; i++) {
                currentPlayer.getBoard().getEntrance().removeStudent(student);
            }
        }

        Cloud cloud = gameManager.getGameState().getCloudById(0);

        List<Student> studentsOnCloud = cloud.pickStudents();
        for (Student student : studentsOnCloud) {
            cloud.receiveStudent(student);
        }

        JsonCommand jsonCommand = new JsonCommand(Command.PLAYER_MOVE_PICK_STUDENTS_FROM_CLOUD)
                .addParameter(Parameters.CLOUD_ID, "0", true);
        serverController.handleRequest(jsonCommand);

        assertTrue(gameManager.getGameState().getCloudById(0).isEmpty());
        for (Student student : studentsOnCloud) {
            assertTrue(currentPlayer.getBoard().getEntrance().getNumStudentsByColor(student) > 0);
        }
    }

    @Test
    void shouldHavePlayedDionysusCharacter() {
        gameManager.getGameState().setStage(Stage.ACTION_MOVE_STUDENTS);
        Player currentPlayer = gameManager.getGameState().getCurrentPlayer();
        currentPlayer.addCoin();
        currentPlayer.addCoin();
        currentPlayer.addCoin();
        gameManager.getGameState().getExpertAttrs().getCoinsFromStock(3);

        Character dionysus = Characters.get(CharacterType.DIONYSUS);
        dionysus.receiveStudent(Student.RED);

        gameManager.getGameState().getExpertAttrs().setCharacters(List.of(dionysus));

        JsonCommand request = new JsonCommand(Command.PLAYER_MOVE_PLAY_CHARACTER)
                .addParameter(Parameters.CHARACTER_TYPE, dionysus.getCharacterType().toString(), true)
                .addParameter(Parameters.ISLAND_ID, "" + gameManager.getGameState().getMotherNaturePosition(), true)
                .addParameter(Parameters.STUDENT_COLOR, Student.RED.toString(), true);

        serverController.handleRequest(request);

        assertEquals(1, gameManager.getGameState().getIslandByPosition(gameManager.getGameState().getMotherNaturePosition()).getNumStudent(Student.RED));
    }

    @Test
    void shouldHavePlayedJesterCharacter() {
        gameManager.getGameState().setStage(Stage.ACTION_MOVE_STUDENTS);
        Player currentPlayer = gameManager.getGameState().getCurrentPlayer();
        currentPlayer.addCoin();
        currentPlayer.addCoin();
        currentPlayer.addCoin();
        gameManager.getGameState().getExpertAttrs().getCoinsFromStock(3);

        for (Student student : Student.values()) {
            int numStudents = currentPlayer.getBoard().getEntrance().getNumStudentsByColor(student);
            for (int i = 0; i < numStudents; i++) {
                currentPlayer.getBoard().getEntrance().removeStudent(student);
            }
        }
        currentPlayer.getBoard().getEntrance().receiveStudent(Student.GREEN);
        currentPlayer.getBoard().getEntrance().receiveStudent(Student.GREEN);
        currentPlayer.getBoard().getEntrance().receiveStudent(Student.GREEN);

        Character jester = Characters.get(CharacterType.JESTER);
        jester.receiveStudent(Student.RED);
        jester.receiveStudent(Student.RED);
        jester.receiveStudent(Student.RED);

        gameManager.getGameState().getExpertAttrs().setCharacters(List.of(jester));

        JsonCommand request = new JsonCommand(Command.PLAYER_MOVE_PLAY_CHARACTER)
                .addParameter(Parameters.CHARACTER_TYPE, jester.getCharacterType().toString(), true)
                .addParameter(Parameters.TO_EXCHANGE_FROM_NUMBER, "3", true)
                .addParameter(Parameters.TO_EXCHANGE_TO_NUMBER, "3", true)
                .addParameter(Parameters.TO_EXCHANGE_FROM + 0, "RED", true)
                .addParameter(Parameters.TO_EXCHANGE_FROM + 1, "RED", true)
                .addParameter(Parameters.TO_EXCHANGE_FROM + 2, "RED", true)
                .addParameter(Parameters.TO_EXCHANGE_TO + 0, "GREEN", true)
                .addParameter(Parameters.TO_EXCHANGE_TO + 1, "GREEN", true)
                .addParameter(Parameters.TO_EXCHANGE_TO + 2, "GREEN", true);

        serverController.handleRequest(request);

        List<Student> greenList = List.of(Student.GREEN, Student.GREEN, Student.GREEN);
        assertTrue(jester.getStudents().containsAll(greenList));
        assertEquals(3, currentPlayer.getBoard().getEntrance().getNumStudentsByColor(Student.RED));
    }

    @Test
    void shouldPerformMoveTroughPropertyChange() {
        Player currentPlayer = gameManager.getGameState().getCurrentPlayer();

        JsonCommand jsonCommand = new JsonCommand(Command.PLAYER_MOVE_PLAY_ASSISTANT)
                .addParameter(Parameters.ASSISTANT, "SNAKE", true);
        serverController.jsonCommandChange(new JsonCommandChangeEvent("messageReceived", jsonCommand));

        assertEquals(5, currentPlayer.getLastPlayedAssistant().getValue());
    }

    @Test
    void shouldReturnIllegalMove() {
        JsonCommand jsonCommand = new JsonCommand(Command.PLAYER_MOVE_END_TURN);
        JsonCommand response = serverController.handleRequest(jsonCommand);
        JsonCommand lastMove = JsonCommand.fromJson(response.getParameter(Parameters.LAST_MOVE));
        assertEquals(Command.ILLEGAL_MOVE, lastMove.getCommand());
    }
}
