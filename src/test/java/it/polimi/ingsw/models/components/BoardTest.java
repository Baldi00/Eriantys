package it.polimi.ingsw.models.components;

import it.polimi.ingsw.models.exceptions.IllegalMoveException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    private final int towerLimit = 2;
    private final int studentLimitOnEntrance = 2;

    @Test
    void shouldReceiveRightKindOfTower() {
        Board board = new Board(Tower.BLACK, towerLimit, studentLimitOnEntrance);
        for (int i = 0; i < towerLimit; i++) {
            assertDoesNotThrow(() ->
                    board.receiveTower(Tower.BLACK)
            );
        }
    }

    @Test
    void shouldThrowExceptionWhenADifferentTypeOfTowerIsAdded() {
        Board board = new Board(Tower.BLACK, towerLimit, studentLimitOnEntrance);
        assertThrows(IllegalMoveException.class, () ->
                board.receiveTower(Tower.WHITE)
        );
    }

    @Test
    void shouldThrowExceptionWhenExceedingTowersLimit() {
        Board board = new Board(Tower.BLACK, towerLimit, studentLimitOnEntrance);
        for (int i = 0; i < towerLimit; i++)
            board.receiveTower(Tower.BLACK);
        assertThrows(IllegalMoveException.class, () ->
                board.receiveTower(Tower.BLACK)
        );
    }

    @Test
    void shouldAcceptTowersAfterRemoval() {
        Board board = new Board(Tower.BLACK, towerLimit, studentLimitOnEntrance);
        for (int i = 0; i < towerLimit; i++)
            board.receiveTower(Tower.BLACK);

        board.removeTower();
        assertDoesNotThrow(() ->
                board.receiveTower(Tower.BLACK)
        );
    }

    @Test
    void newlyCreatedBoardHasNoTowers() {
        Board board = new Board(Tower.BLACK, towerLimit, studentLimitOnEntrance);
        assertFalse(board.hasTowers());
    }

    @Test
    void shouldHaveTowersAfterReceivingSome() {
        Board board = new Board(Tower.BLACK, towerLimit, studentLimitOnEntrance);
        board.receiveTower(Tower.BLACK);
        assertTrue(board.hasTowers());
    }

    @Test
    void shouldNotHaveTowersAfterRemoval() {
        Board board = new Board(Tower.BLACK, towerLimit, studentLimitOnEntrance);
        board.receiveTower(Tower.BLACK);
        board.removeTower();
        assertFalse(board.hasTowers());
    }

    @Test
    void cannotRemoveTowersFromEmptyBoard() {
        Board board = new Board(Tower.BLACK, towerLimit, studentLimitOnEntrance);
        assertFalse(board.removeTower());
    }
}
