package net.minecraft.server.level;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.stream.Stream;

public final class PlayerMap {
    private final Object2BooleanMap<ServerPlayer> players = new Object2BooleanOpenHashMap<>();

    public Stream<ServerPlayer> getPlayers(long param0) {
        return this.players.keySet().stream();
    }

    public void addPlayer(long param0, ServerPlayer param1, boolean param2) {
        this.players.put(param1, param2);
    }

    public void removePlayer(long param0, ServerPlayer param1) {
        this.players.removeBoolean(param1);
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

    public void updatePlayer(long param0, long param1, ServerPlayer param2) {
    }
}
