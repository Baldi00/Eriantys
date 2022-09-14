package it.polimi.ingsw.models.components.hall;

import it.polimi.ingsw.models.components.Student;
import it.polimi.ingsw.models.components.interfaces.StudentReceiver;
import it.polimi.ingsw.models.constants.GameConstants;
import it.polimi.ingsw.models.exceptions.IllegalMoveException;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class Hall implements StudentReceiver, Serializable {

    private final Map<Student, Integer> students;

    // gson lib cannot perform "serialization" if not transient
    private transient HallListener listener;

    public Hall() {
        students = new EnumMap<>(Student.class);
        for (Student s : Student.values()) {
            students.put(s, 0);
        }
    }

    public void setHallListener(HallListener listener) {
        this.listener = listener;
    }

    /**
     * Place a student on the hall
     *
     * @param student the student to receive
     * @throws IllegalMoveException if the Hall cannot receive any more students of one color
     */
    @Override
    public void receiveStudent(Student student) {
        if (!canReceiveStudent(student))
            throw new IllegalMoveException("Cannot add any more " + student + " students to the Hall");

        students.put(student, students.get(student) + 1);

        if (listener != null) {
            listener.hallChanged();
            if (students.get(student) % 3 == 0)
                listener.getCoin();
        }
    }

    @Override
    public boolean canReceiveStudent(Student student) {
        return students.get(student) < GameConstants.MAX_STUDENTS_ON_HALL_PER_COLOR;
    }

    @Override
    public boolean canReceiveStudents(List<Student> students) {
        Map<Student, Integer> studentsToReceive = new EnumMap<>(Student.class);
        for (Student student : Student.values())
            studentsToReceive.put(student, 0);
        for (Student student : students)
            studentsToReceive.put(student, studentsToReceive.get(student) + 1);
        for (Student student : Student.values()) {
            if (this.students.get(student) + studentsToReceive.get(student)
                    > GameConstants.MAX_STUDENTS_ON_HALL_PER_COLOR)
                return false;
        }
        return true;
    }

    public boolean canRemoveStudent(Student student) {
        return students.get(student) > 0;
    }

    /**
     * Remove a student from the hall
     *
     * @param student the type of the student to remove
     * @throws IllegalMoveException if the hall doesn't have the requested student.
     */
    public void removeStudent(Student student) {
        if (!canRemoveStudent(student))
            throw new IllegalMoveException("Cannot remove student " + student.name() + "from Hall.");

        students.put(student, students.get(student) - 1);
        if (listener != null)
            listener.hallChanged();
    }

    /**
     * @return the number of students on hall
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

    @Override
    public String toString() {
        return "Hall:\n" +
                "students=" + students;
    }

}
