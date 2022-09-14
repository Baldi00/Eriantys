package it.polimi.ingsw.models.state;

import it.polimi.ingsw.models.components.*;
import it.polimi.ingsw.models.components.Board;
import it.polimi.ingsw.models.exceptions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class GameStateTest {

    private GameState gameState;

    @BeforeEach
    void setup() {
        gameState = new GameState(2, false);
    }

    @Test
    void shouldSetAvailableWizards() {
        List<Wizard> wizards = Arrays.stream(Wizard.values()).toList();
        gameState.setAvailableWizards(wizards);
        assertEquals(wizards, gameState.getAvailableWizards());
    }

    @Test
    void shouldSetAvailableTowers() {
        List<Tower> towers = Arrays.stream(Tower.values()).toList();
        gameState.setAvailableTowers(towers);
        assertEquals(towers, gameState.getAvailableTowers());
    }

    @Test
    void shouldNotBeExpertMatch() {
        GameState gameState = new GameState(2, false);
        assertFalse(gameState.isExpertMatch());
    }

    @Test
    void shouldBeExpertMatch() {
        GameState gameState = new GameState(2, true);
        assertTrue(gameState.isExpertMatch());
    }

    @Test
    void shouldBePossibleToSetTwoThreeOrFourPlayers() {
        assertDoesNotThrow(() -> new GameState(2, false));
        assertDoesNotThrow(() -> new GameState(3, false));
        assertDoesNotThrow(() -> new GameState(4, false));
    }

    @Test
    void shouldThrowExceptionWhenSettingInvalidNumPlayers() {
        assertThrows(IllegalArgumentException.class, () -> new GameState(1, false));
        assertThrows(IllegalArgumentException.class, () -> new GameState(5, false));
    }

    @Test
    void noOneShouldOwnProfOnInit() {
        GameState gameState = new GameState(2, true);
        for (Student student : Student.values())
            assertNull(gameState.getProfessorOwner(student));
    }

    @Test
    void checkSetAndGetIslands() {
        List<Island> islands = new ArrayList<>();
        islands.add(new Island(1, 1));
        islands.add(new Island(2, 1));
        gameState.setIslands(islands);
        for (int i = 0; i < gameState.getIslands().size(); i++) {
            assertEquals(islands.get(i).getPosition(), gameState.getIslands().get(i).getPosition());
        }
    }

    @Test
    void checkSetAndGetClouds() {
        List<Cloud> clouds = new ArrayList<>();
        clouds.add(new Cloud(1, 1));
        clouds.add(new Cloud(2, 1));
        gameState.setClouds(clouds);
        for (int i = 0; i < gameState.getClouds().size(); i++) {
            assertEquals(clouds.get(i).getId(), gameState.getClouds().get(i).getId());
        }
    }

    @Test
    void shouldAddPlayer() {
        GameState gameState = new GameState(2, false);
        Player player = createPlayer("test", Wizard.WITCH, Tower.BLACK);
        gameState.addPlayer(player);

        assertEquals(1, gameState.getPlayers().size());
        assertEquals(player, gameState.getPlayers().get(0));
    }

    @Test
    void checkSetAndGetMotherNaturePosition() {
        gameState.setMotherNaturePosition(2);
        assertEquals(2, gameState.getMotherNaturePosition());
    }

    @Test
    void shouldThrowAnExceptionIfMotherNaturePositionIsOutsideLimits() {
        assertThrows(InvalidMotherNaturePosition.class, () ->
                gameState.setMotherNaturePosition(-3)
        );
    }

    @Test
    void bagShouldBeEmptyOnInit() {
        assertTrue(gameState.isBagEmpty());
    }

    @Test
    void bagShouldNotBeEmpty() {
        gameState.getBag().receiveStudent(Student.RED);
        assertFalse(gameState.isBagEmpty());
    }

    @Test
    void shouldThrowAnExceptionBecauseSearchingForNonExistingIsland() {
        gameState.setIslands(List.of(
                new Island(0, 1),
                new Island(1, 1),
                new Island(2, 1),
                new Island(3, 1)
        ));

        assertThrows(NoSuchElementException.class, () ->
                gameState.getIslandByPosition(5)
        );
    }

    @Test
    void shouldThrowAnExceptionBecauseSearchingForNonExistingPlayer() {
        GameState gameState = new GameState(2, false);
        Player player1 = createPlayer("p1", Wizard.WITCH, Tower.BLACK);
        Player player2 = createPlayer("p2", Wizard.KING, Tower.WHITE);
        gameState.addPlayer(player1);
        gameState.addPlayer(player2);

        assertThrows(NoSuchElementException.class, () ->
                gameState.getPlayerById(3)
        );
    }

    @Test
    void shouldThrowAnExceptionBecauseSearchingForNonExistingPlayerByTower() {
        GameState gameState = new GameState(2, false);
        Player player1 = createPlayer("p1", Wizard.WITCH, Tower.BLACK);
        Player player2 = createPlayer("p2", Wizard.KING, Tower.WHITE);
        gameState.addPlayer(player1);
        gameState.addPlayer(player2);

        assertThrows(NoSuchElementException.class, () ->
                gameState.getPlayerByTower(Tower.GREY)
        );
    }

    @Test
    void getPlayerByTowerShouldReturnTheLeaderPlayer() {
        GameState gameState = new GameState(2, false);
        Player player1 = new Player(Wizard.WITCH, "test1", new ArrayList<>(), new Board(Tower.BLACK, 1, 1), false);
        Player player2 = new Player(Wizard.KING, "test2", new ArrayList<>(), new Board(Tower.BLACK, 1, 1), true);
        gameState.addPlayer(player1);
        gameState.addPlayer(player2);

        assertEquals(player2, gameState.getPlayerByTower(Tower.BLACK));
    }

    @Test
    void shouldThrowExceptionIfTheRequestedPlayerIdDoesNotExist() {
        GameState gameState = new GameState(2, false);
        Player player = createPlayer("p1", Wizard.WITCH, Tower.BLACK);
        gameState.addPlayer(player);

        int playerId = Wizard.DRUID.ordinal();
        assertThrows(NoSuchElementException.class, () ->
            gameState.getPlayerById(playerId)
        );
    }

    @Test
    void shouldThrowExceptionIfTheRequestedIslandIdDoesNotExist() {
        gameState.setIslands(List.of(
                new Island(1, 1)
        ));
        assertThrows(NoSuchElementException.class, () ->
            gameState.getIslandByPosition(2)
        );
    }

    @Test
    void shouldThrowExceptionWhenTryingTooAddMoreThanNumPlayers() {
        GameState state = new GameState(2, false);
        Board board = new Board(Tower.BLACK, 0, 0);
        Player player1 = new Player(Wizard.KING, "test", List.of(Assistant.values()), board);
        Player player2 = new Player(Wizard.WITCH, "test2", List.of(Assistant.values()), board);
        Player player3 = new Player(Wizard.SAGE, "test3", List.of(Assistant.values()), board);
        state.addPlayer(player1);
        state.addPlayer(player2);
        assertThrows(IllegalMoveException.class, () ->
                state.addPlayer(player3)
        );
    }

    @Test
    void winnerShouldBeNullOnInit() {
        GameState state = new GameState(2, false);
        assertNull(state.getWinner());
    }

    @Test
    void shouldSetWinner() {
        GameState state = new GameState(2, false);
        state.setWinner(Tower.BLACK);
        assertEquals(Tower.BLACK, state.getWinner());
    }

    private Player createPlayer(String name, Wizard wizard, Tower tower) {
        Board board = new Board(tower, 8, 7);
        return new Player(wizard, name, List.of(Assistant.values()), board);
    }

}
