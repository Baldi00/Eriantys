package it.polimi.ingsw.models.operations;

import it.polimi.ingsw.models.components.Assistant;
import it.polimi.ingsw.models.exceptions.PlayerIdAlreadyPresentException;
import it.polimi.ingsw.models.utils.Pair;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ActionQueue {

    private final List<Pair<Integer, Assistant>> queue;

    public ActionQueue() {
        queue = new LinkedList<>();
    }

    /**
     * Add the player and the assistant played
     *
     * @param playerId  the id of the player
     * @param assistant the assistant played by the player
     * @throws PlayerIdAlreadyPresentException if the playerId is already present in the queue
     */
    public void add(int playerId, Assistant assistant) {
        if (containsPlayerId(playerId)) {
            throw new PlayerIdAlreadyPresentException("The playerId is already present in the queue.");
        }

        for (int i = 0; i < queue.size(); ++i) {
            if (getAssistant(i).getValue() > assistant.getValue()) {
                queue.add(i, new Pair<>(playerId, assistant));
                return;
            }
        }

        queue.add(new Pair<>(playerId, assistant));
    }

    private boolean containsPlayerId(int playerId) {
        for (Pair<Integer, Assistant> pair : queue) {
            if (pair.first() == playerId)
                return true;
        }
        return false;
    }

    /**
     * Returns the assistant played from the i-th player in th queue
     *
     * @param i index of the entry
     * @return the assistant played from the i-th player in the queue
     */
    private Assistant getAssistant(int i) {
        return queue.get(i).second();
    }

    /**
     * @return the queue with playerIds in the order they must play
     */
    public List<Integer> getPlayerIdQueue() {
        List<Integer> playerIdQueue = new ArrayList<>();
        for (Pair<Integer, Assistant> pair : queue) {
            playerIdQueue.add(pair.first());
        }
        return playerIdQueue;
    }

    /**
     * Remove all the items in the queue
     */
    public void clear() {
        queue.clear();
    }

}
