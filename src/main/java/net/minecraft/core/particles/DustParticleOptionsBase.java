package net.minecraft.core.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.math.Vector3f;
import java.util.Locale;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class DustParticleOptionsBase implements ParticleOptions {
    protected final Vector3f color;
    protected final float scale;

    public DustParticleOptionsBase(Vector3f param0, float param1) {
        this.color = param0;
        this.scale = Mth.clamp(param1, 0.01F, 4.0F);
    }

    public static Vector3f readVector3f(StringReader param0) throws CommandSyntaxException {
        param0.expect(' ');
        float var0 = param0.readFloat();
        param0.expect(' ');
        float var1 = param0.readFloat();
        param0.expect(' ');
        float var2 = param0.readFloat();
        return new Vector3f(var0, var1, var2);
    }

    public static Vector3f readVector3f(FriendlyByteBuf param0) {
        return new Vector3f(param0.readFloat(), param0.readFloat(), param0.readFloat());
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf param0) {
        param0.writeFloat(this.color.x());
        param0.writeFloat(this.color.y());
        param0.writeFloat(this.color.z());
        param0.writeFloat(this.scale);
    }

    @Override
    public String writeToString() {
        return String.format(
            Locale.ROOT, "%s %.2f %.2f %.2f %.2f", Registry.PARTICLE_TYPE.getKey(this.getType()), this.color.x(), this.color.y(), this.color.z(), this.scale
        );
    }

    @OnlyIn(Dist.CLIENT)
    public Vector3f getColor() {
        return this.color;
    }

    @OnlyIn(Dist.CLIENT)
    public float getScale() {
        return this.scale;
    }
}
