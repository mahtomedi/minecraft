package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class BirchFeature extends AbstractTreeFeature<NoneFeatureConfiguration> {
    private static final BlockState LOG = Blocks.BIRCH_LOG.defaultBlockState();
    private static final BlockState LEAF = Blocks.BIRCH_LEAVES.defaultBlockState();
    private final boolean superBirch;

    public BirchFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> param0, boolean param1, boolean param2) {
        super(param0, param1);
        this.superBirch = param2;
    }

    @Override
    public boolean doPlace(Set<BlockPos> param0, LevelSimulatedRW param1, Random param2, BlockPos param3, BoundingBox param4) {
        int var0 = param2.nextInt(3) + 5;
        if (this.superBirch) {
            var0 += param2.nextInt(7);
        }

        boolean var1 = true;
        if (param3.getY() >= 1 && param3.getY() + var0 + 1 <= 256) {
            for(int var2 = param3.getY(); var2 <= param3.getY() + 1 + var0; ++var2) {
                int var3 = 1;
                if (var2 == param3.getY()) {
                    var3 = 0;
                }

                if (var2 >= param3.getY() + 1 + var0 - 2) {
                    var3 = 2;
                }

                BlockPos.MutableBlockPos var4 = new BlockPos.MutableBlockPos();

                for(int var5 = param3.getX() - var3; var5 <= param3.getX() + var3 && var1; ++var5) {
                    for(int var6 = param3.getZ() - var3; var6 <= param3.getZ() + var3 && var1; ++var6) {
                        if (var2 < 0 || var2 >= 256) {
                            var1 = false;
                        } else if (!isFree(param1, var4.set(var5, var2, var6))) {
                            var1 = false;
                        }
                    }
                }
            }

            if (!var1) {
                return false;
            } else if (isGrassOrDirtOrFarmland(param1, param3.below()) && param3.getY() < 256 - var0 - 1) {
                this.setDirtAt(param1, param3.below());

                for(int var7 = param3.getY() - 3 + var0; var7 <= param3.getY() + var0; ++var7) {
                    int var8 = var7 - (param3.getY() + var0);
                    int var9 = 1 - var8 / 2;

                    for(int var10 = param3.getX() - var9; var10 <= param3.getX() + var9; ++var10) {
                        int var11 = var10 - param3.getX();

                        for(int var12 = param3.getZ() - var9; var12 <= param3.getZ() + var9; ++var12) {
                            int var13 = var12 - param3.getZ();
                            if (Math.abs(var11) != var9 || Math.abs(var13) != var9 || param2.nextInt(2) != 0 && var8 != 0) {
                                BlockPos var14 = new BlockPos(var10, var7, var12);
                                if (isAirOrLeaves(param1, var14)) {
                                    this.setBlock(param0, param1, var14, LEAF, param4);
                                }
                            }
                        }
                    }
                }

                for(int var15 = 0; var15 < var0; ++var15) {
                    if (isAirOrLeaves(param1, param3.above(var15))) {
                        this.setBlock(param0, param1, param3.above(var15), LOG, param4);
                    }
                }

                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
