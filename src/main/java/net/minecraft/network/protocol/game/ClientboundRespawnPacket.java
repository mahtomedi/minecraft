package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GameType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundRespawnPacket implements Packet<ClientGamePacketListener> {
    private ResourceLocation dimension;
    private long seed;
    private GameType playerGameType;
    private boolean isDebug;
    private boolean isFlat;
    private boolean keepAllPlayerData;

    public ClientboundRespawnPacket() {
    }

    public ClientboundRespawnPacket(ResourceLocation param0, long param1, GameType param2, boolean param3, boolean param4, boolean param5) {
        this.dimension = param0;
        this.seed = param1;
        this.playerGameType = param2;
        this.isDebug = param3;
        this.isFlat = param4;
        this.keepAllPlayerData = param5;
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleRespawn(this);
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.dimension = param0.readResourceLocation();
        this.seed = param0.readLong();
        this.playerGameType = GameType.byId(param0.readUnsignedByte());
        this.isDebug = param0.readBoolean();
        this.isFlat = param0.readBoolean();
        this.keepAllPlayerData = param0.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeResourceLocation(this.dimension);
        param0.writeLong(this.seed);
        param0.writeByte(this.playerGameType.getId());
        param0.writeBoolean(this.isDebug);
        param0.writeBoolean(this.isFlat);
        param0.writeBoolean(this.keepAllPlayerData);
    }

    @OnlyIn(Dist.CLIENT)
    public ResourceLocation getDimension() {
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
