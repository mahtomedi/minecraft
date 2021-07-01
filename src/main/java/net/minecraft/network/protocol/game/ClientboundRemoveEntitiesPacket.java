package net.minecraft.network.protocol.game;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundRemoveEntitiesPacket implements Packet<ClientGamePacketListener> {
    private final IntList entityIds;

    public ClientboundRemoveEntitiesPacket(IntList param0) {
        this.entityIds = new IntArrayList(param0);
    }

    public ClientboundRemoveEntitiesPacket(int... param0) {
        this.entityIds = new IntArrayList(param0);
    }

    public ClientboundRemoveEntitiesPacket(FriendlyByteBuf param0) {
        this.entityIds = param0.readIntIdList();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeIntIdList(this.entityIds);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleRemoveEntities(this);
    }

    public IntList getEntityIds() {
        return this.entityIds;
    }
}
