package it.polimi.ingsw.models.components;

import it.polimi.ingsw.models.constants.GameConstants;
import it.polimi.ingsw.models.exceptions.IllegalMoveException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BagTest {

    @Test
    void shouldThrowExceptionWhenAddingStudentToTheBagWhenIsFull() {
        Bag bag = new Bag();
        fillBag(bag);
        assertThrows(IllegalMoveException.class, () ->
                bag.receiveStudent(Student.RED)
        );
    }

    private void fillBag(Bag bag) {
        int numStudentsForColor = GameConstants.MAX_STUDENTS_IN_BAG / Student.values().length;
        for (Student student : Student.values()) {
            for (int i = 0; i < numStudentsForColor; ++i) {
                bag.receiveStudent(student);
            }
        }
    }

    @Test
    void shouldBeCreatedAFullSizeBag() {
        Bag bag = new Bag();
        fillBag(bag);
        assertEquals(GameConstants.MAX_STUDENTS_IN_BAG, bag.getNumStudent());
    }

    @Test
    void shouldBeAbleToReceiveStudents() {
        Bag bag = new Bag();
        List<Student> students = List.of(Student.RED, Student.RED);
        assertTrue(bag.canReceiveStudents(students));
    }

    @Test
    void shouldBeAbleToReceiveStudentsEdgeCase() {
        Bag bag = new Bag();
        fillBag(bag);
        bag.drawStudent();
        bag.drawStudent();
        List<Student> students = List.of(Student.RED, Student.RED);
        assertTrue(bag.canReceiveStudents(students));
    }

    @Test
    void shouldNotBeAbleToReceiveStudentsWhenBagIsFull() {
        Bag bag = new Bag();
        fillBag(bag);
        List<Student> students = List.of(Student.RED, Student.RED);
        assertFalse(bag.canReceiveStudents(students));
    }

    @Test
    void shouldExtractStudents() {
        Bag bag = new Bag();
        bag.receiveStudent(Student.RED);
        bag.receiveStudent(Student.GREEN);
        bag.receiveStudent(Student.YELLOW);
        Student extracted = bag.drawStudent();
        assertTrue(extracted.equals(Student.RED) || extracted.equals(Student.GREEN) ||
                extracted.equals(Student.YELLOW));
    }

    @Test
    void newlyCreatedBagShouldBeEmpty() {
        Bag bag = new Bag();
        assertTrue(bag.isEmpty());
    }

    @Test
    void shouldNotBeEmptyAfterInsertion() {
        Bag bag = new Bag();
        bag.receiveStudent(Student.RED);
        assertFalse(bag.isEmpty());
    }

    @Test
    void shouldBeEmptyAfterAllTheStudentsHaveBeenExtracted() {
        Bag bag = new Bag();
        bag.receiveStudent(Student.PINK);
        bag.drawStudent();
        assertTrue(bag.isEmpty());
    }

    @Test
    void shouldThrowAnExceptionIfExtractFromEmpty() {
        Bag bag = new Bag();
        assertThrows(IllegalMoveException.class, bag::drawStudent);
    }

}
