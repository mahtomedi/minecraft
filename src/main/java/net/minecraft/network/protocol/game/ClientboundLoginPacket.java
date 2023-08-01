package net.minecraft.network.protocol.game;

import com.google.common.collect.Sets;
import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record ClientboundLoginPacket(
    int playerId,
    boolean hardcore,
    Set<ResourceKey<Level>> levels,
    int maxPlayers,
    int chunkRadius,
    int simulationDistance,
    boolean reducedDebugInfo,
    boolean showDeathScreen,
    CommonPlayerSpawnInfo commonPlayerSpawnInfo
) implements Packet<ClientGamePacketListener> {
    public ClientboundLoginPacket(FriendlyByteBuf param0) {
        this(
            param0.readInt(),
            param0.readBoolean(),
            param0.readCollection(Sets::newHashSetWithExpectedSize, param0x -> param0x.readResourceKey(Registries.DIMENSION)),
            param0.readVarInt(),
            param0.readVarInt(),
            param0.readVarInt(),
            param0.readBoolean(),
            param0.readBoolean(),
            new CommonPlayerSpawnInfo(param0)
        );
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeInt(this.playerId);
        param0.writeBoolean(this.hardcore);
        param0.writeCollection(this.levels, FriendlyByteBuf::writeResourceKey);
        param0.writeVarInt(this.maxPlayers);
        param0.writeVarInt(this.chunkRadius);
        param0.writeVarInt(this.simulationDistance);
        param0.writeBoolean(this.reducedDebugInfo);
        param0.writeBoolean(this.showDeathScreen);
        this.commonPlayerSpawnInfo.write(param0);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleLogin(this);
    }
}
