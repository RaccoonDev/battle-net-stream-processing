package com.devraccoon.models;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface ServerEvent {
    Instant getEventTime();
    Optional<UUID> maybeGetPlayerId();
}

