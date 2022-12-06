package net.minecraft.network.protocol.game;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

public class ClientboundRespawnPacket implements Packet<ClientGamePacketListener> {
    public static final byte KEEP_ATTRIBUTES = 1;
    public static final byte KEEP_ENTITY_DATA = 2;
    public static final byte KEEP_ALL_DATA = 3;
    private final ResourceKey<DimensionType> dimensionType;
    private final ResourceKey<Level> dimension;
    private final long seed;
    private final GameType playerGameType;
    @Nullable
    private final GameType previousPlayerGameType;
    private final boolean isDebug;
    private final boolean isFlat;
    private final byte dataToKeep;
    private final Optional<GlobalPos> lastDeathLocation;

    public ClientboundRespawnPacket(
        ResourceKey<DimensionType> param0,
        ResourceKey<Level> param1,
        long param2,
        GameType param3,
        @Nullable GameType param4,
        boolean param5,
        boolean param6,
        byte param7,
        Optional<GlobalPos> param8
    ) {
        this.dimensionType = param0;
        this.dimension = param1;
        this.seed = param2;
        this.playerGameType = param3;
        this.previousPlayerGameType = param4;
        this.isDebug = param5;
        this.isFlat = param6;
        this.dataToKeep = param7;
        this.lastDeathLocation = param8;
    }

    public ClientboundRespawnPacket(FriendlyByteBuf param0) {
        this.dimensionType = param0.readResourceKey(Registries.DIMENSION_TYPE);
        this.dimension = param0.readResourceKey(Registries.DIMENSION);
        this.seed = param0.readLong();
        this.playerGameType = GameType.byId(param0.readUnsignedByte());
        this.previousPlayerGameType = GameType.byNullableId(param0.readByte());
        this.isDebug = param0.readBoolean();
        this.isFlat = param0.readBoolean();
        this.dataToKeep = param0.readByte();
        this.lastDeathLocation = param0.readOptional(FriendlyByteBuf::readGlobalPos);
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeResourceKey(this.dimensionType);
        param0.writeResourceKey(this.dimension);
        param0.writeLong(this.seed);
        param0.writeByte(this.playerGameType.getId());
        param0.writeByte(GameType.getNullableId(this.previousPlayerGameType));
        param0.writeBoolean(this.isDebug);
        param0.writeBoolean(this.isFlat);
        param0.writeByte(this.dataToKeep);
        param0.writeOptional(this.lastDeathLocation, FriendlyByteBuf::writeGlobalPos);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleRespawn(this);
    }

    public ResourceKey<DimensionType> getDimensionType() {
        return this.dimensionType;
    }

    public ResourceKey<Level> getDimension() {
        return this.dimension;
    }

    public long getSeed() {
        return this.seed;
    }

    public GameType getPlayerGameType() {
        return this.playerGameType;
    }

    @Nullable
    public GameType getPreviousPlayerGameType() {
        return this.previousPlayerGameType;
    }

    public boolean isDebug() {
        return this.isDebug;
    }

    public boolean isFlat() {
        return this.isFlat;
    }

    public boolean shouldKeep(byte param0) {
        return (this.dataToKeep & param0) != 0;
    }

    public Optional<GlobalPos> getLastDeathLocation() {
        return this.lastDeathLocation;
    }
}
