package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class SwampTreeFeature extends AbstractTreeFeature<NoneFeatureConfiguration> {
    private static final BlockState TRUNK = Blocks.OAK_LOG.defaultBlockState();
    private static final BlockState LEAF = Blocks.OAK_LEAVES.defaultBlockState();

    public SwampTreeFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> param0) {
        super(param0, false);
    }

    @Override
    public boolean doPlace(Set<BlockPos> param0, LevelSimulatedRW param1, Random param2, BlockPos param3, BoundingBox param4) {
        int var0 = param2.nextInt(4) + 5;
        param3 = param1.getHeightmapPos(Heightmap.Types.OCEAN_FLOOR, param3);
        boolean var1 = true;
        if (param3.getY() >= 1 && param3.getY() + var0 + 1 <= 256) {
            for(int var2 = param3.getY(); var2 <= param3.getY() + 1 + var0; ++var2) {
                int var3 = 1;
                if (var2 == param3.getY()) {
                    var3 = 0;
                }

                if (var2 >= param3.getY() + 1 + var0 - 2) {
                    var3 = 3;
                }

                BlockPos.MutableBlockPos var4 = new BlockPos.MutableBlockPos();

                for(int var5 = param3.getX() - var3; var5 <= param3.getX() + var3 && var1; ++var5) {
                    for(int var6 = param3.getZ() - var3; var6 <= param3.getZ() + var3 && var1; ++var6) {
                        if (var2 >= 0 && var2 < 256) {
                            var4.set(var5, var2, var6);
                            if (!isAirOrLeaves(param1, var4)) {
                                if (isBlockWater(param1, var4)) {
                                    if (var2 > param3.getY()) {
                                        var1 = false;
                                    }
                                } else {
                                    var1 = false;
                                }
                            }
                        } else {
                            var1 = false;
                        }
                    }
                }
            }

            if (!var1) {
                return false;
            } else if (isGrassOrDirt(param1, param3.below()) && param3.getY() < 256 - var0 - 1) {
                this.setDirtAt(param1, param3.below());

                for(int var7 = param3.getY() - 3 + var0; var7 <= param3.getY() + var0; ++var7) {
                    int var8 = var7 - (param3.getY() + var0);
                    int var9 = 2 - var8 / 2;

                    for(int var10 = param3.getX() - var9; var10 <= param3.getX() + var9; ++var10) {
                        int var11 = var10 - param3.getX();

                        for(int var12 = param3.getZ() - var9; var12 <= param3.getZ() + var9; ++var12) {
                            int var13 = var12 - param3.getZ();
                            if (Math.abs(var11) != var9 || Math.abs(var13) != var9 || param2.nextInt(2) != 0 && var8 != 0) {
                                BlockPos var14 = new BlockPos(var10, var7, var12);
                                if (isAirOrLeaves(param1, var14) || isReplaceablePlant(param1, var14)) {
                                    this.setBlock(param0, param1, var14, LEAF, param4);
                                }
                            }
                        }
                    }
                }

                for(int var15 = 0; var15 < var0; ++var15) {
                    BlockPos var16 = param3.above(var15);
                    if (isAirOrLeaves(param1, var16) || isBlockWater(param1, var16)) {
                        this.setBlock(param0, param1, var16, TRUNK, param4);
                    }
                }

                for(int var17 = param3.getY() - 3 + var0; var17 <= param3.getY() + var0; ++var17) {
                    int var18 = var17 - (param3.getY() + var0);
                    int var19 = 2 - var18 / 2;
                    BlockPos.MutableBlockPos var20 = new BlockPos.MutableBlockPos();

                    for(int var21 = param3.getX() - var19; var21 <= param3.getX() + var19; ++var21) {
                        for(int var22 = param3.getZ() - var19; var22 <= param3.getZ() + var19; ++var22) {
                            var20.set(var21, var17, var22);
                            if (isLeaves(param1, var20)) {
                                BlockPos var23 = var20.west();
                                BlockPos var24 = var20.east();
                                BlockPos var25 = var20.north();
                                BlockPos var26 = var20.south();
                                if (param2.nextInt(4) == 0 && isAir(param1, var23)) {
                                    this.addVine(param1, var23, VineBlock.EAST);
                                }

                                if (param2.nextInt(4) == 0 && isAir(param1, var24)) {
                                    this.addVine(param1, var24, VineBlock.WEST);
                                }

                                if (param2.nextInt(4) == 0 && isAir(param1, var25)) {
                                    this.addVine(param1, var25, VineBlock.SOUTH);
                                }

                                if (param2.nextInt(4) == 0 && isAir(param1, var26)) {
                                    this.addVine(param1, var26, VineBlock.NORTH);
                                }
                            }
                        }
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

    private void addVine(LevelSimulatedRW param0, BlockPos param1, BooleanProperty param2) {
        BlockState var0 = Blocks.VINE.defaultBlockState().setValue(param2, Boolean.valueOf(true));
        this.setBlock(param0, param1, var0);
        int var1 = 4;

        for(BlockPos var6 = param1.below(); isAir(param0, var6) && var1 > 0; --var1) {
            this.setBlock(param0, var6, var0);
            var6 = var6.below();
        }

    }
}
