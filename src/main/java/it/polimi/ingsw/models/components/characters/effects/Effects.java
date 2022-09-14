package it.polimi.ingsw.models.components.characters.effects;

import it.polimi.ingsw.models.components.*;
import it.polimi.ingsw.models.components.hall.Hall;
import it.polimi.ingsw.models.components.characters.Character;
import it.polimi.ingsw.models.exceptions.IllegalMoveException;
import it.polimi.ingsw.models.operations.GameOperations;
import it.polimi.ingsw.models.state.ExpertAttrs;
import it.polimi.ingsw.models.state.GameState;

import java.util.ArrayList;
import java.util.List;

/**
 * This is collection of effects.
 *
 * Notes:
 * - to reduce coupling GameOperations.updateProfessorOwners() must be called only
 *   from the effects that DON'T add or remove students from the player halls.
 */
public class Effects {

    private Effects() {
        // hide constructor
    }

    /**
     * Takes a block from the character and blocks the selected island.
     * An island can be blocked multiple times.
     *
     * required args: gameState, island to block, the character where islands blocks are picked.
     *
     * @throws IllegalMoveException if the island or the character does not exist,
     *                              if the character doesn't have blocks to pick.
     * @throws IllegalArgumentException if there are not all the required parameters.
     */
    public static final Effect BLOCK_ISLAND = new Effect() {
        @Override
        public RequiredEffectArgs getRequiredArgs() {
            return new RequiredEffectArgs.Builder()
                    .requireIsland()
                    .requireCharacter()
                    .build();
        }

        @Override
        protected void effect() {
            Island blockedIsland = args.getIsland();
            GameState state = args.getGameState();
            Character character = args.getCharacter();

            if (!state.getIslands().contains(blockedIsland))
                throw new IllegalMoveException("Island to block does not exist");
            if (!state.getExpertAttrs().getCharacters().contains(character))
                throw new IllegalMoveException("Cannot play this character in this match");

            ExpertAttrs expertAttrs = args.getGameState().getExpertAttrs();
            List<Island> blockedIslands = expertAttrs.getBlockedIslands();

            character.pickBlock();
            blockedIslands.add(blockedIsland);
            expertAttrs.setBlockedIslands(blockedIslands);
        }
    };

    /**
     * Calculates the influence on the selected island.
     *
     * required args: gameState, island where the influence is calculated.
     *
     * @throws IllegalMoveException if the island does not exist.
     * @throws IllegalArgumentException if there are not all the required parameters.
     */
    public static final Effect CALCULATE_INFLUENCE_ON_ISLAND = new Effect() {
        @Override
        public RequiredEffectArgs getRequiredArgs() {
            return new RequiredEffectArgs.Builder()
                    .requireIsland()
                    .build();
        }

        @Override
        protected void effect() {
            GameState state = args.getGameState();
            Island island = args.getIsland();

            GameOperations.updateIslandConqueror(state, island);
            GameOperations.mergeIslands(state);
        }
    };

    /**
     * Ignore the specified student color during influence calculation.
     *
     * required args: gameState, student to ignore.
     *
     * @throws IllegalArgumentException if there are not all the required parameters.
     */
    public static final Effect IGNORE_STUDENT_COLOR = new Effect() {
        @Override
        protected void effect() {
            args.getGameState().getExpertAttrs().setIgnoredStudent(args.getStudent());
        }
    };

    /**
     * Ignore tower during in the influence calculation.
     *
     * required args: gameState
     *
     * @throws IllegalArgumentException if there are not all the required parameters.
     */
    public static final Effect IGNORE_TOWERS = new Effect() {
        @Override
        protected void effect() {
            args.getGameState().getExpertAttrs().setIgnoreTowers(true);
        }
    };

    /**
     * Moves one student from the character to the current player hall.
     * It then draws a student from the bag and puts it on the character.
     *
     * required args: gameState, student to move, the character played that has this effect.
     *
     * @throws IllegalMoveException if the player cannot get the student from the character or
     *                              if the student is not present on the character.
     * @throws IllegalArgumentException if there are not all the required parameters.
     */
    public static final Effect ONE_STUDENT_TO_HALL = new Effect() {
        @Override
        public RequiredEffectArgs getRequiredArgs() {
            return new RequiredEffectArgs.Builder()
                    .requireStudent()
                    .requireCharacter()
                    .build();
        }

        @Override
        protected void effect() {
            GameState state = args.getGameState();
            Hall playerHall = state.getCurrentPlayer().getBoard().getHall();
            Character character = args.getCharacter();
            Student student = args.getStudent();

            if (!playerHall.canReceiveStudent(student))
                throw new IllegalMoveException("Current player cannot get student " + student +
                        "from this character");

            character.pickStudent(args.getStudent());
            playerHall.receiveStudent(args.getStudent());

            refillCharacter(state, character);
        }
    };

    /**
     * Moves one student from the character to the specified island.
     * It then draws a student from the bag and puts it on the character.
     *
     * required args: gameState, the student to move, the island where the student is moved.
     *
     * @throws IllegalArgumentException if there are not all the required parameters.
     */
    public static final Effect ONE_STUDENT_TO_ISLAND = new Effect() {
        @Override
        public RequiredEffectArgs getRequiredArgs() {
            return new RequiredEffectArgs.Builder()
                    .requireStudent()
                    .requireCharacter()
                    .requireIsland()
                    .build();
        }

        @Override
        protected void effect() {
            GameState state = args.getGameState();
            Character character = args.getCharacter();
            Student student = args.getStudent();
            Island island = args.getIsland();

            character.pickStudent(student);
            island.receiveStudent(student);

            refillCharacter(state, character);
        }
    };

    /**
     * Give the professor to the current player even if there is tie.
     *
     * required args: gameState.
     *
     * @throws IllegalArgumentException if there are not all the required parameters.
     */
    public static final Effect GET_PROF_ON_STUDENT_TIE = new Effect() {
        @Override
        protected void effect() {
            GameState state = args.getGameState();
            state.getExpertAttrs().setProfOwnerOnStudentsTie(true);
            GameOperations.updateProfessorsOwners(state);
        }
    };

    /**
     * Puts back into the bag 3 students of the selected color from all player halls.
     * If there are not 3 students, it removes all the students it can.
     *
     * required args: gameState, student to remove.
     *
     * @throws IllegalArgumentException if there are not all the required parameters.
     */
    public static final Effect REMOVE_STUDENTS = new Effect() {
        private static final int MAX_STUDENTS_TO_REMOVE = 3;

        @Override
        public RequiredEffectArgs getRequiredArgs() {
            return new RequiredEffectArgs.Builder()
                    .requireStudent()
                    .build();
        }

        @Override
        protected void effect() {
            List<Player> players = args.getGameState().getPlayers();
            Bag bag = args.getGameState().getBag();
            Student studentToRemove = args.getStudent();

            for (Player player : players) {
                Hall playerHall = player.getBoard().getHall();
                for (int i = 0; i < MAX_STUDENTS_TO_REMOVE; ++i) {
                    if (playerHall.canRemoveStudent(studentToRemove)) {
                        playerHall.removeStudent(studentToRemove);
                        bag.receiveStudent(studentToRemove);
                    }
                }
            }
        }
    };

    /**
     * Exchanges max 3 students between the character and the entrance.
     *
     * required args: gameState, lists of students to exchange between the character and the entrance.
     *
     * @throws IllegalMoveException when the number of students to exchange is not equal in from and to,
     *                              when trying to exchange too many students, or when some students cannot
     *                              be exchanged.
     * @throws IllegalArgumentException if there are not all the required parameters.
     */
    public static final Effect EXCHANGE_STUDENTS_BETWEEN_CHARACTER_AND_ENTRANCE = new Effect() {
        @Override
        public RequiredEffectArgs getRequiredArgs() {
            return new RequiredEffectArgs.Builder()
                    .requireToExchangeTo()
                    .requireToExchangeFrom()
                    .requireCharacter()
                    .build();
        }

        @Override
        protected void effect() {
            int maxExchangeableStudents = 3;
            List<Student> characterStudents = args.getSourceStudents();
            List<Student> entranceStudents = args.getDestStudents();

            if (characterStudents.size() > maxExchangeableStudents || entranceStudents.size() > maxExchangeableStudents)
                throw new IllegalMoveException("Cannot exchange more than " + maxExchangeableStudents + " students");

            if (characterStudents.size() != entranceStudents.size())
                throw new IllegalMoveException("The number of students to exchange must be the same for both character and " +
                        "entrance. Exchanged " + characterStudents.size() + " students for " + entranceStudents.size() + " students.");

            Character character = args.getCharacter();
            Entrance playerEntrance = args.getGameState().getCurrentPlayer().getBoard().getEntrance();

            exchangeStudents(character, characterStudents, playerEntrance, entranceStudents);
        }
    };

    /**
     * Exchanges max 2 students between the hall and the entrance.
     *
     * required args: gameState, lists of students to exchange between the hall and the entrance.
     *
     * @throws IllegalMoveException when the number of students to exchange is not equal in from and to,
     *                              when trying to exchange too many students, or when some students cannot
     *                              be exchanged.
     * @throws IllegalArgumentException if there are not all the required parameters.
     */
    public static final Effect EXCHANGE_STUDENTS_BETWEEN_HALL_AND_ENTRANCE = new Effect() {
        @Override
        public RequiredEffectArgs getRequiredArgs() {
            return new RequiredEffectArgs.Builder()
                    .requireToExchangeTo()
                    .requireToExchangeFrom()
                    .build();
        }

        @Override
        protected void effect() {
            int maxExchangeableStudents = 2;
            List<Student> hallStudents = args.getSourceStudents();
            List<Student> entranceStudents = args.getDestStudents();

            if (hallStudents.size() > maxExchangeableStudents || entranceStudents.size() > maxExchangeableStudents)
                throw new IllegalMoveException("Can't exchange more than " + maxExchangeableStudents + " students");

            if (hallStudents.size() != entranceStudents.size())
                throw new IllegalMoveException("The number of students to exchange must be the same for both hall and " +
                        "entrance. Exchanged " + hallStudents.size() + " students for " + entranceStudents.size() + " students.");

            Player player = args.getGameState().getCurrentPlayer();
            Hall playerHall = player.getBoard().getHall();
            Entrance playerEntrance = player.getBoard().getEntrance();

            exchangeStudents(playerHall, hallStudents, playerEntrance, entranceStudents);
        }
    };

    /**
     * Gives to the current player two additional influence points.
     *
     * required args: gameState.
     *
     * @throws IllegalArgumentException if there are not all the required parameters.
     */
    public static final Effect ADD_TWO_INFLUENCE_POINTS = new Effect() {
        @Override
        protected void effect() {
            args.getGameState().getExpertAttrs().setTwoAdditionalPoints(true);
        }
    };

    /**
     * Gives to mother nature two additional steps.
     *
     * @param effectArgs contains gameState.
     *
     * @throws IllegalArgumentException if there are not all the required parameters.
     */
    public static final Effect ADD_TWO_MOTHER_NATURE_STEPS = new Effect() {
        @Override
        protected void effect() {
            args.getGameState().getExpertAttrs().setAdditionalMotherNatureSteps(2);
        }
    };

    private static void exchangeStudents(Character character, List<Student> characterStudents, Entrance entrance, List<Student> entranceStudents) {
        removeStudentsFromCharacter(characterStudents, character);
        removeStudentsFromEntrance(entranceStudents, entrance);
        if (character.canReceiveStudents(entranceStudents) && entrance.canReceiveStudents(characterStudents)) {
            character.receiveStudents(entranceStudents);
            entrance.receiveStudents(characterStudents);
        } else {
            throw new IllegalMoveException("Some students cannot be exchanged between character and entrance");
        }
    }

    private static void exchangeStudents(Hall hall, List<Student> hallStudents, Entrance entrance, List<Student> entranceStudents) {
        removeStudentsFromHall(hallStudents, hall);
        removeStudentsFromEntrance(entranceStudents, entrance);
        if (hall.canReceiveStudents(entranceStudents) && entrance.canReceiveStudents(hallStudents)) {
            hall.receiveStudents(entranceStudents);
            entrance.receiveStudents(hallStudents);
        }  else {
            throw new IllegalMoveException("Some students cannot be exchanged between hall and entrance");
        }
    }

    /**
     * Character is left unchanged if remove fails.
     *
     * @param students  the students to remove from the character.
     * @param character the character where the students are removed.
     * @throws IllegalMoveException if all students cannot be removed.
     */
    private static void removeStudentsFromCharacter(List<Student> students, Character character) {
        List<Student> removed = new ArrayList<>();
        try {
            for (Student student : students) {
                character.pickStudent(student);
                removed.add(student);
            }
        } catch (IllegalMoveException e) {
            // restore students
            character.receiveStudents(removed);
            throw new IllegalMoveException(e.getMessage());
        }
    }

    /**
     * Entrance is left unchanged if remove fails.
     *
     * @param students the students to remove from the entrance.
     * @param entrance the entrance where the students are removed.
     * @throws IllegalMoveException if all students cannot be removed.
     */
    private static void removeStudentsFromEntrance(List<Student> students, Entrance entrance) {
        List<Student> removed = new ArrayList<>();
        try {
            for (Student student : students) {
                entrance.removeStudent(student);
                removed.add(student);
            }
        } catch (IllegalMoveException e) {
            // restore students
            entrance.receiveStudents(removed);
            throw new IllegalMoveException(e.getMessage());
        }
    }

    /**
     * Hall is left unchanged if remove fails.
     *
     * @param students  the students to remove from the hall.
     * @param hall      the hall where the students are removed.
     * @throws IllegalMoveException if all students cannot be removed.
     */
    private static void removeStudentsFromHall(List<Student> students, Hall hall) {
        List<Student> removed = new ArrayList<>();
        try {
            for (Student student : students) {
                hall.removeStudent(student);
                removed.add(student);
            }
        } catch (IllegalMoveException e) {
            // restore students
            hall.receiveStudents(removed);
            throw new IllegalMoveException(e.getMessage());
        }
    }

    /**
     * Draws a student from the bag and places it on the character.
     * If the bag is empty no student is added to the character.
     *
     * @param state     the state of the game.
     * @param character the character to refill.
     */
    private static void refillCharacter(GameState state, Character character) {
        if (!state.isBagEmpty()) {
            Bag bag = state.getBag();
            Student studentDrawn = bag.drawStudent();
            character.receiveStudent(studentDrawn);
        }
    }

}
