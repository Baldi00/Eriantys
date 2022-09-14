package it.polimi.ingsw.models.state;

import it.polimi.ingsw.models.components.Island;
import it.polimi.ingsw.models.components.Student;
import it.polimi.ingsw.models.components.characters.Character;
import it.polimi.ingsw.models.components.characters.CharacterType;
import it.polimi.ingsw.models.components.characters.Characters;
import it.polimi.ingsw.models.constants.GameConstants;
import it.polimi.ingsw.models.exceptions.IllegalMoveException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExpertAttrsTest {

    private ExpertAttrs expertAttrs;

    @BeforeEach
    void setup() {
        expertAttrs = new ExpertAttrs();
    }

    @Test
    void checkSetAndGetCharacters() {
        List<Character> characters = new ArrayList<>();
        characters.add(Characters.get(CharacterType.DIONYSUS));
        characters.add(Characters.get(CharacterType.DAIRYMAN));
        expertAttrs.setCharacters(characters);
        for (int i = 0; i < expertAttrs.getCharacters().size(); i++) {
            assertEquals(characters.get(i).getCharacterType(), expertAttrs.getCharacters().get(i).getCharacterType());
        }
    }

    @Test
    void checkSetAndGetBlockedIslands() {
        List<Island> islands = new ArrayList<>();
        islands.add(new Island(1, 1));
        islands.add(new Island(2, 1));
        expertAttrs.setBlockedIslands(islands);
        for (int i = 0; i < expertAttrs.getBlockedIslands().size(); i++) {
            assertEquals(islands.get(i).getPosition(), expertAttrs.getBlockedIslands().get(i).getPosition());
        }
    }

    @Test
    void shouldRemoveCoinsFromStock() {
        expertAttrs.getCoinsFromStock(2);
        assertEquals(GameConstants.NUM_COINS - 2, expertAttrs.getNumCoinsInStock());
    }
    @Test
    void shouldAddCoinsToStock() {
        expertAttrs.getCoinsFromStock(2);
        expertAttrs.addCoinsToStock(2);
        assertEquals(GameConstants.NUM_COINS, expertAttrs.getNumCoinsInStock());
    }
    @Test
    void shouldThrowExceptionWhenGettingTooManyCoins() {
        assertThrows(IllegalMoveException.class, () ->
                expertAttrs.getCoinsFromStock(21)
        );
    }
    @Test
    void shouldThrowExceptionWhenAddingTooManyCoins() {
        assertThrows(IllegalMoveException.class, () ->
                expertAttrs.addCoinsToStock(1)
        );
    }

    @Test
    void shouldSetAdditionalMotherNatureSteps() {
        expertAttrs.setAdditionalMotherNatureSteps(3);
        assertEquals(3, expertAttrs.getAdditionalMotherNatureSteps());
    }

    @Test
    void checkSetAndGetIgnoreTowers() {
        expertAttrs.setIgnoreTowers(true);
        assertTrue(expertAttrs.isIgnoreTowers());
        expertAttrs.setIgnoreTowers(false);
        assertFalse(expertAttrs.isIgnoreTowers());
    }

    @Test
    void checkSetAndGetTwoAdditionalPoints() {
        expertAttrs.setTwoAdditionalPoints(true);
        assertTrue(expertAttrs.isTwoAdditionalPoints());
        expertAttrs.setTwoAdditionalPoints(false);
        assertFalse(expertAttrs.isTwoAdditionalPoints());
    }

    @Test
    void checkSetAndGetIgnoredStudentType() {
        expertAttrs.setIgnoredStudent(Student.RED);
        assertEquals(Student.RED, expertAttrs.getIgnoredStudentType());
    }

}
