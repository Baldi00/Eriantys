package it.polimi.ingsw.network.messages;

public enum Command {
    INITIALIZATION("initialization"),
    LOGIN("login"),
    LOGIN_SUCCESSFUL("loginSuccessful"),
    JOIN_MATCH("joinMatch"),
    JOIN_SUCCESSFUL("joinSuccessful"),
    CHOOSE_WIZARD_TOWER("chooseWizardTower"),
    ENTER_NICKNAME("enterNickname"),
    NICKNAME_ALREADY_PRESENT("nicknameAlreadyPresent"),
    MOVE_DONE("moveDone"),
    FORCE_END_MATCH("forceEndMatch"),
    PLAYER_MOVE_ADD_PLAYER("addPlayer"),
    PLAYER_MOVE_PLAY_CHARACTER("playerMovePlayCharacter"),
    PLAYER_MOVE_PICK_STUDENTS_FROM_CLOUD("playerMovePickStudentsFromCloud"),
    PLAYER_MOVE_MOVE_MOTHER_NATURE("playerMoveMoveMotherNature"),
    PLAYER_MOVE_MOVE_STUDENT_FROM_ENTRANCE_TO_ISLAND("playerMoveMoveStudentFromEntranceToIsland"),
    PLAYER_MOVE_MOVE_STUDENT_FROM_ENTRANCE_TO_HALL("playerMoveMoveStudentFromEntranceToHall"),
    PLAYER_MOVE_PLAY_ASSISTANT("playerMovePlayAssistant"),
    PLAYER_MOVE_END_TURN("playerMoveEndTurn"),
    BEAT("beat"),
    LOGOUT("logout"),
    ILLEGAL_MOVE("illegalMove");

    private final String commandString;

    Command(String commandString) {
        this.commandString = commandString;
    }

    public String getCommandString() {
        return commandString;
    }

    public static Command fromCommandString(String string) {
        for (Command command : Command.values())
            if (command.getCommandString().equals(string))
                return command;
        throw new IllegalArgumentException("invalid command string: " + string);
    }
}
