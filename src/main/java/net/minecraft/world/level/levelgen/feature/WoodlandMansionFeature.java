package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.WoodlandMansionPieces;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class WoodlandMansionFeature extends StructureFeature<NoneFeatureConfiguration> {
    public WoodlandMansionFeature(Codec<NoneFeatureConfiguration> param0) {
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
        for(Biome var1 : param1.getBiomesWithin(param4.getBlockX(9), param0.getSeaLevel(), param4.getBlockZ(9), 32)) {
            if (!var1.getGenerationSettings().isValidStart(this)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public StructureFeature.StructureStartFactory<NoneFeatureConfiguration> getStartFactory() {
        return WoodlandMansionFeature.WoodlandMansionStart::new;
    }

    public static class WoodlandMansionStart extends StructureStart<NoneFeatureConfiguration> {
        public WoodlandMansionStart(StructureFeature<NoneFeatureConfiguration> param0, ChunkPos param1, int param2, long param3) {
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
            int var1 = 5;
            int var2 = 5;
            if (var0 == Rotation.CLOCKWISE_90) {
                var1 = -5;
            } else if (var0 == Rotation.CLOCKWISE_180) {
                var1 = -5;
                var2 = -5;
            } else if (var0 == Rotation.COUNTERCLOCKWISE_90) {
                var2 = -5;
            }

            int var3 = param3.getBlockX(7);
            int var4 = param3.getBlockZ(7);
            int var5 = param1.getFirstOccupiedHeight(var3, var4, Heightmap.Types.WORLD_SURFACE_WG, param6);
            int var6 = param1.getFirstOccupiedHeight(var3, var4 + var2, Heightmap.Types.WORLD_SURFACE_WG, param6);
            int var7 = param1.getFirstOccupiedHeight(var3 + var1, var4, Heightmap.Types.WORLD_SURFACE_WG, param6);
            int var8 = param1.getFirstOccupiedHeight(var3 + var1, var4 + var2, Heightmap.Types.WORLD_SURFACE_WG, param6);
            int var9 = Math.min(Math.min(var5, var6), Math.min(var7, var8));
            if (var9 >= 60) {
                BlockPos var10 = new BlockPos(param3.getBlockX(8), var9 + 1, param3.getBlockZ(8));
                List<WoodlandMansionPieces.WoodlandMansionPiece> var11 = Lists.newLinkedList();
                WoodlandMansionPieces.generateMansion(param2, var10, var0, var11, this.random);
                var11.forEach(this::addPiece);
            }
        }

        @Override
        public void placeInChunk(
            WorldGenLevel param0, StructureFeatureManager param1, ChunkGenerator param2, Random param3, BoundingBox param4, ChunkPos param5
        ) {
            super.placeInChunk(param0, param1, param2, param3, param4, param5);
            BoundingBox var0 = this.getBoundingBox();
            int var1 = var0.minY();

            for(int var2 = param4.minX(); var2 <= param4.maxX(); ++var2) {
                for(int var3 = param4.minZ(); var3 <= param4.maxZ(); ++var3) {
                    BlockPos var4 = new BlockPos(var2, var1, var3);
                    if (!param0.isEmptyBlock(var4) && var0.isInside(var4) && this.isInsidePiece(var4)) {
                        for(int var5 = var1 - 1; var5 > 1; --var5) {
                            BlockPos var6 = new BlockPos(var2, var5, var3);
                            if (!param0.isEmptyBlock(var6) && !param0.getBlockState(var6).getMaterial().isLiquid()) {
                                break;
                            }

                            param0.setBlock(var6, Blocks.COBBLESTONE.defaultBlockState(), 2);
                        }
                    }
                }
            }

        }
    }
}
