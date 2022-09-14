package it.polimi.ingsw.models.components;

import it.polimi.ingsw.models.components.interfaces.StudentReceiver;
import it.polimi.ingsw.models.constants.GameConstants;
import it.polimi.ingsw.models.exceptions.IllegalMoveException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Bag implements StudentReceiver {

    private final List<Student> students;

    public Bag() {
        students = new ArrayList<>();
    }

    /**
     * Place the student in the bag
     *
     * @param student the type of the student to receive
     * @throws IllegalMoveException if a student is added to the bag when is full
     */
    @Override
    public void receiveStudent(Student student) {
        if (!canReceiveStudent(student)) {
            throw new IllegalMoveException("Cannot add student to a Bag when is full");
        }
        students.add(student);
    }

    @Override
    public boolean canReceiveStudent(Student student) {
        return students.size() < GameConstants.MAX_STUDENTS_IN_BAG;
    }

    @Override
    public boolean canReceiveStudents(List<Student> students) {
        return this.students.size() + students.size() <= GameConstants.MAX_STUDENTS_IN_BAG;
    }

    /**
     * Draw a random student from the bag. The student drawn is removed from the bag.
     *
     * @return a random student from the bag.
     * @throws IllegalMoveException when drawing from an empty bag.
     */
    public Student drawStudent() {
        if (students.isEmpty()) {
            throw new IllegalMoveException("Trying to extract student from an empty bag");
        }
        Collections.shuffle(students);
        Student extracted = students.get(0);
        students.remove(0);
        return extracted;
    }

    public boolean isEmpty() {
        return students.isEmpty();
    }

    public int getNumStudent() {
        return students.size();
    }

    @Override
    public String toString() {
        return "Bag: remaining students=" + students.size();
    }

}
