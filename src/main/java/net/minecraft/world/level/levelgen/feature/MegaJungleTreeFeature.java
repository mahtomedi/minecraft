package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class MegaJungleTreeFeature extends MegaTreeFeature<NoneFeatureConfiguration> {
    public MegaJungleTreeFeature(
        Function<Dynamic<?>, ? extends NoneFeatureConfiguration> param0, boolean param1, int param2, int param3, BlockState param4, BlockState param5
    ) {
        super(param0, param1, param2, param3, param4, param5);
    }

    @Override
    public boolean doPlace(Set<BlockPos> param0, LevelSimulatedRW param1, Random param2, BlockPos param3, BoundingBox param4) {
        int var0 = this.calcTreeHeigth(param2);
        if (!this.prepareTree(param1, param3, var0)) {
            return false;
        } else {
            this.createCrown(param1, param3.above(var0), 2, param4, param0);

            for(int var1 = param3.getY() + var0 - 2 - param2.nextInt(4); var1 > param3.getY() + var0 / 2; var1 -= 2 + param2.nextInt(4)) {
                float var2 = param2.nextFloat() * (float) (Math.PI * 2);
                int var3 = param3.getX() + (int)(0.5F + Mth.cos(var2) * 4.0F);
                int var4 = param3.getZ() + (int)(0.5F + Mth.sin(var2) * 4.0F);

                for(int var5 = 0; var5 < 5; ++var5) {
                    var3 = param3.getX() + (int)(1.5F + Mth.cos(var2) * (float)var5);
                    var4 = param3.getZ() + (int)(1.5F + Mth.sin(var2) * (float)var5);
                    this.setBlock(param0, param1, new BlockPos(var3, var1 - 3 + var5 / 2, var4), this.trunk, param4);
                }

                int var6 = 1 + param2.nextInt(2);
                int var7 = var1;

                for(int var8 = var1 - var6; var8 <= var7; ++var8) {
                    int var9 = var8 - var7;
                    this.placeSingleTrunkLeaves(param1, new BlockPos(var3, var8, var4), 1 - var9, param4, param0);
                }
            }

            for(int var10 = 0; var10 < var0; ++var10) {
                BlockPos var11 = param3.above(var10);
                if (isFree(param1, var11)) {
                    this.setBlock(param0, param1, var11, this.trunk, param4);
                    if (var10 > 0) {
                        this.placeVine(param1, param2, var11.west(), VineBlock.EAST);
                        this.placeVine(param1, param2, var11.north(), VineBlock.SOUTH);
                    }
                }

                if (var10 < var0 - 1) {
                    BlockPos var12 = var11.east();
                    if (isFree(param1, var12)) {
                        this.setBlock(param0, param1, var12, this.trunk, param4);
                        if (var10 > 0) {
                            this.placeVine(param1, param2, var12.east(), VineBlock.WEST);
                            this.placeVine(param1, param2, var12.north(), VineBlock.SOUTH);
                        }
                    }

                    BlockPos var13 = var11.south().east();
                    if (isFree(param1, var13)) {
                        this.setBlock(param0, param1, var13, this.trunk, param4);
                        if (var10 > 0) {
                            this.placeVine(param1, param2, var13.east(), VineBlock.WEST);
                            this.placeVine(param1, param2, var13.south(), VineBlock.NORTH);
                        }
                    }

                    BlockPos var14 = var11.south();
                    if (isFree(param1, var14)) {
                        this.setBlock(param0, param1, var14, this.trunk, param4);
                        if (var10 > 0) {
                            this.placeVine(param1, param2, var14.west(), VineBlock.EAST);
                            this.placeVine(param1, param2, var14.south(), VineBlock.NORTH);
                        }
                    }
                }
            }

            return true;
        }
    }

    private void placeVine(LevelSimulatedRW param0, Random param1, BlockPos param2, BooleanProperty param3) {
        if (param1.nextInt(3) > 0 && isAir(param0, param2)) {
            this.setBlock(param0, param2, Blocks.VINE.defaultBlockState().setValue(param3, Boolean.valueOf(true)));
        }

    }

    private void createCrown(LevelSimulatedRW param0, BlockPos param1, int param2, BoundingBox param3, Set<BlockPos> param4) {
        int var0 = 2;

        for(int var1 = -2; var1 <= 0; ++var1) {
            this.placeDoubleTrunkLeaves(param0, param1.above(var1), param2 + 1 - var1, param3, param4);
        }

    }
}
