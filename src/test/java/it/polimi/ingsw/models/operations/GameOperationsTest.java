package it.polimi.ingsw.models.operations;

import it.polimi.ingsw.models.TestUtils;
import it.polimi.ingsw.models.components.Board;
import it.polimi.ingsw.models.constants.GameConstants;
import it.polimi.ingsw.models.state.GameState;
import it.polimi.ingsw.models.components.*;
import it.polimi.ingsw.models.components.characters.Character;
import it.polimi.ingsw.models.components.characters.CharacterType;
import it.polimi.ingsw.models.components.characters.Characters;
import it.polimi.ingsw.models.components.characters.effects.EffectArgs;
import it.polimi.ingsw.models.exceptions.GameNotOverException;
import it.polimi.ingsw.models.exceptions.IllegalMoveException;
import it.polimi.ingsw.models.exceptions.TowerNotSetException;
import it.polimi.ingsw.models.state.Stage;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameOperationsTest {

    @Test
    void shouldAddThePlayerToMatch() {
        GameState state = new GameState(2, false);
        state.setAvailableWizards(List.of(Wizard.WITCH));
        state.setAvailableTowers(List.of(Tower.BLACK));

        Player player = TestUtils.createPlayer("p1", Wizard.WITCH, Tower.BLACK);
        GameOperations.addPlayer(state, player);

        assertTrue(state.getPlayers().contains(player));
    }

    @Test
    void shouldThrowExceptionWhenPlayerWithSameNameIsAlreadyPresent() {
        GameState state = new GameState(2, false);
        state.setAvailableWizards(List.of(Wizard.WITCH, Wizard.KING));
        state.setAvailableTowers(List.of(Tower.BLACK, Tower.WHITE));

        Player player1 = TestUtils.createPlayer("test", Wizard.WITCH, Tower.BLACK);
        Player player2 = TestUtils.createPlayer("test", Wizard.KING, Tower.WHITE);

        GameOperations.addPlayer(state, player1);
        assertThrows(IllegalArgumentException.class, () ->
                GameOperations.addPlayer(state, player2)
        );
    }

    @Test
    void shouldThrowExceptionWhenAddingPlayerWithAlreadyChosenWizard() {
        GameState state = new GameState(2, false);
        state.setAvailableWizards(List.of(Wizard.WITCH));
        state.setAvailableTowers(List.of(Tower.BLACK, Tower.WHITE));

        Player player1 = TestUtils.createPlayer("p1", Wizard.WITCH, Tower.BLACK);
        Player player2 = TestUtils.createPlayer("p2", Wizard.WITCH, Tower.WHITE);

        GameOperations.addPlayer(state, player1);
        assertThrows(IllegalArgumentException.class, () ->
                GameOperations.addPlayer(state, player2)
        );
    }

    @Test
    void shouldThrowExceptionWhenAddingPlayerWithAlreadyChosenTower() {
        GameState state = new GameState(2, false);
        state.setAvailableWizards(List.of(Wizard.WITCH, Wizard.KING));
        state.setAvailableTowers(List.of(Tower.BLACK));

        Player player1 = TestUtils.createPlayer("p1", Wizard.WITCH, Tower.BLACK);
        Player player2 = TestUtils.createPlayer("p2", Wizard.KING, Tower.BLACK);

        GameOperations.addPlayer(state, player1);
        assertThrows(IllegalArgumentException.class, () ->
                GameOperations.addPlayer(state, player2)
        );
    }

    @Test
    void shouldRemoveWizardFromTheOnesAvailableWhenAddingPlayer() {
        GameState state = new GameState(2, false);
        state.setAvailableWizards(List.of(Wizard.WITCH));
        state.setAvailableTowers(List.of(Tower.BLACK));

        Player player = TestUtils.createPlayer("p1", Wizard.WITCH, Tower.BLACK);
        GameOperations.addPlayer(state, player);

        assertTrue(state.getAvailableWizards().isEmpty());
    }

    @Test
    void shouldRemoveTowerFromTheOnesAvailableWhenAddingPlayer() {
        GameState state = new GameState(2, false);
        state.setAvailableWizards(List.of(Wizard.WITCH));
        state.setAvailableTowers(List.of(Tower.BLACK));

        Player player = TestUtils.createPlayer("p1", Wizard.WITCH, Tower.BLACK);
        GameOperations.addPlayer(state, player);

        assertTrue(state.getAvailableTowers().isEmpty());
    }

    @Test
    void firstPlayerOfPlanningQueueShouldBeTheFirstInTheActionQueue() {
        GameState state = new GameState(2, false);

        List<Integer> clockwiseOrder = List.of(1, 2, 3, 4);
        state.setClockwiseOrder(clockwiseOrder);
        List<Integer> actionQueue = List.of(3, 2, 1, 4);
        state.setPlayerQueue(actionQueue);

        GameOperations.preparePlanningQueue(state);
        List<Integer> planningQueue = state.getPlayerQueue();

        assertEquals(3, planningQueue.get(0));
    }

    @Test
    void planningQueueShouldHavePlayersInClockwiseOrder() {
        GameState state = new GameState(2, false);

        List<Integer> clockwiseOrder = List.of(1, 2, 3, 4);
        state.setClockwiseOrder(clockwiseOrder);
        List<Integer> actionQueue = List.of(3, 2, 1, 4);
        state.setPlayerQueue(actionQueue);

        GameOperations.preparePlanningQueue(state);
        List<Integer> planningQueue = state.getPlayerQueue();

        List<Integer> expected = List.of(3, 4, 1, 2);
        assertEquals(expected, planningQueue);
    }

    @Test
    void planningQueueShouldHavePlayersInClockwiseOrderAlsoWhenActionQueueIsNotProvided() {
        GameState state = new GameState(2, false);

        List<Integer> clockwiseOrder = List.of(1, 2, 3, 4);
        state.setClockwiseOrder(clockwiseOrder);

        GameOperations.preparePlanningQueue(state);
        List<Integer> planningQueue = state.getPlayerQueue();

        int startingPlayer = planningQueue.get(0);
        int index = clockwiseOrder.indexOf(startingPlayer);
        for (int i = 0; i < 4; ++i) {
            assertEquals(clockwiseOrder.get(index), planningQueue.get(i));
            index = (index + 1) % 4;
        }
    }

    @Test
    void playersInActionQueueShouldBeOrderedWithAscendingAssistantValue() {
        List<Integer> planningQueue = List.of(1, 2, 3, 4);
        List<Assistant> assistants = List.of(
                Assistant.DOG,
                Assistant.OCTOPUS,
                Assistant.ELEPHANT,
                Assistant.FOX
        );

        List<Integer> actionQueue = GameOperations.getActionQueue(planningQueue, assistants);
        List<Integer> expected = List.of(3, 1, 2, 4);
        assertEquals(expected, actionQueue);
    }

    /**
     * If two players play the same assistant they should be ordered like in the
     * planning queue.
     */
    @Test
    void playersInActionQueueShouldBeOrderedWithAscendingAssistantValueSpecialCase() {
        List<Integer> planningQueue = List.of(1, 2, 3, 4);
        List<Assistant> assistants = List.of(
                Assistant.FOX,
                Assistant.OCTOPUS,
                Assistant.OCTOPUS,
                Assistant.ELEPHANT
        );

        List<Integer> actionQueue = GameOperations.getActionQueue(planningQueue, assistants);
        List<Integer> expected = List.of(4, 2, 3, 1);
        assertEquals(expected, actionQueue);
    }

    @Test
    void shouldThrowExceptionWhenPlayerDoesNotHaveEnoughCoinsToPlayCharacter() {
        GameState state = new GameState(2, true);

        Player player = TestUtils.createPlayer("p1", Wizard.WITCH, Tower.BLACK);

        state.addPlayer(player);
        state.setPlayerQueue(List.of(player.getId()));

        List<Character> characters = new ArrayList<>();
        characters.add(Characters.get(CharacterType.CENTAUR));
        characters.add(Characters.get(CharacterType.JESTER));

        state.getExpertAttrs().setCharacters(characters);

        EffectArgs effectArgs = new EffectArgs.Builder()
                .setGameState(state)
                .build();

        assertThrows(IllegalMoveException.class, () ->
                GameOperations.playCharacter(state, CharacterType.CENTAUR, effectArgs)
        );
    }

    @Test
    void shouldThrowExceptionWhenTryingToPlayNonExistingCharacter() {
        GameState state = new GameState(2, true);

        Board board = new Board(Tower.BLACK, 1, 1);
        Player player1 = new Player(Wizard.WITCH, "1", List.of(Assistant.values()), board);
        Player player2 = new Player(Wizard.KING, "2", List.of(Assistant.values()), board);

        state.addPlayer(player1);
        state.addPlayer(player2);

        List<Character> characters = new ArrayList<>();
        characters.add(Characters.get(CharacterType.CENTAUR));
        characters.add(Characters.get(CharacterType.JESTER));
        state.getExpertAttrs().setCharacters(characters);

        EffectArgs effectArgs = new EffectArgs.Builder()
                .setGameState(state)
                .build();

        assertThrows(IllegalMoveException.class, () ->
                GameOperations.playCharacter(state, CharacterType.DIONYSUS, effectArgs)
        );
    }

    @Test
    void shouldThrowExceptionWhenPlayingCharacterInNonExpertMatch() {
        GameState state = new GameState(2, false);
        assertThrows(IllegalMoveException.class, () ->
                GameOperations.playCharacter(state, CharacterType.DIONYSUS, null)
        );
    }

    @Test
    void shouldThrowExceptionWhenPlayingMoreThanOneCharacterInTheSameTurn() {
        GameState state = new GameState(2, true);

        Board board = new Board(Tower.BLACK, 1, 1);
        Player player = new Player(Wizard.WITCH, "1", List.of(Assistant.values()), board);
        player.addCoin();
        player.addCoin();
        player.addCoin();
        state.getExpertAttrs().getCoinsFromStock(3);
        state.addPlayer(player);
        state.setPlayerQueue(List.of(player.getId()));

        List<Character> characters = new ArrayList<>();
        characters.add(Characters.get(CharacterType.CENTAUR));
        state.getExpertAttrs().setCharacters(characters);

        EffectArgs effectArgs = new EffectArgs.Builder()
                .setGameState(state)
                .build();

        GameOperations.playCharacter(state, CharacterType.CENTAUR, effectArgs);

        assertThrows(IllegalMoveException.class, () ->
                GameOperations.playCharacter(state, CharacterType.CENTAUR, effectArgs)
        );
    }

    @Test
    void shouldReturnCoinsFromPlayerToStockAfterPlayingTheCharacter() {
        GameState state = new GameState(2, true);

        Player player = TestUtils.createPlayer("test", Wizard.WITCH, Tower.BLACK);

        state.addPlayer(player);
        state.setPlayerQueue(List.of(player.getId()));

        state.getExpertAttrs().getCoinsFromStock(3);
        player.addCoin();
        player.addCoin();
        player.addCoin();

        // TODO: maybe TestUtils should give you the option to create a dummy Character
        List<Character> characters = List.of(Characters.get(CharacterType.CENTAUR));
        state.getExpertAttrs().setCharacters(characters);

        EffectArgs effectArgs = new EffectArgs.Builder()
                .setGameState(state)
                .build();

        // CENTAUR costs 3
        GameOperations.playCharacter(state, CharacterType.CENTAUR, effectArgs);

        // NB: one coin it's retained by the character because it's the first time is used
        assertEquals(GameConstants.NUM_COINS - 1, state.getExpertAttrs().getNumCoinsInStock());
    }

    @Test
    void characterCostShouldBeIncrementedItHasBeenPlayed() {
        GameState state = new GameState(2, true);

        Board board = new Board(Tower.BLACK, 1, 1);
        Player player = new Player(Wizard.WITCH, "1", List.of(Assistant.values()), board);
        player.addCoin();
        player.addCoin();
        player.addCoin();
        state.getExpertAttrs().getCoinsFromStock(3);
        state.addPlayer(player);
        state.setPlayerQueue(List.of(player.getId()));

        Character character = Characters.get(CharacterType.CENTAUR);
        state.getExpertAttrs().setCharacters(List.of(character));

        EffectArgs effectArgs = new EffectArgs.Builder()
                .setGameState(state)
                .build();

        // CENTAUR costs 3
        GameOperations.playCharacter(state, CharacterType.CENTAUR, effectArgs);
        assertEquals(4, character.getCost());
    }

    @Test
    void shouldThrowExceptionWhenMotherNatureIsNotMoved() {
        GameState state = new GameState(2, false);
        assertThrows(IllegalMoveException.class, () ->
                GameOperations.moveMotherNature(state, 0)
        );
    }

    @Test
    void shouldThrowExceptionWhenMotherNatureIsMovedOfTooManySteps() {
        GameState state = new GameState(2, false);

        Board board = new Board(Tower.BLACK, 1, 1);
        Player player = new Player(Wizard.WITCH, "test", List.of(Assistant.values()), board);

        state.addPlayer(player);
        state.setPlayerQueue(List.of(player.getId()));
        state.setCurrentTurn(0);

        player.playAssistant(Assistant.TURTLE);

        assertThrows(IllegalMoveException.class, () ->
                GameOperations.moveMotherNature(state, 2)
        );
    }

    @Test
    void shouldMoveMotherNatureToNextIsland() {
        GameState state = new GameState(2, false);

        Board board = new Board(Tower.BLACK, 1, 1);
        Player player = new Player(Wizard.WITCH, "test", List.of(Assistant.values()), board);
        state.addPlayer(player);
        state.setPlayerQueue(List.of(player.getId()));
        state.setCurrentTurn(0);

        player.playAssistant(Assistant.TURTLE);

        Island island1 = new Island(0, 1);
        Island island2 = new Island(1, 1);
        state.setIslands(List.of(island1, island2));

        GameOperations.moveMotherNature(state, 1);

        assertEquals(1, state.getMotherNaturePosition());
    }

    @Test
    void shouldMoveMotherNatureToNextIslandWithAdditionalSteps() {
        GameState state = new GameState(2, true);

        Board board = new Board(Tower.BLACK, 1, 1);
        Player player = new Player(Wizard.WITCH, "test", List.of(Assistant.values()), board);
        state.addPlayer(player);
        state.setPlayerQueue(List.of(player.getId()));
        state.setCurrentTurn(0);

        player.playAssistant(Assistant.TURTLE);

        Island island1 = new Island(0, 1);
        Island island2 = new Island(1, 1);
        state.setIslands(List.of(island1, island2));

        state.getExpertAttrs().setAdditionalMotherNatureSteps(2);
        GameOperations.moveMotherNature(state, 2);

        assertEquals(0, state.getMotherNaturePosition());
    }

    @Test
    void shouldMoveMotherNatureOnTheNextIslandWhenOnAMergedIsland() {
        GameState state = new GameState(2, false);

        Board board = new Board(Tower.BLACK, 1, 1);
        Player player = new Player(Wizard.WITCH, "test", List.of(Assistant.values()), board);
        state.addPlayer(player);
        state.setPlayerQueue(List.of(player.getId()));
        state.setCurrentTurn(0);

        player.playAssistant(Assistant.TURTLE);

        Island island1 = new Island(0, 2);
        Island island2 = new Island(2, 1);
        state.setIslands(List.of(island1, island2));

        state.setMotherNaturePosition(0);
        GameOperations.moveMotherNature(state, 1);
        assertEquals(2, state.getMotherNaturePosition());
    }

    @Test
    void shouldMoveMotherNatureAcrossMultipleMergedIslands() {
        GameState state = new GameState(2, false);

        Board board = new Board(Tower.BLACK, 1, 1);
        Player player = new Player(Wizard.WITCH, "test", List.of(Assistant.values()), board);
        state.addPlayer(player);
        state.setPlayerQueue(List.of(player.getId()));
        state.setCurrentTurn(0);

        player.playAssistant(Assistant.FOX);

        Island island1 = new Island(0, 2);
        Island island2 = new Island(2, 2);
        Island island3 = new Island(4, 6);
        state.setIslands(List.of(island1, island2, island3));

        state.setMotherNaturePosition(0);
        GameOperations.moveMotherNature(state, 3);
        assertEquals(0, state.getMotherNaturePosition());
    }

    @Test
    void islandMergeShouldSetTheCorrectDimension1() {
        Island island1 = new Island(0, 1);
        Island island2 = new Island(1, 1);
        island1.receiveTower(Tower.BLACK);
        island2.receiveTower(Tower.BLACK);
        assertEquals(2, GameOperations.mergeIslands(island1, island2).getDimension());
    }

    @Test
    void islandMergeShouldSetTheCorrectDimension2() {
        Island island1 = new Island(0, 1);
        Island island2 = new Island(1, 4);
        island1.receiveTower(Tower.BLACK);
        island2.receiveTower(Tower.BLACK);
        assertEquals(5, GameOperations.mergeIslands(island1, island2).getDimension());
    }

    @Test
    void islandMergeShouldSetTheCorrectPosition1() {
        Island island1 = new Island(0, 1);
        Island island2 = new Island(1, 1);
        island1.receiveTower(Tower.BLACK);
        island2.receiveTower(Tower.BLACK);
        assertEquals(0, GameOperations.mergeIslands(island1, island2).getPosition());
    }

    /**
     * Try also with first and last island
     */
    @Test
    void islandMergeShouldSetTheCorrectPosition2() {
        Island island1 = new Island(0, 1);
        Island island2 = new Island(11, 1);
        island1.receiveTower(Tower.BLACK);
        island2.receiveTower(Tower.BLACK);
        assertEquals(11, GameOperations.mergeIslands(island1, island2).getPosition());
    }

    @Test
    void shouldThrowExceptionWhenMergingNonAdjacentIslands() {
        Island island1 = new Island(0, 1);
        Island island2 = new Island(10, 1);
        island1.receiveTower(Tower.BLACK);
        island2.receiveTower(Tower.BLACK);
        assertThrows(IllegalMoveException.class, () ->
                GameOperations.mergeIslands(island1, island2)
        );
    }

    @Test
    void shouldThrowExceptionWhenMergingIslandsWithNoTowers() {
        Island island1 = new Island(0, 1);
        Island island2 = new Island(1, 1);
        island1.receiveTower(Tower.BLACK);
        assertThrows(TowerNotSetException.class, () ->
                GameOperations.mergeIslands(island1, island2)
        );
    }

    @Test
    void shouldThrowExceptionWhenMergingIslandsWithDifferentTowers() {
        Island island1 = new Island(0, 1);
        Island island2 = new Island(1, 1);
        island1.receiveTower(Tower.BLACK);
        island2.receiveTower(Tower.WHITE);
        assertThrows(IllegalMoveException.class, () ->
                GameOperations.mergeIslands(island1, island2)
        );
    }

    @Test
    void threeIslandsMergeShouldSetTheCorrectPosition1() {
        Island island1 = new Island(0, 1);
        Island island2 = new Island(1, 1);
        Island island3 = new Island(2, 1);
        island1.receiveTower(Tower.WHITE);
        island2.receiveTower(Tower.WHITE);
        island3.receiveTower(Tower.WHITE);
        Island island = GameOperations.mergeIslands(island1, island2, island3);
        assertEquals(0, island.getPosition());
    }

    @Test
    void threeIslandsMergeShouldSetTheCorrectPosition2() {
        Island island1 = new Island(0, 1);
        Island island2 = new Island(1, 1);
        Island island3 = new Island(11, 1);
        island1.receiveTower(Tower.WHITE);
        island2.receiveTower(Tower.WHITE);
        island3.receiveTower(Tower.WHITE);
        Island island = GameOperations.mergeIslands(island1, island2, island3);
        assertEquals(11, island.getPosition());
    }

    @Test
    void threeIslandsMergeShouldSetTheCorrectDimension1() {
        Island island1 = new Island(0, 1);
        Island island2 = new Island(1, 1);
        Island island3 = new Island(11, 1);
        island1.receiveTower(Tower.WHITE);
        island2.receiveTower(Tower.WHITE);
        island3.receiveTower(Tower.WHITE);
        Island island = GameOperations.mergeIslands(island1, island2, island3);
        assertEquals(3, island.getDimension());
    }

    @Test
    void threeIslandsMergeShouldSetTheCorrectDimension2() {
        Island island1 = new Island(0, 1);
        Island island2 = new Island(1, 4);
        Island island3 = new Island(11, 1);
        island1.receiveTower(Tower.WHITE);
        island2.receiveTower(Tower.WHITE);
        island3.receiveTower(Tower.WHITE);
        Island island = GameOperations.mergeIslands(island1, island2, island3);
        assertEquals(6, island.getDimension());
    }

    @Test
    void threeIslandsMergeShouldWorkWithArgumentsInDifferentOrders() {
        Island island1 = new Island(1, 4);
        Island island2 = new Island(11, 1);
        Island island3 = new Island(0, 1);
        island1.receiveTower(Tower.WHITE);
        island2.receiveTower(Tower.WHITE);
        island3.receiveTower(Tower.WHITE);
        Island island = GameOperations.mergeIslands(island1, island2, island3);

        assertEquals(11, island.getPosition());
        assertEquals(6, island.getDimension());
    }

    @Test
    void shouldReturnAllTheMergeableIslands1() {
        Island island1 = new Island(0, 1);
        Island island2 = new Island(1, 1);
        Island island3 = new Island(2, 1);
        Island island4 = new Island(11, 1);
        island1.receiveTower(Tower.BLACK);
        island2.receiveTower(Tower.BLACK);
        island3.receiveTower(Tower.BLACK);
        island4.receiveTower(Tower.BLACK);

        List<Island> islands = List.of(
                island1, island2, island3, island4
        );
        assertEquals(islands, GameOperations.getMergeableIslands(islands));
    }

    @Test
    void shouldReturnAllTheMergeableIslands2() {
        Island island1 = new Island(0, 1);
        Island island2 = new Island(1, 1);
        Island island3 = new Island(4, 1);
        Island island4 = new Island(11, 1);
        island1.receiveTower(Tower.BLACK);
        island2.receiveTower(Tower.BLACK);
        island3.receiveTower(Tower.BLACK);
        island4.receiveTower(Tower.BLACK);

        List<Island> islands = List.of(
                island1, island2, island3, island4
        );
        List<Island> expected = List.of(
                island1, island2, island4
        );
        assertEquals(expected, GameOperations.getMergeableIslands(islands));
    }

    @Test
    void shouldReturnEmptyListWhenThereAreNoMergeableIslands1() {
        Island island1 = new Island(0, 1);
        island1.receiveTower(Tower.BLACK);

        List<Island> islands = List.of(island1);
        assertTrue(GameOperations.getMergeableIslands(islands).isEmpty());
    }

    @Test
    void shouldReturnEmptyListWhenThereAreNoMergeableIslands2() {
        Island island1 = new Island(0, 1);
        Island island2 = new Island(4, 1);
        Island island3 = new Island(10, 1);
        island1.receiveTower(Tower.BLACK);
        island2.receiveTower(Tower.BLACK);
        island3.receiveTower(Tower.BLACK);

        List<Island> islands = List.of(
                island1, island2, island3
        );
        assertTrue(GameOperations.getMergeableIslands(islands).isEmpty());
    }

    @Test
    void shouldFindAndMergeTwoIslands() {
        GameState state = new GameState(2, false);
        Island island1 = new Island(0, 1);
        Island island2 = new Island(1, 1);
        island1.receiveTower(Tower.BLACK);
        island2.receiveTower(Tower.BLACK);
        state.setIslands(List.of(island1, island2));

        assertEquals(2, state.getIslands().size());
        GameOperations.mergeIslands(state);
        assertEquals(1, state.getIslands().size());
    }

    @Test
    void shouldFindAndMergeThreeIslands() {
        GameState state = new GameState(2, false);
        Island island1 = new Island(0, 1);
        Island island2 = new Island(1, 1);
        Island island3 = new Island(2, 1);
        island1.receiveTower(Tower.BLACK);
        island2.receiveTower(Tower.BLACK);
        island3.receiveTower(Tower.BLACK);
        state.setIslands(List.of(island1, island2, island3));

        assertEquals(3, state.getIslands().size());
        GameOperations.mergeIslands(state);
        assertEquals(1, state.getIslands().size());
    }

    @Test
    void shouldThrowExceptionWhenMergingMoreThanThreeIslands() {
        GameState state = new GameState(2, false);
        Island island1 = new Island(0, 1);
        Island island2 = new Island(1, 1);
        Island island3 = new Island(2, 1);
        Island island4 = new Island(3, 1);
        island1.receiveTower(Tower.BLACK);
        island2.receiveTower(Tower.BLACK);
        island3.receiveTower(Tower.BLACK);
        island4.receiveTower(Tower.BLACK);
        state.setIslands(List.of(island1, island2, island3, island4));

        assertEquals(4, state.getIslands().size());
        assertThrows(IllegalMoveException.class, () ->
                GameOperations.mergeIslands(state)
        );
    }

    @Test
    void mergingIslandsShouldMoveTheBlocksOnTheNewIsland() {
        GameState state = new GameState(2, true);
        Island island1 = new Island(0, 1);
        Island island2 = new Island(1, 1);
        Island island3 = new Island(2, 1);
        island1.receiveTower(Tower.BLACK);
        island2.receiveTower(Tower.BLACK);
        island3.receiveTower(Tower.BLACK);

        List<Island> islands = List.of(island1, island2, island3);
        state.setIslands(islands);
        state.getExpertAttrs().setBlockedIslands(islands);

        GameOperations.mergeIslands(state);

        List<Island> expected = List.of(island1, island1, island1);
        assertEquals(expected, state.getExpertAttrs().getBlockedIslands());
    }

    @Test
    void shouldNotRemoveBlocksFromIslandsThatAreNotMerged() {
        GameState state = new GameState(2, true);

        Island island1 = new Island(0, 1);
        Island island2 = new Island(1, 1);
        Island island3 = new Island(2, 1);
        island1.receiveTower(Tower.BLACK);
        island2.receiveTower(Tower.BLACK);

        List<Island> islands = List.of(island1, island2);
        state.setIslands(islands);

        List<Island> blocked = List.of(island3);
        state.getExpertAttrs().setBlockedIslands(blocked);

        GameOperations.mergeIslands(state);
        assertEquals(blocked, state.getExpertAttrs().getBlockedIslands());
    }

    @Test
    void shouldNotGiveProfToAnyone() {
        GameState state = new GameState(2, false);

        Board board1 = new Board(Tower.BLACK, 1, 1);
        Player player1 = new Player(Wizard.WITCH, "p1", new ArrayList<>(), board1);

        Board board2 = new Board(Tower.WHITE, 1, 1);
        Player player2 = new Player(Wizard.KING, "p2", new ArrayList<>(), board2);

        state.addPlayer(player1);
        state.addPlayer(player2);

        GameOperations.updateProfessorsOwners(state);
        for (Student student : Student.values()) {
            assertNull(state.getProfessorOwner(student));
        }
    }

    @Test
    void shouldGiveProfToPlayerWithMostStudentsOfAColor() {
        GameState state = new GameState(2, false);

        Board board1 = new Board(Tower.BLACK, 1, 1);
        Player player1 = new Player(Wizard.WITCH, "p1", new ArrayList<>(), board1);
        player1.getBoard().getHall().receiveStudent(Student.RED);

        Board board2 = new Board(Tower.WHITE, 1, 1);
        Player player2 = new Player(Wizard.KING, "p2", new ArrayList<>(), board2);

        state.addPlayer(player1);
        state.addPlayer(player2);

        GameOperations.updateProfessorsOwners(state);
        assertEquals(player1.getId(), state.getProfessorOwner(Student.RED));
    }

    @Test
    void shouldNotGiveProfToPlayerIfAnotherPlayerHasTheSameNumberOfStudents() {
        GameState state = new GameState(2, false);

        Board board1 = new Board(Tower.BLACK, 1, 1);
        Player player1 = new Player(Wizard.WITCH, "p1", new ArrayList<>(), board1);
        player1.getBoard().getHall().receiveStudent(Student.RED);

        Board board2 = new Board(Tower.WHITE, 1, 1);
        Player player2 = new Player(Wizard.KING, "p2", new ArrayList<>(), board2);
        player2.getBoard().getHall().receiveStudent(Student.RED);

        state.addPlayer(player1);
        state.addPlayer(player2);

        GameOperations.updateProfessorsOwners(state);
        assertNull(state.getProfessorOwner(Student.RED));
    }

    /**
     * When playing the appropriate character is possible to get the ownership on
     * a professor even if another player has the same number of students.
     */
    @Test
    void shouldGiveProfToPlayerEvenIfAnotherPlayerHasTheSameNumberOfStudents() {
        GameState state = new GameState(2, true);

        Board board1 = new Board(Tower.BLACK, 1, 1);
        Player player1 = new Player(Wizard.WITCH, "p1", new ArrayList<>(), board1);
        player1.getBoard().getHall().receiveStudent(Student.RED);

        Board board2 = new Board(Tower.WHITE, 1, 1);
        Player player2 = new Player(Wizard.KING, "p2", new ArrayList<>(), board2);
        player2.getBoard().getHall().receiveStudent(Student.RED);

        state.addPlayer(player1);
        state.addPlayer(player2);

        // player 1 is the current player
        state.setPlayerQueue(List.of(Wizard.WITCH.ordinal(), Wizard.KING.ordinal()));
        state.setCurrentTurn(0);

        state.getExpertAttrs().setProfOwnerOnStudentsTie(true);
        GameOperations.updateProfessorsOwners(state);
        assertEquals(player1.getId(), state.getProfessorOwner(Student.RED));
    }

    @Test
    void shouldThrowExceptionWhenTryingToConquerNonExistingIsland() {
        GameState state = new GameState(2, false);

        Board board1 = new Board(Tower.BLACK, 5, 1);
        Player player1 = new Player(Wizard.WITCH, "p1", new ArrayList<>(), board1);

        Board board2 = new Board(Tower.WHITE, 5, 1);
        Player player2 = new Player(Wizard.KING, "p2", new ArrayList<>(), board2);

        state.addPlayer(player1);
        state.addPlayer(player2);

        Island island = new Island(0, 1);

        player1.getBoard().getHall().receiveStudent(Student.RED);
        island.receiveStudent(Student.RED);
        player2.getBoard().getHall().receiveStudent(Student.GREEN);
        island.receiveStudent(Student.GREEN);

        GameOperations.updateProfessorsOwners(state);

        assertThrows(IllegalMoveException.class, () ->
                GameOperations.updateIslandConqueror(state, island)
        );
    }

    @Test
    void nobodyShouldConquerTheIsland() {
        GameState state = new GameState(2, false);

        Board board1 = new Board(Tower.BLACK, 5, 1);
        Player player1 = new Player(Wizard.WITCH, "p1", new ArrayList<>(), board1);

        Board board2 = new Board(Tower.WHITE, 5, 1);
        Player player2 = new Player(Wizard.KING, "p2", new ArrayList<>(), board2);

        state.addPlayer(player1);
        state.addPlayer(player2);

        Island island = new Island(0, 1);
        state.setIslands(List.of(island));

        player1.getBoard().getHall().receiveStudent(Student.RED);
        island.receiveStudent(Student.RED);
        player2.getBoard().getHall().receiveStudent(Student.GREEN);
        island.receiveStudent(Student.GREEN);

        GameOperations.updateProfessorsOwners(state);
        GameOperations.updateIslandConqueror(state, island);

        assertFalse(island.hasTowers());
    }

    @Test
    void islandShouldBeConquered() {
        GameState state = new GameState(2, false);

        Board board1 = new Board(Tower.BLACK, 5, 1);
        board1.receiveTower(Tower.BLACK);
        Player player1 = new Player(Wizard.WITCH, "p1", new ArrayList<>(), board1);

        Board board2 = new Board(Tower.WHITE, 5, 1);
        board2.receiveTower(Tower.WHITE);
        Player player2 = new Player(Wizard.KING, "p2", new ArrayList<>(), board2);

        state.addPlayer(player1);
        state.addPlayer(player2);

        Island island = new Island(0, 1);
        state.setIslands(List.of(island));

        player1.getBoard().getHall().receiveStudent(Student.RED);
        island.receiveStudent(Student.RED);

        GameOperations.updateProfessorsOwners(state);
        GameOperations.updateIslandConqueror(state, island);

        assertEquals(player1.getBoard().getTowerType(), island.getTowerType());
        assertEquals(0, player1.getBoard().getNumTowers());
    }

    @Test
    void islandShouldBeConqueredByTheTeam1() {
        GameState state = new GameState(4, false);

        // team #1
        Board board1 = new Board(Tower.BLACK, 5, 1);
        board1.receiveTower(Tower.BLACK);
        Player player1 = new Player(Wizard.WITCH, "p1", new ArrayList<>(), board1);

        Board board2 = new Board(Tower.BLACK, 5, 1);
        Player player2 = new Player(Wizard.SAGE, "p1", new ArrayList<>(), board2, false);


        // team #2
        Board board3 = new Board(Tower.WHITE, 5, 1);
        board3.receiveTower(Tower.WHITE);
        Player player3 = new Player(Wizard.KING, "p3", new ArrayList<>(), board3);

        Board board4 = new Board(Tower.BLACK, 5, 1);
        Player player4 = new Player(Wizard.DRUID, "p4", new ArrayList<>(), board4, false);

        state.addPlayer(player1);
        state.addPlayer(player2);
        state.addPlayer(player3);
        state.addPlayer(player4);

        Island island = new Island(0, 1);
        state.setIslands(List.of(island));

        // team #1 has 4 influence points
        player1.getBoard().getHall().receiveStudent(Student.RED);
        island.receiveStudent(Student.RED);
        island.receiveStudent(Student.RED);

        player2.getBoard().getHall().receiveStudent(Student.YELLOW);
        island.receiveStudent(Student.YELLOW);
        island.receiveStudent(Student.YELLOW);

        // team #2 has 3 influence points
        player3.getBoard().getHall().receiveStudent(Student.GREEN);
        island.receiveStudent(Student.GREEN);
        island.receiveStudent(Student.GREEN);
        island.receiveStudent(Student.GREEN);

        GameOperations.updateProfessorsOwners(state);
        GameOperations.updateIslandConqueror(state, island);

        assertEquals(player1.getBoard().getTowerType(), island.getTowerType());
        assertEquals(0, player1.getBoard().getNumTowers());
    }

    @Test
    void islandShouldBeConqueredAndThenConqueredByAnotherPlayer() {
        GameState state = new GameState(2, false);

        Player player1 = TestUtils.createPlayer("p1", Wizard.WITCH, Tower.BLACK);
        Player player2 = TestUtils.createPlayer("p2", Wizard.KING, Tower.WHITE);

        player1.getBoard().receiveTower(Tower.BLACK);
        player2.getBoard().receiveTower(Tower.WHITE);

        state.addPlayer(player1);
        state.addPlayer(player2);

        Island island = new Island(0, 1);
        island.receiveStudent(Student.RED);
        island.receiveStudent(Student.RED);
        state.setIslands(List.of(island));

        player1.getBoard().getHall().receiveStudent(Student.RED);

        // player 1 have the red professor and more influence ==> he conquers the island
        GameOperations.updateProfessorsOwners(state);
        GameOperations.updateIslandConqueror(state, island);

        player2.getBoard().getHall().receiveStudent(Student.RED);
        player2.getBoard().getHall().receiveStudent(Student.RED);

        // player 1: loose red prof and has influence=1 given by the tower on the island
        // player 2: takes Red Prof and has influence=2 given by red students on the island
        GameOperations.updateProfessorsOwners(state);
        GameOperations.updateIslandConqueror(state, island);

        assertEquals(player2.getBoard().getTowerType(), island.getTowerType());
        assertEquals(1, player1.getBoard().getNumTowers());
    }

    /**
     * When there are three players and player 1 has conquered the island and player 2
     * and player 3 have the same influence on the island, the island remains conquered
     * by player 1 even if player 2 & 3 have more influence.
     */
    @Test
    void islandShouldNotBeConqueredSpecialCase() {
        GameState state = new GameState(3, false);

        Player player1 = TestUtils.createPlayer("p1", Wizard.WITCH, Tower.BLACK);
        Player player2 = TestUtils.createPlayer("p2", Wizard.KING, Tower.WHITE);
        Player player3 = TestUtils.createPlayer("p3", Wizard.SAGE, Tower.GREY);

        player1.getBoard().receiveTower(Tower.BLACK);
        player2.getBoard().receiveTower(Tower.WHITE);
        player3.getBoard().receiveTower(Tower.GREY);

        state.addPlayer(player1);
        state.addPlayer(player2);
        state.addPlayer(player3);

        Island island = new Island(0, 1);
        island.receiveStudent(Student.RED);
        island.receiveStudent(Student.RED);
        island.receiveStudent(Student.GREEN);
        island.receiveStudent(Student.GREEN);
        state.setIslands(List.of(island));

        player1.getBoard().getHall().receiveStudent(Student.RED);

        // player 1 have the red professor and more influence ==> he conquers the island
        GameOperations.updateProfessorsOwners(state);
        GameOperations.updateIslandConqueror(state, island);

        player2.getBoard().getHall().receiveStudent(Student.RED);
        player2.getBoard().getHall().receiveStudent(Student.RED);

        player3.getBoard().getHall().receiveStudent(Student.GREEN);

        // player 1: no profs         => influence = 1 given by the tower on the island
        // player 2: takes red Prof   => influence = 2 given by red students on the island
        // player 3: takes green prof => influence = 2 given by green students on the island
        GameOperations.updateProfessorsOwners(state);
        GameOperations.updateIslandConqueror(state, island);

        assertEquals(player1.getBoard().getTowerType(), island.getTowerType());
        assertEquals(0, player1.getBoard().getNumTowers());
    }

    /**
     * When ignore student effect is active it should change the way influence is
     * calculated. In this test both players should have the same influence even if
     * one has the control of the red professor and so no one can conquer the island.
     */
    @Test
    void islandShouldNotBeConqueredIgnoreStudentEffect() {
        GameState state = new GameState(2, true);

        Player player1 = TestUtils.createPlayer("p1", Wizard.WITCH, Tower.BLACK);
        Player player2 = TestUtils.createPlayer("p2", Wizard.KING, Tower.WHITE);

        player1.getBoard().receiveTower(Tower.BLACK);
        player2.getBoard().receiveTower(Tower.WHITE);

        state.addPlayer(player1);
        state.addPlayer(player2);
        state.setCurrentTurn(0);
        state.setPlayerQueue(List.of(player1.getId(), player2.getId()));

        Island island = new Island(0, 1);
        island.receiveStudent(Student.RED);
        island.receiveStudent(Student.RED);
        state.setIslands(List.of(island));

        player1.getBoard().getHall().receiveStudent(Student.RED);
        state.getExpertAttrs().setIgnoredStudent(Student.RED);

        // player 2 has the red prof but both players have the same influence
        // because ignore student is active
        GameOperations.updateProfessorsOwners(state);
        GameOperations.updateIslandConqueror(state, island);

        assertFalse(island.hasTowers());
    }

    /**
     * When the island is conquered by player 1 and player 2 has the same influence
     * on the island, if ignore tower effect is active player 2 conquers the island.
     */
    @Test
    void islandShouldBeConqueredAndThenConqueredByAnotherPlayerIgnoreTower() {
        GameState state = new GameState(2, true);

        Player player1 = TestUtils.createPlayer("p1", Wizard.WITCH, Tower.BLACK);
        Player player2 = TestUtils.createPlayer("p2", Wizard.KING, Tower.WHITE);

        player1.getBoard().receiveTower(Tower.BLACK);
        player2.getBoard().receiveTower(Tower.WHITE);

        state.addPlayer(player1);
        state.addPlayer(player2);
        state.setCurrentTurn(0);
        state.setPlayerQueue(List.of(player1.getId(), player2.getId()));

        Island island = new Island(0, 1);
        island.receiveStudent(Student.RED);
        state.setIslands(List.of(island));

        player1.getBoard().getHall().receiveStudent(Student.RED);

        GameOperations.updateProfessorsOwners(state);
        GameOperations.updateIslandConqueror(state, island);

        player2.getBoard().getHall().receiveStudent(Student.RED);
        player2.getBoard().getHall().receiveStudent(Student.RED);

        state.getExpertAttrs().setIgnoreTowers(true);

        // player 1: has influence=0 because his tower doesn't give any point
        // player 2: takes red prof and has influence=1 given by red student on the island
        GameOperations.updateProfessorsOwners(state);
        GameOperations.updateIslandConqueror(state, island);

        assertEquals(player2.getBoard().getTowerType(), island.getTowerType());
        assertEquals(1, player1.getBoard().getNumTowers());
    }

    @Test
    void shouldReturnBlockToCharacterIfIslandIsBlocked() {
        GameState state = new GameState(2, true);

        Island island = new Island(0, 1);
        state.setIslands(List.of(island));
        state.getExpertAttrs().addBlockToIsland(island);

        Character character = Characters.get(CharacterType.CIRCE);
        character.pickBlock();
        state.getExpertAttrs().addCharacter(character);

        GameOperations.updateIslandConqueror(state, island);

        assertEquals(4, character.getNumIslandBlocks());
        assertTrue(state.getExpertAttrs().getBlockedIslands().isEmpty());
    }

    @Test
    void shouldNotCalculateInfluenceIfIslandIsBlocked() {
        GameState state = new GameState(2, true);

        Player player1 = TestUtils.createPlayer("p1", Wizard.WITCH, Tower.BLACK);
        Player player2 = TestUtils.createPlayer("p2", Wizard.KING, Tower.WHITE);

        player1.getBoard().receiveTower(Tower.BLACK);
        player2.getBoard().receiveTower(Tower.WHITE);

        state.addPlayer(player1);
        state.addPlayer(player2);

        player1.getBoard().getHall().receiveStudent(Student.RED);

        Island island = new Island(0, 1);
        island.receiveStudent(Student.RED);
        island.receiveStudent(Student.RED);
        state.setIslands(List.of(island));

        // player 1 have the red professor and more influence
        GameOperations.updateProfessorsOwners(state);

        Character character = Characters.get(CharacterType.CIRCE);
        character.pickBlock();
        state.getExpertAttrs().addCharacter(character);
        state.getExpertAttrs().addBlockToIsland(island);

        GameOperations.updateIslandConqueror(state, island);

        // the island is blocked so player 1 cannot conquer it
        assertFalse(island.hasTowers());
    }

    @Test
    void gameShouldBeOverWhenItsAlreadyGameOver() {
        GameState state = new GameState(2, false);

        Player player1 = TestUtils.createPlayer("p1", Wizard.WITCH, Tower.BLACK);
        Player player2 = TestUtils.createPlayer("p2", Wizard.KING, Tower.WHITE);

        player1.getBoard().receiveTower(Tower.BLACK);
        player2.getBoard().receiveTower(Tower.WHITE);

        state.addPlayer(player1);
        state.addPlayer(player2);

        Island island1 = new Island(0, 1);
        Island island2 = new Island(1, 1);
        Island island3 = new Island(2, 1);
        Island island4 = new Island(3, 1);
        state.setIslands(List.of(island1, island2, island3, island4));

        state.setStage(Stage.GAME_OVER);

        assertTrue(GameOperations.isGameOver(state));
    }

    @Test
    void gameShouldBeOverWhenThereAreLessThanThreeIslands() {
        GameState state = new GameState(2, false);

        Player player1 = TestUtils.createPlayer("p1", Wizard.WITCH, Tower.BLACK);
        Player player2 = TestUtils.createPlayer("p2", Wizard.KING, Tower.WHITE);

        player1.getBoard().receiveTower(Tower.BLACK);
        player2.getBoard().receiveTower(Tower.WHITE);

        state.addPlayer(player1);
        state.addPlayer(player2);

        Island island = new Island(0, 1);
        state.setIslands(List.of(island));

        assertTrue(GameOperations.isGameOver(state));
    }

    @Test
    void gameShouldBeOverWhenOnePlayerHasPlacedAllTheTowers() {
        GameState state = new GameState(2, false);

        Player player1 = TestUtils.createPlayer("p1", Wizard.WITCH, Tower.BLACK);
        Player player2 = TestUtils.createPlayer("p2", Wizard.KING, Tower.WHITE);

        player1.getBoard().receiveTower(Tower.BLACK);

        state.addPlayer(player1);
        state.addPlayer(player2);

        Island island1 = new Island(0, 1);
        Island island2 = new Island(1, 1);
        Island island3 = new Island(2, 1);
        Island island4 = new Island(3, 1);
        state.setIslands(List.of(island1, island2, island3, island4));

        assertTrue(GameOperations.isGameOver(state));
    }

    @Test
    void gameShouldNotBeOverWhenNoneOfTheConditionsIsMet() {
        GameState state = new GameState(2, false);

        Player player1 = TestUtils.createPlayer("p1", Wizard.WITCH, Tower.BLACK);
        Player player2 = TestUtils.createPlayer("p2", Wizard.KING, Tower.WHITE);

        player1.getBoard().receiveTower(Tower.BLACK);
        player2.getBoard().receiveTower(Tower.WHITE);

        state.addPlayer(player1);
        state.addPlayer(player2);

        Island island1 = new Island(0, 1);
        Island island2 = new Island(1, 1);
        Island island3 = new Island(2, 1);
        Island island4 = new Island(3, 1);
        state.setIslands(List.of(island1, island2, island3, island4));

        assertFalse(GameOperations.isGameOver(state));
    }

    @Test
    void shouldThrowExceptionWhenGettingWinnerWhenTheGameIsNotOver() {
        GameState state = new GameState(2, false);

        Player player1 = TestUtils.createPlayer("p1", Wizard.WITCH, Tower.BLACK);
        Player player2 = TestUtils.createPlayer("p2", Wizard.KING, Tower.WHITE);

        player1.getBoard().receiveTower(Tower.BLACK);
        player2.getBoard().receiveTower(Tower.WHITE);

        state.addPlayer(player1);
        state.addPlayer(player2);

        Island island1 = new Island(0, 1);
        Island island2 = new Island(1, 1);
        Island island3 = new Island(2, 1);
        Island island4 = new Island(3, 1);
        state.setIslands(List.of(island1, island2, island3, island4));

        assertThrows(GameNotOverException.class, () ->
                GameOperations.getWinner(state)
        );
    }

    @Test
    void winnerShouldBeThePlayerWithMoreTowersOnTheIslands() {
        GameState state = new GameState(2, false);

        Player player1 = TestUtils.createPlayer("p1", Wizard.WITCH, Tower.BLACK);
        Player player2 = TestUtils.createPlayer("p2", Wizard.KING, Tower.WHITE);

        state.addPlayer(player1);
        state.addPlayer(player2);

        Island island1 = new Island(0, 1);
        Island island2 = new Island(1, 1);
        Island island3 = new Island(2, 1);
        Island island4 = new Island(3, 1);
        state.setIslands(List.of(island1, island2, island3, island4));

        island1.receiveTower(Tower.WHITE);

        Tower tower = GameOperations.getWinner(state);
        assertEquals(player2.getBoard().getTowerType(), tower);
    }

    @Test
    void winnerShouldBeThePlayerWithMoreProfWhenThereIsATieOnTowers() {
        GameState state = new GameState(2, false);

        Player player1 = TestUtils.createPlayer("p1", Wizard.WITCH, Tower.BLACK);
        Player player2 = TestUtils.createPlayer("p2", Wizard.KING, Tower.WHITE);

        player1.getBoard().getHall().receiveStudent(Student.RED);

        state.addPlayer(player1);
        state.addPlayer(player2);

        Island island1 = new Island(0, 1);
        Island island2 = new Island(1, 1);
        Island island3 = new Island(2, 1);
        Island island4 = new Island(3, 1);
        state.setIslands(List.of(island1, island2, island3, island4));

        GameOperations.updateProfessorsOwners(state);
        Tower tower = GameOperations.getWinner(state);
        assertEquals(player1.getBoard().getTowerType(), tower);
    }

    @Test
    void winnerShouldBeTheTeamWithMoreTowersOnIslands() {
        GameState state = new GameState(4, false);

        Player player1 = TestUtils.createPlayer("p1", Wizard.WITCH, Tower.BLACK);
        Player player2 = TestUtils.createPlayer("p2", Wizard.KING, Tower.BLACK);
        Player player3 = TestUtils.createPlayer("p3", Wizard.DRUID, Tower.WHITE);
        Player player4 = TestUtils.createPlayer("p4", Wizard.SAGE, Tower.WHITE);

        state.addPlayer(player1);
        state.addPlayer(player2);
        state.addPlayer(player3);
        state.addPlayer(player4);

        Island island1 = new Island(0, 1);
        Island island2 = new Island(1, 1);
        Island island3 = new Island(2, 1);
        Island island4 = new Island(3, 1);
        state.setIslands(List.of(island1, island2, island3, island4));

        island2.receiveTower(Tower.WHITE);
        island3.receiveTower(Tower.WHITE);
        island4.receiveTower(Tower.BLACK);

        Tower tower = GameOperations.getWinner(state);
        assertEquals(player3.getBoard().getTowerType(), tower);
        assertEquals(player4.getBoard().getTowerType(), tower);
    }

    @Test
    void shouldBeATieIfPlayersHaveTheNumberOfTowersOnIslandAndProfOnHall() {
        GameState state = new GameState(2, false);

        Player player1 = TestUtils.createPlayer("p1", Wizard.WITCH, Tower.BLACK);
        Player player2 = TestUtils.createPlayer("p2", Wizard.KING, Tower.WHITE);

        player1.getBoard().getHall().receiveStudent(Student.RED);
        player2.getBoard().getHall().receiveStudent(Student.GREEN);

        state.addPlayer(player1);
        state.addPlayer(player2);

        Island island1 = new Island(0, 1);
        Island island2 = new Island(1, 1);
        Island island3 = new Island(2, 1);
        Island island4 = new Island(3, 1);
        state.setIslands(List.of(island1, island2, island3, island4));

        GameOperations.updateProfessorsOwners(state);
        assertNull(GameOperations.getWinner(state));
    }

    @Test
    void shouldBeATieIfTeamsHaveTheNumberOfTowersOnIslandAndProfOnHall() {
        GameState state = new GameState(4, false);

        Player player1 = TestUtils.createPlayer("p1", Wizard.WITCH, Tower.BLACK);
        Player player2 = TestUtils.createPlayer("p2", Wizard.KING, Tower.BLACK);
        Player player3 = TestUtils.createPlayer("p3", Wizard.DRUID, Tower.WHITE);
        Player player4 = TestUtils.createPlayer("p4", Wizard.SAGE, Tower.WHITE);

        state.addPlayer(player1);
        state.addPlayer(player2);
        state.addPlayer(player3);
        state.addPlayer(player4);

        Island island1 = new Island(0, 1);
        Island island2 = new Island(1, 1);
        Island island3 = new Island(2, 1);
        Island island4 = new Island(3, 1);
        state.setIslands(List.of(island1, island2, island3, island4));

        // 2 professors for each team
        player1.getBoard().getHall().receiveStudent(Student.RED);
        player2.getBoard().getHall().receiveStudent(Student.GREEN);
        player3.getBoard().getHall().receiveStudent(Student.YELLOW);
        player4.getBoard().getHall().receiveStudent(Student.CYAN);

        // 2 towers for each team
        island3.receiveTower(Tower.WHITE);
        island4.receiveTower(Tower.BLACK);

        GameOperations.updateProfessorsOwners(state);
        assertNull(GameOperations.getWinner(state));
    }

    @Test
    void shouldNotReturnAssistantsPlayedByOtherPlayers() {
        GameState state = new GameState(2, false);

        Player player1 = TestUtils.createPlayer("p1", Wizard.WITCH, Tower.BLACK);
        Player player2 = TestUtils.createPlayer("p2", Wizard.KING, Tower.WHITE);

        state.addPlayer(player1);
        state.addPlayer(player2);

        state.setPlayerQueue(List.of(player1.getId(), player2.getId()));
        player1.playAssistant(Assistant.TURTLE);
        state.nextTurn();

        List<Assistant> assistants = GameOperations.getPlayableAssistants(state);
        assertFalse(assistants.contains(Assistant.TURTLE));
    }

    @Test
    void shouldReturnAssistantsPlayedByOtherPlayersOnlyIfNoOtherAssistantsCanBePlayed() {
        GameState state = new GameState(2, false);

        Player player1 = TestUtils.createPlayer("p1", Wizard.WITCH, Tower.BLACK);
        Player player2 = TestUtils.createPlayer("p2", Wizard.KING, Tower.WHITE, List.of(Assistant.TURTLE));

        state.addPlayer(player1);
        state.addPlayer(player2);

        state.setPlayerQueue(List.of(player1.getId(), player2.getId()));
        player1.playAssistant(Assistant.TURTLE);
        state.nextTurn();

        List<Assistant> assistants = GameOperations.getPlayableAssistants(state);
        assertTrue(assistants.contains(Assistant.TURTLE));
    }

}