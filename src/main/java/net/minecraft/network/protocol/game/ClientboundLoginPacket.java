package net.minecraft.network.protocol.game;

import com.google.common.collect.Sets;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.RegistryOps;
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
    RegistryAccess.Frozen registryHolder,
    ResourceKey<DimensionType> dimensionType,
    ResourceKey<Level> dimension,
    long seed,
    int maxPlayers,
    int chunkRadius,
    int simulationDistance,
    boolean reducedDebugInfo,
    boolean showDeathScreen,
    boolean isDebug,
    boolean isFlat,
    Optional<GlobalPos> lastDeathLocation,
    int portalCooldown
) implements Packet<ClientGamePacketListener> {
    private static final RegistryOps<Tag> BUILTIN_CONTEXT_OPS = RegistryOps.create(
        NbtOps.INSTANCE, RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY)
    );

    public ClientboundLoginPacket(FriendlyByteBuf param0) {
        this(
            param0.readInt(),
            param0.readBoolean(),
            GameType.byId(param0.readByte()),
            GameType.byNullableId(param0.readByte()),
            param0.readCollection(Sets::newHashSetWithExpectedSize, param0x -> param0x.readResourceKey(Registries.DIMENSION)),
            param0.readWithCodec(BUILTIN_CONTEXT_OPS, RegistrySynchronization.NETWORK_CODEC).freeze(),
            param0.readResourceKey(Registries.DIMENSION_TYPE),
            param0.readResourceKey(Registries.DIMENSION),
            param0.readLong(),
            param0.readVarInt(),
            param0.readVarInt(),
            param0.readVarInt(),
            param0.readBoolean(),
            param0.readBoolean(),
            param0.readBoolean(),
            param0.readBoolean(),
            param0.readOptional(FriendlyByteBuf::readGlobalPos),
            param0.readVarInt()
        );
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeInt(this.playerId);
        param0.writeBoolean(this.hardcore);
        param0.writeByte(this.gameType.getId());
        param0.writeByte(GameType.getNullableId(this.previousGameType));
        param0.writeCollection(this.levels, FriendlyByteBuf::writeResourceKey);
        param0.writeWithCodec(BUILTIN_CONTEXT_OPS, RegistrySynchronization.NETWORK_CODEC, this.registryHolder);
        param0.writeResourceKey(this.dimensionType);
        param0.writeResourceKey(this.dimension);
        param0.writeLong(this.seed);
        param0.writeVarInt(this.maxPlayers);
        param0.writeVarInt(this.chunkRadius);
        param0.writeVarInt(this.simulationDistance);
        param0.writeBoolean(this.reducedDebugInfo);
        param0.writeBoolean(this.showDeathScreen);
        param0.writeBoolean(this.isDebug);
        param0.writeBoolean(this.isFlat);
        param0.writeOptional(this.lastDeathLocation, FriendlyByteBuf::writeGlobalPos);
        param0.writeVarInt(this.portalCooldown);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleLogin(this);
    }
}
