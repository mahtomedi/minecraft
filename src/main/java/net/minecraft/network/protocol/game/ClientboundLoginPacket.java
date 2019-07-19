package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelType;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundLoginPacket implements Packet<ClientGamePacketListener> {
    private int playerId;
    private boolean hardcore;
    private GameType gameType;
    private DimensionType dimension;
    private int maxPlayers;
    private LevelType levelType;
    private int chunkRadius;
    private boolean reducedDebugInfo;

    public ClientboundLoginPacket() {
    }

    public ClientboundLoginPacket(int param0, GameType param1, boolean param2, DimensionType param3, int param4, LevelType param5, int param6, boolean param7) {
        this.playerId = param0;
        this.dimension = param3;
        this.gameType = param1;
        this.maxPlayers = param4;
        this.hardcore = param2;
        this.levelType = param5;
        this.chunkRadius = param6;
        this.reducedDebugInfo = param7;
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.playerId = param0.readInt();
        int var0 = param0.readUnsignedByte();
        this.hardcore = (var0 & 8) == 8;
        var0 &= -9;
        this.gameType = GameType.byId(var0);
        this.dimension = DimensionType.getById(param0.readInt());
        this.maxPlayers = param0.readUnsignedByte();
        this.levelType = LevelType.getLevelType(param0.readUtf(16));
        if (this.levelType == null) {
            this.levelType = LevelType.NORMAL;
        }

        this.chunkRadius = param0.readVarInt();
        this.reducedDebugInfo = param0.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeInt(this.playerId);
        int var0 = this.gameType.getId();
        if (this.hardcore) {
            var0 |= 8;
        }

        param0.writeByte(var0);
        param0.writeInt(this.dimension.getId());
        param0.writeByte(this.maxPlayers);
        param0.writeUtf(this.levelType.getName());
        param0.writeVarInt(this.chunkRadius);
        param0.writeBoolean(this.reducedDebugInfo);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleLogin(this);
    }

    @OnlyIn(Dist.CLIENT)
    public int getPlayerId() {
        return this.playerId;
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
    public DimensionType getDimension() {
        return this.dimension;
    }

    @OnlyIn(Dist.CLIENT)
    public LevelType getLevelType() {
        return this.levelType;
    }

    @OnlyIn(Dist.CLIENT)
    public int getChunkRadius() {
        return this.chunkRadius;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isReducedDebugInfo() {
        return this.reducedDebugInfo;
    }
}
