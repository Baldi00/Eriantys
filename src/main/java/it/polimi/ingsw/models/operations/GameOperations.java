package it.polimi.ingsw.models.operations;

import it.polimi.ingsw.models.components.*;
import it.polimi.ingsw.models.components.characters.Characters;
import it.polimi.ingsw.models.constants.GameConstants;
import it.polimi.ingsw.models.state.ExpertAttrs;
import it.polimi.ingsw.models.state.GameState;
import it.polimi.ingsw.models.components.characters.Character;
import it.polimi.ingsw.models.components.characters.CharacterType;
import it.polimi.ingsw.models.components.characters.effects.EffectArgs;
import it.polimi.ingsw.models.exceptions.GameNotOverException;
import it.polimi.ingsw.models.exceptions.IllegalMoveException;
import it.polimi.ingsw.models.operations.influence.IgnoreStudentInfluence;
import it.polimi.ingsw.models.operations.influence.IgnoreTowerInfluence;
import it.polimi.ingsw.models.operations.influence.InfluenceCalculator;
import it.polimi.ingsw.models.operations.influence.StandardInfluence;
import it.polimi.ingsw.models.state.Stage;

import java.security.SecureRandom;
import java.util.*;

/**
 * Utility class which contains game operations.
 * Instead of testing operations by reproducing entire matches, this class helps
 * to test the correct behaviours of the operations by exposing static methods.
 */
public class GameOperations {

    private GameOperations() {
        // hide constructor
    }

    /**
     * Set the available wizards for the game.
     *
     * @param state the state of the game.
     */
    public static void setAvailableWizards(GameState state) {
        List<Wizard> wizards = Arrays.stream(Wizard.values()).toList();
        state.setAvailableWizards(wizards);
    }

    /**
     * Set the available towers for the match.
     * - 2 players: 1 white / 1 black
     * - 3 players: 1 white / 1 black / 1 grey
     * - 4 players: 2 whites / 2 blacks
     *
     * @param state the state of the game.
     * @throws IllegalArgumentException if the number of players is not in 2 <= numPlayers <= 4 range.
     */
    public static void setAvailableTowers(GameState state) {
        int numPlayers = state.getNumPlayers();
        switch (numPlayers) {
            case 2 -> state.setAvailableTowers(List.of(Tower.BLACK, Tower.WHITE));
            case 3 -> state.setAvailableTowers(Arrays.stream(Tower.values()).toList());
            case 4 -> {
                List<Tower> availableTowers = List.of(
                        Tower.BLACK, Tower.BLACK,
                        Tower.WHITE, Tower.WHITE
                );
                state.setAvailableTowers(availableTowers);
            }
            default -> throw new IllegalArgumentException("Cannot create a match with "
                    + numPlayers + " players");
        }
    }

    /**
     * Add a player to the game. When a player is added the wizard and the tower chosen
     * are removed from the ones available.
     *
     * @param newPlayer  the player to add.
     * @throws IllegalArgumentException if players with same name, wizard or tower are already present.
     * @throws IllegalMoveException when trying to add too many players.
     */
    public static void addPlayer(GameState state, Player newPlayer) {
        String name = newPlayer.getName();
        Wizard wizard = newPlayer.getWizard();
        Tower tower = newPlayer.getBoard().getTowerType();

        if (!state.getAvailableWizards().contains(wizard))
            throw new IllegalArgumentException("Wizard has already been chosen");
        if (!state.getAvailableTowers().contains(tower))
            throw new IllegalArgumentException("Tower has already been chosen or is not available for this match");
        for (Player player : state.getPlayers())
            if (player.getName().equals(name))
                throw new IllegalArgumentException("Player Name has already been taken");

        state.addPlayer(newPlayer);

        updateAvailableWizards(state, wizard);
        updateAvailableTowers(state, tower);
    }

    /**
     * @param wizard the wizard to remove from the ones available.
     */
    private static void updateAvailableWizards(GameState state, Wizard wizard) {
        List<Wizard> wizards = state.getAvailableWizards();
        wizards.remove(wizard);
        state.setAvailableWizards(wizards);
    }

    /**
     * @param tower the tower to remove from the ones available.
     */
    private static void updateAvailableTowers(GameState state, Tower tower) {
        List<Tower> towers = state.getAvailableTowers();
        towers.remove(tower);
        state.setAvailableTowers(towers);
    }

    public static void preparePlanningQueue(GameState state) {
        // at the beginning actionQueue is null
        List<Integer> actionQueue = state.getPlayerQueue();
        List<Integer> clockwiseOrder = state.getClockwiseOrder();
        List<Integer> planningQueue = GameOperations.getPlanningQueue(actionQueue, clockwiseOrder);
        state.setPlayerQueue(planningQueue);
    }

    /**
     * Returns the planning queue. The first player is random if the action queue
     * is not provided, otherwise is the first player in the action queue. The other
     * players are in clockwise order starting from the first one.
     *
     * @param actionQueue The action queue, null if it's not available.
     * @param clockwiseOrder A list of players id in clockwise order.
     * @return the planning queue.
     */
    private static List<Integer> getPlanningQueue(List<Integer> actionQueue, List<Integer> clockwiseOrder) {
        List<Integer> planningQueue = new ArrayList<>();
        int numPlayers = clockwiseOrder.size();

        int firstPlayerId;
        if (actionQueue == null) {
            // on first round the first player is chosen randomly
            int firstPlayerIndex = new SecureRandom().nextInt(0, numPlayers);
            firstPlayerId = clockwiseOrder.get(firstPlayerIndex);
        } else {
            // first player in planning queue is the first player in the action queue
            firstPlayerId = actionQueue.get(0);
        }
        planningQueue.add(firstPlayerId);

        // next players are added in "clockwise" order
        int index = clockwiseOrder.indexOf(firstPlayerId);
        int i = (index + 1) % numPlayers;
        while (i != index) {
            int nextPlayer = clockwiseOrder.get(i);
            planningQueue.add(nextPlayer);
            i = (i + 1) % numPlayers;
        }
        return planningQueue;
    }

    public static void prepareActionQueue(GameState state) {
        List<Integer> planningQueue = state.getPlayerQueue();
        List<Assistant> assistants = new ArrayList<>();
        for (Integer playerId : planningQueue) {
            Player player = state.getPlayerById(playerId);
            assistants.add(player.getLastPlayedAssistant());
        }
        List<Integer> actionQueue = GameOperations.getActionQueue(planningQueue, assistants);
        state.setPlayerQueue(actionQueue);
    }

    static List<Integer> getActionQueue(List<Integer> planningQueue, List<Assistant> assistants) {
        ActionQueue actionQueue = new ActionQueue();
        for (int i = 0; i < planningQueue.size(); ++i) {
            actionQueue.add(planningQueue.get(i), assistants.get(i));
        }
        return actionQueue.getPlayerIdQueue();
    }

    public static void preparation(GameState state) {
        setupIslands(state);
        setupClouds(state);
        setupMotherNature(state);
        setupStudentsOnIslands(state);
        setupBag(state);
        setupPlayerEntrances(state);
        setClockwiseOrder(state);
        if (state.isExpertMatch()) {
            setupCharacters(state);
            giveOneCoinToPlayers(state);
        }
    }

    /**
     * Initialize gameState islands. Islands are empty on startup.
     */
    private static void setupIslands(GameState state) {
        List<Island> islands = new ArrayList<>(GameConstants.NUMBER_OF_ISLANDS);
        for (int i = 0; i < GameConstants.NUMBER_OF_ISLANDS; i++) {
            islands.add(new Island(i, 1));
        }
        state.setIslands(islands);
    }

    /**
     * Initialize gameState clouds. Clouds are empty on startup.
     */
    private static void setupClouds(GameState state) {
        GameConstants gameConstants = GameConstants.fromNumPlayers(state.getNumPlayers());
        List<Cloud> clouds = new ArrayList<>(gameConstants.getNumClouds());
        for (int i = 0; i < gameConstants.getNumClouds(); i++) {
            clouds.add(new Cloud(i, gameConstants.getNumStudentsOnCloud()));
        }
        state.setClouds(clouds);
    }

    /**
     * Place Mother Nature on a random island
     */
    private static void setupMotherNature(GameState gameState) {
        int position = new SecureRandom().nextInt(0, GameConstants.NUMBER_OF_ISLANDS);
        gameState.setMotherNaturePosition(position);
    }

    /**
     * Add one student to every island except where there is mother nature and
     * on the opposite island of where is mother nature.
     * NB: mother nature position must be set & islands must be initialized
     */
    private static void setupStudentsOnIslands(GameState gameState) {
        Bag bag = new Bag();
        for (Student student : Student.values()) {
            bag.receiveStudent(student);
            bag.receiveStudent(student);
        }

        int motherNaturePosition = gameState.getMotherNaturePosition();
        int oppositePosition = (motherNaturePosition + (GameConstants.NUMBER_OF_ISLANDS / 2)) % GameConstants.NUMBER_OF_ISLANDS;

        for (Island island : gameState.getIslands()) {
            if (island.getPosition() != motherNaturePosition && island.getPosition() != oppositePosition) {
                island.receiveStudent(bag.drawStudent());
            }
        }
    }

    /**
     * Initialize the bag with the remaining students not placed on the islands.
     */
    private static void setupBag(GameState gameState) {
        int studentsPlacedOnIslands = GameConstants.NUMBER_OF_ISLANDS - 2;
        int studentsForEveryColor = (GameConstants.MAX_STUDENTS_IN_BAG - studentsPlacedOnIslands) / Student.values().length;
        for (Student student : Student.values()) {
            for (int i = 0; i < studentsForEveryColor; ++i) {
                gameState.getBag().receiveStudent(student);
            }
        }
    }

    /**
     * Fill every player's board entrance with students extracted from the bag
     */
    private static void setupPlayerEntrances(GameState gameState) {
        GameConstants gameConstants = GameConstants.fromNumPlayers(gameState.getNumPlayers());
        Bag bag = gameState.getBag();
        for (Player player : gameState.getPlayers()) {
            Entrance entrance = player.getBoard().getEntrance();
            for (int i = 0; i < gameConstants.getMaxStudentsOnEntrance(); ++i) {
                Student student = bag.drawStudent();
                entrance.receiveStudent(student);
            }
        }
    }

    /**
     * Add three different characters to the match
     */
    private static void setupCharacters(GameState gameState) {
        SecureRandom random = new SecureRandom();
        Set<Integer> charactersIds = new TreeSet<>();
        while (charactersIds.size() != GameConstants.NUM_CHARACTERS) {
            int characterId = random.nextInt(CharacterType.values().length);
            charactersIds.add(characterId);
        }
        List<Character> characters = new ArrayList<>();
        for (Integer id : charactersIds) {
            Character character = Characters.get(CharacterType.values()[id]);
            if (character.getStudentsLimit() > 0) {
                giveStudentsToCharacter(gameState, character);
            }
            characters.add(character);
        }
        gameState.getExpertAttrs().setCharacters(characters);
    }

    private static void giveStudentsToCharacter(GameState gameState, Character character) {
        Bag bag = gameState.getBag();
        for (int i = 0; i < character.getStudentsLimit(); ++i) {
            Student student = bag.drawStudent();
            character.receiveStudent(student);
        }
    }

    private static void giveOneCoinToPlayers(GameState gameState) {
        for (Player player : gameState.getPlayers()) {
            giveOneCoinToPlayer(gameState, player);
        }
    }

    /**
     * The player is given a coin only if there are some left in the stock.
     *
     * @param gameState the state of the game.
     * @param player    the player who must receive the coin.
     */
    public static void giveOneCoinToPlayer(GameState gameState, Player player) {
        if (gameState.getExpertAttrs().canGetCoins(1)) {
            gameState.getExpertAttrs().getCoinsFromStock(1);
            player.addCoin();
        }
    }

    private static void setClockwiseOrder(GameState gameState) {
        List<Integer> clockwiseOrder = new ArrayList<>();
        for (Player player : gameState.getPlayers())
            clockwiseOrder.add(player.getId());
        gameState.setClockwiseOrder(clockwiseOrder);
    }

    /**
     * Plays the given character. This method can be called in any moment of the ACTION stage.
     *
     * @param characterType the character to be played
     * @param effectArgs    the parameters for applying the character effect
     * @throws IllegalMoveException if the character is played in non-expert matches, is not one of the three playable
     *                              character, if the player doesn't have enough coins or if the current player has already
     *                              been played another character.
     */
    public static void playCharacter(GameState state, CharacterType characterType, EffectArgs effectArgs) {
        if (!state.isExpertMatch())
            throw new IllegalMoveException("Characters cannot be used in non-expert matches");

        if (state.getExpertAttrs().isCharacterAlreadyPlayed())
            throw new IllegalMoveException("Cannot play two characters in the same turn");

        Character character;
        try {
            character = state.getExpertAttrs().getCharacterByType(characterType);
        } catch (NoSuchElementException e) {
            throw new IllegalMoveException(characterType + " cannot be played in this match");
        }

        Player currentPlayer = state.getCurrentPlayer();

        int removedCoins = character.getCost();
        currentPlayer.removeCoins(removedCoins);
        int coinsToStock = character.performEffect(effectArgs, removedCoins);
        state.getExpertAttrs().addCoinsToStock(coinsToStock);

        state.getExpertAttrs().setCharacterAlreadyPlayed(true);
    }

    /**
     * Merge three islands.
     *
     * @param island1 the first island.
     * @param island2 the second island.
     * @param island3 the third island.
     * @return the island resulting from the merge.
     * @throws IllegalMoveException when islands are not adjacent or the tower type is different.
     */
    static Island mergeIslands(Island island1, Island island2, Island island3) {
        if (areAdjacentIslands(island1, island2))
            return mergeIslands(mergeIslands(island1, island2), island3);
        else
            return mergeIslands(mergeIslands(island1, island3), island2);
    }

    /**
     * Merge two islands.
     * The resulting island will have the position of the island before the other island,
     * a dimension resulting from the sum of the dimensions, all the students on the two
     * islands and all the towers on the islands.
     *
     * @param island1 the first island.
     * @param island2 the second island.
     * @return one island which is the result of the merge.
     * @throws IllegalMoveException when islands are not adjacent or the tower type is different.
     */
    static Island mergeIslands(Island island1, Island island2) {
        if (!areAdjacentIslands(island1, island2))
            throw new IllegalMoveException("Cannot merge not adjacent islands.");

        int position = island1.isBefore(island2) ? island1.getPosition() : island2.getPosition();
        int dimension = getIslandsDimensionSum(List.of(island1, island2));

        Island newIsland = new Island(position, dimension);

        if (!island1.getTowerType().equals(island2.getTowerType()))
            throw new IllegalMoveException("Cannot merge islands with different towers");

        Tower tower = island1.getTowerType();
        int numTowers = island1.getNumTowers() + island2.getNumTowers();
        giveTowersToIsland(newIsland, tower, numTowers);

        for (Student student : Student.values()) {
            int numStudents = island1.getNumStudent(student) + island2.getNumStudent(student);
            giveStudentsToIsland(newIsland, student, numStudents);
        }

        return newIsland;
    }

    /**
     * NB: An island is not considered adjacent to itself.
     *
     * @param island1 the first island.
     * @param island2 the second island.
     * @return true if island1 is before island 2 or vice versa, false otherwise.
     */
    private static boolean areAdjacentIslands(Island island1, Island island2) {
        return island1.isBefore(island2) || island2.isBefore(island1);
    }

    /**
     * Returns the sum of the islands dimension.
     * NB: This method doesn't check if the islands are adjacent.
     *
     * @param islands the list of islands.
     * @return the sum of islands dimension.
     */
    private static int getIslandsDimensionSum(List<Island> islands) {
        int sum = 0;
        for (Island island : islands)
            sum += island.getDimension();
        return sum;
    }

    /**
     * Give towers to the island.
     *
     * @param island   the island that should receive towers.
     * @param tower    the tower to give to the island.
     * @param quantity the number of towers to give to the island.
     */
    private static void giveTowersToIsland(Island island, Tower tower, int quantity) {
        for (int i = 0; i < quantity; ++i) {
            island.receiveTower(tower);
        }
    }

    /**
     * Give students to the island.
     *
     * @param island   the island that should receive towers.
     * @param student  the student to give to the island.
     * @param quantity the number of students to give to the island.
     */
    private static void giveStudentsToIsland(Island island, Student student, int quantity) {
        for (int i = 0; i < quantity; ++i) {
            island.receiveStudent(student);
        }
    }

    /**
     * @param islands the list of islands.
     * @return a list with all the islands that can be merged, an empty list
     * if there are no islands to merge.
     */
    static List<Island> getMergeableIslands(List<Island> islands) {
        List<Island> copy = new ArrayList<>(islands);
        Collections.sort(copy);
        // we must check also if the first and last islands can be merged
        copy.add(copy.get(0));

        Set<Island> mergeableIslands = new TreeSet<>();
        for (int i = 0; i < copy.size() - 1; ++i) {
            if (areMergeableIslands(copy.get(i), copy.get(i + 1))) {
                mergeableIslands.add(copy.get(i));
                mergeableIslands.add(copy.get(i + 1));
            }
        }
        return mergeableIslands.stream().toList();
    }

    /**
     * @param island1 the first island.
     * @param island2 the second island.
     * @return true if the two islands can be merged together, false otherwise.
     */
    private static boolean areMergeableIslands(Island island1, Island island2) {
        return haveSameTower(island1, island2) && areAdjacentIslands(island1, island2);
    }

    /**
     * Merge islands if there are any.
     */
    public static void mergeIslands(GameState gameState) {
        List<Island> islands = gameState.getIslands();
        List<Island> mergeableIslands = GameOperations.getMergeableIslands(islands);
        if (mergeableIslands.size() >= 2) {
            Island newIsland;
            newIsland = switch (mergeableIslands.size()) {
                case 2 -> GameOperations.mergeIslands(mergeableIslands.get(0), mergeableIslands.get(1));
                case 3 -> GameOperations.mergeIslands(mergeableIslands.get(0), mergeableIslands.get(1),
                        mergeableIslands.get(2));
                default -> throw new IllegalMoveException("Is it really possible to have " + mergeableIslands.size() +
                        " islands to merge???");
            };

            gameState.setMotherNaturePosition(newIsland.getPosition());

            islands.removeAll(mergeableIslands);
            islands.add(newIsland);
            Collections.sort(islands);
            gameState.setIslands(islands);

            if (gameState.isExpertMatch()) {
                moveBlocksOnTheNewIsland(gameState, mergeableIslands, newIsland);
            }
        }
    }

    private static void moveBlocksOnTheNewIsland(GameState gameState, List<Island> mergeableIslands, Island newIsland) {
        List<Island> blockedIslands = gameState.getExpertAttrs().getBlockedIslands();
        int numBlocks = 0;
        for (Island island : mergeableIslands) {
            if (blockedIslands.contains(island)) {
                numBlocks++;
                blockedIslands.remove(island);
            }
        }
        if (numBlocks != 0) {
            while (numBlocks != 0) {
                blockedIslands.add(newIsland);
                numBlocks--;
            }
            gameState.getExpertAttrs().setBlockedIslands(blockedIslands);
        }
    }

    /**
     * @param island1 the first island.
     * @param island2 the second island.
     * @return true if the islands have the same tower, false otherwise.
     */
    private static boolean haveSameTower(Island island1, Island island2) {
        return island1.hasTowers() &&
                island2.hasTowers() &&
                island1.getTowerType().equals(island2.getTowerType());
    }

    /**
     * Updates the owners of the professors. A player owns a prof if
     * he has the maximum number of students of the same of color of the prof
     * on the hall. When multiple players have the same number of students of
     * one color, then no one own the professor.
     *
     * @param state the state of the game
     */
    public static void updateProfessorsOwners(GameState state) {
        List<Player> players = state.getPlayers();
        for (Student student : Student.values()) {
            List<Player> results = GameOperations.getPlayersWithMoreStudentsOfColor(players, student);

            Player player = null;
            if (results.size() > 1 && state.isExpertMatch()) {
                if (state.getExpertAttrs().getProfOwnershipOnTie()) {
                    Player owner = state.getCurrentPlayer();
                    if (results.contains(owner))
                        player = owner;
                }
            } else if (results.size() == 1) {
                player = results.get(0);
            }

            if (player == null)
                state.setProfessorOwner(student, null);
            else
                state.setProfessorOwner(student, player.getId());
        }
    }

    /**
     * @param players the list of students.
     * @param student the student color.
     * @return the player with more students of the specified color. If more players have the same
     * number of students then both players are returned.
     */
    private static List<Player> getPlayersWithMoreStudentsOfColor(List<Player> players, Student student) {
        List<Player> result = new ArrayList<>();
        // players with 0 students doesn't count
        int maxStudents = 1;
        for (Player player : players) {
            int currentStudents = player.getBoard().getHall().getNumStudentsByColor(student);
            if (maxStudents < currentStudents) {
                result.clear();
                result.add(player);
                maxStudents = currentStudents;
            } else if (maxStudents == currentStudents) {
                result.add(player);
            }
        }
        return result;
    }

    /**
     * Move mother nature of the specified steps. Once mother nature has been moved on the
     * new island, it calculates the influence on that island and merges it with other islands
     * if there are any.
     * 
     * @param state the state of the game.
     * @param steps the number of steps mother nature has to perform.
     *              
     * @throws IllegalMoveException if mother nature isn't moved for at least one step,
     *                              or when is moved for too many steps.
     */
    public static void moveMotherNature(GameState state, int steps) {
        if (steps < GameConstants.MIN_MOTHER_NATURE_STEPS)
            throw new IllegalMoveException("Mother nature must perform at least " + GameConstants.MIN_MOTHER_NATURE_STEPS + " steps");

        if (steps > getMaxMotherNatureSteps(state))
            throw new IllegalMoveException("Mother nature cannot perform " + steps + " steps");

        List<Island> islands = state.getIslands();
        Island island = state.getIslandByPosition(state.getMotherNaturePosition());
        int index = getIndexOfIsland(islands, island);

        int nextIndex = (index + steps) % islands.size();
        Island nextIsland = islands.get(nextIndex);
        state.setMotherNaturePosition(nextIsland.getPosition());

        updateIslandConqueror(state, nextIsland);
        mergeIslands(state);
    }

    /**
     * @param islands the list of islands.
     * @param island the to search in the list.
     * @return the index of the island in the list.
     * @throws NoSuchElementException if island is not present in the list.
     */
    private static int getIndexOfIsland(List<Island> islands, Island island) {
        for (int i = 0; i < islands.size(); ++i)
            if (islands.get(i).equals(island))
                return i;
        throw new NoSuchElementException("Island index not found. If this exception occurs it means " +
                "that mother nature position is calculated in the wrong way");
    }

    private static int getMaxMotherNatureSteps(GameState state) {
        int steps = state.getCurrentPlayer().getLastPlayedAssistant().getMotherNatureSteps();
        int additionalSteps = getAdditionalMotherNatureSteps(state);
        return steps + additionalSteps;
    }

    private static int getAdditionalMotherNatureSteps(GameState state) {
        return state.isExpertMatch() ? state.getExpertAttrs().getAdditionalMotherNatureSteps() : 0;
    }

    /**
     * Calculates the influence on the island and updates the conqueror
     * of the island.
     *
     * @param state the state of the game.
     * @param island the island where the influence is calculated.
     * @throws IllegalMoveException if the island doesn't exist in gameState.
     */
    public static void updateIslandConqueror(GameState state, Island island) {
        if (state.isExpertMatch()) {
            boolean blocked = state.getExpertAttrs().isIslandBlocked(island);
            if (blocked) {
                returnBlockToCharacter(state, island);
                return;
            }
        }

        if (!state.getIslands().contains(island))
            throw new IllegalMoveException("Given island doesn't exist");

        Player conqueror = getIslandConqueror(state, island);
        Player prevConqueror = null;
        if (island.hasTowers())
            prevConqueror = state.getPlayerByTower(island.getTowerType());
        if (conqueror != null && !conqueror.equals(prevConqueror)) {
            if (prevConqueror != null)
                removePreviousConquerorTowersFromIsland(island, prevConqueror);
            putConquerorTowersOnIsland(island, conqueror);
        }
    }

    private static void returnBlockToCharacter(GameState state, Island island) {
        Character character = state.getExpertAttrs().getCharacterByType(CharacterType.CIRCE);
        state.getExpertAttrs().removeBlockFromIsland(island);
        character.receiveBlock();
    }

    /**
     * Get the player who conquered the island. It's important to pass the island
     * because there are cases where island conqueror is calculated not only for
     * the island where is mother nature.
     * In a 4 player match is returned the leader of the team.
     *
     * @param state  the state of the game.
     * @param island the island where the conqueror is calculated.
     * @return the player who conquered the island, null if no one can conquer the island.
     */
    private static Player getIslandConqueror(GameState state, Island island) {
        InfluenceCalculator influenceCalc = getInfluenceCalculator(state);

        Map<Tower, Integer> influencePoints = new EnumMap<>(Tower.class);
        for (Tower tower : Tower.values())
            influencePoints.put(tower, 0);

        for (Player player : state.getPlayers()) {
            List<Student> professors = state.getPlayerProfessors(player);

            int playerInfluence;
            if (state.isExpertMatch() && state.isCurrentPlayer(player)) {
                int additionalPoints = state.getExpertAttrs().isTwoAdditionalPoints() ? 2 : 0;
                playerInfluence = influenceCalc.calculateInfluence(island, player, professors, additionalPoints);
            } else {
                playerInfluence = influenceCalc.calculateInfluence(island, player, professors);
            }

            Tower playerTower = player.getBoard().getTowerType();
            influencePoints.put(playerTower, influencePoints.get(playerTower) + playerInfluence);
        }

        int maxInfluence = 0;
        Player conqueror = null;
        for (Tower tower : Tower.values()) {
            int influence = influencePoints.get(tower);
            if (influence > maxInfluence) {
                maxInfluence = influence;
                conqueror = state.getPlayerByTower(tower);
            } else if (influence == maxInfluence) {
                // when multiple players have the same influence, no one can conquer the island
                conqueror = null;
            }
        }

        return conqueror;
    }

    /**
     * @param gameState the state of the game.
     * @return the appropriate influence calculator depending on state of the game.
     */
    private static InfluenceCalculator getInfluenceCalculator(GameState gameState) {
        if (gameState.isExpertMatch()) {
            if (gameState.getExpertAttrs().isIgnoreTowers())
                return new IgnoreTowerInfluence();

            Student ignoredStudent = gameState.getExpertAttrs().getIgnoredStudentType();
            if (ignoredStudent != null)
                return new IgnoreStudentInfluence(ignoredStudent);
        }
        return new StandardInfluence();
    }

    /**
     * Remove all the towers from the island and return them to the player who
     * previously conquered the island.
     *
     * @param island        the conquered island.
     * @param prevConqueror the previous conqueror of the island.
     */
    private static void removePreviousConquerorTowersFromIsland(Island island, Player prevConqueror) {
        Tower tower = island.getTowerType();
        int removedTowers = island.removeAllTowers();
        for (int i = 0; i < removedTowers; ++i) {
            prevConqueror.getBoard().receiveTower(tower);
        }
    }

    /**
     * Puts the towers of the conqueror on the island.
     *
     * @param island    the island where to put the towers on.
     * @param conqueror the player that conquered the island.
     */
    private static void putConquerorTowersOnIsland(Island island, Player conqueror) {
        Tower tower = conqueror.getBoard().getTowerType();
        for (int i = 0; i < island.getDimension(); i++) {
            if (!conqueror.hasPlacedAllTowers()) {
                conqueror.getBoard().removeTower();
                island.receiveTower(tower);
            }
        }
    }

    /**
     * The game is over immediately when:
     * - there are only three islands remaining
     * - one player has placed all his towers
     * The game is over at the of a round when:
     * - there are no more students in the bag
     * - there are no more assistants to play
     *
     * @param state The state of the game.
     * @return true when the game is over, false otherwise
     */
    public static boolean isGameOver(GameState state) {
        return state.isStage(Stage.GAME_OVER) || threeOrLessRemainingIslands(state) || onePlayerHasPlacedAllTowers(state) ||
                (state.isStage(Stage.ROUND_END) && (isBagEmpty(state) || noMoreAssistantsToPlay(state)));
    }

    private static boolean onePlayerHasPlacedAllTowers(GameState state) {
        for (Player player : state.getPlayers())
            if (player.isLeader() && player.hasPlacedAllTowers())
                return true;
        return false;
    }

    private static boolean threeOrLessRemainingIslands(GameState state) {
        return state.getIslands().size() <= 3;
    }

    private static boolean isBagEmpty(GameState state) {
        return state.getBag().isEmpty();
    }

    private static boolean noMoreAssistantsToPlay(GameState state) {
        for (Player player : state.getPlayers())
            if (!player.hasAssistants())
                return true;
        return false;
    }

    /**
     * @param state The state of the game.
     * @return the tower of the player(s) who won the match, null on tie.
     * @throws GameNotOverException if the game is not ended yet.
     */
    public static Tower getWinner(GameState state) {
        if (!isGameOver(state))
            throw new GameNotOverException("Game is not over yet");

        List<Tower> towers = getTowersPlacedMoreTimesOnIslands(state.getIslands());
        if (towers.size() == 1) {
            return towers.get(0);
        } else if (towers.size() > 1) {
            towers = getTowersWithMoreProfessors(state);
            if (towers.size() == 1)
                return towers.get(0);
        }

        return null;
    }

    /**
     * @param islands the list of islands.
     * @return the tower placed more times on the islands. If more towers are placed the same
     *         number of times they are both returned.
     */
    private static List<Tower> getTowersPlacedMoreTimesOnIslands(List<Island> islands) {
        Map<Tower, Integer> occurrences = new EnumMap<>(Tower.class);
        for (Tower tower : Tower.values())
            occurrences.put(tower, 0);

        for (Island island : islands) {
            if (island.hasTowers()) {
                Tower tower = island.getTowerType();
                occurrences.put(tower, occurrences.get(tower) + island.getNumTowers());
            }
        }

        List<Tower> towers = new ArrayList<>();

        int max = 0;
        for (Tower tower : Tower.values()) {
            int timesPlaced = occurrences.get(tower);
            if (timesPlaced > max) {
                max = timesPlaced;
                towers.clear();
                towers.add(tower);
            } else if (timesPlaced == max) {
                towers.add(tower);
            }
        }

        return towers;
    }

    /**
     * @param state the state of the game.
     * @return the tower with more professors. In 2/3 players matches is returned
     *         the tower of the player with more professors. In 4 players matches
     *         is returned the tower of the team with more professors.
     */
    private static List<Tower> getTowersWithMoreProfessors(GameState state) {
        List<Tower> towersWithMoreProfessors = new ArrayList<>();
        int max = 0;
        for (Tower tower : Tower.values()) {
            int numProfessors = getNumberOfProfessorByTower(state, tower);
            if (numProfessors > max) {
                max = numProfessors;
                towersWithMoreProfessors.clear();
                towersWithMoreProfessors.add(tower);
            } else if (numProfessors == max) {
                towersWithMoreProfessors.add(tower);
            }
        }
        return towersWithMoreProfessors;
    }

    /**
     * @param state the state of the game.
     * @param tower the tower for which the number of professors are calculated.
     * @return the professors related to a tower. For 2/3 players matches are just the
     *         number of professors of the single players. For 4 players match the number
     *         of professors is the sum of the professors owned by the team.
     */
    private static int getNumberOfProfessorByTower(GameState state, Tower tower) {
        int numProfessors = 0;
        for (Player player : state.getPlayers()) {
            if (player.getBoard().getTowerType() == tower) {
                numProfessors += state.getPlayerProfessors(player).size();
            }
        }
        return numProfessors;
    }

    /**
     * Disable all the effects that should end at the end of the turn.
     *
     * @param state the state of the game.
     */
    public static void disableEffects(GameState state) {
        if (state.isExpertMatch()) {
            ExpertAttrs expertAttrs = state.getExpertAttrs();
            expertAttrs.setAdditionalMotherNatureSteps(0);
            expertAttrs.setIgnoreTowers(false);
            expertAttrs.setTwoAdditionalPoints(false);
            expertAttrs.setIgnoredStudent(null);
            expertAttrs.setProfOwnerOnStudentsTie(false);
            updateProfessorsOwners(state);
        }
    }

    /**
     * Makes the characters playable
     */
    public static void makeCharactersPlayable(GameState state) {
        if (state.isExpertMatch()) {
            state.getExpertAttrs().setCharacterAlreadyPlayed(false);
        }
    }

    public static List<Assistant> getPlayableAssistants(GameState state) {
        Player currentPlayer = state.getCurrentPlayer();

        List<Assistant> assistants = currentPlayer.getPlayableAssistants();
        List<Assistant> alreadyPlayedAssistants = getAlreadyPlayedAssistantsOfThisRound(state);

        assistants.removeAll(alreadyPlayedAssistants);
        if (assistants.isEmpty())
            // special case: you can play already played assistants
            return currentPlayer.getPlayableAssistants();
        else
            return assistants;
    }

    private static List<Assistant> getAlreadyPlayedAssistantsOfThisRound(GameState state) {
        List<Player> players = getPlayersWhoHaveAlreadyDoneTheirTurn(state);
        List<Assistant> assistants = new ArrayList<>(players.size());
        for (Player player : players)
            assistants.add(player.getLastPlayedAssistant());
        return assistants;
    }

    private static List<Player> getPlayersWhoHaveAlreadyDoneTheirTurn(GameState state) {
        List<Player> players = new ArrayList<>();
        for (int i = 0; i < state.getCurrentTurn(); ++i) {
            int playerId = state.getPlayerQueue().get(i);
            Player player = state.getPlayerById(playerId);
            players.add(player);
        }
        return players;
    }

}
