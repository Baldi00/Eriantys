package it.polimi.ingsw.models.components;

/**
 * Collections of all the assistants of the game
 */
public enum Assistant {
    TURTLE(1, 1),
    ELEPHANT(2, 1),
    DOG(3, 2),
    OCTOPUS(4, 2),
    SNAKE(5, 3),
    FOX(6, 3),
    EAGLE(7, 4),
    CAT(8, 4),
    OSTRICH(9, 5),
    LEOPARD(10, 5);

    private final int value;
    private final int motherNatureSteps;

    Assistant(int value, int motherNatureSteps) {
        this.value = value;
        this.motherNatureSteps = motherNatureSteps;
    }

    public int getValue() {
        return value;
    }

    public int getMotherNatureSteps() {
        return motherNatureSteps;
    }

    @Override
    public String toString() {
        return "Assistant{" +
                "name=" + this.name() +
                ", value=" + value +
                ", mother nature steps=" + motherNatureSteps +
                '}';
    }
}
