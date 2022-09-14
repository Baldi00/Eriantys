package it.polimi.ingsw.clients.gui.view.components.frames.setup_frame;

import it.polimi.ingsw.models.components.Tower;
import it.polimi.ingsw.models.components.Wizard;

public interface SetupFrameListener {
    void receiveIpAndPort(String ip, int port);
    void receiveNickname(String nickname);
    void receiveMatchMode(int numPlayers, boolean expertMode);
    void receiveWizardAndTower(Wizard wizard, Tower tower);
}
