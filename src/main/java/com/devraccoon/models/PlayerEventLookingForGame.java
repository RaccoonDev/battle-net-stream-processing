package com.devraccoon.models;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class PlayerEventLookingForGame implements PlayerEvent {
    private final Instant eventTime;
    private final UUID playerId;
    private final GameType gameType;

    public PlayerEventLookingForGame(Instant eventTime, UUID playerId, GameType gameType) {
        this.eventTime = eventTime;
        this.playerId = playerId;
        this.gameType = gameType;
    }

    @Override
    public Instant getEventTime() {
        return eventTime;
    }

    @Override
    public UUID getPlayerId() {
        return playerId;
    }

    public GameType getGameType() {
        return gameType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerEventLookingForGame that = (PlayerEventLookingForGame) o;
        return Objects.equals(eventTime, that.eventTime) && Objects.equals(playerId, that.playerId) && gameType == that.gameType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventTime, playerId, gameType);
    }

    @Override
    public String toString() {
        return "PlayerLookingForGame{" +
                "eventTime=" + eventTime +
                ", playerId=" + playerId +
                ", gameType=" + gameType +
                '}';
    }
}
