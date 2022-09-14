package it.polimi.ingsw.clients.gui.view.components.frames.game_frame;

import it.polimi.ingsw.models.components.Assistant;

public interface GameFrameListener {
    void receiveAssistant(Assistant assistant);
    void leaveMatch();
    void endTurn();
}
