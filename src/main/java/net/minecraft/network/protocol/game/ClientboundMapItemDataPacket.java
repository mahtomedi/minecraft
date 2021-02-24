package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
        if (param0.readBoolean()) {
            this.decorations = param0.readList(
                param0x -> {
                    MapDecoration.Type var0x = param0x.readEnum(MapDecoration.Type.class);
                    return new MapDecoration(
                        var0x, param0x.readByte(), param0x.readByte(), (byte)(param0x.readByte() & 15), param0x.readBoolean() ? param0x.readComponent() : null
                    );
                }
            );
        } else {
            this.decorations = null;
        }

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
        if (this.decorations != null) {
            param0.writeBoolean(true);
            param0.writeCollection(this.decorations, (param0x, param1) -> {
                param0x.writeEnum(param1.getType());
                param0x.writeByte(param1.getX());
                param0x.writeByte(param1.getY());
                param0x.writeByte(param1.getRot() & 15);
                if (param1.getName() != null) {
                    param0x.writeBoolean(true);
                    param0x.writeComponent(param1.getName());
                } else {
                    param0x.writeBoolean(false);
                }

            });
        } else {
            param0.writeBoolean(false);
        }

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

    @OnlyIn(Dist.CLIENT)
    public int getMapId() {
        return this.mapId;
    }

    @OnlyIn(Dist.CLIENT)
    public void applyToMap(MapItemSavedData param0) {
        if (this.decorations != null) {
            param0.addClientSideDecorations(this.decorations);
        }

        if (this.colorPatch != null) {
            this.colorPatch.applyToMap(param0);
        }

    }

    @OnlyIn(Dist.CLIENT)
    public byte getScale() {
        return this.scale;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isLocked() {
        return this.locked;
    }
}
