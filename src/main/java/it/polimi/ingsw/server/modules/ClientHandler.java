package it.polimi.ingsw.server.modules;

import it.polimi.ingsw.network.JsonCommand;
import it.polimi.ingsw.network.SocketStreamUtils;
import it.polimi.ingsw.network.messages.Command;
import it.polimi.ingsw.network.observers.ClientHandlerChangeListener;
import it.polimi.ingsw.network.observers.ClientHandlerChangeSupport;
import it.polimi.ingsw.network.observers.JsonCommandChangeListener;
import it.polimi.ingsw.network.observers.JsonCommandChangeSupport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles the communication with a single client
 */
public class ClientHandler implements Runnable {
    private final Socket socket;
    private final BufferedReader inputStream;
    private final PrintWriter outputStream;
    private final String clientIp;
    private final JsonCommandChangeSupport messageListeners;
    private final ClientHandlerChangeSupport beatListeners;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        messageListeners = new JsonCommandChangeSupport();
        beatListeners = new ClientHandlerChangeSupport();

        clientIp = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
        inputStream = SocketStreamUtils.getInputStream(socket);
        outputStream = SocketStreamUtils.getOutputStream(socket);
        if (inputStream == null || outputStream == null) {
            String message = "Client " + clientIp + ": Errore nell'apertura dei flussi di rete";
            Logger.getLogger(ClientHandler.class.getName()).log(Level.INFO, () -> message);
        }
    }

    /**
     * Listens for messages from the client
     * When a message is received it notifies the registered listeners
     */
    @Override
    public void run() {
        boolean running = true;
        while (running) {
            try {
                String command;
                if ((command = inputStream.readLine()) != null) {
                    JsonCommand jsonCommand = JsonCommand.fromJson(command);
                    notifyListeners(jsonCommand);
                } else {
                    running = false;
                }
            } catch (IOException e) {
                running = false;
            }
        }
        String message = "Client " + clientIp + ": Si è disconnesso";
        Logger.getLogger(ClientHandler.class.getName()).log(Level.INFO, () -> message);
    }

    /**
     * Register a new message listener
     */
    public void addMessageListener(JsonCommandChangeListener jccl) {
        messageListeners.addJsonCommandChangeListener(jccl);
    }

    /**
     * Register a new beat listener
     */
    public void addBeatListener(ClientHandlerChangeListener chcl) {
        beatListeners.addClientHandlerChangeListener(chcl);
    }

    public void removeMessageListener(JsonCommandChangeListener jccl) {
        messageListeners.removeJsonCommandChangeListener(jccl);
    }

    public void removeBeatListener(ClientHandlerChangeListener chcl) {
        beatListeners.removeClientHandlerChangeListener(chcl);
    }

    /**
     * Removes all the registered listeners
     */
    public void removeAllListeners() {
        JsonCommandChangeListener[] mListeners = messageListeners.getJsonCommandChangeListeners();
        ClientHandlerChangeListener[] bListeners = beatListeners.getClientHandlerChangeListeners();

        for (JsonCommandChangeListener ml : mListeners) {
            removeMessageListener(ml);
        }

        for (ClientHandlerChangeListener bl : bListeners) {
            removeBeatListener(bl);
        }
    }

    /**
     * Notifies the registered listeners of the received message
     * If the message is a beat from the client it notifies the registered beat listeners (i.e. the server)
     * Otherwise it notifies the message listeners
     */
    private void notifyListeners(JsonCommand jsonCommand) {
        if (jsonCommand.getCommand().equals(Command.BEAT)) {
            beatListeners.fireClientHandlerChange("beatReceived", this);
        } else {
            messageListeners.fireJsonCommandChange("messageReceived", jsonCommand);
        }
    }

    /**
     * Sends the message to the handled client
     */
    public void sendMessageToClient(String message) {
        outputStream.println(message);
    }

    public String getClientIp() {
        return clientIp;
    }

    public Socket getSocket() {
        return socket;
    }
}
