package it.polimi.ingsw.models.components;

import it.polimi.ingsw.models.exceptions.IllegalMoveException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EntranceTest {

    @Test
    void isEmptyOnInit() {
        Entrance entrance = new Entrance(2);
        assertTrue(entrance.isEmpty());
    }

    @Test
    void isNotEmptyAfterInsertion() {
        Entrance entrance = new Entrance(2);
        entrance.receiveStudent(Student.PINK);
        assertFalse(entrance.isEmpty());
    }

    @Test
    void isEmptyAfterRemoval() {
        Entrance entrance = new Entrance(2);
        entrance.receiveStudent(Student.PINK);
        entrance.receiveStudent(Student.RED);
        entrance.removeStudent(Student.PINK);
        entrance.removeStudent(Student.RED);
        assertTrue(entrance.isEmpty());
    }

    @Test
    void shouldThrowExceptionWhenExceedingStudentLimit() {
        Entrance entrance = new Entrance(2);
        entrance.receiveStudent(Student.PINK);
        entrance.receiveStudent(Student.PINK);
        assertThrows(IllegalMoveException.class, () ->
                entrance.receiveStudent(Student.PINK)
        );
    }

    @Test
    void shouldNotRemoveNonExistingStudents() {
        Entrance entrance = new Entrance(2);
        entrance.receiveStudent(Student.PINK);
        entrance.receiveStudent(Student.PINK);
        assertFalse(entrance.removeStudent(Student.RED));
    }

    @Test
    void shouldNotRemoveFromEmptyEntrance() {
        Entrance entrance = new Entrance(2);
        entrance.receiveStudent(Student.RED);
        entrance.receiveStudent(Student.RED);
        entrance.removeStudent(Student.RED);
        entrance.removeStudent(Student.RED);
        assertFalse(entrance.removeStudent(Student.RED));
    }

    @Test
    void getNumStudentsWorks() {
        Entrance entrance = new Entrance(3);
        assertEquals(0, entrance.getNumStudents());

        entrance.receiveStudent(Student.GREEN);
        assertEquals(1, entrance.getNumStudents());

        entrance.receiveStudent(Student.RED);
        assertEquals(2, entrance.getNumStudents());

        entrance.removeStudent(Student.GREEN);
        assertEquals(1, entrance.getNumStudents());

        entrance.removeStudent(Student.YELLOW);
        assertEquals(1, entrance.getNumStudents());
    }

    @Test
    void availableStudentsAndNumStudentsShouldBeEqual() {
        Entrance entrance = new Entrance(3);
        assertEquals(0, entrance.getNumStudents());
        entrance.receiveStudent(Student.GREEN);
        entrance.receiveStudent(Student.GREEN);
        assertEquals(entrance.getNumStudents(), entrance.getNumStudentsByColor(Student.GREEN));
    }

}
