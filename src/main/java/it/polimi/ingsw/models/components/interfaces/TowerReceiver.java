package it.polimi.ingsw.models.components.interfaces;

import it.polimi.ingsw.models.components.Tower;

public interface TowerReceiver {

    /**
     * Classes which implements this interface are able to receive towers
     *
     * @param tower the tower to receive
     * @throws RuntimeException if the tower cannot be received
     */
    void receiveTower(Tower tower);

}
