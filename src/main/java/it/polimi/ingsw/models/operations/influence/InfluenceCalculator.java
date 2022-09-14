package it.polimi.ingsw.models.operations.influence;

import it.polimi.ingsw.models.components.Player;
import it.polimi.ingsw.models.components.Island;
import it.polimi.ingsw.models.components.Student;

import java.util.List;

public interface InfluenceCalculator {

    /**
     * Return the player's influence on the island.
     *
     * @param island the island where the influence is calculated.
     * @param player the player for which the influence is calculated.
     * @param prof   the professors who have th player.
     * @return the player's influence on the island.
     */
    int calculateInfluence(Island island, Player player, List<Student> prof);

    /**
     * Return the player's influence on the island.
     *
     * @param island           the island where the influence is calculated.
     * @param player           the player for which the influence is calculated.
     * @param prof             the professors who have th player.
     * @param additionalPoints additional points to add to the player.
     * @return the player's influence on the island.
     */
    default int calculateInfluence(Island island, Player player, List<Student> prof, int additionalPoints) {
        return calculateInfluence(island, player, prof) + additionalPoints;
    }

}
