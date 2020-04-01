package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.VillageConfiguration;
import net.minecraft.world.level.levelgen.structure.BeardedStructureStart;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class VillageFeature extends StructureFeature<VillageConfiguration> {
    public VillageFeature(Function<Dynamic<?>, ? extends VillageConfiguration> param0, Function<Random, ? extends VillageConfiguration> param1) {
        super(param0, param1);
    }

    @Override
    protected ChunkPos getPotentialFeatureChunkFromLocationWithOffset(ChunkGenerator<?> param0, Random param1, int param2, int param3, int param4, int param5) {
        int var0 = param0.getSettings().getVillagesSpacing();
        int var1 = param0.getSettings().getVillagesSeparation();
        int var2 = param2 + var0 * param4;
        int var3 = param3 + var0 * param5;
        int var4 = var2 < 0 ? var2 - var0 + 1 : var2;
        int var5 = var3 < 0 ? var3 - var0 + 1 : var3;
        int var6 = var4 / var0;
        int var7 = var5 / var0;
        ((WorldgenRandom)param1).setLargeFeatureWithSalt(param0.getSeed(), var6, var7, 10387312);
        var6 *= var0;
        var7 *= var0;
        var6 += param1.nextInt(var0 - var1);
        var7 += param1.nextInt(var0 - var1);
        return new ChunkPos(var6, var7);
    }

    @Override
    public boolean isFeatureChunk(BiomeManager param0, ChunkGenerator<?> param1, Random param2, int param3, int param4, Biome param5) {
        ChunkPos var0 = this.getPotentialFeatureChunkFromLocationWithOffset(param1, param2, param3, param4, 0, 0);
        return param3 == var0.x && param4 == var0.z ? param1.isBiomeValidStartForStructure(param5, this) : false;
    }

    @Override
    public StructureFeature.StructureStartFactory getStartFactory() {
        return VillageFeature.FeatureStart::new;
    }

    @Override
    public String getFeatureName() {
        return "Village";
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
            VillageConfiguration var0 = param0.getStructureConfiguration(param4, Feature.VILLAGE);
            BlockPos var1 = new BlockPos(param2 * 16, 0, param3 * 16);
            VillagePieces.addPieces(param0, param1, var1, this.pieces, this.random, var0);
            this.calculateBoundingBox();
        }
    }
}
