package it.polimi.ingsw.models.operations.influence;

import it.polimi.ingsw.models.components.Player;
import it.polimi.ingsw.models.components.Island;
import it.polimi.ingsw.models.components.Student;

import java.util.List;

public class IgnoreTowerInfluence implements InfluenceCalculator {

    @Override
    public int calculateInfluence(Island island, Player player, List<Student> prof) {
        int influence = 0;

        for (Student color : prof) {
            influence += island.getNumStudent(color);
        }

        return influence;
    }

}
