package it.polimi.ingsw.network.observers;

import it.polimi.ingsw.server.modules.Match;

public record MatchChangeEvent(String eventName, Match match) {

    public Match getMatch() {
        return match;
    }

    public String getEventName() {
        return eventName;
    }
}
