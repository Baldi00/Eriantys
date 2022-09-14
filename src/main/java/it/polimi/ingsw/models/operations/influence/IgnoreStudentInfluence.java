package it.polimi.ingsw.models.operations.influence;

import it.polimi.ingsw.models.components.Player;
import it.polimi.ingsw.models.components.Island;
import it.polimi.ingsw.models.components.Student;

import java.util.List;

public class IgnoreStudentInfluence implements InfluenceCalculator {

    private final Student ignoredColor;

    public IgnoreStudentInfluence(Student ignoredColor) {
        this.ignoredColor = ignoredColor;
    }

    @Override
    public int calculateInfluence(Island island, Player player, List<Student> prof) {
        int influence = 0;

        // in 4 player matches only the leader takes influence points given by the towers
        if (player.isLeader()
                && island.hasTowers()
                && island.getTowerType() == player.getBoard().getTowerType()) {
            influence += island.getNumTowers();
        }

        for (Student color : prof) {
            if (color != ignoredColor)
                influence += island.getNumStudent(color);
        }

        return influence;
    }

}
