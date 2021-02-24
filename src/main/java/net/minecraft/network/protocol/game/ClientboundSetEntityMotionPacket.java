package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundSetEntityMotionPacket implements Packet<ClientGamePacketListener> {
    private final int id;
    private final int xa;
    private final int ya;
    private final int za;

    public ClientboundSetEntityMotionPacket(Entity param0) {
        this(param0.getId(), param0.getDeltaMovement());
    }

    public ClientboundSetEntityMotionPacket(int param0, Vec3 param1) {
        this.id = param0;
        double var0 = 3.9;
        double var1 = Mth.clamp(param1.x, -3.9, 3.9);
        double var2 = Mth.clamp(param1.y, -3.9, 3.9);
        double var3 = Mth.clamp(param1.z, -3.9, 3.9);
        this.xa = (int)(var1 * 8000.0);
        this.ya = (int)(var2 * 8000.0);
        this.za = (int)(var3 * 8000.0);
    }

    public ClientboundSetEntityMotionPacket(FriendlyByteBuf param0) {
        this.id = param0.readVarInt();
        this.xa = param0.readShort();
        this.ya = param0.readShort();
        this.za = param0.readShort();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeVarInt(this.id);
        param0.writeShort(this.xa);
        param0.writeShort(this.ya);
        param0.writeShort(this.za);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleSetEntityMotion(this);
    }

    @OnlyIn(Dist.CLIENT)
    public int getId() {
        return this.id;
    }

    @OnlyIn(Dist.CLIENT)
    public int getXa() {
        return this.xa;
    }

    @OnlyIn(Dist.CLIENT)
    public int getYa() {
        return this.ya;
    }

    @OnlyIn(Dist.CLIENT)
    public int getZa() {
        return this.za;
    }
}
