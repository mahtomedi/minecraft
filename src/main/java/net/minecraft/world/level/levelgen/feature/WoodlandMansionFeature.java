package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.mojang.datafixers.Dynamic;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.WoodlandMansionPieces;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class WoodlandMansionFeature extends StructureFeature<NoneFeatureConfiguration> {
    public WoodlandMansionFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> param0) {
        super(param0);
    }

    @Override
    protected int getSpacing(DimensionType param0, ChunkGeneratorSettings param1) {
        return param1.getWoodlandMansionSpacing();
    }

    @Override
    protected int getSeparation(DimensionType param0, ChunkGeneratorSettings param1) {
        return param1.getWoodlandMansionSeparation();
    }

    @Override
    protected int getRandomSalt(ChunkGeneratorSettings param0) {
        return 10387319;
    }

    @Override
    protected boolean linearSeparation() {
        return false;
    }

    @Override
    protected boolean isFeatureChunk(
        BiomeManager param0, ChunkGenerator<?> param1, WorldgenRandom param2, int param3, int param4, Biome param5, ChunkPos param6
    ) {
        for(Biome var1 : param1.getBiomeSource().getBiomesWithin(param3 * 16 + 9, param1.getSeaLevel(), param4 * 16 + 9, 32)) {
            if (!param1.isBiomeValidStartForStructure(var1, this)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public StructureFeature.StructureStartFactory getStartFactory() {
        return WoodlandMansionFeature.WoodlandMansionStart::new;
    }

    @Override
    public String getFeatureName() {
        return "Mansion";
    }

    @Override
    public int getLookupRange() {
        return 8;
    }

    public static class WoodlandMansionStart extends StructureStart {
        public WoodlandMansionStart(StructureFeature<?> param0, int param1, int param2, BoundingBox param3, int param4, long param5) {
            super(param0, param1, param2, param3, param4, param5);
        }

        @Override
        public void generatePieces(ChunkGenerator<?> param0, StructureManager param1, int param2, int param3, Biome param4) {
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

            int var3 = (param2 << 4) + 7;
            int var4 = (param3 << 4) + 7;
            int var5 = param0.getFirstOccupiedHeight(var3, var4, Heightmap.Types.WORLD_SURFACE_WG);
            int var6 = param0.getFirstOccupiedHeight(var3, var4 + var2, Heightmap.Types.WORLD_SURFACE_WG);
            int var7 = param0.getFirstOccupiedHeight(var3 + var1, var4, Heightmap.Types.WORLD_SURFACE_WG);
            int var8 = param0.getFirstOccupiedHeight(var3 + var1, var4 + var2, Heightmap.Types.WORLD_SURFACE_WG);
            int var9 = Math.min(Math.min(var5, var6), Math.min(var7, var8));
            if (var9 >= 60) {
                BlockPos var10 = new BlockPos(param2 * 16 + 8, var9 + 1, param3 * 16 + 8);
                List<WoodlandMansionPieces.WoodlandMansionPiece> var11 = Lists.newLinkedList();
                WoodlandMansionPieces.generateMansion(param1, var10, var0, var11, this.random);
                this.pieces.addAll(var11);
                this.calculateBoundingBox();
            }
        }

        @Override
        public void postProcess(
            LevelAccessor param0, StructureFeatureManager param1, ChunkGenerator<?> param2, Random param3, BoundingBox param4, ChunkPos param5
        ) {
            super.postProcess(param0, param1, param2, param3, param4, param5);
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
