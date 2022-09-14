package it.polimi.ingsw.server;

import it.polimi.ingsw.server.modules.ClientAuthenticator;
import it.polimi.ingsw.server.modules.ClientHandler;
import it.polimi.ingsw.server.modules.Match;
import it.polimi.ingsw.server.modules.MatchMaker;
import it.polimi.ingsw.network.JsonCommand;
import it.polimi.ingsw.network.SocketStreamUtils;
import it.polimi.ingsw.network.messages.Command;
import it.polimi.ingsw.network.observers.ClientHandlerChangeEvent;
import it.polimi.ingsw.network.observers.ClientHandlerChangeListener;
import it.polimi.ingsw.network.observers.MatchChangeEvent;
import it.polimi.ingsw.network.observers.MatchChangeListener;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server implements MatchChangeListener, ClientHandlerChangeListener, Runnable {

    private static final int DEFAULT_PORT = 5000;
    private static final long MILLIS_BETWEEN_BEATS = 1000;
    private static final long MILLIS_BETWEEN_SERVER_DOWN_CHECK = 2000;
    private static final long MILLIS_TO_CONSIDER_SERVER_DOWN = 3000;
    private static final String CLIENT = "Client ";

    private final int port;
    private Queue<ClientHandler> connectedClients;
    private final ConcurrentMap<ClientHandler, Long> connectedClientsLastBeat;
    private final ConcurrentMap<ClientHandler, String> clientNicknames;
    private final ConcurrentSkipListSet<String> nicknames;
    private final Queue<Match> waitingMatches;
    private final Queue<Match> activeMatches;
    private boolean serverRunning;

    public Server() {
        this(DEFAULT_PORT);
    }

    public Server(int port) {
        this.port = port;

        connectedClients = new ConcurrentLinkedQueue<>();
        connectedClientsLastBeat = new ConcurrentHashMap<>();
        clientNicknames = new ConcurrentHashMap<>();
        nicknames = new ConcurrentSkipListSet<>();
        waitingMatches = new ConcurrentLinkedQueue<>();
        activeMatches = new ConcurrentLinkedQueue<>();
    }

    public void run() {
        setServerRunning(true);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            String message = "Server avviato all'indirizzo: " + InetAddress.getLocalHost().getHostAddress() + ":" + port;
            Logger.getLogger(Server.class.getName()).log(Level.INFO, () -> message);
            Logger.getLogger(Server.class.getName()).log(Level.INFO, "Server in attesa di connessioni...");

            ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(2);
            executor.scheduleAtFixedRate(sendBeatsToClients, 0, MILLIS_BETWEEN_BEATS, TimeUnit.MILLISECONDS);
            executor.scheduleAtFixedRate(removeOfflineClients, 0, MILLIS_BETWEEN_SERVER_DOWN_CHECK, TimeUnit.MILLISECONDS);

            acceptClients(serverSocket);
        } catch (IOException e) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, "Errore nella creazione del server");
        }
    }

    private void acceptClients(ServerSocket serverSocket) {
        while (serverRunning) {
            try {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clientHandler.addBeatListener(this);
                connectedClients.add(clientHandler);
                connectedClientsLastBeat.put(clientHandler, System.currentTimeMillis());

                String message = CLIENT + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort() + ": Si è connesso";
                Logger.getLogger(Server.class.getName()).log(Level.INFO, () -> message);

                new Thread(clientHandler).start();
                new ClientAuthenticator(clientHandler, nicknames, clientNicknames, this);
                clientHandler.sendMessageToClient(new JsonCommand(Command.ENTER_NICKNAME).toJson());
            } catch (IOException e) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, "Errore nella accept del client");
            }
        }
    }

    private final Runnable sendBeatsToClients = () -> {
        JsonCommand beat = new JsonCommand(Command.BEAT);
        for (ClientHandler client : connectedClients) {
            PrintWriter outputStream = SocketStreamUtils.getOutputStream(client.getSocket());
            if (outputStream != null)
                outputStream.println(beat);
        }
    };

    private final Runnable removeOfflineClients = () -> {
        for (ClientHandler client : connectedClients) {
            if (isClientDown(client)) {
                removeClientFromServerLists(client);
                Match match = findMatchByClientHandler(client);
                if (match != null) {
                    removeClientsOfTheMatchFromServerLists(client, match);
                }

                String message = CLIENT + client.getSocket().getInetAddress().getHostAddress() + ":" + client.getSocket().getPort() + ": Il client è stato disconnesso definitivamente";
                Logger.getLogger(Server.class.getName()).log(Level.INFO, () -> message);
            }
        }
    };

    private boolean isClientDown(ClientHandler client) {
        long currentTimestamp = System.currentTimeMillis();
        Long lastBeat = connectedClientsLastBeat.get(client);
        return lastBeat != null
                && Math.abs(currentTimestamp - lastBeat) > MILLIS_TO_CONSIDER_SERVER_DOWN;
    }

    private void removeClientsOfTheMatchFromServerLists(ClientHandler client, Match match) {
        Queue<ClientHandler> otherClientsInMatch = match.getClients();
        for (ClientHandler otherClient : otherClientsInMatch) {
            if (!otherClient.equals(client)) {
                otherClient.sendMessageToClient(new JsonCommand(Command.FORCE_END_MATCH).toJson());
                String message = CLIENT + client.getSocket().getInetAddress().getHostAddress() + ":" + client.getSocket().getPort() + ": Il client è stato disconnesso definitivamente";
                Logger.getLogger(Server.class.getName()).log(Level.INFO, () -> message);
                removeClientFromServerLists(otherClient);
            }
        }
        waitingMatches.remove(match);
        activeMatches.remove(match);
    }

    private void removeClientFromServerLists(ClientHandler client) {
        // This call is mandatory to avoid memory leaks after a match is ended
        client.removeAllListeners();

        connectedClients.remove(client);
        connectedClientsLastBeat.remove(client);
        nicknames.remove(clientNicknames.remove(client));
    }

    private Match findMatchByClientHandler(ClientHandler client) {
        for (Match match : activeMatches) {
            if (match.getClients().contains(client))
                return match;
        }
        for (Match match : waitingMatches) {
            if (match.getClients().contains(client))
                return match;
        }
        return null;
    }

    public void setServerRunning(boolean serverRunning) {
        this.serverRunning = serverRunning;
    }

    @Override
    public void matchChange(MatchChangeEvent event) {
        if (event.getEventName().equals("startMatch")) {
            event.getMatch().create();
        }
    }

    @Override
    public void clientHandlerChange(ClientHandlerChangeEvent event) {
        if (event.getEventName().equals("clientAuthenticated")) {
            new MatchMaker(event.getClientHandler(), clientNicknames.get(event.getClientHandler()), waitingMatches, activeMatches, this);
        } else if (event.getEventName().equals("beatReceived")) {
            connectedClientsLastBeat.put(event.getClientHandler(), System.currentTimeMillis());
        }
    }
}
