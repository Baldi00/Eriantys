package it.polimi.ingsw.network.observers;

import it.polimi.ingsw.network.JsonCommand;

public record JsonCommandChangeEvent(String eventName, JsonCommand jsonCommand) {

    public JsonCommand getJsonCommand() {
        return jsonCommand;
    }

    public String getEventName() {
        return eventName;
    }
}
