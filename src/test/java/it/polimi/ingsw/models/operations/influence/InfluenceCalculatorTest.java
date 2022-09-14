package it.polimi.ingsw.models.operations.influence;

import it.polimi.ingsw.models.components.Player;
import it.polimi.ingsw.models.components.Wizard;
import it.polimi.ingsw.models.components.Board;
import it.polimi.ingsw.models.components.Island;
import it.polimi.ingsw.models.components.Student;
import it.polimi.ingsw.models.components.Tower;
import it.polimi.ingsw.models.components.Assistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InfluenceCalculatorTest {

    private InfluenceCalculator influenceCalc;
    private Player player;
    // list with all the professors
    private List<Student> professors;

    @BeforeEach
    void initInfluenceCalculator() {
        influenceCalc = new StandardInfluence();
    }

    @BeforeEach
    void initPlayer() {
        Board board = new Board(Tower.BLACK, 1, 1);
        player = new Player(Wizard.WITCH, "test", new ArrayList<>(), board);
    }

    @BeforeEach
    void initProfessors() {
        professors = new ArrayList<>();
        professors.addAll(Arrays.asList(Student.values()));
    }

    @Test
    void emptyIslandDoesNotGiveInfluence() {
        Island island = new Island(0, 1);

        int influence = influenceCalc.calculateInfluence(island, player, professors);
        assertEquals(0, influence);

        influenceCalc = new IgnoreStudentInfluence(Student.RED);
        influence = influenceCalc.calculateInfluence(island, player, professors);
        assertEquals(0, influence);

        influenceCalc = new IgnoreTowerInfluence();
        influence = influenceCalc.calculateInfluence(island, player, professors);
        assertEquals(0, influence);
    }

    @Test
    void influenceShouldIncreaseWhenTowersAreOfThePlayer() {
        Island island = new Island(0, 3);

        island.receiveTower(Tower.BLACK);
        int influence = influenceCalc.calculateInfluence(island, player, professors);
        assertEquals(1, influence);

        island.receiveTower(Tower.BLACK);
        influence = influenceCalc.calculateInfluence(island, player, professors);
        assertEquals(2, influence);

        influenceCalc = new IgnoreStudentInfluence(Student.RED);
        influence = influenceCalc.calculateInfluence(island, player, professors);
        assertEquals(2, influence);
    }

    @Test
    void influenceShouldNotIncreaseWhenTowersAreNotOfThePlayer() {
        Island island = new Island(0, 3);

        island.receiveTower(Tower.WHITE);
        int influence = influenceCalc.calculateInfluence(island, player, professors);
        assertEquals(0, influence);

        influenceCalc = new IgnoreStudentInfluence(Student.RED);
        influence = influenceCalc.calculateInfluence(island, player, professors);
        assertEquals(0, influence);
    }

    @Test
    void ignoreTowerInfluenceShouldIgnoreTowers() {
        Island island = new Island(0, 3);
        island.receiveTower(Tower.BLACK);

        influenceCalc = new IgnoreTowerInfluence();
        int influence = influenceCalc.calculateInfluence(island, player, professors);
        assertEquals(0, influence);
    }

    @Test
    void studentsShouldIncreaseInfluenceIfPlayerHasTheCorrespondingProf() {
        Island island = new Island(0, 1);
        island.receiveStudent(Student.RED);
        island.receiveStudent(Student.RED);
        island.receiveStudent(Student.GREEN);
        island.receiveStudent(Student.CYAN);
        island.receiveStudent(Student.YELLOW);

        int influence = influenceCalc.calculateInfluence(island, player, professors);
        assertEquals(5, influence);
    }

    @Test
    void studentsShouldNotIncreaseInfluenceIfPlayerHasNotTheCorrespondingProf() {
        Island island = new Island(0, 1);
        island.receiveStudent(Student.RED);
        island.receiveStudent(Student.RED);
        island.receiveStudent(Student.GREEN);
        island.receiveStudent(Student.CYAN);
        island.receiveStudent(Student.YELLOW);

        int influence = influenceCalc.calculateInfluence(
                island, player,
                List.of(Student.RED)
        );
        assertEquals(2, influence);
    }

    @Test
    void studentsShouldNotIncreaseInfluenceIfIgnored() {
        Island island = new Island(0, 1);
        island.receiveStudent(Student.RED);
        island.receiveStudent(Student.RED);
        island.receiveStudent(Student.GREEN);
        island.receiveStudent(Student.CYAN);
        island.receiveStudent(Student.YELLOW);

        influenceCalc = new IgnoreStudentInfluence(Student.RED);
        int influence = influenceCalc.calculateInfluence(island, player, professors);
        assertEquals(3, influence);
    }

    @Test
    void shouldNotGiveTowerInfluencePointsToNonLeaderPlayer() {
        Island island = new Island(0, 3);

        Board board = new Board(Tower.BLACK, 1, 0);
        Player player = new Player(Wizard.WITCH, "test", List.of(Assistant.values()), board, false);

        island.receiveTower(Tower.BLACK);
        int influence = influenceCalc.calculateInfluence(island, player, professors);
        assertEquals(0, influence);

        influenceCalc = new IgnoreStudentInfluence(Student.RED);
        influence = influenceCalc.calculateInfluence(island, player, professors);
        assertEquals(0, influence);
    }

    @Test
    void shouldAddAdditionalPoints() {
        Island island = new Island(0, 1);
        island.receiveStudent(Student.RED);
        island.receiveStudent(Student.RED);
        island.receiveStudent(Student.GREEN);
        island.receiveStudent(Student.CYAN);
        island.receiveStudent(Student.YELLOW);

        int influence = influenceCalc.calculateInfluence(island, player, professors, 2);
        assertEquals(7, influence);
    }

}