package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BuriedTreasurePieces;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class BuriedTreasureFeature extends StructureFeature<ProbabilityFeatureConfiguration> {
    private static final int RANDOM_SALT = 10387320;

    public BuriedTreasureFeature(Codec<ProbabilityFeatureConfiguration> param0) {
        super(param0);
    }

    protected boolean isFeatureChunk(
        ChunkGenerator param0,
        BiomeSource param1,
        long param2,
        WorldgenRandom param3,
        ChunkPos param4,
        ChunkPos param5,
        ProbabilityFeatureConfiguration param6,
        LevelHeightAccessor param7
    ) {
        param3.setLargeFeatureWithSalt(param2, param4.x, param4.z, 10387320);
        return param3.nextFloat() < param6.probability;
    }

    @Override
    public StructureFeature.StructureStartFactory<ProbabilityFeatureConfiguration> getStartFactory() {
        return BuriedTreasureFeature.BuriedTreasureStart::new;
    }

    public static class BuriedTreasureStart extends StructureStart<ProbabilityFeatureConfiguration> {
        public BuriedTreasureStart(StructureFeature<ProbabilityFeatureConfiguration> param0, ChunkPos param1, int param2, long param3) {
            super(param0, param1, param2, param3);
        }

        public void generatePieces(
            RegistryAccess param0,
            ChunkGenerator param1,
            StructureManager param2,
            ChunkPos param3,
            ProbabilityFeatureConfiguration param4,
            LevelHeightAccessor param5,
            Predicate<Biome> param6
        ) {
            if (StructureFeature.validBiomeOnTop(param1, param5, param6, Heightmap.Types.OCEAN_FLOOR_WG, param3.getMiddleBlockX(), param3.getMiddleBlockZ())) {
                BlockPos var0 = new BlockPos(param3.getBlockX(9), 90, param3.getBlockZ(9));
                this.addPiece(new BuriedTreasurePieces.BuriedTreasurePiece(var0));
            }
        }

        @Override
        public BlockPos getLocatePos() {
            ChunkPos var0 = this.getChunkPos();
            return new BlockPos(var0.getBlockX(9), 0, var0.getBlockZ(9));
        }
    }
}
