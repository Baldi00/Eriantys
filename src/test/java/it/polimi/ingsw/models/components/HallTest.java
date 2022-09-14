package it.polimi.ingsw.models.components;

import it.polimi.ingsw.models.components.hall.Hall;
import it.polimi.ingsw.models.constants.GameConstants;
import it.polimi.ingsw.models.exceptions.IllegalMoveException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HallTest {

    @Test
    void shouldNotHaveStudentOnInit() {
        Hall hall = new Hall();
        assertEquals(0, hall.getNumStudents());
    }

    @Test
    void shouldThrowExceptionWhenExceedingStudentLimitPerColor() {
        Hall hall = new Hall();
        for (Student studentColor : Student.values()) {
            for (int i = 0; i < GameConstants.MAX_STUDENTS_ON_HALL_PER_COLOR; i++)
                hall.receiveStudent(studentColor);

            assertThrows(IllegalMoveException.class, () ->
                    hall.receiveStudent(studentColor)
            );
        }
    }

    @Test
    void shouldRemoveStudent() {
        Hall hall = new Hall();
        hall.receiveStudent(Student.RED);
        hall.removeStudent(Student.RED);
        assertEquals(0, hall.getNumStudentsByColor(Student.RED));
    }

    @Test
    void shouldThrowExceptionWhenStudentCannotBeRemoved() {
        Hall hall = new Hall();
        assertThrows(IllegalMoveException.class, () ->
                hall.removeStudent(Student.RED)
        );
    }

    @Test
    void addingStudentsShouldIncreaseTheNumberOfStudents() {
        Hall hall = new Hall();
        int prevStudents;

        for (int i = 0; i < 5; i++) {
            prevStudents = hall.getNumStudents();
            hall.receiveStudent(Student.PINK);
            assertEquals(prevStudents + 1, hall.getNumStudents());
        }
    }

    @Test
    void removingStudentsShouldDecreaseTheNumberOfStudents() {
        Hall hall = new Hall();

        List<Student> toInsert = List.of(
                Student.RED,
                Student.GREEN,
                Student.PINK
        );

        for (Student student : toInsert)
            hall.receiveStudent(student);

        for (Student student : toInsert) {
            int prevStudents = hall.getNumStudents();
            hall.removeStudent(student);
            assertEquals(prevStudents - 1, hall.getNumStudents());
        }
    }

    @Test
    void numStudentsShouldBeEqualToTheSumOfNumStudentsByColor() {
        Hall hall = new Hall();
        hall.receiveStudent(Student.PINK);
        hall.receiveStudent(Student.PINK);
        hall.receiveStudent(Student.GREEN);

        int sum = 0;
        for (Student color : Student.values()) {
            sum += hall.getNumStudentsByColor(color);
        }

        assertEquals(hall.getNumStudents(), sum);
    }

    @Test
    void addingStudentOfOneColorShouldIncreaseTheNumStudentOfThatColor() {
        Hall hall = new Hall();
        for (Student student : Student.values()) {
            hall.receiveStudent(student);
            assertEquals(1, hall.getNumStudentsByColor(student));
        }
    }

    @Test
    void shouldReceiveStudents() {
        Hall hall = new Hall();
        for (int i = 0; i < 8; ++i)
            hall.receiveStudent(Student.RED);
        List<Student> students = List.of(Student.RED, Student.RED);
        assertTrue(hall.canReceiveStudents(students));
    }

    @Test
    void shouldNotReceiveStudents() {
        Hall hall = new Hall();
        for (int i = 0; i < 9; ++i)
            hall.receiveStudent(Student.RED);
        List<Student> students = List.of(Student.RED, Student.RED);
        assertFalse(hall.canReceiveStudents(students));
    }

}
