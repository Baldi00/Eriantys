package it.polimi.ingsw.models.components.characters.effects;

/**
 * Used to add required parameters to effects.
 */
public class RequiredEffectArgs {
    private final boolean island;
    private final boolean student;
    private final boolean character;
    private final boolean toExchangeFrom;
    private final boolean toExchangeTo;

    public static RequiredEffectArgs noOptionalArgsRequired() {
        return new Builder().build();
    }

    private RequiredEffectArgs(Builder builder) {
        island = builder.island;
        student = builder.student;
        character = builder.character;
        toExchangeFrom = builder.toExchangeFrom;
        toExchangeTo = builder.toExchangeTo;
    }

    public boolean isIslandRequired() {
        return island;
    }

    public boolean isStudentRequired() {
        return student;
    }

    public boolean isCharacterRequired() {
        return character;
    }

    public boolean isToExchangeFromRequired() {
        return toExchangeFrom;
    }

    public boolean isToExchangeToRequired() {
        return toExchangeTo;
    }

    public static class Builder {
        private boolean island;
        private boolean student;
        private boolean character;
        private boolean toExchangeFrom;
        private boolean toExchangeTo;

        public Builder requireIsland() {
            island = true;
            return this;
        }

        public Builder requireStudent() {
            student = true;
            return this;
        }

        public Builder requireCharacter() {
            character = true;
            return this;
        }

        public Builder requireToExchangeFrom() {
            toExchangeFrom = true;
            return this;
        }

        public Builder requireToExchangeTo() {
            toExchangeTo = true;
            return this;
        }

        public RequiredEffectArgs build() {
            return new RequiredEffectArgs(this);
        }
    }
}
