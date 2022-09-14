package it.polimi.ingsw.clients.cli;

import it.polimi.ingsw.clients.ClientController;
import it.polimi.ingsw.models.components.*;
import it.polimi.ingsw.models.components.hall.Hall;
import it.polimi.ingsw.models.components.characters.Character;
import it.polimi.ingsw.models.components.characters.CharacterType;
import it.polimi.ingsw.models.components.characters.effects.EffectArgs;
import it.polimi.ingsw.models.exceptions.IllegalMoveException;
import it.polimi.ingsw.models.state.GameState;
import it.polimi.ingsw.models.state.Stage;
import it.polimi.ingsw.network.GsonManager;
import it.polimi.ingsw.network.JsonCommand;
import it.polimi.ingsw.network.messages.Command;
import it.polimi.ingsw.network.messages.Parameters;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.List;

/**
 * Listen and sends messages from/to server and asks the moves to the user via CLI
 */
public class ClientControllerCli extends ClientController implements Runnable {

    private static final String YOUR_CHOICE = "La tua scelta: ";
    private static final String CHOOSE_STUDENT_BETWEEN = "Scegli uno studente tra";
    private static final String CHOOSE_STUDENT_FROM_HALL = "Scegli uno studente dalla sala";
    private static final String CHOOSE_STUDENT_FROM_ENTRANCE = "Scegli uno studente dall'ingresso";
    private static final String CHOOSE_STUDENT_FROM_CARD = "Scegli uno studente dalla carta";
    private static final String SKIP_LINE = "\n----------------------------------------------------------------------------------\n";

    private final ClientViewCli clientViewCli;
    private final List<Command> possibleMoves;

    public ClientControllerCli() {
        super();
        clientViewCli = new ClientViewCli();
        possibleMoves = new ArrayList<>();
    }

    @Override
    public void showServerUnreachableMessage() {
        printMessage("\n\nIl server non è raggiungibile. Prova a ricollegarti più tardi.");
    }

    /**
     * Connects to server than interacts with server
     */
    @Override
    public void run() {
        boolean connectedToServer = false;

        printMessage("Effettua il login al server");

        while (!connectedToServer) {
            String serverIp = requestServerIpToUser();
            Integer serverPort = requestServerPortToUser();

            connectedToServer = connectToServer(serverIp, serverPort);
            if (connectedToServer) {
                printMessage("Connesso al server");
            } else {
                printMessage("Server non trovato, reinserisci i dati del server");
            }
        }

        startSendingBeatsToServer();
        updateServerBeatTimestamp();
        startCheckingIfServerIsUp();
        listenAndProcessServerMessage();
    }

    /**
     * Listens for server messages and launches a message processor when a message arrives
     */
    private void listenAndProcessServerMessage() {
        BufferedReader stream = getInputStream();
        while (isClientRunning()) {
            try {
                String message;
                if ((message = stream.readLine()) != null)
                    startProcessingMessage(message);
                else
                    setClientRunning(false);
            } catch (IOException e) {
                setClientRunning(false);
            }
        }
    }

    /**
     * Creates and launches a message processor for processing the message sent from server
     *
     * @param message message to be processed
     */
    private void startProcessingMessage(String message) {
        new Thread(new MessageProcessor(message)).start();
    }

    // REQUESTS FOR USER INPUT

    private String requestServerIpToUser() {
        String ip;
        do {
            clientViewCli.print("Inserisci l'indirizzo IP del server: ");
            ip = clientViewCli.nextLine();
        } while (ip.equals(""));

        return ip;
    }

    private Integer requestServerPortToUser() {
        Integer serverPort;
        do {
            clientViewCli.print("Inserisci la porta del server: ");
            try {
                serverPort = clientViewCli.nextInt();
            } catch (InputMismatchException e) {
                serverPort = null;
            }
            clientViewCli.nextLine();
        } while (serverPort == null);
        return serverPort;
    }

    // UTILS

    public void printMessage(String message) {
        clientViewCli.println(message);
    }

    private class MessageProcessor implements Runnable {

        private final String message;

        private MessageProcessor(String message) {
            this.message = message;
        }

        /**
         * Executes the right function based on the received message
         */
        @Override
        public void run() {
            JsonCommand jsonMessage = JsonCommand.fromJson(message);
            switch (jsonMessage.getCommand()) {
                case ENTER_NICKNAME -> requestAndSendNickname(false);
                case LOGIN_SUCCESSFUL -> requestAndSendMatchType();
                case NICKNAME_ALREADY_PRESENT -> requestAndSendNickname(true);
                case JOIN_SUCCESSFUL -> printJoinSuccessfulMessage();
                case CHOOSE_WIZARD_TOWER -> {
                    setGameState(GsonManager.getInstance().fromJson(jsonMessage.getParameter(Parameters.GAME_STATE), GameState.class));
                    String nicknameThatHasToChoose = jsonMessage.getParameter(Parameters.NICKNAME);
                    if (nicknameThatHasToChoose.equals(getNickname())) {
                        List<Wizard> availableWizards = getGameState().getAvailableWizards();
                        List<Tower> availableTowers = getGameState().getAvailableTowers();
                        requestAndSendWizardAndTower(availableWizards, availableTowers);
                    }
                }
                case MOVE_DONE -> {
                    setLastMoveFromServer(JsonCommand.fromJson((jsonMessage.getParameter(Parameters.LAST_MOVE))));
                    if (!getLastMoveFromServer().getCommand().equals(Command.ILLEGAL_MOVE)) {
                        setGameState(GsonManager.getInstance().fromJson(jsonMessage.getParameter(Parameters.GAME_STATE), GameState.class));
                    }

                    clientViewCli.clear();
                    printGameState();
                    printLastMove();

                    if (!isGameOver()) {
                        if (isMyTurn()) {
                            Command nextMove = chooseNextMove();
                            performPlayerMove(nextMove);
                        } else {
                            clientViewCli.println(SKIP_LINE);
                            clientViewCli.println("Attendi il tuo turno...");
                        }
                    } else {
                        printGameOverAndWinner();
                    }
                }
                case FORCE_END_MATCH -> forceEndMatch();
                case BEAT -> updateServerBeatTimestamp();
                default -> throw new IllegalStateException("Invalid command received from server");
            }
        }

        /**
         * Shows the possible moves and requests to user the next move to be performed
         *
         * @return the next move to be performed
         */
        private Command chooseNextMove() {
            preparePossibleMoves();
            printPossibleMoves();
            return requestNextMoveToUser();
        }

        /**
         * Prepares the list of possible moves that user can do
         */
        private void preparePossibleMoves() {
            possibleMoves.clear();

            switch (getGameState().getStage()) {
                case PLANNING_PLAY_ASSISTANTS -> possibleMoves.add(Command.PLAYER_MOVE_PLAY_ASSISTANT);
                case ACTION_MOVE_STUDENTS -> {
                    possibleMoves.add(Command.PLAYER_MOVE_MOVE_STUDENT_FROM_ENTRANCE_TO_HALL);
                    possibleMoves.add(Command.PLAYER_MOVE_MOVE_STUDENT_FROM_ENTRANCE_TO_ISLAND);
                }
                case ACTION_MOVE_MOTHER_NATURE -> possibleMoves.add(Command.PLAYER_MOVE_MOVE_MOTHER_NATURE);
                case ACTION_TAKE_STUDENTS_FROM_CLOUD -> possibleMoves.add(Command.PLAYER_MOVE_PICK_STUDENTS_FROM_CLOUD);
                case ACTION_END_TURN -> possibleMoves.add(Command.PLAYER_MOVE_END_TURN);
                default -> throw new IllegalStateException("Game state is in a wrong stage (" + getGameState().getStage() + ")");
            }

            if (getGameState().isExpertMatch() && canPlayCharacter()) {
                possibleMoves.add(Command.PLAYER_MOVE_PLAY_CHARACTER);
            }

            possibleMoves.add(Command.LOGOUT);
        }

        /**
         * Performs the move done by the user
         *
         * @param move move to be performed
         */
        private void performPlayerMove(Command move) {
            switch (move) {
                case PLAYER_MOVE_PLAY_ASSISTANT -> {
                    Assistant chosenAssistant = requestAssistantToUser();
                    sendAssistantToServer(chosenAssistant);
                }
                case PLAYER_MOVE_MOVE_STUDENT_FROM_ENTRANCE_TO_HALL -> {
                    Student chosenStudent = requestStudentToMoveFromEntranceToUser();
                    sendStudentToMoveFromEntranceToHallToServer(chosenStudent);
                }
                case PLAYER_MOVE_MOVE_STUDENT_FROM_ENTRANCE_TO_ISLAND -> {
                    Student chosenStudent = requestStudentToMoveFromEntranceToUser();
                    int islandId = requestIslandIdToUser();
                    sendStudentToMoveFromEntranceToIslandToServer(chosenStudent, islandId);
                }
                case PLAYER_MOVE_MOVE_MOTHER_NATURE -> {
                    int chosenSteps = requestMotherNatureStepsToUser();
                    sendMotherNatureStepsToServer(chosenSteps);
                }
                case PLAYER_MOVE_PICK_STUDENTS_FROM_CLOUD -> {
                    int chosenCloud = requestCloudIdToUser();
                    sendCloudToServer(chosenCloud - 1);
                }
                case PLAYER_MOVE_PLAY_CHARACTER -> {
                    CharacterType characterType = requestCharacterToUser();
                    EffectArgs effectArgs = requestCharacterParametersToUser(characterType);
                    sendCharacterToServer(characterType, effectArgs.getStudent(), effectArgs.getIsland(), effectArgs.getSourceStudents(), effectArgs.getDestStudents());
                }
                case PLAYER_MOVE_END_TURN -> sendEndTurnToServer();
                case LOGOUT -> {
                    boolean userIsSureToLogout = requestAreYouSureToLogoutToUser();
                    if (userIsSureToLogout) {
                        sendLogoutMessageToServer();
                        System.exit(0);
                    } else {
                        Command nextMove = chooseNextMove();
                        performPlayerMove(nextMove);
                    }
                }
                default -> throw new IllegalMoveException("Invalid move");
            }
        }

        // REQUESTS FOR USER INPUT

        /**
         * @param failed true means previous attempt was unsuccessful so a proper message is shown
         */
        private void requestNicknameToUser(boolean failed) {
            if (failed) {
                printMessage("Nickname già presente, scegliene un altro");
            }
            String nickname;
            do {
                clientViewCli.print("Inserisci il tuo nickname: ");
                nickname = clientViewCli.nextLine();
            } while (nickname.equals(""));
            setNickname(nickname);
        }

        private Integer requestNumPlayerToUser() {
            Integer numPlayers;
            do {
                clientViewCli.print("Inserisci il numero di giocatori (2/3/4): ");
                try {
                    numPlayers = clientViewCli.nextInt();
                } catch (InputMismatchException e) {
                    numPlayers = null;
                }
                clientViewCli.nextLine();
            } while (numPlayers == null || (numPlayers != 2 && numPlayers != 3 && numPlayers != 4));
            return numPlayers;
        }

        private boolean requestIfExpertMatchToUser() {
            boolean expertMatch;
            String expertMatchString;
            do {
                clientViewCli.print("Vuoi giocare alla variante per esperti? (s/n): ");
                expertMatchString = clientViewCli.nextLine();
            } while (!expertMatchString.equalsIgnoreCase("s") && !expertMatchString.equalsIgnoreCase("n"));

            expertMatch = expertMatchString.equalsIgnoreCase("s");
            return expertMatch;
        }

        private Wizard requestWizardToUser(List<Wizard> wizards) {
            int chosenWizardNumber = chooseIntegerFromList("Scegli il mago tra", wizards, true);
            return Wizard.valueOf(wizards.get(chosenWizardNumber - 1).toString());
        }

        private Tower requestTowerToUser(List<Tower> towers) {
            int chosenTowerNumber = chooseIntegerFromList("Scegli la torre tra", towers, true);
            return Tower.valueOf(towers.get(chosenTowerNumber - 1).toString());
        }

        private Command requestNextMoveToUser() {
            int nextMove = requestBoundedIntegerToUser(possibleMoves.size());
            return possibleMoves.get(nextMove - 1);
        }

        private Assistant requestAssistantToUser() {
            clientViewCli.println("Scegli tra i tuoi assistenti:");
            printYourAssistants();
            Integer chosenAssistant;
            do {
                clientViewCli.print(YOUR_CHOICE);
                try {
                    chosenAssistant = clientViewCli.nextInt();
                } catch (InputMismatchException e) {
                    chosenAssistant = null;
                }
                clientViewCli.nextLine();
            } while (chosenAssistant == null || chosenAssistant < 1 || chosenAssistant > getGameState().getCurrentPlayer().getPlayableAssistants().size());
            return getGameState().getCurrentPlayer().getPlayableAssistants().get(chosenAssistant - 1);
        }

        private Student requestStudentToMoveFromEntranceToUser() {
            clientViewCli.println("Scelgi uno studente dall'entrata:");
            Entrance entrance = getGameState().getCurrentPlayer().getBoard().getEntrance();
            for (int i = 0; i < Student.values().length; i++) {
                clientViewCli.print("(" + (i + 1) + ") " + entrance.getNumStudentsByColor(Student.values()[i]) + " " + Student.values()[i] + " | ");
            }
            Integer chosenStudent;
            do {
                clientViewCli.print(YOUR_CHOICE);
                try {
                    chosenStudent = clientViewCli.nextInt();
                } catch (InputMismatchException e) {
                    chosenStudent = null;
                }
                clientViewCli.nextLine();
                if (chosenStudent != null && chosenStudent > 0 && chosenStudent <= Student.values().length && entrance.getNumStudentsByColor(Student.values()[chosenStudent - 1]) == 0) {
                    clientViewCli.println("Non ci sono studenti di questo colore, scegline un altro");
                }
            } while (chosenStudent == null || chosenStudent < 1 || chosenStudent > Student.values().length || entrance.getNumStudentsByColor(Student.values()[chosenStudent - 1]) == 0);
            return Student.values()[chosenStudent - 1];
        }

        private int requestIslandIdToUser() {
            clientViewCli.println("Scegli un'isola:");
            printIslands();

            Integer chosenIsland;
            do {
                clientViewCli.print(YOUR_CHOICE);
                try {
                    chosenIsland = clientViewCli.nextInt();
                } catch (InputMismatchException e) {
                    chosenIsland = null;
                }
                clientViewCli.nextLine();
            } while (chosenIsland == null || chosenIsland < 1 || chosenIsland > getGameState().getIslands().size());
            return convertChosenIslandToIslandId(chosenIsland);
        }

        private int requestMotherNatureStepsToUser() {
            int maxMotherNatureSteps = getGameState().getCurrentPlayer().getLastPlayedAssistant().getMotherNatureSteps();
            printIslands();
            Integer chosenSteps;
            do {
                clientViewCli.print("Di quanti passi vuoi far muovere madre natura (1-" + maxMotherNatureSteps + ")? ");
                try {
                    chosenSteps = clientViewCli.nextInt();
                } catch (InputMismatchException e) {
                    chosenSteps = null;
                }
                clientViewCli.nextLine();
            } while (chosenSteps == null || chosenSteps < 1 || chosenSteps > maxMotherNatureSteps);
            return chosenSteps;
        }

        private int requestCloudIdToUser() {
            clientViewCli.println("Scegli da quale nuvola prendere gli studenti:");
            printClouds();
            Integer cloudId;
            do {
                clientViewCli.print(YOUR_CHOICE);
                try {
                    cloudId = clientViewCli.nextInt();
                } catch (InputMismatchException e) {
                    cloudId = null;
                }
                clientViewCli.nextLine();
                if (cloudId != null && cloudId > 0 && cloudId <= getGameState().getClouds().size() && getGameState().getCloudById(cloudId - 1).isEmpty()) {
                    clientViewCli.println("L'isola non ha studenti, scegline un'altra");
                }
            } while (cloudId == null || cloudId < 1 || cloudId > getGameState().getClouds().size() || getGameState().getCloudById(cloudId - 1).isEmpty());
            return cloudId;
        }

        private CharacterType requestCharacterToUser() {
            clientViewCli.println("Scegli tra le carte personaggio:");
            printCharacters();
            Integer chosenCharacter;
            do {
                clientViewCli.print(YOUR_CHOICE);
                try {
                    chosenCharacter = clientViewCli.nextInt();
                } catch (InputMismatchException e) {
                    chosenCharacter = null;
                }
                clientViewCli.nextLine();
                if (chosenCharacter != null && getGameState().getExpertAttrs().getCharacters().get(chosenCharacter - 1).getCost() > getGameState().getCurrentPlayer().getNumCoins()) {
                    clientViewCli.println("Non hai abbastanza monete per giocare la carta personaggio");
                }
            } while (chosenCharacter == null || getGameState().getExpertAttrs().getCharacters().get(chosenCharacter - 1).getCost() > getGameState().getCurrentPlayer().getNumCoins());
            return getGameState().getExpertAttrs().getCharacters().get(chosenCharacter - 1).getCharacterType();
        }

        private boolean requestAreYouSureToLogoutToUser() {
            boolean logout;
            String logoutString;
            do {
                clientViewCli.print("Sei sicuro di volerti disconnettere? (s/n): ");
                logoutString = clientViewCli.nextLine();
            } while (!logoutString.equalsIgnoreCase("s") && !logoutString.equalsIgnoreCase("n"));

            logout = logoutString.equalsIgnoreCase("s");
            return logout;
        }

        /**
         * @param characterType the type of the character used for deciding what to ask user
         * @return the chosen parameters structured
         */
        private EffectArgs requestCharacterParametersToUser(CharacterType characterType) {
            EffectArgs.Builder effectArgsBuilder = new EffectArgs.Builder();
            Student student = null;
            Island island = null;
            List<Student> toExchangeFrom = null;
            List<Student> toExchangeTo = null;

            Character character = getGameState().getExpertAttrs().getCharacterByType(characterType);
            switch (characterType) {
                case DIONYSUS -> {
                    int chosenStudent = chooseIntegerFromList(CHOOSE_STUDENT_BETWEEN, character.getStudents(), true);
                    student = character.getStudents().get(chosenStudent - 1);
                    printIslands();
                    int chosenIsland = chooseIntegerFromList(YOUR_CHOICE, getGameState().getIslands(), false);
                    island = getGameState().getIslandByPosition(chosenIsland - 1);
                }
                case ORIFLAMME, CIRCE -> {
                    printIslands();
                    int chosenIsland = chooseIntegerFromList(YOUR_CHOICE, getGameState().getIslands(), false);
                    island = getGameState().getIslandByPosition(chosenIsland - 1);
                }
                case GOOMBA, THIEF -> {
                    int chosenStudent = chooseIntegerFromList(CHOOSE_STUDENT_BETWEEN, Arrays.asList(Student.values()), true);
                    student = Student.values()[chosenStudent - 1];
                }
                case APHRODITE -> {
                    int chosenStudent = chooseIntegerFromList(CHOOSE_STUDENT_BETWEEN, character.getStudents(), true);
                    student = character.getStudents().get(chosenStudent - 1);
                }
                case BARD -> {
                    List<Student> studentsOnHall = getStudentsOnHall();
                    List<Student> studentsOnEntrance = getStudentsOnEntrance();
                    int chosenStudentFromHall = chooseIntegerFromList(CHOOSE_STUDENT_FROM_HALL, studentsOnHall, true);
                    int chosenStudentFromEntrance = chooseIntegerFromList(CHOOSE_STUDENT_FROM_ENTRANCE, studentsOnEntrance, true);
                    toExchangeFrom = new ArrayList<>();
                    toExchangeTo = new ArrayList<>();
                    toExchangeFrom.add(studentsOnHall.get(chosenStudentFromHall - 1));
                    toExchangeTo.add(studentsOnEntrance.get(chosenStudentFromEntrance - 1));

                    boolean continueChoosing = requestContinueChoosingStudentsToExchangeToUser();

                    if (continueChoosing) {
                        chosenStudentFromHall = chooseIntegerFromList(CHOOSE_STUDENT_FROM_HALL, studentsOnHall, true);
                        chosenStudentFromEntrance = chooseIntegerFromList(CHOOSE_STUDENT_FROM_ENTRANCE, studentsOnEntrance, true);
                        toExchangeFrom.add(studentsOnHall.get(chosenStudentFromHall - 1));
                        toExchangeTo.add(studentsOnEntrance.get(chosenStudentFromEntrance - 1));
                    }
                }
                case JESTER -> {
                    List<Student> studentsOnEntrance = getStudentsOnEntrance();
                    int chosenStudentFromCharacter = chooseIntegerFromList(CHOOSE_STUDENT_FROM_CARD, character.getStudents(), true);
                    int chosenStudentFromEntrance = chooseIntegerFromList(CHOOSE_STUDENT_FROM_HALL, studentsOnEntrance, true);
                    toExchangeFrom = new ArrayList<>();
                    toExchangeTo = new ArrayList<>();
                    toExchangeFrom.add(character.getStudents().get(chosenStudentFromCharacter - 1));
                    toExchangeTo.add(studentsOnEntrance.get(chosenStudentFromEntrance - 1));

                    boolean continueChoosing = requestContinueChoosingStudentsToExchangeToUser();

                    if (continueChoosing) {
                        chosenStudentFromCharacter = chooseIntegerFromList(CHOOSE_STUDENT_FROM_CARD, character.getStudents(), true);
                        chosenStudentFromEntrance = chooseIntegerFromList(CHOOSE_STUDENT_FROM_HALL, studentsOnEntrance, true);
                        toExchangeFrom.add(character.getStudents().get(chosenStudentFromCharacter - 1));
                        toExchangeTo.add(studentsOnEntrance.get(chosenStudentFromEntrance - 1));

                        continueChoosing = requestContinueChoosingStudentsToExchangeToUser();

                        if (continueChoosing) {
                            chosenStudentFromCharacter = chooseIntegerFromList(CHOOSE_STUDENT_FROM_CARD, character.getStudents(), true);
                            chosenStudentFromEntrance = chooseIntegerFromList(CHOOSE_STUDENT_FROM_HALL, studentsOnEntrance, true);
                            toExchangeFrom.add(character.getStudents().get(chosenStudentFromCharacter - 1));
                            toExchangeTo.add(studentsOnEntrance.get(chosenStudentFromEntrance - 1));
                        }
                    }

                }
                default -> clientViewCli.print("");
            }

            if (student != null) {
                effectArgsBuilder.setStudent(student);
            }
            if (island != null) {
                effectArgsBuilder.setIsland(island);
            }
            if (toExchangeFrom != null) {
                effectArgsBuilder.setSourceStudents(toExchangeFrom);
            }
            if (toExchangeTo != null) {
                effectArgsBuilder.setDestStudents(toExchangeTo);
            }

            return effectArgsBuilder.build();
        }

        /**
         * Requests the user if he wants to continue exchanging students
         *
         * @return true if user wants to continue, false otherwise
         */
        private boolean requestContinueChoosingStudentsToExchangeToUser() {
            String continueChoosing;
            do {
                clientViewCli.print("Vuoi scambiare un altro studente (s/n)? ");
                continueChoosing = clientViewCli.nextLine();
            } while (!continueChoosing.equals("s") && !continueChoosing.equals("n"));
            return continueChoosing.equals("s");
        }

        /**
         * Requests a number to user. It re-requests it if the given number is grater than the maximum number
         * Minimum number that user can select is 1
         *
         * @param maxNumber maximum number that user can choose
         * @return the number chosen by the user
         */
        private int requestBoundedIntegerToUser(int maxNumber) {
            Integer number;
            do {
                clientViewCli.print(YOUR_CHOICE);
                try {
                    number = clientViewCli.nextInt();
                } catch (InputMismatchException e) {
                    number = null;
                }
                clientViewCli.nextLine();
            } while (number == null || number < 1 || number > maxNumber);

            return number;
        }

        // PRINTS

        private void printPossibleMoves() {
            clientViewCli.println(SKIP_LINE);
            clientViewCli.println("E' il tuo turno, scegli la tua prossima mossa:");

            for (int i = 1; i <= possibleMoves.size(); i++) {
                clientViewCli.println("(" + i + ") " + getCompleteMoveName(possibleMoves.get(i - 1)));
            }
        }

        private void printGameState() {
            clientViewCli.println(SKIP_LINE);
            printIslands();
            printAllBoards();
            printClouds();
            printBag();
            printYourAssistants();
            if (getGameState().isExpertMatch()) {
                printCharacters();
            }
        }

        private void printYourAssistants() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("\nI tuoi assistenti:\n");
            List<Assistant> assistants = getGameState().getCurrentPlayer().getPlayableAssistants();
            for (int i = 0; i < assistants.size(); i++) {
                Assistant assistant = assistants.get(i);
                stringBuilder.append(String.format("%2d", i + 1)).append(") ");
                stringBuilder.append(String.format("%41s", getAssistantDetails(assistant)));
                stringBuilder.append("\n");
            }
            clientViewCli.print(stringBuilder.toString());
        }

        private void printBoard(Player player) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("\n").append(player.getName()).append("\n");

            stringBuilder.append("Entrata:");
            Entrance entrance = player.getBoard().getEntrance();
            for (Student student : Student.values()) {
                stringBuilder.append(" ").append(entrance.getNumStudentsByColor(student)).append(" ").append(student).append(" |");
            }

            stringBuilder.append("\n").append("Sala:   ");
            Hall hall = player.getBoard().getHall();
            for (Student student : Student.values()) {
                stringBuilder.append(" ").append(hall.getNumStudentsByColor(student)).append(" ").append(student).append(" |");
            }

            stringBuilder.append("\n").append("Torri:   ")
                    .append(player.getBoard().getNumTowers()).append(" ")
                    .append(player.getBoard().getTowerType()).append("\n");

            Assistant assistant = player.getLastPlayedAssistant();
            if (assistant != null) {
                stringBuilder.append("Ultimo assistente giocato: ")
                        .append(getAssistantDetails(assistant));
                stringBuilder.append("\n");
            }

            if (getGameState().isExpertMatch()) {
                stringBuilder.append("Monete:  ")
                        .append(getGameState().getPlayerById(player.getId()).getNumCoins()).append("\n");
            }

            stringBuilder.append("Professori controllati: ");
            if (getGameState().getPlayerProfessors(player).isEmpty()) {
                stringBuilder.append("Nessuno");
            } else {
                for (Student student : getGameState().getPlayerProfessors(player)) {
                    stringBuilder.append(student).append(" ");
                }
            }
            stringBuilder.append("\n");

            clientViewCli.print(stringBuilder.toString());
        }

        private void printAllBoards() {
            clientViewCli.println("\nPlance:");
            List<Player> players = getGameState().getPlayers();
            for (Player player : players) {
                printBoard(player);
            }
        }

        private void printIslands() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Isole:\n");
            List<Island> islands = getGameState().getIslands();
            for (int i = 0; i < islands.size(); i++) {
                Island island = islands.get(i);
                stringBuilder.append(String.format("%2d", i + 1)).append(")");
                for (Student student : Student.values()) {
                    stringBuilder.append(" ").append(island.getNumStudent(student)).append(" ").append(student).append(" |");
                }

                stringBuilder.append("| Dimensione ").append(island.getDimension());

                if (island.hasTowers())
                    stringBuilder.append(" | Torre ").append(island.getTowerType());

                if (island.getPosition() == getGameState().getMotherNaturePosition()) {
                    stringBuilder.append(" | Madre Natura");
                }

                if (getGameState().isExpertMatch() && getGameState().getExpertAttrs().isIslandBlocked(island)) {
                    stringBuilder.append(" | Bloccata");
                }

                stringBuilder.append("\n");
            }
            clientViewCli.print(stringBuilder.toString());
        }

        private void printClouds() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("\nNuvole:\n");
            List<Cloud> clouds = getGameState().getClouds();
            for (int i = 0; i < clouds.size(); i++) {
                Cloud cloud = clouds.get(i);
                if (cloud.isEmpty()) {
                    stringBuilder.append(i + 1).append(") Nessun studente");
                } else {
                    List<Student> studentsOnCloud = cloud.pickStudents();
                    stringBuilder.append(i + 1).append(")");
                    for (Student student : studentsOnCloud) {
                        stringBuilder.append(" ").append(student);
                        cloud.receiveStudent(student);
                    }
                }
                stringBuilder.append("\n");
            }
            clientViewCli.print(stringBuilder.toString());
        }

        private void printBag() {
            int numStudentsInBag = getGameState().getBag().getNumStudent();
            clientViewCli.print("\nSacchetto: " + numStudentsInBag + " studenti rimasti\n");
        }

        private void printCharacters() {
            if (getGameState().isExpertMatch()) {
                clientViewCli.println("\nPersonaggi:");
                List<Character> characters = getGameState().getExpertAttrs().getCharacters();
                for (int i = 0; i < characters.size(); i++) {
                    Character character = characters.get(i);
                    clientViewCli.print((i + 1) + ") ");
                    printCharacter(character);
                }
            }
        }

        private void printCharacter(Character character) {
            clientViewCli.print(character.getCharacterType() + ", Costo " + character.getCost());
            switch (character.getCharacterType()) {
                case DIONYSUS, JESTER, APHRODITE -> {
                    clientViewCli.print(", Studenti:");
                    for (Student student : character.getStudents()) {
                        clientViewCli.print(" " + student);
                    }
                    clientViewCli.println("");
                }
                case CIRCE -> clientViewCli.print(", Divieti: " + character.getNumIslandBlocks() + "\n");
                default -> clientViewCli.println("");
            }
            clientViewCli.print("Effetto: ");
            printCharacterEffectDescription(character.getCharacterType());
        }

        private void printCharacterEffectDescription(CharacterType characterType) {
            switch (characterType) {
                case DIONYSUS -> clientViewCli.println("Prendi uno studente dalla carta e mettilo su un isola");
                case DAIRYMAN -> clientViewCli.println("Prendi controllo dei professori anche in caso di parità");
                case ORIFLAMME -> clientViewCli.println("Scegli un'isola su cui calcolare l'influenza");
                case ERMES -> clientViewCli.println("Puoi muovere madre natura fino a 2 isole addizionali");
                case CIRCE -> clientViewCli.println("Piazza una tessera divieto su un isola");
                case CENTAUR -> clientViewCli.println("Durante il calcolo dell'influenza le torri non sono considerate");
                case JESTER -> clientViewCli.println("Scambia fino a 3 studenti tra carta e ingresso");
                case KNIGHT -> clientViewCli.println("Hai 2 punti influenza addizionali");
                case GOOMBA -> clientViewCli.println("Scegli il colore di uno studente che non fornirà influenza");
                case BARD -> clientViewCli.println("Scambia fino a 2 studenti tra sala e ingresso");
                case APHRODITE -> clientViewCli.println("Prendi uno studente dalla carta e mettilo nella sala");
                case THIEF -> clientViewCli.println("Scegli il colore di uno studente, tutti i giocatori ne perdono 3 di quel colore");
            }
        }

        private void printLastMove() {
            clientViewCli.println("\nUltima mossa:");
            switch (getLastMoveFromServer().getCommand()) {
                case INITIALIZATION -> printLastMoveInitialization();
                case PLAYER_MOVE_PLAY_ASSISTANT -> printLastMovePlayAssistant();
                case PLAYER_MOVE_MOVE_STUDENT_FROM_ENTRANCE_TO_HALL -> printLastMoveMoveStudentFromEntranceToHall();
                case PLAYER_MOVE_MOVE_STUDENT_FROM_ENTRANCE_TO_ISLAND -> printLastMoveMoveStudentFromEntranceToIsland();
                case PLAYER_MOVE_MOVE_MOTHER_NATURE -> printLastMoveMoveMotherNature();
                case PLAYER_MOVE_PICK_STUDENTS_FROM_CLOUD -> printLastMovePickStudentsFromCloud();
                case PLAYER_MOVE_END_TURN -> printLastMoveEndTurn();
                case PLAYER_MOVE_PLAY_CHARACTER -> printLastMovePlayCharacter();
                case ILLEGAL_MOVE -> printLastMoveIllegalMove();
                default -> clientViewCli.println("L'ultima mossa eseguita da qualcuno non è valida");
            }
        }

        private void printLastMoveInitialization() {
            int numPlayers = getGameState().getPlayers().size();
            clientViewCli.println("""
                    Madre natura è stata posizionata su un isola casuale
                    10 studenti casuali sono stati piazzati sulle isole""");

            if (numPlayers == 2) {
                clientViewCli.println("7 studenti casuali sono stati dati a ogni giocatore");
                clientViewCli.println("8 torri sono state date a ogni giocatore");
            } else if (numPlayers == 3) {
                clientViewCli.println("9 studenti casuali sono stati dati a ogni giocatore");
                clientViewCli.println("6 torri sono state date a ogni giocatore");
            } else if (numPlayers == 4) {
                clientViewCli.println("7 studenti casuali sono stati dati a ogni giocatore");
                clientViewCli.println("8 torri sono state date a ogni caposquadra");
            }
        }

        private void printLastMovePlayAssistant() {
            String playerWhoMovedNickname = getLastMoveFromServer().getParameter(Parameters.NICKNAME);
            String assistantName = getLastMoveFromServer().getParameter(Parameters.ASSISTANT);
            Assistant assistant = Assistant.valueOf(assistantName);
            clientViewCli.println(playerWhoMovedNickname + " ha giocato l'assistente " + getAssistantDetails(assistant));
        }

        private void printLastMoveMoveStudentFromEntranceToHall() {
            String playerWhoMovedNickname = getLastMoveFromServer().getParameter(Parameters.NICKNAME);
            String studentColor = getLastMoveFromServer().getParameter(Parameters.STUDENT_COLOR);
            clientViewCli.println(playerWhoMovedNickname + " ha spostato uno studente " + studentColor + " dall'ingresso alla sala");
        }

        private void printLastMoveMoveStudentFromEntranceToIsland() {
            String playerWhoMovedNickname = getLastMoveFromServer().getParameter(Parameters.NICKNAME);
            String studentColor = getLastMoveFromServer().getParameter(Parameters.STUDENT_COLOR);
            int islandId = Integer.parseInt(getLastMoveFromServer().getParameter(Parameters.ISLAND_ID));
            int islandNumber = convertIslandIdToCurrentIslandNumber(islandId);
            clientViewCli.println(playerWhoMovedNickname + " ha spostato uno studente " + studentColor + " dall'ingresso all'isola " + islandNumber);
        }

        private void printLastMoveMoveMotherNature() {
            String playerWhoMovedNickname = getLastMoveFromServer().getParameter(Parameters.NICKNAME);
            String steps = getLastMoveFromServer().getParameter(Parameters.STEPS);
            clientViewCli.println(playerWhoMovedNickname + " ha mosso madre natura di " + steps);
        }

        private void printLastMovePickStudentsFromCloud() {
            String playerWhoMovedNickname = getLastMoveFromServer().getParameter(Parameters.NICKNAME);
            int cloudId = Integer.parseInt(getLastMoveFromServer().getParameter(Parameters.CLOUD_ID));
            clientViewCli.println(playerWhoMovedNickname + " ha preso gli studenti dalla nuvola " + (cloudId + 1));
        }

        private void printLastMoveEndTurn() {
            String playerWhoMovedNickname = getLastMoveFromServer().getParameter(Parameters.NICKNAME);
            clientViewCli.println(playerWhoMovedNickname + " ha terminato il turno");
        }

        private void printLastMovePlayCharacter() {
            String playerWhoMovedNickname = getLastMoveFromServer().getParameter(Parameters.NICKNAME);
            String character = getLastMoveFromServer().getParameter(Parameters.CHARACTER_TYPE);
            String student = getLastMoveFromServer().getParameter(Parameters.STUDENT_COLOR);
            String islandId = getLastMoveFromServer().getParameter(Parameters.ISLAND_ID);
            String toExchangeFromNumber = getLastMoveFromServer().getParameter(Parameters.TO_EXCHANGE_FROM_NUMBER);
            String toExchangeToNumber = getLastMoveFromServer().getParameter(Parameters.TO_EXCHANGE_TO_NUMBER);
            clientViewCli.println(playerWhoMovedNickname + " ha giocato il personaggio " + character);
            if (student != null) {
                clientViewCli.println("\tha scelto lo studente " + student);
            }
            if (islandId != null) {
                int islandNumber = convertIslandIdToCurrentIslandNumber(Integer.parseInt(islandId));
                clientViewCli.println("\tha scelto l'isola " + islandNumber);
            }
            if (toExchangeFromNumber != null) {
                clientViewCli.print("\tha scambiato");
                for (int i = 0; i < Integer.parseInt(toExchangeFromNumber); i++) {
                    clientViewCli.print(" " + getLastMoveFromServer().getParameter(Parameters.TO_EXCHANGE_FROM + i));
                }
            }
            if (toExchangeToNumber != null) {
                clientViewCli.print(" con");
                for (int i = 0; i < Integer.parseInt(toExchangeToNumber); i++) {
                    clientViewCli.print(" " + getLastMoveFromServer().getParameter(Parameters.TO_EXCHANGE_TO + i));
                }
                clientViewCli.println("");
            }
        }

        private void printLastMoveIllegalMove() {
            String playerWhoMovedNickname = getLastMoveFromServer().getParameter(Parameters.NICKNAME);
            clientViewCli.println(playerWhoMovedNickname + " ha eseguito una mossa non valida, dovrà ripeterla");
        }

        private void printGameOverAndWinner() {
            Tower winner = getGameState().getWinner();
            clientViewCli.println("Il gioco è terminato con un vincitore");
            clientViewCli.println("Il vincitore è il giocatore con la torre " + winner);
            clientViewCli.println("GAME OVER");
        }

        private void printJoinSuccessfulMessage() {
            printMessage("Ti sei unito a un match, attendi gli altri giocatori...");
        }

        // UTILS

        private void forceEndMatch() {
            printMessage("\n\nUn giocatore si è disconnesso, la partita è terminata");
            System.exit(0);
        }

        private void requestAndSendNickname(boolean failed) {
            requestNicknameToUser(failed);
            sendNicknameToServer();
        }

        private void requestAndSendMatchType() {
            printMessage("A che tipo di partita vuoi unirti?");

            Integer numPlayers = requestNumPlayerToUser();
            boolean expertMatch = requestIfExpertMatchToUser();

            sendMatchTypeToServer(numPlayers, expertMatch);
        }

        private void requestAndSendWizardAndTower(List<Wizard> availableWizards, List<Tower> availableTowers) {
            printMessage("La partita è iniziata!");

            Wizard wizard = requestWizardToUser(availableWizards);
            Tower tower = requestTowerToUser(availableTowers);

            sendWizardAndTowerToServer(getNickname(), wizard, tower);
            printMessage("Attendi che gli altri giocatori scelgano mago e torre...");
        }

        private Integer chooseIntegerFromList(String chooseBetween, List<?> list, boolean print) {
            Integer chosen;
            do {
                clientViewCli.print(chooseBetween);
                if (print) {
                    for (int i = 0; i < list.size(); i++) {
                        clientViewCli.print(" (" + (i + 1) + ") " + list.get(i));
                    }
                    clientViewCli.print(": ");
                }
                try {
                    chosen = clientViewCli.nextInt();
                } catch (InputMismatchException e) {
                    chosen = null;
                }
                clientViewCli.nextLine();
            } while (chosen == null || chosen < 1 || chosen > list.size());
            return chosen;
        }

        /**
         * @return true if the current player can play a character
         */
        private boolean canPlayCharacter() {
            GameState gameState = getGameState();
            if (!gameState.getExpertAttrs().isCharacterAlreadyPlayed() && Stage.isActionStage(gameState.getStage())) {
                int playerCoins = gameState.getCurrentPlayer().getNumCoins();
                for (Character character : gameState.getExpertAttrs().getCharacters()) {
                    if (character.getCost() <= playerCoins) {
                        return true;
                    }
                }
            }
            return false;
        }

        private String getCompleteMoveName(Command move) {
            return switch (move) {
                case PLAYER_MOVE_PLAY_ASSISTANT -> "Gioca una carta assistente";
                case PLAYER_MOVE_MOVE_STUDENT_FROM_ENTRANCE_TO_HALL -> "Sposta uno studente dall'entrata alla sala";
                case PLAYER_MOVE_MOVE_STUDENT_FROM_ENTRANCE_TO_ISLAND -> "Sposta uno studente dall'entrata su un isola";
                case PLAYER_MOVE_MOVE_MOTHER_NATURE -> "Muovi madre natura";
                case PLAYER_MOVE_PICK_STUDENTS_FROM_CLOUD -> "Prendi studenti da una nuvola";
                case PLAYER_MOVE_PLAY_CHARACTER -> "Gioca una carta personaggio";
                case PLAYER_MOVE_END_TURN -> "Termina il turno";
                case LOGOUT -> "Logout";
                default -> null;
            };
        }

        private String getAssistantDetails(Assistant assistant) {
            return assistant.name() +
                    ", Valore " + String.format("%2d", assistant.getValue()) +
                    ", Passi Madre Natura " + assistant.getMotherNatureSteps();
        }

        private List<Student> getStudentsOnEntrance() {
            List<Student> studentsOnEntrance = new ArrayList<>();
            for (Student student : Student.values()) {
                for (int i = 0; i < getGameState().getCurrentPlayer().getBoard().getEntrance().getNumStudentsByColor(student); i++) {
                    studentsOnEntrance.add(student);
                }
            }
            return studentsOnEntrance;
        }

        private List<Student> getStudentsOnHall() {
            List<Student> studentsOnHall = new ArrayList<>();
            for (Student student : Student.values()) {
                for (int i = 0; i < getGameState().getCurrentPlayer().getBoard().getHall().getNumStudentsByColor(student); i++) {
                    studentsOnHall.add(student);
                }
            }
            return studentsOnHall;
        }

        /**
         * Converts the number of the island chosen from the CLI to the real islandId.
         * CLI user can choose only between incremental numbers but ids can be not incremental
         * (for example CLI prints islands "1 2 3" while real ids are "1 4 6")
         *
         * @param chosenIsland the island chosen by the client
         * @return the islandId associated to the chosen island
         * @throws IllegalMoveException if requested island does not exist
         */
        private int convertChosenIslandToIslandId(int chosenIsland) {
            chosenIsland--;
            List<Island> islands = getGameState().getIslands();
            if (chosenIsland >= islands.size()) {
                throw new IllegalMoveException("Requested island does not exist");
            }
            return islands.get(chosenIsland).getPosition();
        }

        /**
         * Converts the islandId to the current island number for CLI.
         * CLI prints islands with a number that is not the real island ids
         * (for example CLI prints islands "1 2 3" while real ids are "1 4 6")
         *
         * @param islandId the islandId to be converted
         * @return the current number associated to the island in CLI
         * @throws IllegalMoveException if requested island does not exist
         */
        private int convertIslandIdToCurrentIslandNumber(int islandId) {
            List<Island> islands = getGameState().getIslands();
            for (int i = 0; i < islands.size(); i++) {
                if (islands.get(i).getPosition() == islandId) {
                    return i + 1;
                }
            }
            throw new IllegalMoveException("Requested island id does not exist");
        }
    }
}
