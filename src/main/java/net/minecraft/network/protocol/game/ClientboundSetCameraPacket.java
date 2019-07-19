package net.minecraft.network.protocol.game;

import java.io.IOException;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundSetCameraPacket implements Packet<ClientGamePacketListener> {
    public int cameraId;

    public ClientboundSetCameraPacket() {
    }

    public ClientboundSetCameraPacket(Entity param0) {
        this.cameraId = param0.getId();
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.cameraId = param0.readVarInt();
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeVarInt(this.cameraId);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleSetCamera(this);
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    public Entity getEntity(Level param0) {
        return param0.getEntity(this.cameraId);
    }
}
