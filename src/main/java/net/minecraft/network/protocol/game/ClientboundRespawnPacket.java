package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

public class ClientboundRespawnPacket implements Packet<ClientGamePacketListener> {
    private final Holder<DimensionType> dimensionType;
    private final ResourceKey<Level> dimension;
    private final long seed;
    private final GameType playerGameType;
    @Nullable
    private final GameType previousPlayerGameType;
    private final boolean isDebug;
    private final boolean isFlat;
    private final boolean keepAllPlayerData;

    public ClientboundRespawnPacket(
        Holder<DimensionType> param0,
        ResourceKey<Level> param1,
        long param2,
        GameType param3,
        @Nullable GameType param4,
        boolean param5,
        boolean param6,
        boolean param7
    ) {
        this.dimensionType = param0;
        this.dimension = param1;
        this.seed = param2;
        this.playerGameType = param3;
        this.previousPlayerGameType = param4;
        this.isDebug = param5;
        this.isFlat = param6;
        this.keepAllPlayerData = param7;
    }

    public ClientboundRespawnPacket(FriendlyByteBuf param0) {
        this.dimensionType = param0.readWithCodec(DimensionType.CODEC);
        this.dimension = ResourceKey.create(Registry.DIMENSION_REGISTRY, param0.readResourceLocation());
        this.seed = param0.readLong();
        this.playerGameType = GameType.byId(param0.readUnsignedByte());
        this.previousPlayerGameType = GameType.byNullableId(param0.readByte());
        this.isDebug = param0.readBoolean();
        this.isFlat = param0.readBoolean();
        this.keepAllPlayerData = param0.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeWithCodec(DimensionType.CODEC, this.dimensionType);
        param0.writeResourceLocation(this.dimension.location());
        param0.writeLong(this.seed);
        param0.writeByte(this.playerGameType.getId());
        param0.writeByte(GameType.getNullableId(this.previousPlayerGameType));
        param0.writeBoolean(this.isDebug);
        param0.writeBoolean(this.isFlat);
        param0.writeBoolean(this.keepAllPlayerData);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleRespawn(this);
    }

    public Holder<DimensionType> getDimensionType() {
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

    public boolean shouldKeepAllPlayerData() {
        return this.keepAllPlayerData;
    }
}
