package it.polimi.ingsw.models.components;

import it.polimi.ingsw.models.exceptions.IllegalMoveException;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CloudTest {

    @Test
    void shouldHaveTheIdSetInConstructor() {
        int id = 1;
        Cloud cloud = new Cloud(id, 1);
        assertEquals(id, cloud.getId());
    }

    @Test
    void shouldBeEmptyOnInit() {
        Cloud cloud = new Cloud(1, 1);
        assertTrue(cloud.isEmpty());
    }

    @Test
    void shouldNotBeEmptyIfAStudentIsAdded() {
        Cloud cloud = new Cloud(1, 1);
        cloud.receiveStudent(Student.RED);
        assertFalse(cloud.isEmpty());
    }

    @Test
    void shouldThrowExceptionIfTooManyStudentsAreAdded() {
        Cloud cloud = new Cloud(1, 1);
        cloud.receiveStudent(Student.RED);
        assertThrows(IllegalMoveException.class, () ->
                cloud.receiveStudent(Student.RED)
        );
    }

    @Test
    void shouldThrowExceptionIfStudentsArePickedFromEmptyCloud() {
        Cloud cloud = new Cloud(1, 1);
        assertThrows(IllegalMoveException.class, cloud::pickStudents);
    }

    @Test
    void pickShouldReturnAllTheStudentsReceived() {
        List<Student> students = List.of(
                Student.RED,
                Student.PINK,
                Student.YELLOW,
                Student.CYAN,
                Student.RED
        );

        Cloud cloud = new Cloud(1, students.size());
        for (Student student : students) {
            cloud.receiveStudent(student);
        }

        // NB: equals() cannot be used to compare the list returned by pick()
        // and the initial list because the order of the elements can differ

        List<Student> studentsOnCloud = cloud.pickStudents();

        assertEquals(students.size(), studentsOnCloud.size());

        Map<Student, Integer> counter1 = new HashMap<>();
        Map<Student, Integer> counter2 = new HashMap<>();

        for (Student student : students) {
            if (!counter1.containsKey(student)) {
                counter1.put(student, 0);
            }

            int currentCounter = counter1.get(student);
            counter1.put(student, currentCounter + 1);
        }

        for (Student student : studentsOnCloud) {
            if (!counter2.containsKey(student)) {
                counter2.put(student, 0);
            }

            int currentCounter = counter2.get(student);
            counter2.put(student, currentCounter + 1);
        }

        for (Student student : Student.values()) {
            assertEquals(counter1.get(student), counter2.get(student));
        }
    }
}
