package com.devraccoon.models;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class PlayerOffline implements ServerEvent {
    private final Instant eventTime;
    private final UUID playerId;
    private final String nickname;

    public PlayerOffline(Instant eventTime, UUID playerId, String nickname) {
        this.eventTime = eventTime;
        this.playerId = playerId;
        this.nickname = nickname;
    }

    @Override
    public Instant getEventTime() {
        return eventTime;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public String getNickname() {
        return nickname;
    }

    @Override
    public Optional<UUID> maybeGetPlayerId() {
        return Optional.of(getPlayerId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerOffline that = (PlayerOffline) o;
        return Objects.equals(eventTime, that.eventTime) && Objects.equals(playerId, that.playerId) && Objects.equals(nickname, that.nickname);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventTime, playerId, nickname);
    }

    @Override
    public String toString() {
        return "PlayerOffline{" +
                "eventTime=" + eventTime +
                ", playerId=" + playerId +
                ", nickname='" + nickname + '\'' +
                '}';
    }
}
