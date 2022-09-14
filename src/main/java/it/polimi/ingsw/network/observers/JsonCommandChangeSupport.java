package it.polimi.ingsw.network.observers;

import it.polimi.ingsw.network.JsonCommand;

import java.util.ArrayList;
import java.util.List;

public class JsonCommandChangeSupport {
    private final List<JsonCommandChangeListener> jsonCommandChangeListeners;

    public JsonCommandChangeSupport() {
        jsonCommandChangeListeners = new ArrayList<>();
    }

    public void addJsonCommandChangeListener(JsonCommandChangeListener listener) {
        jsonCommandChangeListeners.add(listener);
    }

    public void removeJsonCommandChangeListener(JsonCommandChangeListener listener) {
        jsonCommandChangeListeners.remove(listener);
    }

    public void fireJsonCommandChange(String eventName, JsonCommand newValue) {
        JsonCommandChangeEvent jsonCommandChangeEvent = new JsonCommandChangeEvent(eventName, newValue);
        for (JsonCommandChangeListener jccl : jsonCommandChangeListeners) {
            jccl.jsonCommandChange(jsonCommandChangeEvent);
        }
    }

    public JsonCommandChangeListener[] getJsonCommandChangeListeners() {
        return jsonCommandChangeListeners.toArray(JsonCommandChangeListener[]::new);
    }
}
