package it.polimi.ingsw.network.observers;

import it.polimi.ingsw.server.modules.Match;

import java.util.ArrayList;
import java.util.List;

public class MatchChangeSupport {
    private final List<MatchChangeListener> matchChangeListeners;

    public MatchChangeSupport() {
        matchChangeListeners = new ArrayList<>();
    }

    public void addMatchChangeListener(MatchChangeListener listener) {
        matchChangeListeners.add(listener);
    }

    public void removeMatchChangeListener(MatchChangeListener listener) {
        matchChangeListeners.remove(listener);
    }

    public void fireMatchChange(String eventName, Match newValue) {
        MatchChangeEvent matchChangeEvent = new MatchChangeEvent(eventName, newValue);
        for (MatchChangeListener mcl : matchChangeListeners) {
            mcl.matchChange(matchChangeEvent);
        }
    }

    public MatchChangeListener[] getMatchChangeListeners() {
        return matchChangeListeners.toArray(MatchChangeListener[]::new);
    }
}
