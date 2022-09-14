package it.polimi.ingsw.models.components;

import it.polimi.ingsw.models.exceptions.IllegalMoveException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    private Board board;

    @BeforeEach
    void setup() {
        board = new Board(Tower.BLACK, 2, 2);
    }

    @Test
    void wizardShouldBeTheSamePassedInConstructor() {
        for (Wizard wizard : Wizard.values()) {
            Player player = new Player(wizard, "test", List.of(Assistant.values()), board);
            assertEquals(wizard, player.getWizard());
        }
    }

    @Test
    void wizardShouldBeTheId() {
        Player player = new Player(Wizard.SAGE, "test", List.of(Assistant.values()), board);
        assertEquals(Wizard.SAGE.ordinal(), player.getId());
    }

    @Test
    void shouldHaveTheNicknameSet() {
        String nickname = "test";
        Player player = new Player(Wizard.SAGE, nickname, List.of(Assistant.values()), board);
        assertEquals(nickname, player.getName());
    }

    @Test
    void shouldThrowExceptionIfAMissingAssistantIsPlayed() {
        List<Assistant> assistants = new ArrayList<>();
        Player player = new Player(Wizard.WITCH, "test", assistants, board);
        assertThrows(IllegalMoveException.class, () ->
            player.playAssistant(Assistant.ELEPHANT)
        );
    }

    @Test
    void shouldNotThrowExceptionIfTheAssistantPlayedIsPresent() {
        Player player = new Player(Wizard.WITCH, "test", List.of(Assistant.values()), board);
        assertDoesNotThrow(() ->
            player.playAssistant(Assistant.TURTLE)
        );
    }

    @Test
    void lastPlayedAssistantShouldBeTheLastPlayed() {
        Player player = new Player(Wizard.WITCH, "test", List.of(Assistant.values()), board);
        player.playAssistant(Assistant.TURTLE);
        assertEquals(Assistant.TURTLE, player.getLastPlayedAssistant());
    }

    @Test
    void lastPlayedAssistantShouldBeNullOnInit() {
        Player player = new Player(Wizard.WITCH, "test", List.of(Assistant.values()), board);
        assertNull(player.getLastPlayedAssistant());
    }

    @Test
    void hasPlacedAllTowersShouldBeFalseWhenThereAreTowersLeftOnTheBoard() {
        Player player = new Player(Wizard.WITCH, "test", List.of(Assistant.values()), board);
        player.getBoard().receiveTower(Tower.BLACK);
        assertFalse(player.hasPlacedAllTowers());
    }

    @Test
    void hasPlacedAllTowersShouldBeTrueWhenThereAreNoTowersLeftOnTheBoard() {
        Player player = new Player(Wizard.WITCH, "test", List.of(Assistant.values()), board);
        player.getBoard().receiveTower(Tower.BLACK);
        player.getBoard().removeTower();
        assertTrue(player.hasPlacedAllTowers());
    }

    @Test
    void shouldHaveAssistantsIfTheyArePresent() {
        Player player = new Player(Wizard.WITCH, "test", List.of(Assistant.values()), board);
        assertTrue(player.hasAssistants());
    }

    @Test
    void shouldNotHaveAssistantsIfTheyNotArePresent() {
        List<Assistant> assistants = new ArrayList<>();
        Player player = new Player(Wizard.WITCH, "test", assistants, board);
        assertFalse(player.hasAssistants());
    }

    @Test
    void shouldHaveZeroCoinsOnInit() {
        Player player = new Player(Wizard.WITCH, "test", List.of(Assistant.values()), board);
        assertEquals(0, player.getNumCoins());
    }

    @Test
    void shouldHaveCoinsIfTheyHaveBeenGrabbed() {
        Player player = new Player(Wizard.WITCH, "test", List.of(Assistant.values()), board);
        player.addCoin();
        assertEquals(1, player.getNumCoins());
        player.addCoin();
        assertEquals(2, player.getNumCoins());
    }

    @Test
    void shouldRemoveCoinFromPlayer() {
        Player player = new Player(Wizard.WITCH, "test", List.of(Assistant.values()), board);
        player.addCoin();
        player.addCoin();
        player.removeCoins(1);
        assertEquals(1, player.getNumCoins());
    }

    @Test
    void shouldThrowExceptionWhenRemovingNumCoinsThatPlayerDoesNotHave() {
        Player player = new Player(Wizard.WITCH, "test", List.of(Assistant.values()), board);
        assertThrows(IllegalMoveException.class, () ->
                player.removeCoins(1)
        );
    }

    @Test
    void shouldReturnOnlyTheAssistantsNotPlayed() {
        List<Assistant> assistants = List.of(Assistant.TURTLE);
        Player player = new Player(Wizard.WITCH, "test", assistants, board);
        assertEquals(assistants, player.getPlayableAssistants());
        player.playAssistant(Assistant.TURTLE);
        assertTrue(player.getPlayableAssistants().isEmpty());
    }

    @Test
    void playersWithDifferentIdShouldNotBeEqual() {
        Player player1 = new Player(Wizard.WITCH, "test", List.of(Assistant.values()), board);
        Player player2 = new Player(Wizard.SAGE, "test", List.of(Assistant.values()), board);
        assertNotEquals(player1, player2);
        assertNotEquals(player1.hashCode(), player2.hashCode());
    }

    @Test
    void playersWithSameIdShouldBeEqual() {
        Player player1 = new Player(Wizard.WITCH, "test", List.of(Assistant.values()), board);
        Player player2 = new Player(Wizard.WITCH, "test", List.of(Assistant.values()), board);
        assertEquals(player1, player2);
        assertEquals(player1.hashCode(), player2.hashCode());
    }
}
