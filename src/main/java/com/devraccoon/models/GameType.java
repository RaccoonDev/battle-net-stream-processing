package com.devraccoon.models;

import java.util.Arrays;
import java.util.Optional;

public enum GameType {
    ONE_VS_ONE(2),
    TWO_VS_TWO(4),
    THREE_VS_THREE(6),
    FOUR_VS_FOUR(8);

    private final int totalNumberOfPlayers;

    GameType(int totalNumberOfPlayers) {
        this.totalNumberOfPlayers = totalNumberOfPlayers;
    }

    public int getTotalNumberOfPlayers() {
        return totalNumberOfPlayers;
    }

    public static Optional<GameType> valueOf(int value) {
        return Arrays.stream(values()).filter(v -> v.totalNumberOfPlayers == value).findFirst();
    }
}
