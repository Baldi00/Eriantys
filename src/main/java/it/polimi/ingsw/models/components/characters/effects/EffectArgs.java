package it.polimi.ingsw.models.components.characters.effects;

import it.polimi.ingsw.models.components.characters.Character;
import it.polimi.ingsw.models.state.GameState;
import it.polimi.ingsw.models.components.Island;
import it.polimi.ingsw.models.components.Student;

import java.util.List;

public class EffectArgs {
    private final GameState gameState;
    private final Island island;
    private final Student student;
    private final Character character;
    private final List<Student> sourceStudents;
    private final List<Student> destStudents;

    private EffectArgs(Builder builder) {
        this.gameState = builder.gameState;
        this.island = builder.island;
        this.student = builder.student;
        this.character = builder.character;
        this.sourceStudents = builder.sourceStudents;
        this.destStudents = builder.destStudents;
    }

    public GameState getGameState() {
        return gameState;
    }

    public Island getIsland() {
        return island;
    }

    public Student getStudent() {
        return student;
    }

    public Character getCharacter() {
        return character;
    }

    public List<Student> getSourceStudents() {
        return sourceStudents;
    }

    public List<Student> getDestStudents() {
        return destStudents;
    }

    public static class Builder {
        private GameState gameState;
        private Island island;
        private Student student;
        private Character character;
        private List<Student> sourceStudents;
        private List<Student> destStudents;

        public Builder setGameState(GameState gameState) {
            this.gameState = gameState;
            return this;
        }

        public Builder setIsland(Island island) {
            this.island = island;
            return this;
        }

        public Builder setStudent(Student student) {
            this.student = student;
            return this;
        }

        public Builder setCharacter(Character character) {
            this.character = character;
            return this;
        }

        public Builder setSourceStudents(List<Student> sourceStudents) {
            this.sourceStudents = sourceStudents;
            return this;
        }

        public Builder setDestStudents(List<Student> destStudents) {
            this.destStudents = destStudents;
            return this;
        }

        public EffectArgs build() {
            return new EffectArgs(this);
        }
    }
}
