package it.polimi.ingsw.models.constants;

public enum MatchType {
    TWO_PLAYERS(2),
    THREE_PLAYERS(3),
    FOUR_PLAYERS(4);

    final int numPlayers;

    MatchType(int numPlayers) {
        this.numPlayers = numPlayers;
    }

    public int getNumPlayers() {
        return numPlayers;
    }

    /**
     * @param numPlayers the number of players of the match.
     * @return a MatchType object corresponding to the number of players.
     * @throws IllegalArgumentException if the number of players is not in [2, 4] range.
     */
    public static MatchType fromNumPlayers(int numPlayers) {
        return switch (numPlayers) {
            case 2 -> MatchType.TWO_PLAYERS;
            case 3 -> MatchType.THREE_PLAYERS;
            case 4 -> MatchType.FOUR_PLAYERS;
            default -> throw new IllegalArgumentException(
                    "Cannot create match type with " + numPlayers + " players"
            );
        };
    }
}
