package it.polimi.ingsw.models.state;

public enum Stage {
    WAIT_FOR_PLAYERS,
    PREPARATION,
    PLANNING_FILL_CLOUDS,
    PLANNING_PLAY_ASSISTANTS,
    ACTION_MOVE_STUDENTS,
    ACTION_MOVE_MOTHER_NATURE,
    ACTION_TAKE_STUDENTS_FROM_CLOUD,
    /**
     * This is needed because otherwise player cannot play a character
     * after taking the students from the cloud.
     */
    ACTION_END_TURN,
    /**
     * This is needed because otherwise it's not possible to check the winner.
     */
    ROUND_END,
    GAME_OVER;

    public static boolean isActionStage(Stage stage) {
        return stage == Stage.ACTION_MOVE_STUDENTS
                || stage == Stage.ACTION_MOVE_MOTHER_NATURE
                || stage == Stage.ACTION_TAKE_STUDENTS_FROM_CLOUD
                || stage == Stage.ACTION_END_TURN;
    }
}
