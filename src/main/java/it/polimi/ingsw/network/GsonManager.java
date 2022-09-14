package it.polimi.ingsw.network;

import com.google.gson.GsonBuilder;
import com.google.gson.Gson;

public class GsonManager {
    private static Gson instance;

    private GsonManager() {
    }

    public static Gson getInstance() {
        if (instance == null)
            instance = new GsonBuilder().create();
        return instance;
    }
}
