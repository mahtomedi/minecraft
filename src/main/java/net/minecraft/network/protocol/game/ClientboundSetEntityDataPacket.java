package net.minecraft.network.protocol.game;

import java.io.IOException;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundSetEntityDataPacket implements Packet<ClientGamePacketListener> {
    private int id;
    private List<SynchedEntityData.DataItem<?>> packedItems;

    public ClientboundSetEntityDataPacket() {
    }

    public ClientboundSetEntityDataPacket(int param0, SynchedEntityData param1, boolean param2) {
        this.id = param0;
        if (param2) {
            this.packedItems = param1.getAll();
            param1.clearDirty();
        } else {
            this.packedItems = param1.packDirty();
        }

    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.id = param0.readVarInt();
        this.packedItems = SynchedEntityData.unpack(param0);
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeVarInt(this.id);
        SynchedEntityData.pack(this.packedItems, param0);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleSetEntityData(this);
    }

    @OnlyIn(Dist.CLIENT)
    public List<SynchedEntityData.DataItem<?>> getUnpackedData() {
        return this.packedItems;
    }

    @OnlyIn(Dist.CLIENT)
    public int getId() {
        return this.id;
    }
}
