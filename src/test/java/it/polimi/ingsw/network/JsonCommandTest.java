package it.polimi.ingsw.network;

import it.polimi.ingsw.network.messages.Command;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JsonCommandTest {

    @Test
    void shouldCreateCorrectJSON(){
        JsonCommand jsonCommand = new JsonCommand(Command.PLAYER_MOVE_ADD_PLAYER);
        jsonCommand
                .addParameter("playerId", "1", false)
                .addParameter("tower", "BLACK", true)
                .addParameter("wizard", "KING", true);

        String expected = "{\"command\": \"addPlayer\", \"playerId\": 1, \"tower\": \"BLACK\", \"wizard\": \"KING\"}";
        assertEquals(expected, jsonCommand.toString());
    }

    @Test
    void shouldUseDoubleQuotes(){
        JsonCommand jsonCommand = new JsonCommand(Command.PLAYER_MOVE_ADD_PLAYER);
        jsonCommand
                .addParameter("playerId", "1", false)
                .addParameter("tower", "BLACK", true)
                .addParameter("wizard", "KING", true);

        String expected = "{\"command\": \"addPlayer\", \"playerId\": 1, \"tower\": \"BLACK\", \"wizard\": \"KING\"}";
        assertEquals(expected, jsonCommand.toJson());
    }

    @Test
    void shouldUseSingleQuotes(){
        JsonCommand jsonCommand = new JsonCommand(Command.PLAYER_MOVE_ADD_PLAYER);
        jsonCommand
                .addParameter("playerId", "1", false)
                .addParameterSingleQuotes("tower", "BLACK")
                .addParameterSingleQuotes("wizard", "KING");

        String expected = "{\"command\": \"addPlayer\", \"playerId\": 1, \"tower\": 'BLACK', \"wizard\": 'KING'}";
        assertEquals(expected, jsonCommand.toJson());
    }

    @Test
    void shouldReturnTheCorrectCommand(){
        JsonCommand jsonCommand = new JsonCommand(Command.PLAYER_MOVE_ADD_PLAYER);
        jsonCommand
                .addParameter("playerId", "1", false)
                .addParameterSingleQuotes("tower", "BLACK")
                .addParameterSingleQuotes("wizard", "KING");

        assertEquals(Command.PLAYER_MOVE_ADD_PLAYER, jsonCommand.getCommand());
    }

    @Test
    void shouldReturnTheCorrectParameter(){
        JsonCommand jsonCommand = new JsonCommand(Command.PLAYER_MOVE_ADD_PLAYER);
        jsonCommand
                .addParameter("playerId", "1", false)
                .addParameterSingleQuotes("tower", "BLACK")
                .addParameterSingleQuotes("wizard", "KING");

        assertEquals("BLACK", jsonCommand.getParameter("tower"));
    }

    @Test
    void shouldCreateTheCorrectJsonCommand(){
        String json = "{\"command\": \"addPlayer\", \"playerId\": 1, \"tower\": 'BLACK', \"wizard\": 'KING', \"availableWizards\": [WITCH, KING, SAGE, DRUID]}";
        JsonCommand jsonCommand = JsonCommand.fromJson(json);

        assertEquals(Command.PLAYER_MOVE_ADD_PLAYER, jsonCommand.getCommand());
        assertEquals("1", jsonCommand.getParameter("playerId"));
        assertEquals("BLACK", jsonCommand.getParameter("tower"));
        assertEquals("KING", jsonCommand.getParameter("wizard"));
        assertEquals("[WITCH, KING, SAGE, DRUID]", jsonCommand.getParameter("availableWizards"));
    }

    @Test
    void shouldThrowExceptionBecauseCommandParameterIsMissing(){
        String json = "{\"playerId\": 1, \"tower\": 'BLACK', \"wizard\": 'KING', \"availableWizards\": [WITCH, KING, SAGE, DRUID]}";
        assertThrows(IllegalArgumentException.class, () ->
                JsonCommand.fromJson(json)
        );
    }
}
