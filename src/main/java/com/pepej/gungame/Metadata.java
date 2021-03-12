package com.pepej.gungame;

import com.pepej.papi.metadata.MetadataKey;

import java.util.UUID;

public final class Metadata {

    private Metadata() {
        throw new UnsupportedOperationException("This class cannot be initialized");
    }

    public static final MetadataKey<UUID> LAST_ATTACKER_KEY = MetadataKey.createUuidKey("last-attacker");
}
