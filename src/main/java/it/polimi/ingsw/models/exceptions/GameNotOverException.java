package it.polimi.ingsw.models.exceptions;

public class GameNotOverException extends RuntimeException {
    public GameNotOverException(String message) {
        super(message);
    }
}
