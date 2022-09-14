package it.polimi.ingsw.models.state;

import it.polimi.ingsw.models.components.Island;
import it.polimi.ingsw.models.components.Student;
import it.polimi.ingsw.models.components.characters.Character;
import it.polimi.ingsw.models.components.characters.CharacterType;
import it.polimi.ingsw.models.constants.GameConstants;
import it.polimi.ingsw.models.exceptions.IllegalMoveException;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class ExpertAttrs {

    private final List<Character> characters;
    private int coinStock;
    private boolean characterAlreadyPlayed;

    // effects
    private final List<Island> blockedIslands;
    private int additionalMotherNatureSteps;
    private boolean ignoreTowers;
    private boolean twoAdditionalPoints;
    private Student ignoredStudentType;
    private boolean profOwnershipOnTie;

    public ExpertAttrs() {
        characters = new ArrayList<>();
        blockedIslands = new ArrayList<>();
        coinStock = GameConstants.NUM_COINS;
    }

    public boolean isCharacterAlreadyPlayed() {
        return characterAlreadyPlayed;
    }

    public void setCharacterAlreadyPlayed(boolean characterAlreadyPlayed) {
        this.characterAlreadyPlayed = characterAlreadyPlayed;
    }

    public List<Character> getCharacters() {
        return new ArrayList<>(characters);
    }

    public void setCharacters(List<Character> characters) {
        this.characters.clear();
        this.characters.addAll(characters);
    }

    public void addCharacter(Character character) {
        characters.add(character);
    }

    public List<Island> getBlockedIslands() {
        return new ArrayList<>(blockedIslands);
    }

    public boolean isIslandBlocked(Island island) {
        return blockedIslands.contains(island);
    }

    public void removeBlockFromIsland(Island island) {
        blockedIslands.remove(island);
    }

    public void setBlockedIslands(List<Island> islands) {
        this.blockedIslands.clear();
        this.blockedIslands.addAll(islands);
    }

    public void addBlockToIsland(Island island) {
        blockedIslands.add(island);
    }

    public int getNumCoinsInStock() {
        return coinStock;
    }

    public void getCoinsFromStock(int coins) {
        if (!canGetCoins(coins))
            throw new IllegalMoveException("Cannot get more than " + coinStock + " coins from stock");
        coinStock -= coins;
    }

    public void addCoinsToStock(int coins) {
        if (coinStock + coins > GameConstants.NUM_COINS)
            throw new IllegalMoveException("Cannot add more than " + GameConstants.NUM_COINS + " coins to stock");
        coinStock += coins;
    }

    public boolean canGetCoins(int coins){
        return coinStock >= coins;
    }

    public int getAdditionalMotherNatureSteps() {
        return additionalMotherNatureSteps;
    }

    public void setAdditionalMotherNatureSteps(int additionalMotherNatureSteps) {
        this.additionalMotherNatureSteps = additionalMotherNatureSteps;
    }

    public boolean isIgnoreTowers() {
        return ignoreTowers;
    }

    public void setIgnoreTowers(boolean ignoreTowers) {
        this.ignoreTowers = ignoreTowers;
    }

    public boolean isTwoAdditionalPoints() {
        return twoAdditionalPoints;
    }

    public void setTwoAdditionalPoints(boolean twoAdditionalPoints) {
        this.twoAdditionalPoints = twoAdditionalPoints;
    }

    public Student getIgnoredStudentType() {
        return ignoredStudentType;
    }

    public void setIgnoredStudent(Student ignoredStudentType) {
        this.ignoredStudentType = ignoredStudentType;
    }

    public boolean getProfOwnershipOnTie() {
        return profOwnershipOnTie;
    }

    public void setProfOwnerOnStudentsTie(boolean profOwnerOnStudentsParity) {
        this.profOwnershipOnTie = profOwnerOnStudentsParity;
    }

    /**
     * @throws NoSuchElementException if the given character is not in the character list
     */
    public Character getCharacterByType(CharacterType characterType) {
        for (Character character : characters) {
            if (character.getCharacterType() == characterType) {
                return character;
            }
        }
        throw new NoSuchElementException(characterType + " is not present");
    }

}
