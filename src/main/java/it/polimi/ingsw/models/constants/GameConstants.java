package it.polimi.ingsw.models.constants;

public class GameConstants {

    public static final int NUMBER_OF_ISLANDS = 12;
    public static final int MAX_STUDENTS_ON_HALL_PER_COLOR = 10;
    public static final int MAX_STUDENTS_IN_BAG = 130;
    public static final int MIN_MOTHER_NATURE_STEPS = 1;
    public static final int NUM_COINS = 20;
    public static final int NUM_CHARACTERS = 3;

    private static final int[] maxStudentsOnEntrance = new int[] {7, 9, 7};
    private static final int[] maxTowersOnBoard = new int[] {8, 6, 8};
    private static final int[] numClouds = new int[] {2, 3, 4};
    private static final int[] numStudentsOnCloud = new int[] {3, 4, 3};
    private static final int[] numStudentsToMoveOutFromEntrance = new int[] {3, 4, 3};

    private final int arrayIndex;

    public GameConstants(MatchType matchType) {
        arrayIndex = switch (matchType) {
            case TWO_PLAYERS -> 0;
            case THREE_PLAYERS -> 1;
            case FOUR_PLAYERS -> 2;
        };
    }

    /**
     * @param numPlayers the number of players of the match.
     * @return a GameConstant object with the appropriate constants for the type of match.
     * @throws IllegalArgumentException if the number of players is illegal.
     */
    public static GameConstants fromNumPlayers(int numPlayers) {
        return new GameConstants(MatchType.fromNumPlayers(numPlayers));
    }

    public int getMaxStudentsOnEntrance() {
        return maxStudentsOnEntrance[arrayIndex];
    }

    public int getMaxTowersOnBoard() {
        return maxTowersOnBoard[arrayIndex];
    }

    public int getNumClouds() {
        return numClouds[arrayIndex];
    }

    public int getNumStudentsOnCloud() {
        return numStudentsOnCloud[arrayIndex];
    }

    public int getNumStudentsToMoveOutFromEntrance() {
        return numStudentsToMoveOutFromEntrance[arrayIndex];
    }

}
