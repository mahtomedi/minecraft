package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.EndCityPieces;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class EndCityFeature extends StructureFeature<NoneFeatureConfiguration> {
    public EndCityFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> param0) {
        super(param0);
    }

    @Override
    protected ChunkPos getPotentialFeatureChunkFromLocationWithOffset(ChunkGenerator<?> param0, Random param1, int param2, int param3, int param4, int param5) {
        int var0 = param0.getSettings().getEndCitySpacing();
        int var1 = param0.getSettings().getEndCitySeparation();
        int var2 = param2 + var0 * param4;
        int var3 = param3 + var0 * param5;
        int var4 = var2 < 0 ? var2 - var0 + 1 : var2;
        int var5 = var3 < 0 ? var3 - var0 + 1 : var3;
        int var6 = var4 / var0;
        int var7 = var5 / var0;
        ((WorldgenRandom)param1).setLargeFeatureWithSalt(param0.getSeed(), var6, var7, 10387313);
        var6 *= var0;
        var7 *= var0;
        var6 += (param1.nextInt(var0 - var1) + param1.nextInt(var0 - var1)) / 2;
        var7 += (param1.nextInt(var0 - var1) + param1.nextInt(var0 - var1)) / 2;
        return new ChunkPos(var6, var7);
    }

    @Override
    public boolean isFeatureChunk(BiomeManager param0, ChunkGenerator<?> param1, Random param2, int param3, int param4, Biome param5) {
        ChunkPos var0 = this.getPotentialFeatureChunkFromLocationWithOffset(param1, param2, param3, param4, 0, 0);
        if (param3 != var0.x || param4 != var0.z) {
            return false;
        } else if (!param1.isBiomeValidStartForStructure(param5, this)) {
            return false;
        } else {
            int var1 = getYPositionForFeature(param3, param4, param1);
            return var1 >= 60;
        }
    }

    @Override
    public StructureFeature.StructureStartFactory getStartFactory() {
        return EndCityFeature.EndCityStart::new;
    }

    @Override
    public String getFeatureName() {
        return "EndCity";
    }

    @Override
    public int getLookupRange() {
        return 8;
    }

    private static int getYPositionForFeature(int param0, int param1, ChunkGenerator<?> param2) {
        Random var0 = new Random((long)(param0 + param1 * 10387313));
        Rotation var1 = Rotation.values()[var0.nextInt(Rotation.values().length)];
        int var2 = 5;
        int var3 = 5;
        if (var1 == Rotation.CLOCKWISE_90) {
            var2 = -5;
        } else if (var1 == Rotation.CLOCKWISE_180) {
            var2 = -5;
            var3 = -5;
        } else if (var1 == Rotation.COUNTERCLOCKWISE_90) {
            var3 = -5;
        }

        int var4 = (param0 << 4) + 7;
        int var5 = (param1 << 4) + 7;
        int var6 = param2.getFirstOccupiedHeight(var4, var5, Heightmap.Types.WORLD_SURFACE_WG);
        int var7 = param2.getFirstOccupiedHeight(var4, var5 + var3, Heightmap.Types.WORLD_SURFACE_WG);
        int var8 = param2.getFirstOccupiedHeight(var4 + var2, var5, Heightmap.Types.WORLD_SURFACE_WG);
        int var9 = param2.getFirstOccupiedHeight(var4 + var2, var5 + var3, Heightmap.Types.WORLD_SURFACE_WG);
        return Math.min(Math.min(var6, var7), Math.min(var8, var9));
    }

    public static class EndCityStart extends StructureStart {
        public EndCityStart(StructureFeature<?> param0, int param1, int param2, BoundingBox param3, int param4, long param5) {
            super(param0, param1, param2, param3, param4, param5);
        }

        @Override
        public void generatePieces(ChunkGenerator<?> param0, StructureManager param1, int param2, int param3, Biome param4) {
            Rotation var0 = Rotation.values()[this.random.nextInt(Rotation.values().length)];
            int var1 = EndCityFeature.getYPositionForFeature(param2, param3, param0);
            if (var1 >= 60) {
                BlockPos var2 = new BlockPos(param2 * 16 + 8, var1, param3 * 16 + 8);
                EndCityPieces.startHouseTower(param1, var2, var0, this.pieces, this.random);
                this.calculateBoundingBox();
            }
        }
    }
}
