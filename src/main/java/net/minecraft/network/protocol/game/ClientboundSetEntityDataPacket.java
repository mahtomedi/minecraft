package net.minecraft.network.protocol.game;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.SynchedEntityData;

public class ClientboundSetEntityDataPacket implements Packet<ClientGamePacketListener> {
    private final int id;
    @Nullable
    private final List<SynchedEntityData.DataItem<?>> packedItems;

    public ClientboundSetEntityDataPacket(int param0, SynchedEntityData param1, boolean param2) {
        this.id = param0;
        if (param2) {
            this.packedItems = param1.getAll();
            param1.clearDirty();
        } else {
            this.packedItems = param1.packDirty();
        }

    }

    public ClientboundSetEntityDataPacket(FriendlyByteBuf param0) {
        this.id = param0.readVarInt();
        this.packedItems = SynchedEntityData.unpack(param0);
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeVarInt(this.id);
        SynchedEntityData.pack(this.packedItems, param0);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleSetEntityData(this);
    }

    @Nullable
    public List<SynchedEntityData.DataItem<?>> getUnpackedData() {
        return this.packedItems;
    }

    public int getId() {
        return this.id;
    }
}
