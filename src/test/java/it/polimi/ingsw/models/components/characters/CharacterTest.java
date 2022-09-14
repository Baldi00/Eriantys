package it.polimi.ingsw.models.components.characters;

import it.polimi.ingsw.models.components.Student;
import it.polimi.ingsw.models.components.characters.effects.Effect;
import it.polimi.ingsw.models.components.characters.effects.EffectArgs;
import it.polimi.ingsw.models.exceptions.IllegalMoveException;
import it.polimi.ingsw.models.state.GameState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CharacterTest {
    @Test
    void shouldHaveTheCharacterTypeSet() {
        Character character = new Character(CharacterType.CENTAUR, 0, null);
        assertEquals(CharacterType.CENTAUR, character.getCharacterType());
    }

    @Test
    void charactersWithSameTypeShouldBeEqual() {
        Character character1 = new Character(CharacterType.CENTAUR, 0, null);
        Character character2 = new Character(CharacterType.CENTAUR, 1, null, 2, 3);
        assertEquals(character1, character2);
    }

    @Test
    void charactersWithSameTypeShouldHaveSameHashCode() {
        Character character1 = new Character(CharacterType.CENTAUR, 0, null);
        Character character2 = new Character(CharacterType.CENTAUR, 1, null, 2, 3);
        assertEquals(character1.hashCode(), character2.hashCode());
    }

    @Test
    void characterCostShouldBeTheOneSet() {
        int cost = 34;
        Character character = new Character(null, cost, null);
        assertEquals(cost, character.getCost());
    }

    @Test
    void characterCostShouldIncrementAfterItHasBeenUsed() {
        Effect doNothingEffect = new Effect() {
            @Override
            protected void effect() {}
        };

        EffectArgs args = new EffectArgs.Builder()
                .setGameState(new GameState(2, true))
                .build();

        int cost = 2;
        Character character = new Character(null, cost, doNothingEffect);

        character.performEffect(args, cost);
        assertEquals(cost + 1, character.getCost());
    }

    @Test
    void characterShouldRetainOneCoinTheFirstItIsUsed() {
        Effect doNothingEffect = new Effect() {
            @Override
            protected void effect() {}
        };

        EffectArgs args = new EffectArgs.Builder()
                .setGameState(new GameState(2, true))
                .build();

        int cost = 2;
        Character character = new Character(null, cost, doNothingEffect);

        assertEquals(cost - 1, character.performEffect(args, cost));
    }

    @Test
    void characterCostShouldNotIncrementFromTheSecondTimeItIsUsed() {
        Effect doNothingEffect = new Effect() {
            @Override
            protected void effect() {}
        };

        EffectArgs args = new EffectArgs.Builder()
                .setGameState(new GameState(2, true))
                .build();

        int cost = 2;
        Character character = new Character(null, cost, doNothingEffect);

        character.performEffect(args, cost);
        character.performEffect(args, cost + 1);
        assertEquals(cost + 1, character.getCost());
    }

    @Test
    void characterShouldNotRetainOneCoinFromTheSecondTimeItIsUsed() {
        Effect doNothingEffect = new Effect() {
            @Override
            protected void effect() {}
        };

        EffectArgs args = new EffectArgs.Builder()
                .setGameState(new GameState(2, true))
                .build();

        int cost = 2;
        Character character = new Character(null, cost, doNothingEffect);
        character.performEffect(args, cost);

        int newCost = cost + 1;
        assertEquals(newCost, character.performEffect(args, newCost));
    }

    @Test
    void shouldThrowExceptionWhenNotEnoughCoinsArePassed() {
        Effect doNothingEffect = new Effect() {
            @Override
            protected void effect() {}
        };

        EffectArgs args = new EffectArgs.Builder()
                .setGameState(new GameState(2, true))
                .build();

        int cost = 2;
        Character character = new Character(null, cost, doNothingEffect);

        assertThrows(IllegalMoveException.class, () ->
                character.performEffect(args, cost - 1)
        );
    }

    @Test
    void shouldThrowExceptionWhenExceedingTheStudentsLimitNumber() {
        Character character = new Character(null, 0, null, 1, 0);
        character.receiveStudent(Student.RED);
        assertThrows(IllegalMoveException.class, () ->
                character.receiveStudent(Student.RED)
        );
    }

    @Test
    void shouldThrowExceptionWhenPickingStudentFromCharacterWithoutStudents() {
        Character character = new Character(null, 0, null);
        assertThrows(IllegalMoveException.class, () ->
                character.pickStudent(Student.RED)
        );
    }

    @Test
    void shouldThrowExceptionWhenTheStudentPickedIsNotPresent() {
        Character character = new Character(null, 0, null, 1, 0);
        character.receiveStudent(Student.RED);
        assertThrows(IllegalMoveException.class, () ->
            character.pickStudent(Student.GREEN)
        );
    }

    @Test
    void shouldHaveTheSpecifiedNumberOfBlocksOnInit() {
        Character character = new Character(null, 0, null, 1, 4);
        assertEquals(4, character.getNumIslandBlocks());
    }

    @Test
    void shouldDecreaseTheNumberOfBlocksWhenPickingOne() {
        Character character = new Character(null, 0, null, 1, 4);
        character.pickBlock();
        assertEquals(3, character.getNumIslandBlocks());
    }

    @Test
    void shouldIncreaseTheNumberOfBlocksWhenReceivingOne() {
        Character character = new Character(null, 0, null, 1, 4);
        character.pickBlock();
        assertEquals(3, character.getNumIslandBlocks());
        character.receiveBlock();
        assertEquals(4, character.getNumIslandBlocks());
    }

    @Test
    void shouldNotHaveStudentsOnInit() {
        Character character = new Character(null, 0, null, 1, 0);
        assertEquals(0, character.getStudents().size());
    }

    @Test
    void pickingStudentShouldRemoveItFromTheCharacter() {
        Character character = new Character(null, 0, null, 1, 0);
        character.receiveStudent(Student.RED);
        assertEquals(1, character.getStudents().size());
        character.pickStudent(Student.RED);
        assertEquals(0, character.getStudents().size());
    }

    @Test
    void shouldThrowExceptionWhenPickingBlockFromCharacterWithNoBlocks() {
        Character character = new Character(null, 0, null);
        assertThrows(IllegalMoveException.class, character::pickBlock);
    }

    @Test
    void shouldPickBlockIfCharacterHasAny() {
        Character character = new Character(null, 0, null, 0, 1);
        assertDoesNotThrow(character::pickBlock);
    }

    @Test
    void shouldThrowExceptionWhenCharacterReceiveBlockThatCannotReceive() {
        Character character = new Character(null, 0, null);
        assertThrows(IllegalMoveException.class, character::receiveBlock);
    }

    @Test
    void shouldThrowExceptionWhenCharacterReceivesTooManyBlocks() {
        Character character = new Character(null, 0, null, 0, 1);
        assertThrows(IllegalMoveException.class, character::receiveBlock);
    }

    @Test
    void shouldReceiveBlockWhenTheCharacterCanHaveBlocks() {
        Character character = new Character(null, 0, null, 0, 1);
        character.pickBlock();
        assertDoesNotThrow(character::receiveBlock);
    }
}
