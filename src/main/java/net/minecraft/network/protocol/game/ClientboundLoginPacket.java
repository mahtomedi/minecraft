package net.minecraft.network.protocol.game;

import com.google.common.collect.Sets;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

public record ClientboundLoginPacket(
    int playerId,
    boolean hardcore,
    GameType gameType,
    @Nullable GameType previousGameType,
    Set<ResourceKey<Level>> levels,
    RegistryAccess.RegistryHolder registryHolder,
    DimensionType dimensionType,
    ResourceKey<Level> dimension,
    long seed,
    int maxPlayers,
    int chunkRadius,
    int simulationDistance,
    boolean reducedDebugInfo,
    boolean showDeathScreen,
    boolean isDebug,
    boolean isFlat
) implements Packet<ClientGamePacketListener> {
    public ClientboundLoginPacket(FriendlyByteBuf param0) {
        this(
            param0.readInt(),
            param0.readBoolean(),
            GameType.byId(param0.readByte()),
            GameType.byNullableId(param0.readByte()),
            param0.readCollection(Sets::newHashSetWithExpectedSize, param0x -> ResourceKey.create(Registry.DIMENSION_REGISTRY, param0x.readResourceLocation())),
            param0.readWithCodec(RegistryAccess.RegistryHolder.NETWORK_CODEC),
            param0.readWithCodec(DimensionType.CODEC).get(),
            ResourceKey.create(Registry.DIMENSION_REGISTRY, param0.readResourceLocation()),
            param0.readLong(),
            param0.readVarInt(),
            param0.readVarInt(),
            param0.readVarInt(),
            param0.readBoolean(),
            param0.readBoolean(),
            param0.readBoolean(),
            param0.readBoolean()
        );
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeInt(this.playerId);
        param0.writeBoolean(this.hardcore);
        param0.writeByte(this.gameType.getId());
        param0.writeByte(GameType.getNullableId(this.previousGameType));
        param0.writeCollection(this.levels, (param0x, param1) -> param0x.writeResourceLocation(param1.location()));
        param0.writeWithCodec(RegistryAccess.RegistryHolder.NETWORK_CODEC, this.registryHolder);
        param0.writeWithCodec(DimensionType.CODEC, () -> this.dimensionType);
        param0.writeResourceLocation(this.dimension.location());
        param0.writeLong(this.seed);
        param0.writeVarInt(this.maxPlayers);
        param0.writeVarInt(this.chunkRadius);
        param0.writeVarInt(this.simulationDistance);
        param0.writeBoolean(this.reducedDebugInfo);
        param0.writeBoolean(this.showDeathScreen);
        param0.writeBoolean(this.isDebug);
        param0.writeBoolean(this.isFlat);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleLogin(this);
    }
}
