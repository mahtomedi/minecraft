package net.minecraft.network.protocol.game;

import java.io.IOException;
import java.util.Collection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundMapItemDataPacket implements Packet<ClientGamePacketListener> {
    private int mapId;
    private byte scale;
    private boolean trackingPosition;
    private boolean locked;
    private MapDecoration[] decorations;
    private int startX;
    private int startY;
    private int width;
    private int height;
    private byte[] mapColors;

    public ClientboundMapItemDataPacket() {
    }

    public ClientboundMapItemDataPacket(
        int param0,
        byte param1,
        boolean param2,
        boolean param3,
        Collection<MapDecoration> param4,
        byte[] param5,
        int param6,
        int param7,
        int param8,
        int param9
    ) {
        this.mapId = param0;
        this.scale = param1;
        this.trackingPosition = param2;
        this.locked = param3;
        this.decorations = param4.toArray(new MapDecoration[param4.size()]);
        this.startX = param6;
        this.startY = param7;
        this.width = param8;
        this.height = param9;
        this.mapColors = new byte[param8 * param9];

        for(int var0 = 0; var0 < param8; ++var0) {
            for(int var1 = 0; var1 < param9; ++var1) {
                this.mapColors[var0 + var1 * param8] = param5[param6 + var0 + (param7 + var1) * 128];
            }
        }

    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.mapId = param0.readVarInt();
        this.scale = param0.readByte();
        this.trackingPosition = param0.readBoolean();
        this.locked = param0.readBoolean();
        this.decorations = new MapDecoration[param0.readVarInt()];

        for(int var0 = 0; var0 < this.decorations.length; ++var0) {
            MapDecoration.Type var1 = param0.readEnum(MapDecoration.Type.class);
            this.decorations[var0] = new MapDecoration(
                var1, param0.readByte(), param0.readByte(), (byte)(param0.readByte() & 15), param0.readBoolean() ? param0.readComponent() : null
            );
        }

        this.width = param0.readUnsignedByte();
        if (this.width > 0) {
            this.height = param0.readUnsignedByte();
            this.startX = param0.readUnsignedByte();
            this.startY = param0.readUnsignedByte();
            this.mapColors = param0.readByteArray();
        }

    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeVarInt(this.mapId);
        param0.writeByte(this.scale);
        param0.writeBoolean(this.trackingPosition);
        param0.writeBoolean(this.locked);
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

        param0.writeByte(this.width);
        if (this.width > 0) {
            param0.writeByte(this.height);
            param0.writeByte(this.startX);
            param0.writeByte(this.startY);
            param0.writeByteArray(this.mapColors);
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
        param0.scale = this.scale;
        param0.trackingPosition = this.trackingPosition;
        param0.locked = this.locked;
        param0.decorations.clear();

        for(int var0 = 0; var0 < this.decorations.length; ++var0) {
            MapDecoration var1 = this.decorations[var0];
            param0.decorations.put("icon-" + var0, var1);
        }

        for(int var2 = 0; var2 < this.width; ++var2) {
            for(int var3 = 0; var3 < this.height; ++var3) {
                param0.colors[this.startX + var2 + (this.startY + var3) * 128] = this.mapColors[var2 + var3 * this.width];
            }
        }

    }
}
