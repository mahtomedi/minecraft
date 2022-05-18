package net.minecraft.world.level.chunk;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;

public class ChunkGenerators {
    public static Codec<? extends ChunkGenerator> bootstrap(Registry<Codec<? extends ChunkGenerator>> param0) {
        Registry.register(param0, "noise", NoiseBasedChunkGenerator.CODEC);
        Registry.register(param0, "flat", FlatLevelSource.CODEC);
        return Registry.register(param0, "debug", DebugLevelSource.CODEC);
    }
}
