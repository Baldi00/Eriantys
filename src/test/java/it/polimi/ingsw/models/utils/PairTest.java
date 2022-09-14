package it.polimi.ingsw.models.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PairTest {

    @Test
    void shouldReturnTheValuesSet() {
        String string = "ciao";
        int number = 289;
        Pair<String, Integer> pair = new Pair<>(string, number);
        assertEquals(string, pair.first());
        assertEquals(number, pair.second());
    }

}