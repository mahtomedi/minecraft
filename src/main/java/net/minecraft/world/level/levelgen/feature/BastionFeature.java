package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;

public class BastionFeature extends JigsawFeature {
    private static final int BASTION_SPAWN_HEIGHT = 33;

    public BastionFeature(Codec<JigsawConfiguration> param0) {
        super(param0, 33, false, false);
    }

    protected boolean isFeatureChunk(
        ChunkGenerator param0, BiomeSource param1, long param2, ChunkPos param3, JigsawConfiguration param4, LevelHeightAccessor param5
    ) {
        WorldgenRandom var0 = new WorldgenRandom(new LegacyRandomSource(0L));
        var0.setLargeFeatureSeed(param2, param3.x, param3.z);
        return var0.nextInt(5) >= 2;
    }
}
