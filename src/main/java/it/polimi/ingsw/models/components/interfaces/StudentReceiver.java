package it.polimi.ingsw.models.components.interfaces;

import it.polimi.ingsw.models.components.Student;

import java.util.List;

public interface StudentReceiver {

    /**
     * Classes which implements this interface are able to receive students
     *
     * @param student the student to receive
     * @throws RuntimeException if the student cannot be received
     */
    void receiveStudent(Student student);

    // warning: "default" can be used because classes doesn't implement multiple interfaces
    //          with this same method signature.
    default void receiveStudents(List<Student> students) {
        for (Student student : students)
            receiveStudent(student);
    }

    /**
     * @param student the student to receive
     * @return true if the student can be received, false otherwise
     */
    boolean canReceiveStudent(Student student);

    /**
     * @param students the students to receive
     * @return true if the students can be received, false otherwise
     */
    boolean canReceiveStudents(List<Student> students);

}
