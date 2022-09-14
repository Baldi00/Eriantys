package it.polimi.ingsw.network;

import com.google.gson.Gson;
import it.polimi.ingsw.network.messages.Command;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Custom JSON structure
 * It creates an object containing a command
 * It's possible to add other parameters
 * With toJson converts the structure to a JSON string
 */
public class JsonCommand {

    private static final String COMMAND = "command";

    private final Map<String, String> entries;
    private final List<Boolean> useQuotes;
    private final List<Boolean> useSingleQuotes;

    private JsonCommand() {
        entries = new LinkedHashMap<>();
        useQuotes = new ArrayList<>();
        useSingleQuotes = new ArrayList<>();
    }

    /**
     * Creates a JsonCommand with the given command
     */
    public JsonCommand(Command command) {
        this();
        entries.put(COMMAND, command.getCommandString());
        useQuotes.add(true);
        useSingleQuotes.add(false);
    }

    /**
     * @param json a JSON string compatible with JsonCommand structure (should have the command parameter)
     * @return a JsonCommand structure given a JSON string
     * @throws IllegalArgumentException if given string doesn't contain the command parameter
     */
    public static JsonCommand fromJson(String json) {
        JsonCommand jsonCommand = new JsonCommand();

        Gson gson = GsonManager.getInstance();
        Map<?, ?> map = gson.fromJson(json, Map.class);

        if (!map.containsKey(COMMAND)) {
            throw new IllegalArgumentException("Given string doesn't contain command parameter");
        }

        for (Map.Entry<?, ?> entry : map.entrySet()) {
            try {
                jsonCommand.entries.put((String) entry.getKey(), (String) entry.getValue());
            } catch (ClassCastException e) {
                try {
                    jsonCommand.entries.put((String) entry.getKey(), "" + Integer.parseInt(("" + entry.getValue()).replace(".0", "")));
                } catch (NumberFormatException e2) {
                    jsonCommand.entries.put((String) entry.getKey(), entry.getValue().toString());
                }
            }
            jsonCommand.useQuotes.add(true);
            jsonCommand.useSingleQuotes.add(false);
        }

        return jsonCommand;
    }

    /**
     * Adds a parameter to the JSON structure
     * @param useQuotes true if the parameter value should have quotes when converted to JSON
     */
    public JsonCommand addParameter(String parameterName, String parameterValue, boolean useQuotes) {
        return addParameter(parameterName, parameterValue, useQuotes, false);
    }

    /**
     * Adds a parameter to the JSON structure
     * The parameter value will be printed with single quotes instead of double ones
     */
    public JsonCommand addParameterSingleQuotes(String parameterName, String parameterValue) {
        return addParameter(parameterName, parameterValue, true, true);
    }

    /**
     * Adds a parameter to the JSON structure
     * The parameter value will be printed with single, double or no quotes depending on given arguments
     */
    private JsonCommand addParameter(String parameterName, String parameterValue, boolean useQuotes, boolean useSingleQuotes){
        entries.put(parameterName, parameterValue);
        this.useQuotes.add(useQuotes);
        this.useSingleQuotes.add(useSingleQuotes);
        return this;
    }

    /**
     * @return the JSON representation of the object (e.g. {"command": "enterNickname", "parameter1": a, "parameter2": 'b'})
     */
    public String toJson() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{");

        int i = 0;
        for (Map.Entry<String, String> param : entries.entrySet()) {
            stringBuilder.append("\"").append(param.getKey()).append("\": ");

            boolean useQuotesForTheCurrentParameter = useQuotes.get(i);
            if (useQuotesForTheCurrentParameter) {
                boolean useSingleQuotesForTheCurrentParameter = useSingleQuotes.get(i);
                if (useSingleQuotesForTheCurrentParameter) {
                    stringBuilder.append("'").append(param.getValue()).append("'");
                } else {
                    stringBuilder.append("\"").append(param.getValue()).append("\"");
                }
            } else {
                stringBuilder.append(param.getValue());
            }

            if (i < useQuotes.size() - 1) {
                stringBuilder.append(", ");
            }
            i++;
        }

        stringBuilder.append("}");
        return stringBuilder.toString();
    }

    /**
     * @return same as toJson()
     */
    @Override
    public String toString() {
        return toJson();
    }

    public Command getCommand() {
        return Command.fromCommandString(entries.get(COMMAND));
    }

    public String getParameter(String parameterName) {
        return entries.get(parameterName);
    }
}
