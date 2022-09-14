package it.polimi.ingsw.models;

import it.polimi.ingsw.models.components.*;
import it.polimi.ingsw.models.components.Board;

import java.util.List;

public class TestUtils {

    private TestUtils() {
        // avoid instantiation
    }

    /** Create a player with all the assistants */
    public static Player createPlayer(String name, Wizard wizard, Tower tower) {
        return createPlayer(name, wizard, tower, List.of(Assistant.values()));
    }

    /** Create a player with the given assistants, max tower limit and max entrance students limit  */
    public static Player createPlayer(String name, Wizard wizard, Tower tower, List<Assistant> assistants) {
        Board board = new Board(tower, 8, 7);
        return new Player(wizard, name, assistants, board);
    }

}
