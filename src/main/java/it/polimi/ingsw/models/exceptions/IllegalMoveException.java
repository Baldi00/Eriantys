package it.polimi.ingsw.models.exceptions;

/**
 * This exception should be thrown every time a method is
 * called during the right stage but the move is illegal
 * during that stage.
 */
public class IllegalMoveException extends RuntimeException {
    public IllegalMoveException(String message) {
        super(message);
    }
}
