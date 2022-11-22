package net.minecraft.network.protocol.game;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.SynchedEntityData;

public record ClientboundSetEntityDataPacket(int id, List<SynchedEntityData.DataValue<?>> packedItems) implements Packet<ClientGamePacketListener> {
    public static final int EOF_MARKER = 255;

    public ClientboundSetEntityDataPacket(FriendlyByteBuf param0) {
        this(param0.readVarInt(), unpack(param0));
    }

    private static void pack(List<SynchedEntityData.DataValue<?>> param0, FriendlyByteBuf param1) {
        for(SynchedEntityData.DataValue<?> var0 : param0) {
            var0.write(param1);
        }

        param1.writeByte(255);
    }

    private static List<SynchedEntityData.DataValue<?>> unpack(FriendlyByteBuf param0) {
        List<SynchedEntityData.DataValue<?>> var0 = new ArrayList();

        int var1;
        while((var1 = param0.readUnsignedByte()) != 255) {
            var0.add(SynchedEntityData.DataValue.read(param0, var1));
        }

        return var0;
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeVarInt(this.id);
        pack(this.packedItems, param0);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleSetEntityData(this);
    }
}
