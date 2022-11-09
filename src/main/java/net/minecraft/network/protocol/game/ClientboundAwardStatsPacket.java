package net.minecraft.network.protocol.game;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;

public class ClientboundAwardStatsPacket implements Packet<ClientGamePacketListener> {
    private final Object2IntMap<Stat<?>> stats;

    public ClientboundAwardStatsPacket(Object2IntMap<Stat<?>> param0) {
        this.stats = param0;
    }

    public ClientboundAwardStatsPacket(FriendlyByteBuf param0) {
        this.stats = param0.readMap(Object2IntOpenHashMap::new, param1 -> {
            StatType<?> var0 = param1.readById(BuiltInRegistries.STAT_TYPE);
            return readStatCap(param0, var0);
        }, FriendlyByteBuf::readVarInt);
    }

    private static <T> Stat<T> readStatCap(FriendlyByteBuf param0, StatType<T> param1) {
        return param1.get(param0.readById(param1.getRegistry()));
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleAwardStats(this);
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeMap(this.stats, ClientboundAwardStatsPacket::writeStatCap, FriendlyByteBuf::writeVarInt);
    }

    private static <T> void writeStatCap(FriendlyByteBuf param0x, Stat<T> param1) {
        param0x.writeId(BuiltInRegistries.STAT_TYPE, param1.getType());
        param0x.writeId(param1.getType().getRegistry(), param1.getValue());
    }

    public Map<Stat<?>, Integer> getStats() {
        return this.stats;
    }
}
