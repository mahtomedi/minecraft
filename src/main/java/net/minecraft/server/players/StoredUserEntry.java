package net.minecraft.server.players;

import com.google.gson.JsonObject;
import javax.annotation.Nullable;

public class StoredUserEntry<T> {
    @Nullable
    private final T user;

    public StoredUserEntry(T param0) {
        this.user = param0;
    }

    protected StoredUserEntry(@Nullable T param0, JsonObject param1) {
        this.user = param0;
    }

    @Nullable
    T getUser() {
        return this.user;
    }

    boolean hasExpired() {
        return false;
    }

    protected void serialize(JsonObject param0) {
    }
}
