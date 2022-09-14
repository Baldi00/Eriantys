package it.polimi.ingsw.models.components.characters;

import it.polimi.ingsw.models.components.Student;
import it.polimi.ingsw.models.components.characters.effects.Effect;
import it.polimi.ingsw.models.components.characters.effects.EffectArgs;
import it.polimi.ingsw.models.components.interfaces.StudentReceiver;
import it.polimi.ingsw.models.exceptions.IllegalMoveException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Character implements StudentReceiver, Serializable {
    private final CharacterType characterType;
    private transient Effect effect;
    private final int cost;
    private final int studentsLimit;
    private final int maxIslandBlocks;
    private final List<Student> students;
    private boolean costIncrement;
    private int islandBlocks;

    /**
     * Creates a Character object. The max number of students and blocks
     * with this constructor is set to zero.
     *
     * @param characterType an identifier for the character.
     * @param cost          the cost of the character.
     * @param effect        the effect associated to this character.
     */
    Character(CharacterType characterType, int cost, Effect effect) {
        this(characterType, cost, effect, 0, 0);
    }

    /**
     * Creates a Character object.
     *
     * @param characterType type of the character.
     * @param cost          the character cost.
     * @param effect        effect performed by the character on the game.
     * @param studentsLimit max. number of students which can be placed on the card.
     * @param islandBlocks  number of island blocks available on this character. This will also be the maximum number of
     *                      blocks that this Character can receive.
     */
    Character(CharacterType characterType, int cost, Effect effect, int studentsLimit, int islandBlocks) {
        this.characterType = characterType;
        this.cost = cost;
        this.students = new ArrayList<>();
        this.effect = effect;
        this.studentsLimit = studentsLimit;
        this.maxIslandBlocks = islandBlocks;
        this.islandBlocks = islandBlocks;
    }

    public CharacterType getCharacterType() {
        return characterType;
    }

    public int getCost() {
        return costIncrement ? cost + 1 : cost;
    }

    public boolean isCostIncrement() {
        return costIncrement;
    }

    public List<Student> getStudents() {
        return new ArrayList<>(students);
    }

    public int getNumIslandBlocks() {
        return islandBlocks;
    }

    /**
     * Pick a student from the character card
     *
     * @param student student to be picked
     * @throws IllegalMoveException if the character doesn't have students to pick or
     *                              if the requested student is not available
     */
    public void pickStudent(Student student) {
        if (!canPickStudent(student))
            throw new IllegalMoveException("Character doesn't have this student: " + student);
        students.remove(student);
    }

    public boolean canPickStudent(Student student) {
        return !students.isEmpty() && students.contains(student);
    }

    public int getStudentsLimit() {
        return studentsLimit;
    }

    /**
     * Pick a block card from the character card.
     *
     * @throws IllegalMoveException if the character doesn't have any block to pick.
     */
    public void pickBlock() {
        if (!canPickBlock())
            throw new IllegalMoveException("Cannot pick block from this character");
        islandBlocks--;
    }

    public boolean canPickBlock() {
        return islandBlocks > 0;
    }

    /**
     * Place a block card on the character card
     *
     * @throws IllegalMoveException when the character cannot receive blocks or it has received too many blocks
     */
    public void receiveBlock() {
        if (maxIslandBlocks == 0)
            throw new IllegalMoveException(characterType + " cannot receive a block");
        if (islandBlocks == maxIslandBlocks)
            throw new IllegalMoveException(characterType + " cannot receive more than " + islandBlocks + " blocks");
        islandBlocks++;
    }

    /**
     * Perform the character effect. The first time the effect is called,
     * one coin is taken by the character, and it will increment its cost.
     *
     * @param args  parameters on which the card performs its effect.
     * @param coins the number of coins should be greater or equal to the cost of the character.
     * @return the coins that are not used by the character.
     * @throws IllegalMoveException     if the effect cannot be performed or not enough coins are passed.
     * @throws IllegalArgumentException if required parameters are null.
     */
    public int performEffect(EffectArgs args, int coins) {
        if (getCost() > coins)
            throw new IllegalMoveException("Cannot play character with " + coins + " coins." +
                    "Character cost is " + getCost() + ".");

        if (!costIncrement) {
            costIncrement = true;
            coins--;
        }
        effect.performEffect(args);
        return coins;
    }

    /**
     * This method must be used only to restore the effect after
     * a deserialization.
     *
     * @param effect the effect to apply to this character.
     */
    public void setEffect(Effect effect) {
        this.effect = effect;
    }

    /**
     * Place a student on the character card
     *
     * @param student the student to receive
     * @throws IllegalMoveException if no more students cannot be added
     */
    @Override
    public void receiveStudent(Student student) {
        if (!canReceiveStudent(student))
            throw new IllegalMoveException("Cannot add more than " + studentsLimit + " students on the Character");
        students.add(student);
    }

    @Override
    public boolean canReceiveStudent(Student student) {
        return students.size() < studentsLimit;
    }

    @Override
    public boolean canReceiveStudents(List<Student> students) {
        return this.students.size() + students.size() <= studentsLimit;
    }

    /**
     * @param o The object to compare this Character against.
     * @return true if two characters have the same CharacterType, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Character character = (Character) o;
        return characterType == character.characterType;
    }

    /**
     * The hash code is computed using the CharacterType.
     *
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(characterType);
    }

    @Override
    public String toString() {
        return "Character{" +
                "characterType=" + characterType +
                ", cost=" + cost +
                ", students=" + students +
                ", effect=" + effect +
                ", costIncrement=" + costIncrement +
                ", blockNumber=" + islandBlocks +
                '}';
    }

}