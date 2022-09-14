package it.polimi.ingsw.models.state;

import it.polimi.ingsw.models.components.*;
import it.polimi.ingsw.models.constants.GameConstants;
import it.polimi.ingsw.models.constants.MatchType;
import it.polimi.ingsw.models.exceptions.*;

import java.util.*;

/**
 * This class represents the state of the game.
 * NB: It's up to the users of this class to verify that the game
 * is in a legal state or if attributes are valid.
 */
public class GameState {

    /**
     * players that should be in the match.
     */
    private final MatchType matchType;
    private final boolean expertMatch;
    private ExpertAttrs expertAttrs;

    private List<Wizard> availableWizards;
    private List<Tower> availableTowers;

    private final List<Player> players;
    /**
     * List of player ids ordered in clockwise order.
     */
    private List<Integer> clockwiseOrder;
    /**
     * Player turns order. Can be the planning queue or the action queue.
     */
    private List<Integer> playerQueue;

    private final Bag bag;
    private final List<Island> islands;
    private final List<Cloud> clouds;
    private int motherNaturePosition;
    /**
     * Represents what player owns what Professor.
     * The integer represents the player id.
     */
    private final Map<Student, Integer> professorOwners;

    private Stage stage;
    private int currentTurn;
    private int studentsToMove;

    private Tower winner;

    public GameState(int numPlayers, boolean expertMatch) {
        matchType = MatchType.fromNumPlayers(numPlayers);
        this.expertMatch = expertMatch;
        if (expertMatch)
            expertAttrs = new ExpertAttrs();

        availableWizards = new ArrayList<>();
        availableTowers = new ArrayList<>();
        islands = new ArrayList<>();
        clouds = new ArrayList<>();
        players = new ArrayList<>();
        bag = new Bag();

        professorOwners = new EnumMap<>(Student.class);
        for (Student student : Student.values()) {
            professorOwners.put(student, null);
        }
    }

    /**
     * @return the number of players of this match.
     *         Do note that this is NOT the number of players currently in the game.
     */
    public int getNumPlayers() {
        return matchType.getNumPlayers();
    }

    public boolean isExpertMatch() {
        return expertMatch;
    }

    /**
     * @return the attributes for the expert match, null if not expert game
     */
    public ExpertAttrs getExpertAttrs() {
        return expertAttrs;
    }

    public void setAvailableWizards(List<Wizard> availableWizards) {
        this.availableWizards = new ArrayList<>(availableWizards);
    }

    public List<Wizard> getAvailableWizards() {
        return new ArrayList<>(availableWizards);
    }

    public void setAvailableTowers(List<Tower> availableTowers) {
        this.availableTowers = new ArrayList<>(availableTowers);
    }

    public List<Tower> getAvailableTowers() {
        return new ArrayList<>(availableTowers);
    }

    /**
     * @param player the player to add.
     * @throws IllegalMoveException when trying to add more than numPlayers players.
     */
    public void addPlayer(Player player) {
        if (players.size() >= getNumPlayers())
            throw new IllegalMoveException("Cannot add more than " + matchType + " players.");
        players.add(player);
    }

    public List<Player> getPlayers() {
        return new ArrayList<>(players);
    }

    /**
     * @throws NoSuchElementException if the given id does not correspond to an existing player
     */
    public Player getPlayerById(int playerId) {
        for (Player player : players) {
            if (player.getId() == playerId) {
                return player;
            }
        }
        throw new NoSuchElementException("Requested player is not present: id=" + playerId);
    }

    /**
     * In 4 player matches, it returns the player leader (i.e. the player who owns the towers).
     * 
     * @throws NoSuchElementException if the given tower is not associated to an existing player.
     */
    public Player getPlayerByTower(Tower tower) {
        for (Player player : players)
            if (player.isLeader() && player.getBoard().getTowerType() == tower)
                return player;
        throw new NoSuchElementException("Requested tower (" + tower + ") is not associated to a player");
    }

    /**
     * @param name the player name to search for.
     * @return the Player with the given name.
     * @throws NoSuchElementException when the requested player name is not associated to any player.
     */
    public Player getPlayerByName(String name) {
        for (Player player : players)
            if (player.getName().equals(name))
                return player;
        throw new NoSuchElementException("Requested player name is not present. Requested name = " + name);
    }

    public Player getCurrentPlayer() {
        return getPlayerById(playerQueue.get(currentTurn));
    }

    public boolean isCurrentPlayer(Player player) {
        return getCurrentPlayer().equals(player);
    }

    public void setClockwiseOrder(List<Integer> clockwiseOrder) {
        this.clockwiseOrder = clockwiseOrder;
    }

    public List<Integer> getClockwiseOrder() {
        return clockwiseOrder;
    }

    public void setPlayerQueue(List<Integer> playerQueue) {
        this.playerQueue = playerQueue;
    }

    public List<Integer> getPlayerQueue() {
        return playerQueue;
    }

    public Bag getBag() {
        return bag;
    }

    public boolean isBagEmpty() {
        return bag.isEmpty();
    }

    public void setIslands(List<Island> islands) {
        this.islands.clear();
        this.islands.addAll(islands);
    }

    /**
     * @return islands with ascendant id
     */
    public List<Island> getIslands() {
        return new ArrayList<>(islands);
    }

    /**
     * @throws NoSuchElementException if the given position does not correspond to an existing island
     */
    public Island getIslandByPosition(int position) {
        for (Island island : islands) {
            if (island.getPosition() == position) {
                return island;
            }
        }
        throw new NoSuchElementException("Requested island is not present: position=" + position);
    }

    public void setClouds(List<Cloud> clouds) {
        this.clouds.clear();
        this.clouds.addAll(clouds);
    }

    public List<Cloud> getClouds() {
        return new ArrayList<>(clouds);
    }

    /**
     * @throws NoSuchElementException if the given id does not correspond to an existing cloud
     */
    public Cloud getCloudById(int cloudId) {
        for (Cloud cloud : clouds) {
            if (cloud.getId() == cloudId) {
                return cloud;
            }
        }
        throw new NoSuchElementException("Requested cloud is not present: id=" + cloudId);
    }

    /**
     * @throws InvalidMotherNaturePosition if the given position is not associated to an existing island
     */
    public void setMotherNaturePosition(int motherNaturePosition) {
        if (motherNaturePosition < 0 || motherNaturePosition > GameConstants.NUMBER_OF_ISLANDS)
            throw new InvalidMotherNaturePosition("Trying to put mother nature on not existing island");

        this.motherNaturePosition = motherNaturePosition;
    }

    public int getMotherNaturePosition() {
        return motherNaturePosition;
    }

    /**
     * @param student  the color of the professor.
     * @param playerId the id of the player who owns the prof.
     */
    public void setProfessorOwner(Student student, Integer playerId) {
        professorOwners.put(student, playerId);
    }

    /**
     * @param prof the color of the professor.
     * @return the id of the player who owns the prof, null if no players own the prof.
     */
    public Integer getProfessorOwner(Student prof) {
        return professorOwners.get(prof);
    }

    public List<Student> getPlayerProfessors(Player player) {
        List<Student> ownedProf = new ArrayList<>();
        for (Student prof : Student.values()) {
            if (getProfessorOwner(prof) != null && getProfessorOwner(prof) == player.getId()) {
                ownedProf.add(prof);
            }
        }
        return ownedProf;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public Stage getStage() {
        return stage;
    }

    public boolean isStage(Stage stage) {
        return this.stage == stage;
    }

    public int getCurrentTurn() {
        return currentTurn;
    }

    public void setCurrentTurn(int currentTurn) {
        this.currentTurn = currentTurn;
    }

    public void resetTurn() {
        currentTurn = 0;
    }

    public void nextTurn() {
        currentTurn++;
    }

    public boolean isLastTurn() {
        return currentTurn == (getNumPlayers() - 1);
    }

    public int getStudentsToMove() {
        return studentsToMove;
    }

    public void setStudentsToMove(int studentsToMove) {
        this.studentsToMove = studentsToMove;
    }

    public void decrementStudentsToMove() {
        studentsToMove--;
    }

    public Tower getWinner() {
        return winner;
    }

    public void setWinner(Tower winner) {
        this.winner = winner;
    }
}
