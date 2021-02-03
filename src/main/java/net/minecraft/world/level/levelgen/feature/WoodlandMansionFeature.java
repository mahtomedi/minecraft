package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
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
import net.minecraft.world.level.levelgen.structure.StructurePiece;
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
        int param4,
        int param5,
        Biome param6,
        ChunkPos param7,
        NoneFeatureConfiguration param8,
        LevelHeightAccessor param9
    ) {
        for(Biome var1 : param1.getBiomesWithin(SectionPos.sectionToBlockCoord(param4, 9), param0.getSeaLevel(), SectionPos.sectionToBlockCoord(param5, 9), 32)) {
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
        public WoodlandMansionStart(StructureFeature<NoneFeatureConfiguration> param0, int param1, int param2, BoundingBox param3, int param4, long param5) {
            super(param0, param1, param2, param3, param4, param5);
        }

        public void generatePieces(
            RegistryAccess param0,
            ChunkGenerator param1,
            StructureManager param2,
            int param3,
            int param4,
            Biome param5,
            NoneFeatureConfiguration param6,
            LevelHeightAccessor param7
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

            int var3 = SectionPos.sectionToBlockCoord(param3, 7);
            int var4 = SectionPos.sectionToBlockCoord(param4, 7);
            int var5 = param1.getFirstOccupiedHeight(var3, var4, Heightmap.Types.WORLD_SURFACE_WG, param7);
            int var6 = param1.getFirstOccupiedHeight(var3, var4 + var2, Heightmap.Types.WORLD_SURFACE_WG, param7);
            int var7 = param1.getFirstOccupiedHeight(var3 + var1, var4, Heightmap.Types.WORLD_SURFACE_WG, param7);
            int var8 = param1.getFirstOccupiedHeight(var3 + var1, var4 + var2, Heightmap.Types.WORLD_SURFACE_WG, param7);
            int var9 = Math.min(Math.min(var5, var6), Math.min(var7, var8));
            if (var9 >= 60) {
                BlockPos var10 = new BlockPos(SectionPos.sectionToBlockCoord(param3, 8), var9 + 1, SectionPos.sectionToBlockCoord(param4, 8));
                List<WoodlandMansionPieces.WoodlandMansionPiece> var11 = Lists.newLinkedList();
                WoodlandMansionPieces.generateMansion(param2, var10, var0, var11, this.random);
                this.pieces.addAll(var11);
                this.calculateBoundingBox();
            }
        }

        @Override
        public void placeInChunk(
            WorldGenLevel param0, StructureFeatureManager param1, ChunkGenerator param2, Random param3, BoundingBox param4, ChunkPos param5
        ) {
            super.placeInChunk(param0, param1, param2, param3, param4, param5);
            int var0 = this.boundingBox.y0;

            for(int var1 = param4.x0; var1 <= param4.x1; ++var1) {
                for(int var2 = param4.z0; var2 <= param4.z1; ++var2) {
                    BlockPos var3 = new BlockPos(var1, var0, var2);
                    if (!param0.isEmptyBlock(var3) && this.boundingBox.isInside(var3)) {
                        boolean var4 = false;

                        for(StructurePiece var5 : this.pieces) {
                            if (var5.getBoundingBox().isInside(var3)) {
                                var4 = true;
                                break;
                            }
                        }

                        if (var4) {
                            for(int var6 = var0 - 1; var6 > 1; --var6) {
                                BlockPos var7 = new BlockPos(var1, var6, var2);
                                if (!param0.isEmptyBlock(var7) && !param0.getBlockState(var7).getMaterial().isLiquid()) {
                                    break;
                                }

                                param0.setBlock(var7, Blocks.COBBLESTONE.defaultBlockState(), 2);
                            }
                        }
                    }
                }
            }

        }
    }
}
