package net.minecraft.network.protocol.game;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundPlayerPositionPacket implements Packet<ClientGamePacketListener> {
    private double x;
    private double y;
    private double z;
    private float yRot;
    private float xRot;
    private Set<ClientboundPlayerPositionPacket.RelativeArgument> relativeArguments;
    private int id;

    public ClientboundPlayerPositionPacket() {
    }

    public ClientboundPlayerPositionPacket(
        double param0, double param1, double param2, float param3, float param4, Set<ClientboundPlayerPositionPacket.RelativeArgument> param5, int param6
    ) {
        this.x = param0;
        this.y = param1;
        this.z = param2;
        this.yRot = param3;
        this.xRot = param4;
        this.relativeArguments = param5;
        this.id = param6;
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.x = param0.readDouble();
        this.y = param0.readDouble();
        this.z = param0.readDouble();
        this.yRot = param0.readFloat();
        this.xRot = param0.readFloat();
        this.relativeArguments = ClientboundPlayerPositionPacket.RelativeArgument.unpack(param0.readUnsignedByte());
        this.id = param0.readVarInt();
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeDouble(this.x);
        param0.writeDouble(this.y);
        param0.writeDouble(this.z);
        param0.writeFloat(this.yRot);
        param0.writeFloat(this.xRot);
        param0.writeByte(ClientboundPlayerPositionPacket.RelativeArgument.pack(this.relativeArguments));
        param0.writeVarInt(this.id);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleMovePlayer(this);
    }

    @OnlyIn(Dist.CLIENT)
    public double getX() {
        return this.x;
    }

    @OnlyIn(Dist.CLIENT)
    public double getY() {
        return this.y;
    }

    @OnlyIn(Dist.CLIENT)
    public double getZ() {
        return this.z;
    }

    @OnlyIn(Dist.CLIENT)
    public float getYRot() {
        return this.yRot;
    }

    @OnlyIn(Dist.CLIENT)
    public float getXRot() {
        return this.xRot;
    }

    @OnlyIn(Dist.CLIENT)
    public int getId() {
        return this.id;
    }

    @OnlyIn(Dist.CLIENT)
    public Set<ClientboundPlayerPositionPacket.RelativeArgument> getRelativeArguments() {
        return this.relativeArguments;
    }

    public static enum RelativeArgument {
        X(0),
        Y(1),
        Z(2),
        Y_ROT(3),
        X_ROT(4);

        private final int bit;

        private RelativeArgument(int param0) {
            this.bit = param0;
        }

        private int getMask() {
            return 1 << this.bit;
        }

        private boolean isSet(int param0) {
            return (param0 & this.getMask()) == this.getMask();
        }

        public static Set<ClientboundPlayerPositionPacket.RelativeArgument> unpack(int param0) {
            Set<ClientboundPlayerPositionPacket.RelativeArgument> var0 = EnumSet.noneOf(ClientboundPlayerPositionPacket.RelativeArgument.class);

            for(ClientboundPlayerPositionPacket.RelativeArgument var1 : values()) {
                if (var1.isSet(param0)) {
                    var0.add(var1);
                }
            }

            return var0;
        }

        public static int pack(Set<ClientboundPlayerPositionPacket.RelativeArgument> param0) {
            int var0 = 0;

            for(ClientboundPlayerPositionPacket.RelativeArgument var1 : param0) {
                var0 |= var1.getMask();
            }

            return var0;
        }
    }
}
