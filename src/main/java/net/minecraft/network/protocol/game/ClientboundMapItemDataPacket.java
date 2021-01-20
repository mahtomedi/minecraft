package net.minecraft.network.protocol.game;

import java.io.IOException;
import java.util.Collection;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundMapItemDataPacket implements Packet<ClientGamePacketListener> {
    private int mapId;
    private byte scale;
    private boolean locked;
    @Nullable
    private MapDecoration[] decorations;
    @Nullable
    private MapItemSavedData.MapPatch colorPatch;

    public ClientboundMapItemDataPacket() {
    }

    public ClientboundMapItemDataPacket(
        int param0, byte param1, boolean param2, @Nullable Collection<MapDecoration> param3, @Nullable MapItemSavedData.MapPatch param4
    ) {
        this.mapId = param0;
        this.scale = param1;
        this.locked = param2;
        this.decorations = param3 != null ? param3.toArray(new MapDecoration[0]) : null;
        this.colorPatch = param4;
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.mapId = param0.readVarInt();
        this.scale = param0.readByte();
        this.locked = param0.readBoolean();
        if (param0.readBoolean()) {
            this.decorations = new MapDecoration[param0.readVarInt()];

            for(int var0 = 0; var0 < this.decorations.length; ++var0) {
                MapDecoration.Type var1 = param0.readEnum(MapDecoration.Type.class);
                this.decorations[var0] = new MapDecoration(
                    var1, param0.readByte(), param0.readByte(), (byte)(param0.readByte() & 15), param0.readBoolean() ? param0.readComponent() : null
                );
            }
        }

        int var2 = param0.readUnsignedByte();
        if (var2 > 0) {
            int var3 = param0.readUnsignedByte();
            int var4 = param0.readUnsignedByte();
            int var5 = param0.readUnsignedByte();
            byte[] var6 = param0.readByteArray();
            this.colorPatch = new MapItemSavedData.MapPatch(var4, var5, var2, var3, var6);
        }

    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeVarInt(this.mapId);
        param0.writeByte(this.scale);
        param0.writeBoolean(this.locked);
        if (this.decorations != null) {
            param0.writeBoolean(true);
            param0.writeVarInt(this.decorations.length);

            for(MapDecoration var0 : this.decorations) {
                param0.writeEnum(var0.getType());
                param0.writeByte(var0.getX());
                param0.writeByte(var0.getY());
                param0.writeByte(var0.getRot() & 15);
                if (var0.getName() != null) {
                    param0.writeBoolean(true);
                    param0.writeComponent(var0.getName());
                } else {
                    param0.writeBoolean(false);
                }
            }
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
