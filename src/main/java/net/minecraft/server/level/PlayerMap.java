package net.minecraft.server.level;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.Set;

public final class PlayerMap {
    private final Object2BooleanMap<ServerPlayer> players = new Object2BooleanOpenHashMap<>();

    public Set<ServerPlayer> getAllPlayers() {
        return this.players.keySet();
    }

    public void addPlayer(ServerPlayer param0, boolean param1) {
        this.players.put(param0, param1);
    }

    public void removePlayer(ServerPlayer param0) {
        this.players.removeBoolean(param0);
    }

    public void ignorePlayer(ServerPlayer param0) {
        this.players.replace(param0, true);
    }

    public void unIgnorePlayer(ServerPlayer param0) {
        this.players.replace(param0, false);
    }

    public boolean ignoredOrUnknown(ServerPlayer param0) {
        return this.players.getOrDefault(param0, true);
    }

    public boolean ignored(ServerPlayer param0) {
        return this.players.getBoolean(param0);
    }
}
