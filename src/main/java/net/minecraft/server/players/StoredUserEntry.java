package net.minecraft.server.players;

import javax.annotation.Nullable;

public abstract class StoredUserEntry<T> {
    @Nullable
    private final T user;

    public StoredUserEntry(@Nullable T param0) {
        this.user = param0;
    }

    @Nullable
    T getUser() {
        return this.user;
    }

    boolean hasExpired() {
        return false;
    }
}
