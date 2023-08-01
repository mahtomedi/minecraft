package net.minecraft.network.protocol.game;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

public record CommonPlayerSpawnInfo(
    ResourceKey<DimensionType> dimensionType,
    ResourceKey<Level> dimension,
    long seed,
    GameType gameType,
    @Nullable GameType previousGameType,
    boolean isDebug,
    boolean isFlat,
    Optional<GlobalPos> lastDeathLocation,
    int portalCooldown
) {
    public CommonPlayerSpawnInfo(FriendlyByteBuf param0) {
        this(
            param0.readResourceKey(Registries.DIMENSION_TYPE),
            param0.readResourceKey(Registries.DIMENSION),
            param0.readLong(),
            GameType.byId(param0.readByte()),
            GameType.byNullableId(param0.readByte()),
            param0.readBoolean(),
            param0.readBoolean(),
            param0.readOptional(FriendlyByteBuf::readGlobalPos),
            param0.readVarInt()
        );
    }

    public void write(FriendlyByteBuf param0) {
        param0.writeResourceKey(this.dimensionType);
        param0.writeResourceKey(this.dimension);
        param0.writeLong(this.seed);
        param0.writeByte(this.gameType.getId());
        param0.writeByte(GameType.getNullableId(this.previousGameType));
        param0.writeBoolean(this.isDebug);
        param0.writeBoolean(this.isFlat);
        param0.writeOptional(this.lastDeathLocation, FriendlyByteBuf::writeGlobalPos);
        param0.writeVarInt(this.portalCooldown);
    }
}
