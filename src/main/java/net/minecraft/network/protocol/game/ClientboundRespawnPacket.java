package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundRespawnPacket implements Packet<ClientGamePacketListener> {
    private DimensionType dimensionType;
    private ResourceKey<Level> dimension;
    private long seed;
    private GameType playerGameType;
    private GameType previousPlayerGameType;
    private boolean isDebug;
    private boolean isFlat;
    private boolean keepAllPlayerData;

    public ClientboundRespawnPacket() {
    }

    public ClientboundRespawnPacket(
        DimensionType param0, ResourceKey<Level> param1, long param2, GameType param3, GameType param4, boolean param5, boolean param6, boolean param7
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

    public void handle(ClientGamePacketListener param0) {
        param0.handleRespawn(this);
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.dimensionType = param0.readWithCodec(DimensionType.CODEC).get();
        this.dimension = ResourceKey.create(Registry.DIMENSION_REGISTRY, param0.readResourceLocation());
        this.seed = param0.readLong();
        this.playerGameType = GameType.byId(param0.readUnsignedByte());
        this.previousPlayerGameType = GameType.byId(param0.readUnsignedByte());
        this.isDebug = param0.readBoolean();
        this.isFlat = param0.readBoolean();
        this.keepAllPlayerData = param0.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeWithCodec(DimensionType.CODEC, () -> this.dimensionType);
        param0.writeResourceLocation(this.dimension.location());
        param0.writeLong(this.seed);
        param0.writeByte(this.playerGameType.getId());
        param0.writeByte(this.previousPlayerGameType.getId());
        param0.writeBoolean(this.isDebug);
        param0.writeBoolean(this.isFlat);
        param0.writeBoolean(this.keepAllPlayerData);
    }

    @OnlyIn(Dist.CLIENT)
    public DimensionType getDimensionType() {
        return this.dimensionType;
    }

    @OnlyIn(Dist.CLIENT)
    public ResourceKey<Level> getDimension() {
        return this.dimension;
    }

    @OnlyIn(Dist.CLIENT)
    public long getSeed() {
        return this.seed;
    }

    @OnlyIn(Dist.CLIENT)
    public GameType getPlayerGameType() {
        return this.playerGameType;
    }

    @OnlyIn(Dist.CLIENT)
    public GameType getPreviousPlayerGameType() {
        return this.previousPlayerGameType;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isDebug() {
        return this.isDebug;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isFlat() {
        return this.isFlat;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean shouldKeepAllPlayerData() {
        return this.keepAllPlayerData;
    }
}
