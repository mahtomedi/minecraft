package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundExplodePacket implements Packet<ClientGamePacketListener> {
    private final double x;
    private final double y;
    private final double z;
    private final float power;
    private final List<BlockPos> toBlow;
    private final float knockbackX;
    private final float knockbackY;
    private final float knockbackZ;

    public ClientboundExplodePacket(double param0, double param1, double param2, float param3, List<BlockPos> param4, @Nullable Vec3 param5) {
        this.x = param0;
        this.y = param1;
        this.z = param2;
        this.power = param3;
        this.toBlow = Lists.newArrayList(param4);
        if (param5 != null) {
            this.knockbackX = (float)param5.x;
            this.knockbackY = (float)param5.y;
            this.knockbackZ = (float)param5.z;
        } else {
            this.knockbackX = 0.0F;
            this.knockbackY = 0.0F;
            this.knockbackZ = 0.0F;
        }

    }

    public ClientboundExplodePacket(FriendlyByteBuf param0) {
        this.x = (double)param0.readFloat();
        this.y = (double)param0.readFloat();
        this.z = (double)param0.readFloat();
        this.power = param0.readFloat();
        int var0 = Mth.floor(this.x);
        int var1 = Mth.floor(this.y);
        int var2 = Mth.floor(this.z);
        this.toBlow = param0.readList(param3 -> {
            int var0x = param3.readByte() + var0;
            int var1x = param3.readByte() + var1;
            int var2x = param3.readByte() + var2;
            return new BlockPos(var0x, var1x, var2x);
        });
        this.knockbackX = param0.readFloat();
        this.knockbackY = param0.readFloat();
        this.knockbackZ = param0.readFloat();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeFloat((float)this.x);
        param0.writeFloat((float)this.y);
        param0.writeFloat((float)this.z);
        param0.writeFloat(this.power);
        int var0 = Mth.floor(this.x);
        int var1 = Mth.floor(this.y);
        int var2 = Mth.floor(this.z);
        param0.writeCollection(this.toBlow, (param3, param4) -> {
            int var0x = param4.getX() - var0;
            int var1x = param4.getY() - var1;
            int var2x = param4.getZ() - var2;
            param3.writeByte(var0x);
            param3.writeByte(var1x);
            param3.writeByte(var2x);
        });
        param0.writeFloat(this.knockbackX);
        param0.writeFloat(this.knockbackY);
        param0.writeFloat(this.knockbackZ);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleExplosion(this);
    }

    @OnlyIn(Dist.CLIENT)
    public float getKnockbackX() {
        return this.knockbackX;
    }

    @OnlyIn(Dist.CLIENT)
    public float getKnockbackY() {
        return this.knockbackY;
    }

    @OnlyIn(Dist.CLIENT)
    public float getKnockbackZ() {
        return this.knockbackZ;
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
    public float getPower() {
        return this.power;
    }

    @OnlyIn(Dist.CLIENT)
    public List<BlockPos> getToBlow() {
        return this.toBlow;
    }
}
