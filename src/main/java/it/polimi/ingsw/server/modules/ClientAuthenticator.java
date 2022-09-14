package it.polimi.ingsw.server.modules;

import it.polimi.ingsw.network.JsonCommand;
import it.polimi.ingsw.network.messages.Command;
import it.polimi.ingsw.network.messages.Parameters;
import it.polimi.ingsw.network.observers.ClientHandlerChangeListener;
import it.polimi.ingsw.network.observers.ClientHandlerChangeSupport;
import it.polimi.ingsw.network.observers.JsonCommandChangeEvent;
import it.polimi.ingsw.network.observers.JsonCommandChangeListener;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles the client request of entering its nickname
 * If a nickname is already present it will be re-requested to the client
 */
public class ClientAuthenticator implements JsonCommandChangeListener {

    private final ClientHandler clientHandler;
    private final ConcurrentSkipListSet<String> nicknames;
    private final ConcurrentMap<ClientHandler, String> clientNicknames;
    private final ClientHandlerChangeSupport clientHandlerChangeSupport;

    /**
     * @param clientHandler the client handler that handles the current client
     * @param nicknames the list of nicknames that is used to see if client requested nickname is already present
     * @param clientNicknames the association of client and nickname, the client is added to this list when it sends a valid nickname
     * @param chcl the client handler listener that will be notified when the client has inserted a valid nickname (i.e. server)
     */
    public ClientAuthenticator(ClientHandler clientHandler, ConcurrentSkipListSet<String> nicknames, ConcurrentMap<ClientHandler, String> clientNicknames, ClientHandlerChangeListener chcl) {
        this.clientHandler = clientHandler;
        this.nicknames = nicknames;
        this.clientNicknames = clientNicknames;

        clientHandler.addMessageListener(this);

        clientHandlerChangeSupport = new ClientHandlerChangeSupport();
        clientHandlerChangeSupport.addClientHandlerChangeListener(chcl);
    }

    /**
     * Fired when the message with the nickname is received from the client
     */
    @Override
    public void jsonCommandChange(JsonCommandChangeEvent event) {
        if (event.getEventName().equals("messageReceived")) {
            JsonCommand request = event.getJsonCommand();

            Command command = request.getCommand();
            String name = request.getParameter(Parameters.NICKNAME);

            JsonCommand response;
            if (command == Command.LOGIN) {
                if (!isNicknameAlreadyPresent(name)) {
                    addNickname(name);
                    response = new JsonCommand(Command.LOGIN_SUCCESSFUL);
                    logClientEnteredNickname(name);
                } else {
                    response = new JsonCommand(Command.NICKNAME_ALREADY_PRESENT);
                }
                clientHandler.sendMessageToClient(response.toJson());
            } else {
                throw new IllegalArgumentException("Expected login command, " + command + " given");
            }
        }
    }

    /**
     * @return true if the given nickname is already present, false otherwise
     */
    private boolean isNicknameAlreadyPresent(String nickname) {
        return nicknames.contains(nickname);
    }

    /**
     * Adds the nickname to the list owned by the server then notifies it
     */
    private void addNickname(String name) {
        nicknames.add(name);
        clientNicknames.put(clientHandler, name);
        clientHandler.removeMessageListener(this);
        clientHandlerChangeSupport.fireClientHandlerChange("clientAuthenticated", clientHandler);
    }

    private void logClientEnteredNickname(String name) {
        String message = "Client " + clientHandler.getClientIp() + ": Ha inserito il suo nickname (" + name + ")";
        Logger.getLogger(ClientAuthenticator.class.getName()).log(Level.INFO, () -> message);
    }
}
