package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.BuriedTreasureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.BuriedTreasurePieces;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class BuriedTreasureFeature extends StructureFeature<BuriedTreasureConfiguration> {
    public BuriedTreasureFeature(
        Function<Dynamic<?>, ? extends BuriedTreasureConfiguration> param0, Function<Random, ? extends BuriedTreasureConfiguration> param1
    ) {
        super(param0, param1);
    }

    @Override
    public boolean isFeatureChunk(BiomeManager param0, ChunkGenerator<?> param1, Random param2, int param3, int param4, Biome param5) {
        if (param1.isBiomeValidStartForStructure(param5, this)) {
            ((WorldgenRandom)param2).setLargeFeatureWithSalt(param1.getSeed(), param3, param4, 10387320);
            BuriedTreasureConfiguration var0 = param1.getStructureConfiguration(param5, this);
            return param2.nextFloat() < var0.probability;
        } else {
            return false;
        }
    }

    @Override
    public StructureFeature.StructureStartFactory getStartFactory() {
        return BuriedTreasureFeature.BuriedTreasureStart::new;
    }

    @Override
    public String getFeatureName() {
        return "Buried_Treasure";
    }

    @Override
    public int getLookupRange() {
        return 1;
    }

    public static class BuriedTreasureStart extends StructureStart {
        public BuriedTreasureStart(StructureFeature<?> param0, int param1, int param2, BoundingBox param3, int param4, long param5) {
            super(param0, param1, param2, param3, param4, param5);
        }

        @Override
        public void generatePieces(ChunkGenerator<?> param0, StructureManager param1, int param2, int param3, Biome param4) {
            int var0 = param2 * 16;
            int var1 = param3 * 16;
            BlockPos var2 = new BlockPos(var0 + 9, 90, var1 + 9);
            this.pieces.add(new BuriedTreasurePieces.BuriedTreasurePiece(var2));
            this.calculateBoundingBox();
        }

        @Override
        public BlockPos getLocatePos() {
            return new BlockPos((this.getChunkX() << 4) + 9, 0, (this.getChunkZ() << 4) + 9);
        }
    }
}
