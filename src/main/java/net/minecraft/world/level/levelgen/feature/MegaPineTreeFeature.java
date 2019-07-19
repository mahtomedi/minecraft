package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class MegaPineTreeFeature extends MegaTreeFeature<NoneFeatureConfiguration> {
    private static final BlockState TRUNK = Blocks.SPRUCE_LOG.defaultBlockState();
    private static final BlockState LEAF = Blocks.SPRUCE_LEAVES.defaultBlockState();
    private static final BlockState PODZOL = Blocks.PODZOL.defaultBlockState();
    private final boolean isSpruce;

    public MegaPineTreeFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> param0, boolean param1, boolean param2) {
        super(param0, param1, 13, 15, TRUNK, LEAF);
        this.isSpruce = param2;
    }

    @Override
    public boolean doPlace(Set<BlockPos> param0, LevelSimulatedRW param1, Random param2, BlockPos param3, BoundingBox param4) {
        int var0 = this.calcTreeHeigth(param2);
        if (!this.prepareTree(param1, param3, var0)) {
            return false;
        } else {
            this.createCrown(param1, param3.getX(), param3.getZ(), param3.getY() + var0, 0, param2, param4, param0);

            for(int var1 = 0; var1 < var0; ++var1) {
                if (isAirOrLeaves(param1, param3.above(var1))) {
                    this.setBlock(param0, param1, param3.above(var1), this.trunk, param4);
                }

                if (var1 < var0 - 1) {
                    if (isAirOrLeaves(param1, param3.offset(1, var1, 0))) {
                        this.setBlock(param0, param1, param3.offset(1, var1, 0), this.trunk, param4);
                    }

                    if (isAirOrLeaves(param1, param3.offset(1, var1, 1))) {
                        this.setBlock(param0, param1, param3.offset(1, var1, 1), this.trunk, param4);
                    }

                    if (isAirOrLeaves(param1, param3.offset(0, var1, 1))) {
                        this.setBlock(param0, param1, param3.offset(0, var1, 1), this.trunk, param4);
                    }
                }
            }

            this.postPlaceTree(param1, param2, param3);
            return true;
        }
    }

    private void createCrown(LevelSimulatedRW param0, int param1, int param2, int param3, int param4, Random param5, BoundingBox param6, Set<BlockPos> param7) {
        int var0 = param5.nextInt(5) + (this.isSpruce ? this.baseHeight : 3);
        int var1 = 0;

        for(int var2 = param3 - var0; var2 <= param3; ++var2) {
            int var3 = param3 - var2;
            int var4 = param4 + Mth.floor((float)var3 / (float)var0 * 3.5F);
            this.placeDoubleTrunkLeaves(
                param0, new BlockPos(param1, var2, param2), var4 + (var3 > 0 && var4 == var1 && (var2 & 1) == 0 ? 1 : 0), param6, param7
            );
            var1 = var4;
        }

    }

    public void postPlaceTree(LevelSimulatedRW param0, Random param1, BlockPos param2) {
        this.placePodzolCircle(param0, param2.west().north());
        this.placePodzolCircle(param0, param2.east(2).north());
        this.placePodzolCircle(param0, param2.west().south(2));
        this.placePodzolCircle(param0, param2.east(2).south(2));

        for(int var0 = 0; var0 < 5; ++var0) {
            int var1 = param1.nextInt(64);
            int var2 = var1 % 8;
            int var3 = var1 / 8;
            if (var2 == 0 || var2 == 7 || var3 == 0 || var3 == 7) {
                this.placePodzolCircle(param0, param2.offset(-3 + var2, 0, -3 + var3));
            }
        }

    }

    private void placePodzolCircle(LevelSimulatedRW param0, BlockPos param1) {
        for(int var0 = -2; var0 <= 2; ++var0) {
            for(int var1 = -2; var1 <= 2; ++var1) {
                if (Math.abs(var0) != 2 || Math.abs(var1) != 2) {
                    this.placePodzolAt(param0, param1.offset(var0, 0, var1));
                }
            }
        }

    }

    private void placePodzolAt(LevelSimulatedRW param0, BlockPos param1) {
        for(int var0 = 2; var0 >= -3; --var0) {
            BlockPos var1 = param1.above(var0);
            if (isGrassOrDirt(param0, var1)) {
                this.setBlock(param0, var1, PODZOL);
                break;
            }

            if (!isAir(param0, var1) && var0 < 0) {
                break;
            }
        }

    }
}
