package it.polimi.ingsw.models;

import it.polimi.ingsw.models.components.*;
import it.polimi.ingsw.models.components.hall.Hall;
import it.polimi.ingsw.models.constants.GameConstants;
import it.polimi.ingsw.models.constants.MatchType;
import it.polimi.ingsw.models.exceptions.IllegalCallException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameManagerTest {
    private GameManager gameManager;

    @BeforeEach
    void setup() {
        gameManager = new GameManager(3, false);
    }

    @Test
    void shouldCreateExpertMatch() {
        gameManager = new GameManager(2, true);
        assertTrue(gameManager.getGameState().isExpertMatch());
    }

    @Test
    void shouldThrowsAnExceptionBecauseNumberOfPlayerIsWrong() {
        assertThrows(IllegalArgumentException.class, () ->
                gameManager = new GameManager(5, true)
        );
    }

    @Test
    void shouldThrowExceptionWhenPreparationIsDoneBeforePlayersAreAdded() {
        assertThrows(IllegalCallException.class, () ->
                gameManager.preparation()
        );
    }

    @Test
    void shouldNotHavePlayersWhenCreated() {
        GameManager gameManager = new GameManager(2, false);
        assertEquals(0, gameManager.getGameState().getPlayers().size());
    }

    @Test
    void shouldAddPlayer() {
        GameManager gameManager = new GameManager(2, false);
        gameManager.addPlayer("player", Wizard.WITCH, Tower.BLACK);
        assertEquals(1, gameManager.getGameState().getPlayers().size());
    }

    @Test
    void shouldThrowExceptionIfPlayersWithSameNameAreAdded() {
        GameManager gameManager = new GameManager(2, false);
        gameManager.addPlayer("player", Wizard.WITCH, Tower.BLACK);
        assertThrows(IllegalArgumentException.class, () ->
                gameManager.addPlayer("player", Wizard.KING, Tower.WHITE)
        );
    }

    @Test
    void shouldThrowExceptionIfPlayersWithSameWizardAreAdded() {
        GameManager gameManager = new GameManager(2, false);
        gameManager.addPlayer("player", Wizard.WITCH, Tower.BLACK);
        assertThrows(IllegalArgumentException.class, () ->
                gameManager.addPlayer("player2", Wizard.WITCH, Tower.WHITE)
        );
    }

    @Test
    void shouldThrowExceptionIfPlayersWithSameTowerAreAdded() {
        GameManager gameManager = new GameManager(2, false);
        gameManager.addPlayer("player", Wizard.WITCH, Tower.BLACK);
        assertThrows(IllegalArgumentException.class, () ->
                gameManager.addPlayer("player2", Wizard.KING, Tower.BLACK)
        );
    }

    @Test
    void shouldThrowExceptionWhenAddingTooManyPlayers() {
        GameManager gameManager = new GameManager(2, false);
        gameManager.addPlayer("player", Wizard.WITCH, Tower.BLACK);
        gameManager.addPlayer("player2", Wizard.KING, Tower.WHITE);
        assertThrows(IllegalCallException.class, () ->
                gameManager.addPlayer("player3", Wizard.WITCH, Tower.BLACK)
        );
    }

    @Test
    void shouldCreateInitialClouds() {
        addThreePlayersToGameState(gameManager);
        gameManager.preparation();
        assertEquals(3, gameManager.getGameState().getClouds().size());
    }

    @Test
    void shouldCreateInitialIslands() {
        addThreePlayersToGameState(gameManager);
        gameManager.preparation();
        assertEquals(GameConstants.NUMBER_OF_ISLANDS, gameManager.getGameState().getIslands().size());
    }

    @Test
    void shouldNotPutStudentsOnTheIslandWithMotherNature() {
        addThreePlayersToGameState(gameManager);
        gameManager.preparation();

        int motherNaturePosition = gameManager.getGameState().getMotherNaturePosition();
        Island islandWithMotherNature = gameManager.getGameState().getIslandByPosition(motherNaturePosition);

        for (Student student : Student.values()) {
            assertEquals(0, islandWithMotherNature.getNumStudent(student));
        }
    }

    @Test
    void shouldNotPutStudentsOnTheIslandOppositeToMotherNature() {
        addThreePlayersToGameState(gameManager);
        gameManager.preparation();

        int motherNaturePosition = gameManager.getGameState().getMotherNaturePosition();
        int oppositeToMotherNaturePosition = (motherNaturePosition + GameConstants.NUMBER_OF_ISLANDS / 2) % GameConstants.NUMBER_OF_ISLANDS;
        Island islandOppositeToMotherNature = gameManager.getGameState().getIslandByPosition(oppositeToMotherNaturePosition);

        for (Student student : Student.values()) {
            assertEquals(0, islandOppositeToMotherNature.getNumStudent(student));
        }
    }

    @Test
    void cloudsShouldBeFilledWithTheRightNumberOfStudents() {
        GameManager gameManager = new GameManager(2, false);
        addTwoPlayersToGameState(gameManager);
        gameManager.preparation();
        gameManager.fillClouds();

        GameConstants constants = new GameConstants(MatchType.TWO_PLAYERS);
        for (Cloud cloud : gameManager.getGameState().getClouds()) {
            assertEquals(constants.getNumStudentsOnCloud(), cloud.pickStudents().size());
        }
    }

    @Test
    void shouldAddCoinToThePlayerThatHasAddedTheStudentOnTheCoinCellIfExpertGame() {
        gameManager = new GameManager(3, true);
        addThreePlayersToGameState(gameManager);
        gameManager.preparation();

        Player player = gameManager.getGameState().getCurrentPlayer();
        Hall playerHall = player.getBoard().getHall();

        assertEquals(1, player.getNumCoins());

        playerHall.receiveStudent(Student.RED);
        playerHall.receiveStudent(Student.RED);
        playerHall.receiveStudent(Student.RED);

        assertEquals(2, player.getNumCoins());
    }

    @Test
    void shouldNotAddCoinToThePlayerThatHasAddedTheStudentOnTheCoinCellIfStandardGame() {
        gameManager = new GameManager(3, false);
        addThreePlayersToGameState(gameManager);
        gameManager.preparation();

        Player player = gameManager.getGameState().getCurrentPlayer();
        Hall playerHall = player.getBoard().getHall();

        assertEquals(0, player.getNumCoins());

        playerHall.receiveStudent(Student.RED);
        playerHall.receiveStudent(Student.RED);
        playerHall.receiveStudent(Student.RED);

        assertEquals(0, player.getNumCoins());
    }

    private void addTwoPlayersToGameState(GameManager manager) {
        manager.addPlayer("player1", Wizard.WITCH, Tower.BLACK);
        manager.addPlayer("player2", Wizard.KING, Tower.WHITE);
    }

    private void addThreePlayersToGameState(GameManager manager) {
        manager.addPlayer("player1", Wizard.WITCH, Tower.BLACK);
        manager.addPlayer("player2", Wizard.KING, Tower.WHITE);
        manager.addPlayer("player3", Wizard.SAGE, Tower.GREY);
    }
}
