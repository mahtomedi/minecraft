package net.minecraft.core.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Locale;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class DustParticleOptionsBase implements ParticleOptions {
    protected final Vec3 color;
    protected final float scale;

    public DustParticleOptionsBase(Vec3 param0, float param1) {
        this.color = param0;
        this.scale = Mth.clamp(param1, 0.01F, 4.0F);
    }

    public static Vec3 readVec3(StringReader param0) throws CommandSyntaxException {
        param0.expect(' ');
        float var0 = (float)param0.readDouble();
        param0.expect(' ');
        float var1 = (float)param0.readDouble();
        param0.expect(' ');
        float var2 = (float)param0.readDouble();
        return new Vec3((double)var0, (double)var1, (double)var2);
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf param0) {
        param0.writeDouble(this.color.x);
        param0.writeDouble(this.color.y);
        param0.writeDouble(this.color.z);
        param0.writeFloat(this.scale);
    }

    @Override
    public String writeToString() {
        return String.format(
            Locale.ROOT, "%s %.2f %.2f %.2f %.2f", Registry.PARTICLE_TYPE.getKey(this.getType()), this.color.x, this.color.y, this.color.z, this.scale
        );
    }

    @OnlyIn(Dist.CLIENT)
    public Vec3 getColor() {
        return this.color;
    }

    @OnlyIn(Dist.CLIENT)
    public float getScale() {
        return this.scale;
    }
}
