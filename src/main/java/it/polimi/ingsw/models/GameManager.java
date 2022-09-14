package it.polimi.ingsw.models;

import it.polimi.ingsw.models.components.*;
import it.polimi.ingsw.models.components.Board;
import it.polimi.ingsw.models.components.hall.Hall;
import it.polimi.ingsw.models.components.hall.HallListener;
import it.polimi.ingsw.models.components.characters.Character;
import it.polimi.ingsw.models.components.characters.CharacterType;
import it.polimi.ingsw.models.components.characters.effects.EffectArgs;
import it.polimi.ingsw.models.components.characters.effects.Effects;
import it.polimi.ingsw.models.constants.GameConstants;
import it.polimi.ingsw.models.exceptions.*;
import it.polimi.ingsw.models.operations.GameOperations;
import it.polimi.ingsw.models.state.GameState;
import it.polimi.ingsw.models.state.Stage;

import java.util.*;

public class GameManager implements HallListener {
    private final GameState gameState;
    private final GameConstants gameConstants;

    /**
     * @param numPlayers  number of players for this game (2, 3 or 4).
     * @param expertMatch true if the match is the expert version, false otherwise.
     * @throws IllegalArgumentException if the number of players is illegal.
     */
    public GameManager(int numPlayers, boolean expertMatch) {
        gameState = new GameState(numPlayers, expertMatch);
        gameConstants = GameConstants.fromNumPlayers(numPlayers);

        GameOperations.setAvailableWizards(gameState);
        GameOperations.setAvailableTowers(gameState);
        gameState.setStage(Stage.WAIT_FOR_PLAYERS);
    }

    /**
     * Create GameManager from pre-existing game state.
     *
     * @param gameState the state of the game.
     */
    public GameManager(GameState gameState) {
        this.gameState = gameState;
        gameConstants = GameConstants.fromNumPlayers(gameState.getNumPlayers());

        restoreHallListeners(gameState);
        restoreCharacterEffects(gameState);
    }

    private void restoreHallListeners(GameState state) {
        for (Player player : state.getPlayers()) {
            player.getBoard().getHall().setHallListener(this);
        }
    }

    private void restoreCharacterEffects(GameState state) {
        if (state.isExpertMatch()) {
            for (Character character : state.getExpertAttrs().getCharacters()) {
                switch (character.getCharacterType()) {
                    case DIONYSUS -> character.setEffect(Effects.ONE_STUDENT_TO_ISLAND);
                    case DAIRYMAN -> character.setEffect(Effects.GET_PROF_ON_STUDENT_TIE);
                    case ORIFLAMME -> character.setEffect(Effects.CALCULATE_INFLUENCE_ON_ISLAND);
                    case ERMES -> character.setEffect(Effects.ADD_TWO_MOTHER_NATURE_STEPS);
                    case CIRCE -> character.setEffect(Effects.BLOCK_ISLAND);
                    case CENTAUR -> character.setEffect(Effects.IGNORE_TOWERS);
                    case JESTER -> character.setEffect(Effects.EXCHANGE_STUDENTS_BETWEEN_CHARACTER_AND_ENTRANCE);
                    case KNIGHT -> character.setEffect(Effects.ADD_TWO_INFLUENCE_POINTS);
                    case GOOMBA -> character.setEffect(Effects.IGNORE_STUDENT_COLOR);
                    case BARD -> character.setEffect(Effects.EXCHANGE_STUDENTS_BETWEEN_HALL_AND_ENTRANCE);
                    case APHRODITE -> character.setEffect(Effects.ONE_STUDENT_TO_HALL);
                    case THIEF -> character.setEffect(Effects.REMOVE_STUDENTS);
                }
            }
        }
    }

    public GameState getGameState() {
        return gameState;
    }

    // PLAYERS enter the match methods

    /**
     * Add a player to the game. When a player is added the wizard and the tower chosen
     * are removed from the ones available.
     *
     * @param name   the name of the player.
     * @param wizard the wizard chosen by the player.
     * @param tower  the tower chosen by the player.
     * @throws IllegalArgumentException if players with same name, wizard or tower are added.
     */
    public void addPlayer(String name, Wizard wizard, Tower tower) {
        if (!gameState.isStage(Stage.WAIT_FOR_PLAYERS))
            throw new IllegalCallException(gameState.getStage());

        Player player = createPlayer(name, wizard, tower);
        GameOperations.addPlayer(gameState, player);

        proceedToNextStage();
    }

    private Player createPlayer(String name, Wizard wizard, Tower tower) {
        boolean leader = isLeader(tower);
        Board board = createBoard(tower, leader);
        return new Player(wizard, name, List.of(Assistant.values()), board, leader);
    }

    private boolean isLeader(Tower tower) {
        if (gameState.getNumPlayers() == 4) {
            int counter = 0;
            for (Tower availableTower : gameState.getAvailableTowers())
                if (availableTower.equals(tower))
                    counter++;
            return counter == 2;
        } else {
            return true;
        }
    }

    /**
     * Create a board with all the towers.
     * The number of towers depends on the number of players.
     * GameManager will listen for coin events.
     *
     * @param tower tower type
     * @return the board
     */
    private Board createBoard(Tower tower, boolean leader) {
        Board board = new Board(tower, gameConstants.getMaxTowersOnBoard(), gameConstants.getMaxStudentsOnEntrance());
        board.getHall().setHallListener(this);

        if (leader)
            for (int i = 0; i < gameConstants.getMaxTowersOnBoard(); i++)
                board.receiveTower(tower);

        return board;
    }

    // PREPARATION STAGE methods

    /**
     * Sets up the game
     */
    public void preparation() {
        if (!gameState.isStage(Stage.PREPARATION))
            throw new IllegalCallException(gameState.getStage());

        GameOperations.preparation(gameState);

        proceedToNextStage();
    }

    // PLANNING STAGE

    /**
     * This is the first step in the planning stage:
     * - Fill the clouds with students.
     * - The number of students depends on the number of players.
     * - When the bag is empty no more students are added to the clouds.
     */
    public void fillClouds() {
        if (!gameState.isStage(Stage.PLANNING_FILL_CLOUDS))
            throw new IllegalCallException(gameState.getStage());

        for (Cloud cloud : gameState.getClouds()) {
            Bag bag = gameState.getBag();
            for (int i = 0; i < gameConstants.getNumStudentsOnCloud(); ++i) {
                if (!bag.isEmpty()) {
                    Student student = bag.drawStudent();
                    cloud.receiveStudent(student);
                }
            }
        }

        proceedToNextStage();
    }

    /**
     * This is the second step of the Planning stage.
     * Tell what assistant has played the current player.
     *
     * @param assistant the assistant played by the player
     * @throws IllegalMoveException            if the player doesn't have the assistant or
     *                                         if it's called during Action stage
     * @throws PlayerIdAlreadyPresentException if it's called two times for the same player
     */
    public void playAssistant(Assistant assistant) {
        if (!gameState.isStage(Stage.PLANNING_PLAY_ASSISTANTS))
            throw new IllegalCallException(gameState.getStage());

        List<Assistant> playableAssistants = GameOperations.getPlayableAssistants(gameState);

        if (!playableAssistants.contains(assistant))
            throw new IllegalMoveException("Assistant " + assistant.name() + " has already been played by another player");

        gameState.getCurrentPlayer().playAssistant(assistant);

        proceedToNextStage();
    }

    // ACTION STAGE

    /**
     * This is the first step of ACTION stage.
     * Move a student from the current player Entrance to his Hall.
     *
     * @param student the student to move from the entrance.
     * @throws IllegalMoveException if the student cannot be removed from entrance or added to the Hall.
     */
    public void moveStudentFromEntranceToHall(Student student) {
        if (!gameState.isStage(Stage.ACTION_MOVE_STUDENTS))
            throw new IllegalCallException(gameState.getStage());

        Player player = gameState.getCurrentPlayer();
        Hall hall = player.getBoard().getHall();
        if (!hall.canReceiveStudent(student))
            throw new IllegalMoveException("Hall cannot receive " + student);
        boolean removed = player.getBoard().getEntrance().removeStudent(student);
        if (!removed)
            throw new IllegalMoveException("Cannot remove " + student + " from entrance of " + player);
        hall.receiveStudent(student);

        proceedToNextStage();
    }

    /**
     * This is the first step of ACTION stage.
     * Move a student from the current player Entrance to the specified island
     *
     * @param student  the student to move from the entrance.
     * @param islandPosition the id of the island where the student will be placed.
     * @throws IllegalMoveException if the student cannot be removed from entrance or added to the Island.
     */
    public void moveStudentFromEntranceToIsland(Student student, int islandPosition) {
        if (!gameState.isStage(Stage.ACTION_MOVE_STUDENTS))
            throw new IllegalCallException(gameState.getStage());

        Player player = gameState.getCurrentPlayer();
        Island island = gameState.getIslandByPosition(islandPosition);
        if (!island.canReceiveStudent(student))
            throw new IllegalMoveException(island + " cannot receive " + student);
        boolean removed = player.getBoard().getEntrance().removeStudent(student);
        if (!removed)
            throw new IllegalMoveException("Cannot remove " + student + " from entrance of " + player);
        island.receiveStudent(student);

        proceedToNextStage();
    }

    /**
     * This is the second step of ACTION stage.
     * Move mother nature with the specified number of steps.
     *
     * @param steps mother nature steps to perform.
     * @throws IllegalMoveException if the number of steps is not valid
     */
    public void moveMotherNature(int steps) {
        if (!gameState.isStage(Stage.ACTION_MOVE_MOTHER_NATURE))
            throw new IllegalCallException(gameState.getStage());

        GameOperations.moveMotherNature(gameState, steps);

        proceedToNextStage();
    }

    /**
     * This is the third step ot the ACTION stage
     * Moves the students on the selected cloud to the current player entrance
     *
     * @param cloudId the id of the selected cloud
     */
    public void pickStudentsFromCloud(int cloudId) {
        if (!gameState.isStage(Stage.ACTION_TAKE_STUDENTS_FROM_CLOUD))
            throw new IllegalCallException(gameState.getStage());

        Cloud cloud = gameState.getCloudById(cloudId);
        List<Student> pickedStudents = cloud.pickStudents();

        Player currentPlayer = gameState.getCurrentPlayer();
        Entrance currentPlayerEntrance = currentPlayer.getBoard().getEntrance();
        for (Student student : pickedStudents) {
            currentPlayerEntrance.receiveStudent(student);
        }

        proceedToNextStage();
    }

    /**
     * This is the last step of the action stage. It's important to have this stage
     * otherwise player won't be able to play a character after picking students from cloud.
     */
    public void endTurn() {
        if (!gameState.isStage(Stage.ACTION_END_TURN))
            throw new IllegalCallException(gameState.getStage());

        proceedToNextStage();
    }

    public void nextRound() {
        if (!gameState.isStage(Stage.ROUND_END))
            throw new IllegalCallException(gameState.getStage());

        proceedToNextStage();
    }

    /**
     * Plays the given character.
     * This can be played in any moment of the ACTION stage
     *
     * @param characterType the character to be played
     * @param effectArgs    the parameters for applying the character effect
     * @throws IllegalMoveException if the character is played in non-expert matches
     */
    public void playCharacter(CharacterType characterType, EffectArgs effectArgs) {
        if (!Stage.isActionStage(gameState.getStage()))
            throw new IllegalCallException(gameState.getStage());

        GameOperations.playCharacter(gameState, characterType, effectArgs);
    }

    private void proceedToNextStage() {
        Stage nextStage;

        if (!gameState.isStage(Stage.WAIT_FOR_PLAYERS) && GameOperations.isGameOver(gameState)) {
            nextStage = Stage.GAME_OVER;
            gameState.setStage(nextStage);
            endMatch();
        } else {
            nextStage = switch (gameState.getStage()) {
                case WAIT_FOR_PLAYERS ->
                    shouldWaitForOtherPlayers()
                            ? Stage.WAIT_FOR_PLAYERS
                            : Stage.PREPARATION;
                case PREPARATION -> {
                    GameOperations.preparePlanningQueue(gameState);
                    gameState.resetTurn();
                    yield Stage.PLANNING_FILL_CLOUDS;
                }
                case PLANNING_FILL_CLOUDS -> Stage.PLANNING_PLAY_ASSISTANTS;
                case PLANNING_PLAY_ASSISTANTS -> {
                    if (!gameState.isLastTurn()) {
                        gameState.nextTurn();
                        yield Stage.PLANNING_PLAY_ASSISTANTS;
                    } else {
                        gameState.resetTurn();
                        GameOperations.prepareActionQueue(gameState);
                        setStudentsToMove();
                        yield Stage.ACTION_MOVE_STUDENTS;
                    }
                }
                case ACTION_MOVE_STUDENTS -> {
                    gameState.decrementStudentsToMove();
                    if (mustMoveOtherStudents())
                        yield Stage.ACTION_MOVE_STUDENTS;
                    else
                        yield Stage.ACTION_MOVE_MOTHER_NATURE;
                }
                case ACTION_MOVE_MOTHER_NATURE ->
                    allCloudsEmpty()
                            ? Stage.ACTION_END_TURN
                            : Stage.ACTION_TAKE_STUDENTS_FROM_CLOUD;
                case ACTION_TAKE_STUDENTS_FROM_CLOUD -> Stage.ACTION_END_TURN;
                case ACTION_END_TURN -> {
                    GameOperations.disableEffects(gameState);
                    GameOperations.makeCharactersPlayable(gameState);
                    if (!gameState.isLastTurn()) {
                        gameState.nextTurn();
                        setStudentsToMove();
                        yield Stage.ACTION_MOVE_STUDENTS;
                    } else {
                        gameState.resetTurn();
                        yield Stage.ROUND_END;
                    }
                }
                case ROUND_END -> {
                    GameOperations.preparePlanningQueue(gameState);
                    yield Stage.PLANNING_FILL_CLOUDS;
                }
                case GAME_OVER -> Stage.GAME_OVER;
            };
        }

        gameState.setStage(nextStage);
    }

    /**
     * This method must be called when the game ends.
     * It sets the winner in the game state if there is any.
     */
    private void endMatch() {
        if (!gameState.isStage(Stage.GAME_OVER))
            throw new IllegalCallException(gameState.getStage());

        Tower winnerTower = GameOperations.getWinner(gameState);
        gameState.setWinner(winnerTower);
    }

    private boolean allCloudsEmpty() {
        for (Cloud cloud : gameState.getClouds())
            if (!cloud.isEmpty())
                return false;
        return true;
    }

    private boolean shouldWaitForOtherPlayers() {
        return gameState.getPlayers().size() < gameState.getNumPlayers();
    }

    private void setStudentsToMove() {
        gameState.setStudentsToMove(gameConstants.getNumStudentsToMoveOutFromEntrance());
    }

    private boolean mustMoveOtherStudents() {
        return gameState.getStudentsToMove() > 0;
    }

    @Override
    public void hallChanged() {
        GameOperations.updateProfessorsOwners(gameState);
    }

    @Override
    public void getCoin() {
        if (gameState.isExpertMatch())
            GameOperations.giveOneCoinToPlayer(gameState, gameState.getCurrentPlayer());
    }
}
