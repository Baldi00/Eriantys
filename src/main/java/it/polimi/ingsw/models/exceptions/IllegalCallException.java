package it.polimi.ingsw.models.exceptions;

import it.polimi.ingsw.models.state.Stage;

/**
 * This exception should be thrown every time a method is called
 * during the wrong stage.
 */
public class IllegalCallException extends RuntimeException {
    public IllegalCallException(Stage stage) {
        super("Cannot call this method during " + stage + " stage.");
    }
}
