package it.polimi.ingsw.models.components;

import it.polimi.ingsw.models.components.interfaces.StudentReceiver;
import it.polimi.ingsw.models.exceptions.IllegalMoveException;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Entrance section of the Board
 */
public class Entrance implements StudentReceiver {

    private final Map<Student, Integer> students;
    private final int studentsLimit;

    /**
     * @param studentsLimit maximum number of students which can be placed on the entrance
     */
    public Entrance(int studentsLimit) {
        students = new EnumMap<>(Student.class);
        for (Student s : Student.values()) {
            students.put(s, 0);
        }

        this.studentsLimit = studentsLimit;
    }

    /**
     * Check whether the entrance is empty
     *
     * @return true if it's empty, false otherwise
     */
    public boolean isEmpty() {
        for (Student s : Student.values()) {
            if (students.get(s) != 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "Entrance:\n" +
                "students=" + students;
    }

    /**
     * Place a student on the entrance
     *
     * @param student the type of the student to receive
     * @throws IllegalMoveException if a student is added when the entrance is full
     */
    @Override
    public void receiveStudent(Student student) {
        if (!canReceiveStudent(student))
            throw new IllegalMoveException("Entrance cannot contain more than " + studentsLimit + " students");
        students.put(student, students.get(student) + 1);
    }

    @Override
    public boolean canReceiveStudent(Student student) {
        return getNumStudents() < studentsLimit;
    }

    @Override
    public boolean canReceiveStudents(List<Student> students) {
        return getNumStudents() + students.size() <= studentsLimit;
    }

    /**
     * Remove a student from the entrance
     *
     * @param student the type of the student to remove
     * @return true if the student has been correctly removed, false otherwise
     */
    public boolean removeStudent(Student student) {
        if (students.get(student) > 0) {
            students.put(student, students.get(student) - 1);
            return true;
        }
        return false;
    }

    /**
     * Get the number of students on the entrance
     *
     * @return the number of students on the entrance
     */
    public int getNumStudents() {
        int count = 0;
        for (Student s : Student.values()) {
            count += students.get(s);
        }
        return count;
    }

    /**
     * @param color the requested student color
     * @return the number of students on hall of the specified color
     */
    public int getNumStudentsByColor(Student color) {
        return students.get(color);
    }

}