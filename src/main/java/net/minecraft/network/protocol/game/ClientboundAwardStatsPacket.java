package net.minecraft.network.protocol.game;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.io.IOException;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundAwardStatsPacket implements Packet<ClientGamePacketListener> {
    private Object2IntMap<Stat<?>> stats;

    public ClientboundAwardStatsPacket() {
    }

    public ClientboundAwardStatsPacket(Object2IntMap<Stat<?>> param0) {
        this.stats = param0;
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleAwardStats(this);
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        int var0 = param0.readVarInt();
        this.stats = new Object2IntOpenHashMap<>(var0);

        for(int var1 = 0; var1 < var0; ++var1) {
            this.readStat(Registry.STAT_TYPE.byId(param0.readVarInt()), param0);
        }

    }

    private <T> void readStat(StatType<T> param0, FriendlyByteBuf param1) {
        int var0 = param1.readVarInt();
        int var1 = param1.readVarInt();
        this.stats.put(param0.get(param0.getRegistry().byId(var0)), var1);
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeVarInt(this.stats.size());

        for(Entry<Stat<?>> var0 : this.stats.object2IntEntrySet()) {
            Stat<?> var1 = var0.getKey();
            param0.writeVarInt(Registry.STAT_TYPE.getId(var1.getType()));
            param0.writeVarInt(this.getId(var1));
            param0.writeVarInt(var0.getIntValue());
        }

    }

    private <T> int getId(Stat<T> param0) {
        return param0.getType().getRegistry().getId(param0.getValue());
    }

    @OnlyIn(Dist.CLIENT)
    public Map<Stat<?>, Integer> getStats() {
        return this.stats;
    }
}
