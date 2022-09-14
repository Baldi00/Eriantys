package it.polimi.ingsw.models.operations;

import it.polimi.ingsw.models.components.Assistant;
import it.polimi.ingsw.models.exceptions.PlayerIdAlreadyPresentException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ActionQueueTest {

    @Test
    void shouldBeEmptyOnInit() {
        ActionQueue actionQueue = new ActionQueue();
        assertTrue(actionQueue.getPlayerIdQueue().isEmpty());
    }

    @Test
    void queueShouldHaveSizeEqualsToThePlayerIdAdded() {
        ActionQueue actionQueue = new ActionQueue();
        actionQueue.add(0, Assistant.TURTLE);
        assertEquals(1, actionQueue.getPlayerIdQueue().size());
        actionQueue.add(1, Assistant.TURTLE);
        assertEquals(2, actionQueue.getPlayerIdQueue().size());
    }

    @Test
    void shouldThrowExceptionIfPlayerIdIsAlreadyPresent() {
        ActionQueue actionQueue = new ActionQueue();
        actionQueue.add(0, Assistant.TURTLE);
        assertThrows(PlayerIdAlreadyPresentException.class, () ->
            actionQueue.add(0, Assistant.TURTLE)
        );
    }

    @Test
    void assistantsWithGreaterValueHaveLowerPriority() {
        ActionQueue actionQueue = new ActionQueue();
        actionQueue.add(0, Assistant.ELEPHANT);
        actionQueue.add(1, Assistant.TURTLE);
        actionQueue.add(2, Assistant.DOG);

        List<Integer> queue = actionQueue.getPlayerIdQueue();
        assertEquals(1, queue.get(0));
        assertEquals(0, queue.get(1));
        assertEquals(2, queue.get(2));
    }

    @Test
    void clearShouldRemoveAllTheItemsInTheQueue() {
        ActionQueue actionQueue = new ActionQueue();
        actionQueue.add(0, Assistant.ELEPHANT);
        actionQueue.add(1, Assistant.TURTLE);
        actionQueue.add(2, Assistant.DOG);
        actionQueue.clear();

        assertTrue(actionQueue.getPlayerIdQueue().isEmpty());
    }

}