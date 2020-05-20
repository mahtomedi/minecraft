package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.MultiJigsawConfiguration;
import net.minecraft.world.level.levelgen.structure.BeardedStructureStart;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class BastionFeature extends StructureFeature<MultiJigsawConfiguration> {
    public BastionFeature(Codec<MultiJigsawConfiguration> param0) {
        super(param0);
    }

    protected boolean isFeatureChunk(
        ChunkGenerator param0,
        BiomeSource param1,
        long param2,
        WorldgenRandom param3,
        int param4,
        int param5,
        Biome param6,
        ChunkPos param7,
        MultiJigsawConfiguration param8
    ) {
        return param3.nextInt(6) >= 2;
    }

    @Override
    public StructureFeature.StructureStartFactory<MultiJigsawConfiguration> getStartFactory() {
        return BastionFeature.FeatureStart::new;
    }

    public static class FeatureStart extends BeardedStructureStart<MultiJigsawConfiguration> {
        public FeatureStart(StructureFeature<MultiJigsawConfiguration> param0, int param1, int param2, BoundingBox param3, int param4, long param5) {
            super(param0, param1, param2, param3, param4, param5);
        }

        public void generatePieces(ChunkGenerator param0, StructureManager param1, int param2, int param3, Biome param4, MultiJigsawConfiguration param5) {
            BlockPos var0 = new BlockPos(param2 * 16, 33, param3 * 16);
            BastionPieces.addPieces(param0, param1, var0, this.pieces, this.random, param5);
            this.calculateBoundingBox();
        }
    }
}
