package it.polimi.ingsw.server.modules;

import it.polimi.ingsw.models.GameManager;
import it.polimi.ingsw.server.ServerController;

import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

/**
 * A match contains the information about the connected clients and their nicknames
 * When all players are connected it can create the game manager and its gameState
 */
public class Match {
    private final int numPlayers;
    private final boolean expertMatch;
    private final ConcurrentLinkedQueue<ClientHandler> clients;
    private final ConcurrentMap<ClientHandler, String> nicknames;

    public Match(int numPlayers, boolean expertMatch) {
        this.numPlayers = numPlayers;
        this.expertMatch = expertMatch;
        clients = new ConcurrentLinkedQueue<>();
        nicknames = new ConcurrentHashMap<>();
    }

    public void addClient(ClientHandler clientHandler, String nickname) {
        if (!isReadyForStart()) {
            clients.add(clientHandler);
            nicknames.put(clientHandler, nickname);
        }
    }

    /**
     * @return true if the match is ready for start
     *         A match is ready for start when the number of connected clients is the same as declared num players
     */
    public boolean isReadyForStart() {
        return clients.size() == numPlayers;
    }

    /**
     * Creates the game manager with its gameState and the controller that will manage the match
     * A match is ready for creation when the number of connected clients is the same as declared num players
     * @throws IllegalStateException if the match is not ready to be created
     */
    public void create() {
        if (isReadyForStart()) {
            GameManager gameManager = new GameManager(numPlayers, expertMatch);
            ServerController serverController = new ServerController(gameManager, nicknames.values().stream().toList());
            serverController.addClientHandlers(clients.stream().toList());
            serverController.sendFirstWizardAndTowerRequestToClients();
        } else {
            throw new IllegalStateException("Trying to create a match but not all players are connected");
        }
    }

    public Queue<ClientHandler> getClients() {
        return clients;
    }

    public int getNumPlayers() {
        return numPlayers;
    }

    public boolean isExpertMatch() {
        return expertMatch;
    }

    /**
     * Explicit the fact that two matches are equals if they have the same reference
     */
    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    /**
     * Explicit the fact that two matches have the same hashCode if they have the same reference
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
