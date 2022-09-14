package it.polimi.ingsw.network.observers;

import it.polimi.ingsw.server.modules.ClientHandler;

public record ClientHandlerChangeEvent(String eventName, ClientHandler clientHandler) {

    public ClientHandler getClientHandler() {
        return clientHandler;
    }

    public String getEventName() {
        return eventName;
    }
}
