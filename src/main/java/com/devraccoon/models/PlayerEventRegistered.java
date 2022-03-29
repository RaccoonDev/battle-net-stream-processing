package com.devraccoon.models;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class PlayerEventRegistered implements PlayerEvent {
    private final Instant eventTime;
    private final UUID playerId;

    public PlayerEventRegistered(Instant eventTime, UUID playerId) {
        this.eventTime = eventTime;
        this.playerId = playerId;
    }

    public Instant getEventTime() {
        return eventTime;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerEventRegistered that = (PlayerEventRegistered) o;
        return Objects.equals(eventTime, that.eventTime) && Objects.equals(playerId, that.playerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventTime, playerId);
    }

    @Override
    public String toString() {
        return "PlayerEventRegistered{" +
                "eventTime=" + eventTime +
                ", playerId=" + playerId +
                '}';
    }
}

