package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.MultiJigsawConfiguration;
import net.minecraft.world.level.levelgen.structure.BeardedStructureStart;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class BastionFeature extends StructureFeature<MultiJigsawConfiguration> {
    public BastionFeature(Function<Dynamic<?>, ? extends MultiJigsawConfiguration> param0) {
        super(param0);
    }

    @Override
    protected int getSpacing(DimensionType param0, ChunkGeneratorSettings param1) {
        return param1.getRareNetherStructureSpacing();
    }

    @Override
    protected int getSeparation(DimensionType param0, ChunkGeneratorSettings param1) {
        return param1.getRareNetherStructureSeparation();
    }

    @Override
    protected int getRandomSalt(ChunkGeneratorSettings param0) {
        return param0.getRareNetherStructureSalt();
    }

    @Override
    protected boolean isFeatureChunk(
        BiomeManager param0, ChunkGenerator<?> param1, WorldgenRandom param2, int param3, int param4, Biome param5, ChunkPos param6
    ) {
        return param2.nextInt(6) >= 2;
    }

    @Override
    public StructureFeature.StructureStartFactory getStartFactory() {
        return BastionFeature.FeatureStart::new;
    }

    @Override
    public String getFeatureName() {
        return "Bastion_Remnant";
    }

    @Override
    public int getLookupRange() {
        return 8;
    }

    public static class FeatureStart extends BeardedStructureStart {
        public FeatureStart(StructureFeature<?> param0, int param1, int param2, BoundingBox param3, int param4, long param5) {
            super(param0, param1, param2, param3, param4, param5);
        }

        @Override
        public void generatePieces(ChunkGenerator<?> param0, StructureManager param1, int param2, int param3, Biome param4) {
            MultiJigsawConfiguration var0 = param0.getStructureConfiguration(param4, Feature.BASTION_REMNANT);
            BlockPos var1 = new BlockPos(param2 * 16, 33, param3 * 16);
            BastionPieces.addPieces(param0, param1, var1, this.pieces, this.random, var0);
            this.calculateBoundingBox();
        }
    }
}
