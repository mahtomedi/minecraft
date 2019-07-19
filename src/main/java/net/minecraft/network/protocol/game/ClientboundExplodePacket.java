package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundExplodePacket implements Packet<ClientGamePacketListener> {
    private double x;
    private double y;
    private double z;
    private float power;
    private List<BlockPos> toBlow;
    private float knockbackX;
    private float knockbackY;
    private float knockbackZ;

    public ClientboundExplodePacket() {
    }

    public ClientboundExplodePacket(double param0, double param1, double param2, float param3, List<BlockPos> param4, Vec3 param5) {
        this.x = param0;
        this.y = param1;
        this.z = param2;
        this.power = param3;
        this.toBlow = Lists.newArrayList(param4);
        if (param5 != null) {
            this.knockbackX = (float)param5.x;
            this.knockbackY = (float)param5.y;
            this.knockbackZ = (float)param5.z;
        }

    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.x = (double)param0.readFloat();
        this.y = (double)param0.readFloat();
        this.z = (double)param0.readFloat();
        this.power = param0.readFloat();
        int var0 = param0.readInt();
        this.toBlow = Lists.newArrayListWithCapacity(var0);
        int var1 = Mth.floor(this.x);
        int var2 = Mth.floor(this.y);
        int var3 = Mth.floor(this.z);

        for(int var4 = 0; var4 < var0; ++var4) {
            int var5 = param0.readByte() + var1;
            int var6 = param0.readByte() + var2;
            int var7 = param0.readByte() + var3;
            this.toBlow.add(new BlockPos(var5, var6, var7));
        }

        this.knockbackX = param0.readFloat();
        this.knockbackY = param0.readFloat();
        this.knockbackZ = param0.readFloat();
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeFloat((float)this.x);
        param0.writeFloat((float)this.y);
        param0.writeFloat((float)this.z);
        param0.writeFloat(this.power);
        param0.writeInt(this.toBlow.size());
        int var0 = Mth.floor(this.x);
        int var1 = Mth.floor(this.y);
        int var2 = Mth.floor(this.z);

        for(BlockPos var3 : this.toBlow) {
            int var4 = var3.getX() - var0;
            int var5 = var3.getY() - var1;
            int var6 = var3.getZ() - var2;
            param0.writeByte(var4);
            param0.writeByte(var5);
            param0.writeByte(var6);
        }

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
