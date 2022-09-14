package it.polimi.ingsw.clients.gui.view.components.panels.playing_field.events;

import it.polimi.ingsw.clients.gui.view.exceptions.NoClickEventException;
import it.polimi.ingsw.clients.gui.view.components.panels.playing_field.utils.SceneInfoManager;
import it.polimi.ingsw.clients.gui.view.components.panels.playing_field.utils.SceneSprite;
import it.polimi.ingsw.models.components.characters.Character;
import it.polimi.ingsw.models.components.characters.CharacterType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Handles "from-to" events in the canvas
 */
public class ClickEventHandler {
    private static ClickEventHandler instance = null;

    private final Map<ClickActions, List<SceneSprite>> possibleSources = Map.of(
            ClickActions.MOVE_STUDENT_FROM_ENTRANCE_TO_HALL,
            List.of(
                    SceneSprite.BOARD_ENTRANCE_1,
                    SceneSprite.BOARD_ENTRANCE_2,
                    SceneSprite.BOARD_ENTRANCE_3,
                    SceneSprite.BOARD_ENTRANCE_4,
                    SceneSprite.BOARD_ENTRANCE_5,
                    SceneSprite.BOARD_ENTRANCE_6,
                    SceneSprite.BOARD_ENTRANCE_7,
                    SceneSprite.BOARD_ENTRANCE_8,
                    SceneSprite.BOARD_ENTRANCE_9
            ),

            ClickActions.MOVE_STUDENT_FROM_ENTRANCE_TO_ISLAND,
            List.of(
                    SceneSprite.BOARD_ENTRANCE_1,
                    SceneSprite.BOARD_ENTRANCE_2,
                    SceneSprite.BOARD_ENTRANCE_3,
                    SceneSprite.BOARD_ENTRANCE_4,
                    SceneSprite.BOARD_ENTRANCE_5,
                    SceneSprite.BOARD_ENTRANCE_6,
                    SceneSprite.BOARD_ENTRANCE_7,
                    SceneSprite.BOARD_ENTRANCE_8,
                    SceneSprite.BOARD_ENTRANCE_9
            ),

            ClickActions.MOVE_MOTHER_NATURE,
            List.of(
                    SceneSprite.ISLAND_1,
                    SceneSprite.ISLAND_2,
                    SceneSprite.ISLAND_3,
                    SceneSprite.ISLAND_4,
                    SceneSprite.ISLAND_5,
                    SceneSprite.ISLAND_6,
                    SceneSprite.ISLAND_7,
                    SceneSprite.ISLAND_8,
                    SceneSprite.ISLAND_9,
                    SceneSprite.ISLAND_10,
                    SceneSprite.ISLAND_11,
                    SceneSprite.ISLAND_12
            ),

            ClickActions.PICK_FROM_CLOUD,
            List.of(
                    SceneSprite.CLOUD_1,
                    SceneSprite.CLOUD_2,
                    SceneSprite.CLOUD_3,
                    SceneSprite.CLOUD_4
            ),

            ClickActions.MOVE_FROM_CHAR_STUDENT_TO_ISLAND,
            List.of(
                    SceneSprite.CHAR_GREEN_1,
                    SceneSprite.CHAR_YELLOW_1,
                    SceneSprite.CHAR_PINK_1,
                    SceneSprite.CHAR_RED_1,
                    SceneSprite.CHAR_CYAN_1,
                    SceneSprite.CHAR_GREEN_2,
                    SceneSprite.CHAR_YELLOW_2,
                    SceneSprite.CHAR_PINK_2,
                    SceneSprite.CHAR_RED_2,
                    SceneSprite.CHAR_CYAN_2,
                    SceneSprite.CHAR_GREEN_3,
                    SceneSprite.CHAR_YELLOW_3,
                    SceneSprite.CHAR_PINK_3,
                    SceneSprite.CHAR_RED_3,
                    SceneSprite.CHAR_CYAN_3
            ),

            ClickActions.MOVE_FROM_CHAR_STUDENT_TO_HALL,
            List.of(
                    SceneSprite.CHAR_GREEN_1,
                    SceneSprite.CHAR_YELLOW_1,
                    SceneSprite.CHAR_PINK_1,
                    SceneSprite.CHAR_RED_1,
                    SceneSprite.CHAR_CYAN_1,
                    SceneSprite.CHAR_GREEN_2,
                    SceneSprite.CHAR_YELLOW_2,
                    SceneSprite.CHAR_PINK_2,
                    SceneSprite.CHAR_RED_2,
                    SceneSprite.CHAR_CYAN_2,
                    SceneSprite.CHAR_GREEN_3,
                    SceneSprite.CHAR_YELLOW_3,
                    SceneSprite.CHAR_PINK_3,
                    SceneSprite.CHAR_RED_3,
                    SceneSprite.CHAR_CYAN_3
            ),

            ClickActions.SWAP_FROM_CHAR_STUDENT_TO_ENTRANCE,
            List.of(
                    SceneSprite.CHAR_GREEN_1,
                    SceneSprite.CHAR_YELLOW_1,
                    SceneSprite.CHAR_PINK_1,
                    SceneSprite.CHAR_RED_1,
                    SceneSprite.CHAR_CYAN_1,
                    SceneSprite.CHAR_GREEN_2,
                    SceneSprite.CHAR_YELLOW_2,
                    SceneSprite.CHAR_PINK_2,
                    SceneSprite.CHAR_RED_2,
                    SceneSprite.CHAR_CYAN_2,
                    SceneSprite.CHAR_GREEN_3,
                    SceneSprite.CHAR_YELLOW_3,
                    SceneSprite.CHAR_PINK_3,
                    SceneSprite.CHAR_RED_3,
                    SceneSprite.CHAR_CYAN_3
            ),

            ClickActions.SWAP_STUDENT_FROM_HALL_TO_ENTRANCE,
            List.of(
                    SceneSprite.BOARD_HALL_CYAN_1,
                    SceneSprite.BOARD_HALL_GREEN_1,
                    SceneSprite.BOARD_HALL_PINK_1,
                    SceneSprite.BOARD_HALL_RED_1,
                    SceneSprite.BOARD_HALL_YELLOW_1
            ),

            ClickActions.MOVE_FROM_CHAR_TO_ISLAND,
            List.of(
                    SceneSprite.CHAR_CARD_1,
                    SceneSprite.CHAR_CARD_2,
                    SceneSprite.CHAR_CARD_3
            ),

            ClickActions.CHAR_CARD_SELECTION,
            List.of(
                    SceneSprite.CHAR_CARD_1,
                    SceneSprite.CHAR_CARD_2,
                    SceneSprite.CHAR_CARD_3
            )
    );

    private final Map<ClickActions, List<SceneSprite>> possibleDestinations = Map.of(
            ClickActions.MOVE_STUDENT_FROM_ENTRANCE_TO_HALL,
            List.of(
                    SceneSprite.BOARD_HALL_CYAN_1,
                    SceneSprite.BOARD_HALL_GREEN_1,
                    SceneSprite.BOARD_HALL_PINK_1,
                    SceneSprite.BOARD_HALL_RED_1,
                    SceneSprite.BOARD_HALL_YELLOW_1
            ),

            ClickActions.MOVE_STUDENT_FROM_ENTRANCE_TO_ISLAND,
            List.of(
                    SceneSprite.ISLAND_1,
                    SceneSprite.ISLAND_2,
                    SceneSprite.ISLAND_3,
                    SceneSprite.ISLAND_4,
                    SceneSprite.ISLAND_5,
                    SceneSprite.ISLAND_6,
                    SceneSprite.ISLAND_7,
                    SceneSprite.ISLAND_8,
                    SceneSprite.ISLAND_9,
                    SceneSprite.ISLAND_10,
                    SceneSprite.ISLAND_11,
                    SceneSprite.ISLAND_12
            ),

            ClickActions.MOVE_MOTHER_NATURE,
            List.of(
                    SceneSprite.ISLAND_1,
                    SceneSprite.ISLAND_2,
                    SceneSprite.ISLAND_3,
                    SceneSprite.ISLAND_4,
                    SceneSprite.ISLAND_5,
                    SceneSprite.ISLAND_6,
                    SceneSprite.ISLAND_7,
                    SceneSprite.ISLAND_8,
                    SceneSprite.ISLAND_9,
                    SceneSprite.ISLAND_10,
                    SceneSprite.ISLAND_11,
                    SceneSprite.ISLAND_12
            ),

            ClickActions.PICK_FROM_CLOUD,
            List.of(
                    SceneSprite.BOARD_ENTRANCE_1,
                    SceneSprite.BOARD_ENTRANCE_2,
                    SceneSprite.BOARD_ENTRANCE_3,
                    SceneSprite.BOARD_ENTRANCE_4,
                    SceneSprite.BOARD_ENTRANCE_5,
                    SceneSprite.BOARD_ENTRANCE_6,
                    SceneSprite.BOARD_ENTRANCE_7,
                    SceneSprite.BOARD_ENTRANCE_8,
                    SceneSprite.BOARD_ENTRANCE_9
            ),

            ClickActions.MOVE_FROM_CHAR_STUDENT_TO_ISLAND,
            List.of(
                    SceneSprite.ISLAND_1,
                    SceneSprite.ISLAND_2,
                    SceneSprite.ISLAND_3,
                    SceneSprite.ISLAND_4,
                    SceneSprite.ISLAND_5,
                    SceneSprite.ISLAND_6,
                    SceneSprite.ISLAND_7,
                    SceneSprite.ISLAND_8,
                    SceneSprite.ISLAND_9,
                    SceneSprite.ISLAND_10,
                    SceneSprite.ISLAND_11,
                    SceneSprite.ISLAND_12
            ),

            ClickActions.MOVE_FROM_CHAR_STUDENT_TO_HALL,
            List.of(
                    SceneSprite.BOARD_HALL_CYAN_1,
                    SceneSprite.BOARD_HALL_GREEN_1,
                    SceneSprite.BOARD_HALL_PINK_1,
                    SceneSprite.BOARD_HALL_RED_1,
                    SceneSprite.BOARD_HALL_YELLOW_1
            ),

            ClickActions.SWAP_FROM_CHAR_STUDENT_TO_ENTRANCE,
            List.of(
                    SceneSprite.BOARD_ENTRANCE_1,
                    SceneSprite.BOARD_ENTRANCE_2,
                    SceneSprite.BOARD_ENTRANCE_3,
                    SceneSprite.BOARD_ENTRANCE_4,
                    SceneSprite.BOARD_ENTRANCE_5,
                    SceneSprite.BOARD_ENTRANCE_6,
                    SceneSprite.BOARD_ENTRANCE_7,
                    SceneSprite.BOARD_ENTRANCE_8,
                    SceneSprite.BOARD_ENTRANCE_9
            ),

            ClickActions.SWAP_STUDENT_FROM_HALL_TO_ENTRANCE,
            List.of(
                    SceneSprite.BOARD_ENTRANCE_1,
                    SceneSprite.BOARD_ENTRANCE_2,
                    SceneSprite.BOARD_ENTRANCE_3,
                    SceneSprite.BOARD_ENTRANCE_4,
                    SceneSprite.BOARD_ENTRANCE_5,
                    SceneSprite.BOARD_ENTRANCE_6,
                    SceneSprite.BOARD_ENTRANCE_7,
                    SceneSprite.BOARD_ENTRANCE_8,
                    SceneSprite.BOARD_ENTRANCE_9
            ),

            ClickActions.MOVE_FROM_CHAR_TO_ISLAND,
            List.of(
                    SceneSprite.ISLAND_1,
                    SceneSprite.ISLAND_2,
                    SceneSprite.ISLAND_3,
                    SceneSprite.ISLAND_4,
                    SceneSprite.ISLAND_5,
                    SceneSprite.ISLAND_6,
                    SceneSprite.ISLAND_7,
                    SceneSprite.ISLAND_8,
                    SceneSprite.ISLAND_9,
                    SceneSprite.ISLAND_10,
                    SceneSprite.ISLAND_11,
                    SceneSprite.ISLAND_12
            ),

            ClickActions.CHAR_CARD_SELECTION,
            List.of(
                    SceneSprite.CHAR_CARD_1,
                    SceneSprite.CHAR_CARD_2,
                    SceneSprite.CHAR_CARD_3
            )
    );

    /**
     * Source sprite (the firstly clicked sprite)
     */
    private SceneSprite source;

    /**
     * Destination sprite (the secondly clicked sprite)
     */
    private SceneSprite destination;

    private ClickActions decodedAction;

    private ClickEventHandler() {
        source = null;
        destination = null;
        decodedAction = null;
    }

    public static ClickEventHandler getInstance() {
        if (instance == null)
            instance = new ClickEventHandler();
        return instance;
    }

    /**
     * Adds an event in a continuous fashion,
     * that is, treating the queue as a circular buffer
     * when it's full.
     *
     * @param clickedSprite the clicked sprite
     */
    public void registerEvent(SceneSprite clickedSprite) {
        if (source == null) {
            source = clickedSprite;
        } else if (destination == null)
            destination = clickedSprite;
        else {
            source = destination;
            destination = clickedSprite;
        }
    }

    /**
     * Removes every event from the queue
     */
    public synchronized void flushEventQueue() {
        source = null;
        destination = null;
    }

    public boolean hasEnoughEvents() {
        return source != null && destination != null;
    }

    /**
     * checks if the user clicked 2 times on the same card,
     * to tell if it's a valid selection
     *
     * @return true if it's a correct selection, false otherwise
     * (i.e. clicks on different cards)
     */
    private boolean isCorrectSelection() {
        String[] splitted = source.name().split("_");
        int sourceIndex = Integer.parseInt(splitted[splitted.length - 1]);

        splitted = destination.name().split("_");
        int destIndex = Integer.parseInt(splitted[splitted.length - 1]);

        return sourceIndex == destIndex;
    }

    public SceneSprite getSource() throws NoClickEventException {
        if (source == null)
            throw new NoClickEventException("not enough events in the queue");
        return source;
    }

    public SceneSprite getDestination() throws NoClickEventException {
        if (destination == null)
            throw new NoClickEventException("not enough events in the queue");
        return destination;
    }

    private boolean entranceSpotIsEmpty() {
        return SceneInfoManager.getInstance().getEntranceColor(destination) == null;
    }

    public boolean isSwapAction(ClickActions action) {
        return action == ClickActions.SWAP_STUDENT_FROM_HALL_TO_ENTRANCE ||
                action == ClickActions.SWAP_FROM_CHAR_STUDENT_TO_ENTRANCE;
    }

    private void decodeAction() {
        // keeps track of which events could be associated to the click sequence
        List<ClickActions> eligibleEvents = new ArrayList<>();

        // check if the source belongs to some event
        for (Map.Entry<ClickActions, List<SceneSprite>> el : possibleSources.entrySet()) {
            if (el.getValue().contains(source))
                eligibleEvents.add(el.getKey());
        }

        decodedAction = null;
        for (Map.Entry<ClickActions, List<SceneSprite>> el : possibleDestinations.entrySet()) {
            if (el.getValue().contains(destination) && eligibleEvents.contains(el.getKey())) {
                ClickActions electedAction = el.getKey();
                // check for correct selection in case it's one
                if (electedAction == ClickActions.CHAR_CARD_SELECTION && !isCorrectSelection() ||
                        isSwapAction(electedAction) && entranceSpotIsEmpty())
                    decodedAction = null;
                else
                    decodedAction = electedAction;
            }
        }
    }

    /**
     * Returns the decoded action
     *
     * @param inGameCharacters the list of available characters
     * @return the decoded action
     */
    public ClickActions getAction(List<Character> inGameCharacters) {
        decodeAction();
        CharacterType characterType = null;
        if(inGameCharacters != null && SemanticCharacterActionFilter.isCharacterAction(decodedAction)) {
            characterType = inGameCharacters.get(source.getCharacterId()).getCharacterType();
        }
        if (SemanticCharacterActionFilter.isMeaningfulAction(decodedAction, characterType)) {
            return decodedAction;
        }
        else
            return null;
    }

    /**
     * Checks if an action complies with a given semantic
     * Useful only for character actions
     */
    private class SemanticCharacterActionFilter {
        /**
         * The valid source cards for every character action
         */
        private static final Map<ClickActions, List<CharacterType>> meaningfulSources = Map.of(
                ClickActions.MOVE_FROM_CHAR_STUDENT_TO_ISLAND,
                List.of(CharacterType.DIONYSUS),

                ClickActions.MOVE_FROM_CHAR_STUDENT_TO_HALL,
                List.of(CharacterType.APHRODITE),

                ClickActions.SWAP_FROM_CHAR_STUDENT_TO_ENTRANCE,
                List.of(CharacterType.JESTER),

                ClickActions.MOVE_FROM_CHAR_TO_ISLAND,
                List.of(
                        CharacterType.CIRCE,
                        CharacterType.ORIFLAMME
                ),

                ClickActions.CHAR_CARD_SELECTION,
                List.of(
                        CharacterType.DAIRYMAN,
                        CharacterType.ERMES,
                        CharacterType.CENTAUR,
                        CharacterType.KNIGHT,
                        CharacterType.GOOMBA,
                        CharacterType.THIEF,
                        CharacterType.BARD
                )
        );

        private static boolean isCharacterAction(ClickActions action) {
            return action == ClickActions.MOVE_FROM_CHAR_STUDENT_TO_ISLAND ||
                    action == ClickActions.MOVE_FROM_CHAR_STUDENT_TO_HALL ||
                    action == ClickActions.SWAP_FROM_CHAR_STUDENT_TO_ENTRANCE ||
                    action == ClickActions.MOVE_FROM_CHAR_TO_ISLAND ||
                    action == ClickActions.CHAR_CARD_SELECTION;
        }

        /**
         * Checks if the decoded action is actually meaningful,
         * useful only in case of actions involving character cards
         * (e.g. click from charCard to Island, but the source card is Knight...)
         *
         * @return true if the action is meaningful (always in case of non-character actions)
         * false otherwise
         */
        private static boolean isMeaningfulAction(ClickActions action, CharacterType source) {
            if (!isCharacterAction(action))
                return true;

            return meaningfulSources.get(action).contains(source);
        }
    }
}
