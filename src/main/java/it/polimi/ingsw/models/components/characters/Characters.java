package it.polimi.ingsw.models.components.characters;

import it.polimi.ingsw.models.components.characters.effects.*;

// TODO: enum with constructor should solve the problem
/** Cannot create constants because character are stateful */
public class Characters {
    private Characters() {
        // hide constructor
    }

    public static Character get(CharacterType type) {
        return switch (type) {
            case DIONYSUS -> new Character(CharacterType.DIONYSUS, 1, Effects.ONE_STUDENT_TO_ISLAND, 4, 0);
            case DAIRYMAN -> new Character(CharacterType.DAIRYMAN, 2, Effects.GET_PROF_ON_STUDENT_TIE);
            case ORIFLAMME -> new Character(CharacterType.ORIFLAMME, 3, Effects.CALCULATE_INFLUENCE_ON_ISLAND);
            case ERMES -> new Character(CharacterType.ERMES, 1, Effects.ADD_TWO_MOTHER_NATURE_STEPS);
            case CIRCE -> new Character(CharacterType.CIRCE, 2, Effects.BLOCK_ISLAND, 0, 4);
            case CENTAUR -> new Character(CharacterType.CENTAUR, 3, Effects.IGNORE_TOWERS);
            case JESTER -> new Character(CharacterType.JESTER, 1, Effects.EXCHANGE_STUDENTS_BETWEEN_CHARACTER_AND_ENTRANCE, 6, 0);
            case KNIGHT -> new Character(CharacterType.KNIGHT, 2, Effects.ADD_TWO_INFLUENCE_POINTS);
            case GOOMBA -> new Character(CharacterType.GOOMBA, 3, Effects.IGNORE_STUDENT_COLOR);
            case BARD -> new Character(CharacterType.BARD, 1, Effects.EXCHANGE_STUDENTS_BETWEEN_HALL_AND_ENTRANCE);
            case APHRODITE -> new Character(CharacterType.APHRODITE, 2, Effects.ONE_STUDENT_TO_HALL, 4, 0);
            case THIEF -> new Character(CharacterType.THIEF, 3, Effects.REMOVE_STUDENTS);
        };
    }

    /**
     * Returns the Character with the name of the given string.
     * The string must have the exact name of the enum constants all uppercase.
     *
     * @param characterType the name of the Character.
     * @return the Character with the name of the string.
     * @throws IllegalArgumentException if the string doesn't correspond to any character.
     */
    public static Character fromString(String characterType) {
        return get(CharacterType.valueOf(characterType));
    }
}
