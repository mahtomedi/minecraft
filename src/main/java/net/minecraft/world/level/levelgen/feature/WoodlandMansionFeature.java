package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.mojang.datafixers.Dynamic;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
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
    protected ChunkPos getPotentialFeatureChunkFromLocationWithOffset(ChunkGenerator<?> param0, Random param1, int param2, int param3, int param4, int param5) {
        int var0 = param0.getSettings().getWoodlandMansionSpacing();
        int var1 = param0.getSettings().getWoodlandMangionSeparation();
        int var2 = param2 + var0 * param4;
        int var3 = param3 + var0 * param5;
        int var4 = var2 < 0 ? var2 - var0 + 1 : var2;
        int var5 = var3 < 0 ? var3 - var0 + 1 : var3;
        int var6 = var4 / var0;
        int var7 = var5 / var0;
        ((WorldgenRandom)param1).setLargeFeatureWithSalt(param0.getSeed(), var6, var7, 10387319);
        var6 *= var0;
        var7 *= var0;
        var6 += (param1.nextInt(var0 - var1) + param1.nextInt(var0 - var1)) / 2;
        var7 += (param1.nextInt(var0 - var1) + param1.nextInt(var0 - var1)) / 2;
        return new ChunkPos(var6, var7);
    }

    @Override
    public boolean isFeatureChunk(BiomeManager param0, ChunkGenerator<?> param1, Random param2, int param3, int param4, Biome param5) {
        ChunkPos var0 = this.getPotentialFeatureChunkFromLocationWithOffset(param1, param2, param3, param4, 0, 0);
        if (param3 == var0.x && param4 == var0.z) {
            for(Biome var2 : param1.getBiomeSource().getBiomesWithin(param3 * 16 + 9, param1.getSeaLevel(), param4 * 16 + 9, 32)) {
                if (!param1.isBiomeValidStartForStructure(var2, Feature.WOODLAND_MANSION)) {
                    return false;
                }
            }

            return true;
        } else {
            return false;
        }
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
            Rotation var0 = Rotation.values()[this.random.nextInt(Rotation.values().length)];
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
        public void postProcess(LevelAccessor param0, ChunkGenerator<?> param1, Random param2, BoundingBox param3, ChunkPos param4) {
            super.postProcess(param0, param1, param2, param3, param4);
            int var0 = this.boundingBox.y0;

            for(int var1 = param3.x0; var1 <= param3.x1; ++var1) {
                for(int var2 = param3.z0; var2 <= param3.z1; ++var2) {
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
