package it.polimi.ingsw.network.observers;

import it.polimi.ingsw.server.modules.ClientHandler;

import java.util.ArrayList;
import java.util.List;

public class ClientHandlerChangeSupport {
    private final List<ClientHandlerChangeListener> clientHandlerChangeListeners;

    public ClientHandlerChangeSupport() {
        clientHandlerChangeListeners = new ArrayList<>();
    }

    public void addClientHandlerChangeListener(ClientHandlerChangeListener listener) {
        clientHandlerChangeListeners.add(listener);
    }

    public void removeClientHandlerChangeListener(ClientHandlerChangeListener listener) {
        clientHandlerChangeListeners.remove(listener);
    }

    public void fireClientHandlerChange(String eventName, ClientHandler newValue) {
        ClientHandlerChangeEvent clientHandlerChangeEvent = new ClientHandlerChangeEvent(eventName, newValue);
        for (ClientHandlerChangeListener chcl : clientHandlerChangeListeners) {
            chcl.clientHandlerChange(clientHandlerChangeEvent);
        }
    }

    public ClientHandlerChangeListener[] getClientHandlerChangeListeners() {
        return clientHandlerChangeListeners.toArray(ClientHandlerChangeListener[]::new);
    }
}
