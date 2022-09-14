package it.polimi.ingsw.models.components.characters;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CharactersTest {
    @Test
    void characterTypeShouldBeTheSameUsedToCreateIt() {
        Character character = Characters.get(CharacterType.DIONYSUS);
        assertEquals(CharacterType.DIONYSUS, character.getCharacterType());
    }

    @Test
    void shouldReturnTheCorrectCharacterFromString() {
        Character character = Characters.fromString("DIONYSUS");
        assertEquals(Characters.get(CharacterType.DIONYSUS), character);
    }

    @Test
    void shouldThrowExceptionIfTheStringIsLowerCase() {
        assertThrows(IllegalArgumentException.class, () -> Characters.fromString("dionysus"));
    }

    @Test
    void shouldThrowExceptionIfTheStringDoesNotCorrespondToAnExistingCharacter() {
        assertThrows(IllegalArgumentException.class, () ->
                Characters.fromString("this is not a character")
        );
    }
}