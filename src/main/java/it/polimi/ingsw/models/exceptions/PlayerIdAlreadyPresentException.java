package it.polimi.ingsw.models.exceptions;

public class PlayerIdAlreadyPresentException extends RuntimeException {
    public PlayerIdAlreadyPresentException(String message) {
        super(message);
    }
}
