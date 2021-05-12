package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.EndCityPieces;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class EndCityFeature extends StructureFeature<NoneFeatureConfiguration> {
    private static final int RANDOM_SALT = 10387313;

    public EndCityFeature(Codec<NoneFeatureConfiguration> param0) {
        super(param0);
    }

    @Override
    protected boolean linearSeparation() {
        return false;
    }

    protected boolean isFeatureChunk(
        ChunkGenerator param0,
        BiomeSource param1,
        long param2,
        WorldgenRandom param3,
        ChunkPos param4,
        Biome param5,
        ChunkPos param6,
        NoneFeatureConfiguration param7,
        LevelHeightAccessor param8
    ) {
        return getYPositionForFeature(param4, param0, param8) >= 60;
    }

    @Override
    public StructureFeature.StructureStartFactory<NoneFeatureConfiguration> getStartFactory() {
        return EndCityFeature.EndCityStart::new;
    }

    static int getYPositionForFeature(ChunkPos param0, ChunkGenerator param1, LevelHeightAccessor param2) {
        Random var0 = new Random((long)(param0.x + param0.z * 10387313));
        Rotation var1 = Rotation.getRandom(var0);
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

        int var4 = param0.getBlockX(7);
        int var5 = param0.getBlockZ(7);
        int var6 = param1.getFirstOccupiedHeight(var4, var5, Heightmap.Types.WORLD_SURFACE_WG, param2);
        int var7 = param1.getFirstOccupiedHeight(var4, var5 + var3, Heightmap.Types.WORLD_SURFACE_WG, param2);
        int var8 = param1.getFirstOccupiedHeight(var4 + var2, var5, Heightmap.Types.WORLD_SURFACE_WG, param2);
        int var9 = param1.getFirstOccupiedHeight(var4 + var2, var5 + var3, Heightmap.Types.WORLD_SURFACE_WG, param2);
        return Math.min(Math.min(var6, var7), Math.min(var8, var9));
    }

    public static class EndCityStart extends StructureStart<NoneFeatureConfiguration> {
        public EndCityStart(StructureFeature<NoneFeatureConfiguration> param0, ChunkPos param1, int param2, long param3) {
            super(param0, param1, param2, param3);
        }

        public void generatePieces(
            RegistryAccess param0,
            ChunkGenerator param1,
            StructureManager param2,
            ChunkPos param3,
            Biome param4,
            NoneFeatureConfiguration param5,
            LevelHeightAccessor param6
        ) {
            Rotation var0 = Rotation.getRandom(this.random);
            int var1 = EndCityFeature.getYPositionForFeature(param3, param1, param6);
            if (var1 >= 60) {
                BlockPos var2 = param3.getMiddleBlockPosition(var1);
                List<StructurePiece> var3 = Lists.newArrayList();
                EndCityPieces.startHouseTower(param2, var2, var0, var3, this.random);
                var3.forEach(this::addPiece);
            }
        }
    }
}
