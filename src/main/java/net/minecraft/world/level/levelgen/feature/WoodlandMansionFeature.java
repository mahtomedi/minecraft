package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
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
            NoneFeatureConfiguration param4,
            LevelHeightAccessor param5,
            Predicate<Biome> param6
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
            int[] var5 = StructureFeature.getCornerHeights(param1, var3, var1, var4, var2, param5);
            int var6 = Math.min(Math.min(var5[0], var5[1]), Math.min(var5[2], var5[3]));
            if (var6 >= 60) {
                if (param6.test(param1.getNoiseBiome(QuartPos.fromBlock(var3), QuartPos.fromBlock(var5[0]), QuartPos.fromBlock(var4)))) {
                    BlockPos var7 = new BlockPos(param3.getMiddleBlockX(), var6 + 1, param3.getMiddleBlockZ());
                    List<WoodlandMansionPieces.WoodlandMansionPiece> var8 = Lists.newLinkedList();
                    WoodlandMansionPieces.generateMansion(param2, var7, var0, var8, this.random);
                    var8.forEach(this::addPiece);
                }
            }
        }

        @Override
        public void placeInChunk(
            WorldGenLevel param0,
            StructureFeatureManager param1,
            ChunkGenerator param2,
            Random param3,
            Predicate<Biome> param4,
            BoundingBox param5,
            ChunkPos param6
        ) {
            super.placeInChunk(param0, param1, param2, param3, param4, param5, param6);
            BoundingBox var0 = this.getBoundingBox();
            int var1 = var0.minY();

            for(int var2 = param5.minX(); var2 <= param5.maxX(); ++var2) {
                for(int var3 = param5.minZ(); var3 <= param5.maxZ(); ++var3) {
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
