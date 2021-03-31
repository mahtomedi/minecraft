package net.minecraft.network.protocol.game;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Map;
import net.minecraft.core.Registry;
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
        this.stats = param0.readMap(Object2IntOpenHashMap::new, param0x -> {
            int var0 = param0x.readVarInt();
            int var1x = param0x.readVarInt();
            return readStatCap(Registry.STAT_TYPE.byId(var0), var1x);
        }, FriendlyByteBuf::readVarInt);
    }

    private static <T> Stat<T> readStatCap(StatType<T> param0, int param1) {
        return param0.get(param0.getRegistry().byId(param1));
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleAwardStats(this);
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeMap(this.stats, (param0x, param1) -> {
            param0x.writeVarInt(Registry.STAT_TYPE.getId(param1.getType()));
            param0x.writeVarInt(this.getStatIdCap(param1));
        }, FriendlyByteBuf::writeVarInt);
    }

    private <T> int getStatIdCap(Stat<T> param0) {
        return param0.getType().getRegistry().getId(param0.getValue());
    }

    public Map<Stat<?>, Integer> getStats() {
        return this.stats;
    }
}
