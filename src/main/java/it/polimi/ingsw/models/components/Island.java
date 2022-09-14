package it.polimi.ingsw.models.components;

import it.polimi.ingsw.models.components.interfaces.StudentReceiver;
import it.polimi.ingsw.models.components.interfaces.TowerReceiver;
import it.polimi.ingsw.models.constants.GameConstants;
import it.polimi.ingsw.models.exceptions.IllegalMoveException;
import it.polimi.ingsw.models.exceptions.TowerNotSetException;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class Island implements StudentReceiver, TowerReceiver, Comparable<Island> {
    private final int position;
    private final int dimension;
    private final Map<Student, Integer> students;
    private Tower tower;
    private int numTowers;

    /**
     * @param position  position of the island (must be 0 <= position < GameConstants.NUMBER_OF_ISLANDS)
     * @param dimension the dimension of the island (must be 1 < dimension <= GameConstants.NUMBER_OF_ISLANDS)
     * @throws IllegalArgumentException if the position is invalid or the dimension is not positive
     */
    public Island(int position, int dimension) {
        if (position < 0 || GameConstants.NUMBER_OF_ISLANDS <= position)
            throw new IllegalArgumentException("invalid position");
        this.position = position;

        if (dimension <= 0 || dimension > GameConstants.NUMBER_OF_ISLANDS)
            throw new IllegalArgumentException("invalid dimension");
        this.dimension = dimension;

        students = new EnumMap<>(Student.class);
        for (Student student : Student.values()) {
            students.put(student, 0);
        }
    }

    /**
     * Get the island position
     *
     * @return the island position
     */
    public int getPosition() {
        return position;
    }

    /**
     * Get the dimension of the group to which the island belongs
     *
     * @return the dimension of the island group
     */
    public int getDimension() {
        return dimension;
    }

    /**
     * An island is considered before another island when its position is
     * right before the position of the other island. If this island has a
     * dimension greater than one, this island is before another if its
     * (position + dimension) is right before the position of the other island.
     * NB: Islands are positioned in a circular list, so you must also perform
     * the operations using the modulo operator.
     *
     * @param island the island to check this island against.
     * @return true if this island is before the given island, false otherwise.
     */
    public boolean isBefore(Island island) {
        return ((position + dimension) % GameConstants.NUMBER_OF_ISLANDS - island.getPosition()) == 0;
    }

    /**
     * Return the tower type for this Island
     *
     * @return the tower type
     * @throws TowerNotSetException if the tower type hasn't been set yet
     */
    public Tower getTowerType() {
        if (tower == null)
            throw new TowerNotSetException("Tower hasn't been set yet");
        return tower;
    }

    public int getNumTowers() {
        return numTowers;
    }

    public boolean hasTowers() {
        return numTowers != 0;
    }

    @Override
    public String toString() {
        return "Island:\n" +
                "position=" + position +
                ", dimension=" + dimension +
                "\nstudents=" + students +
                "\ntower=" + tower +
                ", numTowers=" + numTowers;
    }

    /**
     * Remove all the towers from this island
     *
     * @return the number of towers removed
     */
    public int removeAllTowers() {
        int removedTowers = getNumTowers();
        tower = null;
        numTowers = 0;
        return removedTowers;
    }

    /**
     * Get the number of student given the type on this island
     *
     * @param student the type of student
     * @return the number of student for the type of student received
     */
    public int getNumStudent(Student student) {
        return students.get(student);
    }

    /**
     * Place a student on the Island
     *
     * @param student the type of the student to receive
     */
    @Override
    public void receiveStudent(Student student) {
        students.put(student, students.get(student) + 1);
    }

    @Override
    public boolean canReceiveStudent(Student student) {
        return true;
    }

    @Override
    public boolean canReceiveStudents(List<Student> students) {
        return true;
    }

    /**
     * Add a tower to the island. The first tower received determines the tower type
     * for this island.
     *
     * @param tower the tower to receive
     * @throws IllegalMoveException if the number of tower is greater than the island dimension or
     *                              if a tower with a different type is added
     */
    @Override
    public void receiveTower(Tower tower) {
        if (numTowers + 1 > dimension)
            throw new IllegalMoveException("The number of towers cannot be higher than" +
                    " the island dimension");

        if (this.tower != null && this.tower != tower) {
            throw new IllegalMoveException("You cannot add another type of tower before" +
                    " removing the already existing ones");
        }

        this.tower = tower;
        numTowers++;
    }

    @Override
    public int compareTo(Island island) {
        return Integer.compare(this.position, island.position);
    }

    /**
     * Two islands are equal when they have the same position.
     *
     * @param o The object to compare this Character against.
     * @return true if the object is equal to this Island, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Island island = (Island) o;

        return position == island.position;
    }

    /**
     * The hash code is calculated with the position of the island.
     *
     * @return the hash code.
     */
    @Override
    public int hashCode() {
        return position;
    }
}
