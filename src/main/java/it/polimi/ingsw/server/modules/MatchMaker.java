package it.polimi.ingsw.server.modules;

import it.polimi.ingsw.network.JsonCommand;
import it.polimi.ingsw.network.messages.Command;
import it.polimi.ingsw.network.messages.Parameters;
import it.polimi.ingsw.network.observers.JsonCommandChangeEvent;
import it.polimi.ingsw.network.observers.JsonCommandChangeListener;
import it.polimi.ingsw.network.observers.MatchChangeListener;
import it.polimi.ingsw.network.observers.MatchChangeSupport;

import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles the client request of joining a match
 * If the client requests to enter an already present match it connects to it
 * Otherwise it creates a new waiting match and connects the client to it
 */
public class MatchMaker implements JsonCommandChangeListener {

    private final ClientHandler clientHandler;
    private final String clientNickname;
    private final Queue<Match> waitingMatches;
    private final Queue<Match> activeMatches;
    private final MatchChangeSupport matchChangeSupport;
    private final String clientIp;

    /**
     * @param clientHandler the client handler that handles the current client
     * @param clientNickname the client nickname
     * @param waitingMatches the list of the waiting matches owned by the server
     * @param activeMatches the list of the active matches owned by the server
     * @param mcl the reference to the match change listener that will be notified when the match is ready to start
     */
    public MatchMaker(ClientHandler clientHandler, String clientNickname, Queue<Match> waitingMatches, Queue<Match> activeMatches, MatchChangeListener mcl) {
        this.clientHandler = clientHandler;
        this.clientNickname = clientNickname;
        this.waitingMatches = waitingMatches;
        this.activeMatches = activeMatches;
        clientIp = "Client " + clientHandler.getClientIp();
        clientHandler.addMessageListener(this);
        matchChangeSupport = new MatchChangeSupport();
        matchChangeSupport.addMatchChangeListener(mcl);
    }

    /**
     * Fired when the message with the requested match is received from the client
     * If the client requests to enter an already present match it connects to it
     * Otherwise it creates a new waiting match and connects the client to it
     */
    @Override
    public void jsonCommandChange(JsonCommandChangeEvent event) {
        if (event.getEventName().equals("messageReceived")) {
            JsonCommand request = event.getJsonCommand();

            if (request.getCommand().equals(Command.JOIN_MATCH)) {
                int numPlayers = Integer.parseInt(request.getParameter(Parameters.NUM_PLAYERS));
                boolean expertMatch = Boolean.parseBoolean(request.getParameter(Parameters.EXPERT_MATCH));

                Match requestedMatch = findRequestedMatch(numPlayers, expertMatch);
                if (requestedMatch == null) {
                    addNewWaitingMatch(numPlayers, expertMatch);
                    sendJoinSuccessfulResponseToClient();
                } else {
                    addClientToMatch(requestedMatch);
                    if (requestedMatch.isReadyForStart()) {
                        logClientJoinAndMatchStart(numPlayers, expertMatch);
                        sendJoinSuccessfulResponseToClient();
                        moveFromWaitingToActive(requestedMatch);
                    } else {
                        sendJoinSuccessfulResponseToClient();
                        logClientJoin(numPlayers, expertMatch);
                    }
                }
            }
        }
    }

    /**
     * Sends the join successful message to the client
     */
    private void sendJoinSuccessfulResponseToClient() {
        JsonCommand response = new JsonCommand(Command.JOIN_SUCCESSFUL);
        clientHandler.sendMessageToClient(response.toJson());
    }

    private void addClientToMatch(Match match) {
        match.addClient(clientHandler, clientNickname);
    }

    /**
     * Searches for the requested match in waiting matches based on num players and expert property
     *
     * @return the requested match or null if there is no waiting matches with requested properties
     */
    private Match findRequestedMatch(int numPlayers, boolean expertMatch) {
        for (Match match : waitingMatches) {
            if (match.getNumPlayers() == numPlayers && match.isExpertMatch() == expertMatch) {
                return match;
            }
        }
        return null;
    }

    /**
     * Add a new match with the given properties to the waiting matches
     */
    private void addNewWaitingMatch(int numPlayers, boolean expertMatch) {
        Match newMatch = new Match(numPlayers, expertMatch);
        addClientToMatch(newMatch);
        waitingMatches.add(newMatch);

        logClientJoinAndCreatedMatch(numPlayers, expertMatch);
    }

    /**
     * Moves the given match from the waiting ones to the active ones
     * Then notifies the server that a match is ready to start
     * @throws IllegalStateException if the match is not ready to start
     */
    private void moveFromWaitingToActive(Match match) {
        if(match.isReadyForStart()) {
            activeMatches.add(match);
            waitingMatches.remove(match);
            clientHandler.removeMessageListener(this);
            matchChangeSupport.fireMatchChange("startMatch", match);
        } else {
            throw new IllegalStateException("Trying to move to active matches a match that is not ready to start");
        }
    }


    // UTILS
    private String getMatchTypeFormatted(int numPlayers, boolean expertMatch) {
        return "(numPlayer=" + numPlayers + ", expert=" + expertMatch + ")";
    }

    private void logClientJoin(int numPlayers, boolean expertMatch) {
        String message = clientIp + ": Si è unito a una partita " + getMatchTypeFormatted(numPlayers, expertMatch);
        Logger.getLogger(MatchMaker.class.getName()).log(Level.INFO, () -> message);
    }

    private void logClientJoinAndCreatedMatch(int numPlayers, boolean expertMatch) {
        String message = clientIp + ": Ha creato una nuova partita " + getMatchTypeFormatted(numPlayers, expertMatch);
        Logger.getLogger(MatchMaker.class.getName()).log(Level.INFO, () -> message);
    }

    private void logClientJoinAndMatchStart(int numPlayers, boolean expertMatch) {
        String message = clientIp + ": Si è unito a una partita, avvio preparazione partita " + getMatchTypeFormatted(numPlayers, expertMatch);
        Logger.getLogger(MatchMaker.class.getName()).log(Level.INFO, () -> message);
    }

}
