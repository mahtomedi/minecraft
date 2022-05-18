package net.minecraft.world.level.biome;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;

public class BiomeSources {
    public static Codec<? extends BiomeSource> bootstrap(Registry<Codec<? extends BiomeSource>> param0) {
        Registry.register(param0, "fixed", FixedBiomeSource.CODEC);
        Registry.register(param0, "multi_noise", MultiNoiseBiomeSource.CODEC);
        Registry.register(param0, "checkerboard", CheckerboardColumnBiomeSource.CODEC);
        return Registry.register(param0, "the_end", TheEndBiomeSource.CODEC);
    }
}
