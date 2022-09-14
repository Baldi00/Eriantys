package it.polimi.ingsw.models.components;

import it.polimi.ingsw.models.components.hall.Hall;
import it.polimi.ingsw.models.components.interfaces.TowerReceiver;
import it.polimi.ingsw.models.exceptions.IllegalMoveException;

public class Board implements TowerReceiver {

    private final Tower towerType;
    private final int towerLimit;
    private final Entrance entrance;
    private final Hall hall;
    private int numTowers;

    public Board(Tower towerType, int towerLimit, int studentLimitOnEntrance) {
        numTowers = 0;
        this.towerType = towerType;
        this.towerLimit = towerLimit;
        entrance = new Entrance(studentLimitOnEntrance);
        hall = new Hall();
    }

    /**
     * Place a tower on the Board
     *
     * @param tower the type of the tower to receive
     * @throws IllegalMoveException if a tower with a different color is added or
     *                              if the board cannot receive any more towers
     */
    @Override
    public void receiveTower(Tower tower) {
        if (!tower.equals(towerType))
            throw new IllegalMoveException("Board can only receive " + towerType.toString() + " towers");
        if (numTowers == towerLimit)
            throw new IllegalMoveException("Cannot add any more towers to the board");
        numTowers++;
    }

    /**
     * Remove a tower from the Board
     *
     * @return true if a tower has been correctly removed, false otherwise
     */
    public boolean removeTower() {
        if (hasTowers()) {
            numTowers--;
            return true;
        }
        return false;
    }

    public boolean hasTowers() {
        return numTowers > 0;
    }

    public int getNumTowers() {
        return numTowers;
    }

    public Tower getTowerType() {
        return towerType;
    }

    public Entrance getEntrance() {
        return entrance;
    }

    public Hall getHall() {
        return hall;
    }

    @Override
    public String toString() {
        return "Board:\n" +
                "entrance=" + entrance +
                "\nhall=" + hall +
                "\ntowerType=" + towerType +
                ", numTowers=" + numTowers;
    }

}
