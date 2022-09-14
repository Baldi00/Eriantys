package it.polimi.ingsw.models.components;

import it.polimi.ingsw.models.constants.GameConstants;
import it.polimi.ingsw.models.exceptions.IllegalMoveException;
import it.polimi.ingsw.models.exceptions.TowerNotSetException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IslandTest {

    @Test
    void positionShouldBeTheOneSet() {
        int position = 1;
        Island island = new Island(position, 1);
        assertEquals(position, island.getPosition());
    }

    @Test
    void shouldThrowExceptionIfPositionIsNegative() {
        assertThrows(IllegalArgumentException.class, () -> new Island(-1, 1));
    }

    @Test
    void shouldThrowExceptionIfPositionExceedsTheMaximumAllowed() {
        assertThrows(IllegalArgumentException.class, () ->
                new Island(GameConstants.NUMBER_OF_ISLANDS, 1)
        );
    }

    @Test
    void dimensionShouldBeTheOneSet() {
        int dimension = 3;
        Island island = new Island(1, dimension);
        assertEquals(dimension, island.getDimension());
    }

    @Test
    void shouldThrowExceptionIfDimensionIsNotPositive() {
        assertThrows(IllegalArgumentException.class, () -> new Island(1, 0));
        assertThrows(IllegalArgumentException.class, () -> new Island(1, -1));
    }

    @Test
    void shouldThrowExceptionIfDimensionExceedsTheMaximumAllowed() {
        assertThrows(IllegalArgumentException.class, () ->
                new Island(0, GameConstants.NUMBER_OF_ISLANDS + 1)
        );
    }

    @Test
    void shouldThrowExceptionIfYouGetTheTowerTypeBeforeSet() {
        Island island = new Island(1, 1);
        assertThrows(TowerNotSetException.class, island::getTowerType);
    }

    @Test
    void towerTypeShouldBeTheSameReceived() {
        Island island = new Island(1, 1);
        island.receiveTower(Tower.BLACK);
        assertEquals(Tower.BLACK, island.getTowerType());
    }

    @Test
    void numTowersShouldBeZeroOnInit() {
        Island island = new Island(1, 1);
        assertEquals(0, island.getNumTowers());
        assertFalse(island.hasTowers());
    }

    @Test
    void numTowersShouldIncreaseWhenAddingTowers() {
        Island island = new Island(1, 1);
        island.receiveTower(Tower.BLACK);
        assertEquals(1, island.getNumTowers());
        assertTrue(island.hasTowers());
    }

    @Test
    void numTowersShouldBeZeroAfterRemovingAllTheTowers() {
        Island island = new Island(1, 1);
        island.receiveTower(Tower.BLACK);
        island.removeAllTowers();
        assertEquals(0, island.getNumTowers());
        assertFalse(island.hasTowers());
    }

    @Test
    void removeTowersShouldReturnTheNumOfTowersRemoved() {
        Island island = new Island(1, 10);
        for (int i = 0; i < 10; ++i) {
            island.receiveTower(Tower.BLACK);
        }
        assertEquals(10, island.removeAllTowers());
    }

    @Test
    void shouldThrowExceptionIfNumTowersIsGreaterThanIslandDimension() {
        Island island = new Island(1, 1);
        island.receiveTower(Tower.BLACK);
        assertThrows(IllegalMoveException.class, () ->
                island.receiveTower(Tower.BLACK)
        );
    }

    @Test
    void shouldThrowExceptionIfADifferentTypeOfTowerIsAdded() {
        Island island = new Island(1, 2);
        island.receiveTower(Tower.BLACK);
        assertThrows(IllegalMoveException.class, () ->
                island.receiveTower(Tower.WHITE)
        );
    }

    @Test
    void shouldHaveZeroStudentsOnInit() {
        Island island = new Island(0, 1);
        for (Student student : Student.values()) {
            assertEquals(0, island.getNumStudent(student));
        }
    }

    @Test
    void shouldIncrementNumStudentAfterReceive() {
        Island island = new Island(0, 1);
        for (Student student : Student.values()) {
            island.receiveStudent(student);
            assertEquals(1, island.getNumStudent(student));
        }
    }

    @Test
    void islandShouldBeBefore1() {
        Island island1 = new Island(0, 1);
        Island island2 = new Island(1, 1);
        assertTrue(island1.isBefore(island2));
    }

    @Test
    void islandShouldBeBefore2() {
        Island island1 = new Island(0, 1);
        Island island2 = new Island(11, 1);
        assertTrue(island2.isBefore(island1));
    }

    @Test
    void islandShouldBeBefore3() {
        Island island1 = new Island(0, 3);
        Island island2 = new Island(3, 1);
        assertTrue(island1.isBefore(island2));
    }

    @Test
    void islandShouldBeBefore4() {
        Island island1 = new Island(2, 1);
        Island island2 = new Island(11, 3);
        assertTrue(island2.isBefore(island1));
    }

    @Test
    void islandsShouldNotBeBefore1() {
        Island island1 = new Island(0, 1);
        Island island2 = new Island(1, 1);
        assertFalse(island2.isBefore(island1));
    }

    @Test
    void islandsShouldNotBeBefore2() {
        Island island1 = new Island(0, 1);
        Island island2 = new Island(2, 1);
        assertFalse(island1.isBefore(island2));
    }

    @Test
    void islandShouldNotBeBeforeItself() {
        Island island = new Island(0, 1);
        assertFalse(island.isBefore(island));
    }

}