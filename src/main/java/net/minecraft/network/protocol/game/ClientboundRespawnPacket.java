package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelType;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundRespawnPacket implements Packet<ClientGamePacketListener> {
    private DimensionType dimension;
    private GameType playerGameType;
    private LevelType levelType;

    public ClientboundRespawnPacket() {
    }

    public ClientboundRespawnPacket(DimensionType param0, LevelType param1, GameType param2) {
        this.dimension = param0;
        this.playerGameType = param2;
        this.levelType = param1;
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleRespawn(this);
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.dimension = DimensionType.getById(param0.readInt());
        this.playerGameType = GameType.byId(param0.readUnsignedByte());
        this.levelType = LevelType.getLevelType(param0.readUtf(16));
        if (this.levelType == null) {
            this.levelType = LevelType.NORMAL;
        }

    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeInt(this.dimension.getId());
        param0.writeByte(this.playerGameType.getId());
        param0.writeUtf(this.levelType.getName());
    }

    @OnlyIn(Dist.CLIENT)
    public DimensionType getDimension() {
        return this.dimension;
    }

    @OnlyIn(Dist.CLIENT)
    public GameType getPlayerGameType() {
        return this.playerGameType;
    }

    @OnlyIn(Dist.CLIENT)
    public LevelType getLevelType() {
        return this.levelType;
    }
}
