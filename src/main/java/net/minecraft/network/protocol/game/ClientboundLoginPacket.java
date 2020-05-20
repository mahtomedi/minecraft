package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GameType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundLoginPacket implements Packet<ClientGamePacketListener> {
    private int playerId;
    private long seed;
    private boolean hardcore;
    private GameType gameType;
    private RegistryAccess.RegistryHolder registryHolder;
    private ResourceLocation dimension;
    private int maxPlayers;
    private int chunkRadius;
    private boolean reducedDebugInfo;
    private boolean showDeathScreen;
    private boolean isDebug;
    private boolean isFlat;

    public ClientboundLoginPacket() {
    }

    public ClientboundLoginPacket(
        int param0,
        GameType param1,
        long param2,
        boolean param3,
        RegistryAccess.RegistryHolder param4,
        ResourceLocation param5,
        int param6,
        int param7,
        boolean param8,
        boolean param9,
        boolean param10,
        boolean param11
    ) {
        this.playerId = param0;
        this.registryHolder = param4;
        this.dimension = param5;
        this.seed = param2;
        this.gameType = param1;
        this.maxPlayers = param6;
        this.hardcore = param3;
        this.chunkRadius = param7;
        this.reducedDebugInfo = param8;
        this.showDeathScreen = param9;
        this.isDebug = param10;
        this.isFlat = param11;
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.playerId = param0.readInt();
        int var0 = param0.readUnsignedByte();
        this.hardcore = (var0 & 8) == 8;
        var0 &= -9;
        this.gameType = GameType.byId(var0);
        this.registryHolder = param0.readWithCodec(RegistryAccess.RegistryHolder.CODEC);
        this.dimension = param0.readResourceLocation();
        this.seed = param0.readLong();
        this.maxPlayers = param0.readUnsignedByte();
        this.chunkRadius = param0.readVarInt();
        this.reducedDebugInfo = param0.readBoolean();
        this.showDeathScreen = param0.readBoolean();
        this.isDebug = param0.readBoolean();
        this.isFlat = param0.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeInt(this.playerId);
        int var0 = this.gameType.getId();
        if (this.hardcore) {
            var0 |= 8;
        }

        param0.writeByte(var0);
        param0.writeWithCodec(RegistryAccess.RegistryHolder.CODEC, this.registryHolder);
        param0.writeResourceLocation(this.dimension);
        param0.writeLong(this.seed);
        param0.writeByte(this.maxPlayers);
        param0.writeVarInt(this.chunkRadius);
        param0.writeBoolean(this.reducedDebugInfo);
        param0.writeBoolean(this.showDeathScreen);
        param0.writeBoolean(this.isDebug);
        param0.writeBoolean(this.isFlat);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleLogin(this);
    }

    @OnlyIn(Dist.CLIENT)
    public int getPlayerId() {
        return this.playerId;
    }

    @OnlyIn(Dist.CLIENT)
    public long getSeed() {
        return this.seed;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isHardcore() {
        return this.hardcore;
    }

    @OnlyIn(Dist.CLIENT)
    public GameType getGameType() {
        return this.gameType;
    }

    @OnlyIn(Dist.CLIENT)
    public RegistryAccess registryAccess() {
        return this.registryHolder;
    }

    @OnlyIn(Dist.CLIENT)
    public ResourceLocation getDimension() {
        return this.dimension;
    }

    @OnlyIn(Dist.CLIENT)
    public int getChunkRadius() {
        return this.chunkRadius;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isReducedDebugInfo() {
        return this.reducedDebugInfo;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean shouldShowDeathScreen() {
        return this.showDeathScreen;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isDebug() {
        return this.isDebug;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isFlat() {
        return this.isFlat;
    }
}
