package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundRemoveEntitiesPacket implements Packet<ClientGamePacketListener> {
    private int[] entityIds;

    public ClientboundRemoveEntitiesPacket() {
    }

    public ClientboundRemoveEntitiesPacket(int... param0) {
        this.entityIds = param0;
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.entityIds = new int[param0.readVarInt()];

        for(int var0 = 0; var0 < this.entityIds.length; ++var0) {
            this.entityIds[var0] = param0.readVarInt();
        }

    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeVarInt(this.entityIds.length);

        for(int var0 : this.entityIds) {
            param0.writeVarInt(var0);
        }

    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleRemoveEntity(this);
    }

    @OnlyIn(Dist.CLIENT)
    public int[] getEntityIds() {
        return this.entityIds;
    }
}
