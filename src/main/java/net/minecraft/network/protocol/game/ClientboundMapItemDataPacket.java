package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public class ClientboundMapItemDataPacket implements Packet<ClientGamePacketListener> {
    private final int mapId;
    private final byte scale;
    private final boolean locked;
    @Nullable
    private final List<MapDecoration> decorations;
    @Nullable
    private final MapItemSavedData.MapPatch colorPatch;

    public ClientboundMapItemDataPacket(
        int param0, byte param1, boolean param2, @Nullable Collection<MapDecoration> param3, @Nullable MapItemSavedData.MapPatch param4
    ) {
        this.mapId = param0;
        this.scale = param1;
        this.locked = param2;
        this.decorations = param3 != null ? Lists.newArrayList(param3) : null;
        this.colorPatch = param4;
    }

    public ClientboundMapItemDataPacket(FriendlyByteBuf param0) {
        this.mapId = param0.readVarInt();
        this.scale = param0.readByte();
        this.locked = param0.readBoolean();
        this.decorations = param0.readNullable(param0x -> param0x.readList(param0xx -> {
                MapDecoration.Type var0x = param0xx.readEnum(MapDecoration.Type.class);
                byte var1x = param0xx.readByte();
                byte var2x = param0xx.readByte();
                byte var3x = (byte)(param0xx.readByte() & 15);
                Component var4x = param0xx.readNullable(FriendlyByteBuf::readComponent);
                return new MapDecoration(var0x, var1x, var2x, var3x, var4x);
            }));
        int var0 = param0.readUnsignedByte();
        if (var0 > 0) {
            int var1 = param0.readUnsignedByte();
            int var2 = param0.readUnsignedByte();
            int var3 = param0.readUnsignedByte();
            byte[] var4 = param0.readByteArray();
            this.colorPatch = new MapItemSavedData.MapPatch(var2, var3, var0, var1, var4);
        } else {
            this.colorPatch = null;
        }

    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeVarInt(this.mapId);
        param0.writeByte(this.scale);
        param0.writeBoolean(this.locked);
        param0.writeNullable(this.decorations, (param0x, param1) -> param0x.writeCollection(param1, (param0xx, param1x) -> {
                param0xx.writeEnum(param1x.type());
                param0xx.writeByte(param1x.x());
                param0xx.writeByte(param1x.y());
                param0xx.writeByte(param1x.rot() & 15);
                param0xx.writeNullable(param1x.name(), FriendlyByteBuf::writeComponent);
            }));
        if (this.colorPatch != null) {
            param0.writeByte(this.colorPatch.width);
            param0.writeByte(this.colorPatch.height);
            param0.writeByte(this.colorPatch.startX);
            param0.writeByte(this.colorPatch.startY);
            param0.writeByteArray(this.colorPatch.mapColors);
        } else {
            param0.writeByte(0);
        }

    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleMapItemData(this);
    }

    public int getMapId() {
        return this.mapId;
    }

    public void applyToMap(MapItemSavedData param0) {
        if (this.decorations != null) {
            param0.addClientSideDecorations(this.decorations);
        }

        if (this.colorPatch != null) {
            this.colorPatch.applyToMap(param0);
        }

    }

    public byte getScale() {
        return this.scale;
    }

    public boolean isLocked() {
        return this.locked;
    }
}
