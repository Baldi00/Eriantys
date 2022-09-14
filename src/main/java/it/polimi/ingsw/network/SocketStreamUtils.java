package it.polimi.ingsw.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SocketStreamUtils {
    public static BufferedReader getInputStream(Socket clientSocket) {
        try {
            return new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            Logger.getLogger(SocketStreamUtils.class.getName()).log(
                    Level.SEVERE, () -> "Errore nell'apertura dello stream di input");
        }
        return null;
    }

    public static PrintWriter getOutputStream(Socket clientSocket) {
        try {
            return new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            Logger.getLogger(SocketStreamUtils.class.getName()).log(
                    Level.SEVERE, () -> "Errore nell'apertura dello stream di output");
        }
        return null;
    }

    private SocketStreamUtils() {
        // hide constructor
    }
}
