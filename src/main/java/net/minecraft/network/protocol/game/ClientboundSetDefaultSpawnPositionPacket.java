package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundSetDefaultSpawnPositionPacket implements Packet<ClientGamePacketListener> {
    private BlockPos pos;
    private float angle;

    public ClientboundSetDefaultSpawnPositionPacket() {
    }

    public ClientboundSetDefaultSpawnPositionPacket(BlockPos param0, float param1) {
        this.pos = param0;
        this.angle = param1;
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.pos = param0.readBlockPos();
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeBlockPos(this.pos);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleSetSpawn(this);
    }

    @OnlyIn(Dist.CLIENT)
    public BlockPos getPos() {
        return this.pos;
    }

    @OnlyIn(Dist.CLIENT)
    public float getAngle() {
        return this.angle;
    }
}
