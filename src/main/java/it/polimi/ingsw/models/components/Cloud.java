package it.polimi.ingsw.models.components;

import it.polimi.ingsw.models.components.interfaces.StudentReceiver;
import it.polimi.ingsw.models.exceptions.IllegalMoveException;

import java.util.ArrayList;
import java.util.List;

public class Cloud implements StudentReceiver {

    private final int id;
    private final List<Student> students;
    private final int studentsLimit;

    /**
     * @param id            the cloud ID
     * @param studentsLimit maximum number of students which can be placed on the cloud
     */
    public Cloud(int id, int studentsLimit) {
        this.id = id;
        this.studentsLimit = studentsLimit;
        this.students = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    /**
     * Place a student on the cloud
     *
     * @param student the type of the student to receive
     * @throws IllegalMoveException if no more students cannot be added to the cloud
     */
    @Override
    public void receiveStudent(Student student) {
        if (!canReceiveStudent(student))
            throw new IllegalMoveException("Cloud can contain only " + studentsLimit + " students");
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
     * Check whether there are any students on the cloud
     *
     * @return true if there are no students on the cloud, false otherwise
     */
    public boolean isEmpty() {
        return students.isEmpty();
    }

    /**
     * Pick all students from the cloud.
     * The students returned are removed from the cloud.
     *
     * @return the students on the cloud
     * @throws IllegalMoveException if students are picked from an empty cloud.
     */
    public List<Student> pickStudents() {
        if (students.isEmpty())
            throw new IllegalMoveException("Trying to pick students from empty cloud");

        List<Student> picked = new ArrayList<>(students);
        removeAllStudents();
        return picked;
    }

    private void removeAllStudents() {
        students.clear();
    }

    @Override
    public String toString() {
        return "Cloud{" +
                "id=" + id +
                ", students=" + students +
                '}';
    }

}
