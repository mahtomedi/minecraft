package net.minecraft.core.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Locale;
import java.util.Random;
import java.util.function.BiFunction;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class DustParticleOptions implements ParticleOptions {
    public static final DustParticleOptions REDSTONE = new DustParticleOptions(1.0F, 0.0F, 0.0F, 1.0F);
    public static final ParticleOptions.Deserializer<DustParticleOptions> DESERIALIZER = new ParticleOptions.Deserializer<DustParticleOptions>() {
        public DustParticleOptions fromCommand(ParticleType<DustParticleOptions> param0, StringReader param1) throws CommandSyntaxException {
            param1.expect(' ');
            float var0 = (float)param1.readDouble();
            param1.expect(' ');
            float var1 = (float)param1.readDouble();
            param1.expect(' ');
            float var2 = (float)param1.readDouble();
            param1.expect(' ');
            float var3 = (float)param1.readDouble();
            return new DustParticleOptions(var0, var1, var2, var3);
        }

        public DustParticleOptions fromNetwork(ParticleType<DustParticleOptions> param0, FriendlyByteBuf param1) {
            return new DustParticleOptions(param1.readFloat(), param1.readFloat(), param1.readFloat(), param1.readFloat());
        }
    };
    private final float r;
    private final float g;
    private final float b;
    private final float scale;
    public static final BiFunction<Random, ParticleType<DustParticleOptions>, DustParticleOptions> RANDOM_PROVIDER = (param0, param1) -> new DustParticleOptions(
            param0.nextFloat(), param0.nextFloat(), param0.nextFloat(), param0.nextFloat() * 2.0F
        );

    public DustParticleOptions(float param0, float param1, float param2, float param3) {
        this.r = param0;
        this.g = param1;
        this.b = param2;
        this.scale = Mth.clamp(param3, 0.01F, 4.0F);
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf param0) {
        param0.writeFloat(this.r);
        param0.writeFloat(this.g);
        param0.writeFloat(this.b);
        param0.writeFloat(this.scale);
    }

    @Override
    public String writeToString() {
        return String.format(Locale.ROOT, "%s %.2f %.2f %.2f %.2f", Registry.PARTICLE_TYPE.getKey(this.getType()), this.r, this.g, this.b, this.scale);
    }

    @Override
    public ParticleType<DustParticleOptions> getType() {
        return ParticleTypes.DUST;
    }

    @OnlyIn(Dist.CLIENT)
    public float getR() {
        return this.r;
    }

    @OnlyIn(Dist.CLIENT)
    public float getG() {
        return this.g;
    }

    @OnlyIn(Dist.CLIENT)
    public float getB() {
        return this.b;
    }

    @OnlyIn(Dist.CLIENT)
    public float getScale() {
        return this.scale;
    }
}
