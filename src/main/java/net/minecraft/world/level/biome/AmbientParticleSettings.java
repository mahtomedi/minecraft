package net.minecraft.world.level.biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;

public class AmbientParticleSettings {
    public static final Codec<AmbientParticleSettings> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    ParticleTypes.CODEC.fieldOf("options").forGetter(param0x -> param0x.options),
                    Codec.FLOAT.fieldOf("probability").forGetter(param0x -> param0x.probability)
                )
                .apply(param0, AmbientParticleSettings::new)
    );
    private final ParticleOptions options;
    private final float probability;

    public AmbientParticleSettings(ParticleOptions param0, float param1) {
        this.options = param0;
        this.probability = param1;
    }

    public ParticleOptions getOptions() {
        return this.options;
    }

    public boolean canSpawn(Random param0) {
        return param0.nextFloat() <= this.probability;
    }
}
