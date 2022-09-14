package it.polimi.ingsw.models.components;

import it.polimi.ingsw.models.exceptions.IllegalMoveException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Player {

    private final Wizard wizard;
    private final String name;
    private final Board board;
    /**
     * In 4 players match it's used to know who keeps the towers.
     */
    private final boolean leader;
    private final List<Assistant> hand;
    private Assistant lastPlayedAssistant;
    private int numCoins;

    /**
     * To use in 2/3 player matches.
     *
     * @param wizard the wizard of the player.
     * @param name the name of the player.
     * @param assistants the assistants of the player.
     * @param board the board of the player.
     */
    public Player(Wizard wizard, String name, List<Assistant> assistants, Board board) {
        this(wizard, name, assistants, board, true);
    }

    /**
     * To use in 4 player matches to set who keeps the towers between
     * 2 players of the same team.
     *
     * @param wizard the wizard of the player.
     * @param name the name of the player.
     * @param assistants the assistants of the player.
     * @param board the board of the player.
     * @param leader true if the player keeps the towers of the team, false otherwise.
     */
    public Player(Wizard wizard, String name, List<Assistant> assistants, Board board, boolean leader) {
        this.wizard = wizard;
        this.name = name;
        this.board = board;
        this.leader = leader;
        hand = new ArrayList<>(assistants);
    }

    public int getId() {
        return wizard.ordinal();
    }

    public Wizard getWizard() {
        return wizard;
    }

    public String getName() {
        return name;
    }

    public Board getBoard() {
        return board;
    }

    public boolean isLeader() {
        return leader;
    }

    /**
     * @return true if the player has remaining assistant cards, false otherwise
     */
    public boolean hasAssistants() {
        return !hand.isEmpty();
    }

    public List<Assistant> getPlayableAssistants() {
        return new ArrayList<>(hand);
    }

    /**
     * @return the last played assistant, null if no assistants have been played
     */
    public Assistant getLastPlayedAssistant() {
        return lastPlayedAssistant;
    }

    /**
     * Play an assistant
     *
     * @param assistant the assistant to play
     * @throws IllegalMoveException if the player doesn't have the assistant
     */
    public void playAssistant(Assistant assistant) {
        if (!hand.remove(assistant))
            throw new IllegalMoveException("Player doesn't have this assistant. " + assistant);
        lastPlayedAssistant = assistant;
    }

    /**
     * @return number of player's coins
     */
    public int getNumCoins() {
        return numCoins;
    }

    /**
     * Increase the number of coins
     */
    public void addCoin() {
        numCoins++;
    }

    /**
     * Remove coins from the player.
     *
     * @param numCoins the number of coins to remove.
     * @throws IllegalMoveException if player has less than numCoins coins.
     */
    public void removeCoins(int numCoins) {
        if (numCoins > this.numCoins)
            throw new IllegalMoveException("Cannot remove " + numCoins + " coins");
        this.numCoins -= numCoins;
    }

    /**
     * @return true if the player doesn't have any tower remaining, false otherwise
     */
    public boolean hasPlacedAllTowers() {
        return !board.hasTowers();
    }

    /**
     * Two players are equal when they have the same id.
     *
     * @param o The object to compare this Player against.
     * @return true if the object is equal to this Player, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return getId() == player.getId();
    }

    /**
     * The hash code is calculated with the id of the player.
     *
     * @return the hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public String toString() {
        return "Player:\n" +
                "wizard=" + wizard +
                ", name='" + name + '\'' +
                "\nboard=" + board +
                "\nhand=" + hand +
                "\nlastPlayedAssistant=" + lastPlayedAssistant +
                "\nnumCoins=" + numCoins;
    }

}
