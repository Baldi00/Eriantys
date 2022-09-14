package it.polimi.ingsw.models.components.characters;

import it.polimi.ingsw.models.TestUtils;
import it.polimi.ingsw.models.components.*;
import it.polimi.ingsw.models.components.Board;
import it.polimi.ingsw.models.components.characters.effects.Effect;
import it.polimi.ingsw.models.components.characters.effects.EffectArgs;
import it.polimi.ingsw.models.components.characters.effects.Effects;
import it.polimi.ingsw.models.components.interfaces.StudentReceiver;
import it.polimi.ingsw.models.exceptions.IllegalMoveException;
import it.polimi.ingsw.models.operations.GameOperations;
import it.polimi.ingsw.models.state.GameState;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for different character cards effects.
 */
class EffectsTest {

    private static final Effect DO_NOTHING_EFFECT = new Effect() {
        @Override
        protected void effect(){}
    };

    /** Dummy character with no effects, cost = 0 and the specified parameters */
    private static Character createDummyCharacter(int studentsLimit, int islandBlocks) {
        return new Character(CharacterType.DAIRYMAN, 0, DO_NOTHING_EFFECT, studentsLimit, islandBlocks);
    }

    /**
     * Character card effect:
     * block the selected island
     */
    @Test
    void shouldBlockIsland() {
        GameState state = new GameState(2, true);

        Character character = createDummyCharacter(0, 1);
        state.getExpertAttrs().addCharacter(character);

        Island island = new Island(0, 1);
        state.setIslands(List.of(island));

        EffectArgs effectArgs = new EffectArgs.Builder()
                .setGameState(state)
                .setIsland(island)
                .setCharacter(character)
                .build();

        Effects.BLOCK_ISLAND.performEffect(effectArgs);
        assertTrue(state.getExpertAttrs().getBlockedIslands().contains(island));
    }

    @Test
    void shouldThrowExceptionIfBlockedIslandDoesNotExist() {
        GameState state = new GameState(2, true);

        Character character = createDummyCharacter(0, 1);
        state.getExpertAttrs().addCharacter(character);

        Island island = new Island(0, 1);
        // island is not added to the game state

        EffectArgs effectArgs = new EffectArgs.Builder()
                .setGameState(state)
                .setIsland(island)
                .setCharacter(character)
                .build();

        assertThrows(IllegalMoveException.class, () ->
                Effects.BLOCK_ISLAND.performEffect(effectArgs)
        );
    }

    @Test
    void shouldThrowExceptionWhenTheGivenCharacterDoesNotExist() {
        GameState state = new GameState(2, true);

        // this character is not added to the game
        Character character = createDummyCharacter(0, 1);

        Island island = new Island(0, 1);
        state.setIslands(List.of(island));

        EffectArgs effectArgs = new EffectArgs.Builder()
                .setGameState(state)
                .setIsland(island)
                .setCharacter(character)
                .build();

        assertThrows(IllegalMoveException.class, () ->
                Effects.BLOCK_ISLAND.performEffect(effectArgs)
        );
    }

    /**
     * Character card effect:
     * immediately calculate influence on a given island
     */
    @Test
    void shouldCalculateInfluenceOnTheGivenIsland() {
        GameState state = new GameState(2, true);

        Island island = new Island(0, 1);
        fillWithStudents(island, Student.RED, 4);
        state.setIslands(List.of(island));

        Board board = new Board(Tower.BLACK, 1, 1);
        board.receiveTower(Tower.BLACK);
        Player player = new Player(Wizard.WITCH, "test", List.of(Assistant.values()), board);
        player.getBoard().getHall().receiveStudent(Student.RED);

        state.addPlayer(player);
        state.setCurrentTurn(0);
        state.setPlayerQueue(List.of(player.getId()));

        EffectArgs effectArgs = new EffectArgs.Builder()
                .setGameState(state)
                .setIsland(island)
                .build();

        GameOperations.updateProfessorsOwners(state);
        Effects.CALCULATE_INFLUENCE_ON_ISLAND.performEffect(effectArgs);

        assertEquals(player.getBoard().getTowerType(), island.getTowerType());
    }

    /**
     * Character card effect:
     * set a student color to ignore when influence is calculated
     */
    @Test
    void shouldSetTheStudentColorToIgnoreDuringCalculatingInfluence() {
        GameState state = new GameState(2, true);

        EffectArgs effectArgs = new EffectArgs.Builder()
                .setGameState(state)
                .setStudent(Student.RED)
                .build();

        Effects.IGNORE_STUDENT_COLOR.performEffect(effectArgs);
        assertEquals(Student.RED, state.getExpertAttrs().getIgnoredStudentType());
    }

    /**
     * Character card effect:
     * set a tower color to ignore when influence is calculated
     */
    @Test
    void shouldSetTheTowerToIgnoreDuringCalculatingInfluence() {
        GameState state = new GameState(2, true);

        EffectArgs effectArgs = new EffectArgs.Builder()
                .setGameState(state)
                .build();

        Effects.IGNORE_TOWERS.performEffect(effectArgs);
        assertTrue(state.getExpertAttrs().isIgnoreTowers());
    }

    /**
     * Character card effect:
     * move the selected student from this card to the hall of the player
     */
    @Test
    void shouldMoveTheSelectedStudentFromThisCharacterToTheHall() {
        GameState state = new GameState(2, true);

        Character character = createDummyCharacter(1, 0);
        character.receiveStudent(Student.RED);

        Board board = new Board(Tower.BLACK, 1, 1);
        Player player = new Player(Wizard.WITCH, "test", List.of(Assistant.values()), board);
        state.addPlayer(player);
        state.setCurrentTurn(0);
        state.setPlayerQueue(List.of(player.getId()));

        EffectArgs effectArgs = new EffectArgs.Builder()
                .setGameState(state)
                .setStudent(Student.RED)
                .setCharacter(character)
                .build();

        Effects.ONE_STUDENT_TO_HALL.performEffect(effectArgs);
        assertEquals(1, player.getBoard().getHall().getNumStudentsByColor(Student.RED));
    }

    @Test
    void shouldThrowExceptionWhenHallCannotReceivesStudentFromCharacter() {
        GameState state = new GameState(2, true);

        Character character = createDummyCharacter(1, 0);
        character.receiveStudent(Student.RED);

        Board board = new Board(Tower.BLACK, 1, 1);
        fillWithStudents(board.getHall(), Student.RED, 10);

        Player player = new Player(Wizard.WITCH, "test", List.of(Assistant.values()), board);
        state.addPlayer(player);
        state.setCurrentTurn(0);
        state.setPlayerQueue(List.of(player.getId()));

        EffectArgs effectArgs = new EffectArgs.Builder()
                .setGameState(state)
                .setStudent(Student.RED)
                .setCharacter(character)
                .build();

        assertThrows(IllegalMoveException.class, () ->
                Effects.ONE_STUDENT_TO_HALL.performEffect(effectArgs)
        );
    }

    /**
     * Character card effect:
     * move the selected student from this card to the island selected by the player
     */
    @Test
    void shouldMoveTheSelectedStudentFromCharacterToTheSelectedIsland() {
        GameState state = new GameState(2, true);

        Character character = createDummyCharacter(1, 0);
        character.receiveStudent(Student.RED);

        Island island = new Island(0, 1);
        state.setIslands(List.of(island));

        EffectArgs effectArgs = new EffectArgs.Builder()
                .setGameState(state)
                .setStudent(Student.RED)
                .setIsland(island)
                .setCharacter(character)
                .build();

        Effects.ONE_STUDENT_TO_ISLAND.performEffect(effectArgs);
        assertEquals(1, island.getNumStudent(Student.RED));
    }

    @Test
    void shouldDrawStudentFromBagAfterStudentHaveBeenMovedToIsland() {
        GameState state = new GameState(2, true);

        state.getBag().receiveStudent(Student.RED);

        Character character = createDummyCharacter(1, 0);
        character.receiveStudent(Student.RED);

        Island island = new Island(0, 1);
        state.setIslands(List.of(island));

        EffectArgs effectArgs = new EffectArgs.Builder()
                .setGameState(state)
                .setStudent(Student.RED)
                .setIsland(island)
                .setCharacter(character)
                .build();

        Effects.ONE_STUDENT_TO_ISLAND.performEffect(effectArgs);
        assertEquals(1, character.getStudents().size());
    }

    @Test
    void shouldNotDrawStudentIfBagIsEmptyAfterStudentHaveBeenMovedToIsland() {
        GameState state = new GameState(2, true);

        Character character = createDummyCharacter(1, 0);
        character.receiveStudent(Student.RED);

        Island island = new Island(0, 1);
        state.setIslands(List.of(island));

        EffectArgs effectArgs = new EffectArgs.Builder()
                .setGameState(state)
                .setStudent(Student.RED)
                .setIsland(island)
                .setCharacter(character)
                .build();

        Effects.ONE_STUDENT_TO_ISLAND.performEffect(effectArgs);
        assertEquals(0, character.getStudents().size());
    }

    /**
     * Character card effect:
     * take control of the professors even when you have the same number of students of another player
     */
    @Test
    void shouldEnableProfOwnershipEvenOnTieEffect() {
        GameState state = new GameState(2, true);

        EffectArgs effectArgs = new EffectArgs.Builder()
                .setGameState(state)
                .build();

        Effects.GET_PROF_ON_STUDENT_TIE.performEffect(effectArgs);
        assertTrue(state.getExpertAttrs().getProfOwnershipOnTie());
    }

    @Test
    void shouldGiveProfOwnershipEvenOnTie() {
        GameState state = new GameState(2, true);

        Player player1 = TestUtils.createPlayer("test1", Wizard.WITCH, Tower.BLACK);
        Player player2 = TestUtils.createPlayer("test2", Wizard.KING, Tower.WHITE);

        fillWithStudents(player1.getBoard().getHall(), Student.RED, 3);
        fillWithStudents(player1.getBoard().getHall(), Student.RED, 3);

        state.addPlayer(player1);
        state.addPlayer(player2);

        EffectArgs effectArgs = new EffectArgs.Builder()
                .setGameState(state)
                .build();

        Effects.GET_PROF_ON_STUDENT_TIE.performEffect(effectArgs);
        assertEquals(player1.getId(), state.getProfessorOwner(Student.RED));
    }

    /**
     * Character card effect:
     * removes max 3 student of a specified color from the hall of all the players
     */
    @Test
    void shouldRemoveAtLeastThreeStudentsOfTheGivenColorFromTheHallOfAllPlayers() {
        GameState state = new GameState(3, true);

        Player player1 = TestUtils.createPlayer("test1", Wizard.WITCH, Tower.BLACK);
        Player player2 = TestUtils.createPlayer("test2", Wizard.KING, Tower.WHITE);
        Player player3 = TestUtils.createPlayer("test3", Wizard.DRUID, Tower.GREY);

        fillWithStudents(player1.getBoard().getHall(), Student.RED, 3);
        fillWithStudents(player2.getBoard().getHall(), Student.RED, 2);

        state.addPlayer(player1);
        state.addPlayer(player2);
        state.addPlayer(player3);

        EffectArgs effectArgs = new EffectArgs.Builder()
                .setGameState(state)
                .setStudent(Student.RED)
                .build();

        Effects.REMOVE_STUDENTS.performEffect(effectArgs);
        assertEquals(0, player1.getBoard().getHall().getNumStudentsByColor(Student.RED));
        assertEquals(0, player2.getBoard().getHall().getNumStudentsByColor(Student.RED));
        assertEquals(0, player3.getBoard().getHall().getNumStudentsByColor(Student.RED));
    }

    @Test
    void removedStudentsShouldBePutInTheBag() {
        GameState state = new GameState(3, true);

        Player player1 = TestUtils.createPlayer("test1", Wizard.WITCH, Tower.BLACK);
        Player player2 = TestUtils.createPlayer("test2", Wizard.KING, Tower.WHITE);
        Player player3 = TestUtils.createPlayer("test3", Wizard.DRUID, Tower.GREY);

        fillWithStudents(player1.getBoard().getHall(), Student.RED, 2);
        fillWithStudents(player2.getBoard().getHall(), Student.RED, 2);

        state.addPlayer(player1);
        state.addPlayer(player2);
        state.addPlayer(player3);

        EffectArgs effectArgs = new EffectArgs.Builder()
                .setGameState(state)
                .setStudent(Student.RED)
                .build();

        Effects.REMOVE_STUDENTS.performEffect(effectArgs);
        assertEquals(4, state.getBag().getNumStudent());
    }

    @Test
    void noStudentsShouldBePutInTheBag() {
        GameState state = new GameState(3, true);

        Player player1 = TestUtils.createPlayer("test1", Wizard.WITCH, Tower.BLACK);
        Player player2 = TestUtils.createPlayer("test2", Wizard.KING, Tower.WHITE);
        Player player3 = TestUtils.createPlayer("test3", Wizard.DRUID, Tower.GREY);

        fillWithStudents(player1.getBoard().getHall(), Student.RED, 0);
        fillWithStudents(player2.getBoard().getHall(), Student.RED, 0);

        state.addPlayer(player1);
        state.addPlayer(player2);
        state.addPlayer(player3);

        EffectArgs effectArgs = new EffectArgs.Builder()
                .setGameState(state)
                .setStudent(Student.RED)
                .build();

        Effects.REMOVE_STUDENTS.performEffect(effectArgs);
        assertEquals(0, state.getBag().getNumStudent());
    }

    /**
     * Character card effect:
     * exchange 3 students between this card and the entrance of the player
     */
    @Test
    void shouldExchangeThreeStudentsBetweenCharacterAndEntrance() {
        GameState state = new GameState(2, true);

        List<Student> characterStudents = List.of(Student.RED, Student.RED, Student.RED);
        List<Student> entranceStudents = List.of(Student.YELLOW, Student.YELLOW, Student.YELLOW);

        Character character = createDummyCharacter(3, 0);
        character.receiveStudents(characterStudents);

        Board board = new Board(Tower.BLACK, 1, 3);
        board.getEntrance().receiveStudents(entranceStudents);
        Player player = new Player(Wizard.WITCH, "test1", List.of(Assistant.values()), board);

        state.addPlayer(player);
        state.setCurrentTurn(0);
        state.setPlayerQueue(List.of(player.getId()));

        EffectArgs effectArgs = new EffectArgs.Builder()
                .setGameState(state)
                .setCharacter(character)
                .setSourceStudents(characterStudents)
                .setDestStudents(entranceStudents)
                .build();

        Effects.EXCHANGE_STUDENTS_BETWEEN_CHARACTER_AND_ENTRANCE.performEffect(effectArgs);
        assertEquals(entranceStudents, character.getStudents());
        assertEquals(3, board.getEntrance().getNumStudentsByColor(Student.RED));
        assertEquals(0, board.getEntrance().getNumStudentsByColor(Student.YELLOW));
    }

    // character students are wrong
    @Test
    void shouldThrowExceptionWhenExchangingTooManyStudents1() {
        GameState state = new GameState(2, true);

        List<Student> characterStudents = List.of(Student.RED, Student.RED, Student.RED, Student.RED);
        List<Student> entranceStudents = List.of(Student.YELLOW, Student.YELLOW, Student.YELLOW);

        Character character = createDummyCharacter(4, 0);
        character.receiveStudents(characterStudents);

        Board board = new Board(Tower.BLACK, 1, 3);
        board.getEntrance().receiveStudents(entranceStudents);
        Player player = new Player(Wizard.WITCH, "test1", List.of(Assistant.values()), board);

        state.addPlayer(player);
        state.setCurrentTurn(0);
        state.setPlayerQueue(List.of(player.getId()));

        EffectArgs effectArgs = new EffectArgs.Builder()
                .setGameState(state)
                .setCharacter(character)
                .setSourceStudents(characterStudents)
                .setDestStudents(entranceStudents)
                .build();

        assertThrows(IllegalMoveException.class, () ->
                Effects.EXCHANGE_STUDENTS_BETWEEN_CHARACTER_AND_ENTRANCE.performEffect(effectArgs)
        );
    }

    // entrance students are wrong
    @Test
    void shouldThrowExceptionWhenExchangingTooManyStudents2() {
        GameState state = new GameState(2, true);

        List<Student> characterStudents = List.of(Student.RED, Student.RED, Student.RED);
        List<Student> entranceStudents = List.of(Student.YELLOW, Student.YELLOW, Student.YELLOW, Student.YELLOW);

        Character character = createDummyCharacter(3, 0);
        character.receiveStudents(characterStudents);

        Board board = new Board(Tower.BLACK, 1, 4);
        board.getEntrance().receiveStudents(entranceStudents);
        Player player = new Player(Wizard.WITCH, "test1", List.of(Assistant.values()), board);

        state.addPlayer(player);
        state.setCurrentTurn(0);
        state.setPlayerQueue(List.of(player.getId()));

        EffectArgs effectArgs = new EffectArgs.Builder()
                .setGameState(state)
                .setCharacter(character)
                .setSourceStudents(characterStudents)
                .setDestStudents(entranceStudents)
                .build();

        assertThrows(IllegalMoveException.class, () ->
                Effects.EXCHANGE_STUDENTS_BETWEEN_CHARACTER_AND_ENTRANCE.performEffect(effectArgs)
        );
    }

    @Test
    void shouldThrowExceptionWhenExchangedStudentsOfEntranceAndCharacterAreDifferent() {
        GameState state = new GameState(2, true);

        List<Student> characterStudents = List.of(Student.RED, Student.RED);
        List<Student> entranceStudents = List.of(Student.YELLOW, Student.YELLOW, Student.YELLOW);

        Character character = createDummyCharacter(3, 0);
        character.receiveStudents(characterStudents);

        Board board = new Board(Tower.BLACK, 1, 3);
        board.getEntrance().receiveStudents(entranceStudents);
        Player player = new Player(Wizard.WITCH, "test1", List.of(Assistant.values()), board);

        state.addPlayer(player);
        state.setCurrentTurn(0);
        state.setPlayerQueue(List.of(player.getId()));

        EffectArgs effectArgs = new EffectArgs.Builder()
                .setGameState(state)
                .setCharacter(character)
                .setSourceStudents(characterStudents)
                .setDestStudents(entranceStudents)
                .build();

        assertThrows(IllegalMoveException.class, () ->
                Effects.EXCHANGE_STUDENTS_BETWEEN_CHARACTER_AND_ENTRANCE.performEffect(effectArgs)
        );
    }

    /**
     * Character card effect:
     * exchange 2 students between the hall and the entrance of the player
     */
    @Test
    void shouldExchangeTwoStudentsFromHallToEntrance() {
        GameState state = new GameState(2, true);

        List<Student> hallStudents = List.of(Student.RED, Student.RED);
        List<Student> entranceStudents = List.of(Student.YELLOW, Student.YELLOW);

        Board board = new Board(Tower.BLACK, 1, 2);
        board.getHall().receiveStudents(hallStudents);
        board.getEntrance().receiveStudents(entranceStudents);
        Player player = new Player(Wizard.WITCH, "test", List.of(Assistant.values()), board);

        state.addPlayer(player);
        state.setCurrentTurn(0);
        state.setPlayerQueue(List.of(player.getId()));

        EffectArgs effectArgs = new EffectArgs.Builder()
                .setGameState(state)
                .setSourceStudents(hallStudents)
                .setDestStudents(entranceStudents)
                .build();

        Effects.EXCHANGE_STUDENTS_BETWEEN_HALL_AND_ENTRANCE.performEffect(effectArgs);
        assertEquals(2, board.getEntrance().getNumStudentsByColor(Student.RED));
        assertEquals(0, board.getEntrance().getNumStudentsByColor(Student.YELLOW));
        assertEquals(2, board.getHall().getNumStudentsByColor(Student.YELLOW));
        assertEquals(0, board.getHall().getNumStudentsByColor(Student.RED));
    }

    /**
     * Character card effect:
     * add 2 additional influence points
     */
    @Test
    void shouldHaveTwoAdditionalInfluencePoints() {
        GameState state = new GameState(2, true);

        EffectArgs effectArgs = new EffectArgs.Builder()
                .setGameState(state)
                .build();

        Effects.ADD_TWO_INFLUENCE_POINTS.performEffect(effectArgs);
        assertTrue(state.getExpertAttrs().isTwoAdditionalPoints());
    }

    /**
     * Character card effect:
     * add 2 additional mother nature steps
     */
    @Test
    void shouldAddTwoAdditionalMotherNatureSteps() {
        GameState state = new GameState(2, true);

        EffectArgs effectArgs = new EffectArgs.Builder()
                .setGameState(state)
                .build();

        Effects.ADD_TWO_MOTHER_NATURE_STEPS.performEffect(effectArgs);
        assertEquals(2, state.getExpertAttrs().getAdditionalMotherNatureSteps());
    }

    private static void fillWithStudents(StudentReceiver receiver, Student student, int numStudents) {
        for (int i = 0; i < numStudents; ++i) {
            receiver.receiveStudent(student);
        }
    }

}
