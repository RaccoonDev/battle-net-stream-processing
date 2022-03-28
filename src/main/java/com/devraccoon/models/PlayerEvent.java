package com.devraccoon.models;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class PlayerEvent {
    private final Instant eventTime;
    private final UUID playerId;
    private final PlayerEventType eventType;

    public PlayerEvent(Instant eventTime, UUID playerId, PlayerEventType eventType) {
        this.eventTime = eventTime;
        this.playerId = playerId;
        this.eventType = eventType;
    }

    public Instant getEventTime() {
        return eventTime;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public PlayerEventType getEventType() {
        return eventType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerEvent that = (PlayerEvent) o;
        return Objects.equals(eventTime, that.eventTime) && Objects.equals(playerId, that.playerId) && eventType == that.eventType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventTime, playerId, eventType);
    }

    @Override
    public String toString() {
        return "PlayerEvent{" +
                "eventTime=" + eventTime +
                ", playerId=" + playerId +
                ", eventType=" + eventType +
                '}';
    }
}
