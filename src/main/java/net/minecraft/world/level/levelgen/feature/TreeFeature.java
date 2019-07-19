package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class TreeFeature extends AbstractTreeFeature<NoneFeatureConfiguration> {
    private static final BlockState DEFAULT_TRUNK = Blocks.OAK_LOG.defaultBlockState();
    private static final BlockState DEFAULT_LEAF = Blocks.OAK_LEAVES.defaultBlockState();
    protected final int baseHeight;
    private final boolean addJungleFeatures;
    private final BlockState trunk;
    private final BlockState leaf;

    public TreeFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> param0, boolean param1) {
        this(param0, param1, 4, DEFAULT_TRUNK, DEFAULT_LEAF, false);
    }

    public TreeFeature(
        Function<Dynamic<?>, ? extends NoneFeatureConfiguration> param0, boolean param1, int param2, BlockState param3, BlockState param4, boolean param5
    ) {
        super(param0, param1);
        this.baseHeight = param2;
        this.trunk = param3;
        this.leaf = param4;
        this.addJungleFeatures = param5;
    }

    @Override
    public boolean doPlace(Set<BlockPos> param0, LevelSimulatedRW param1, Random param2, BlockPos param3, BoundingBox param4) {
        int var0 = this.getTreeHeight(param2);
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
                int var7 = 3;
                int var8 = 0;

                for(int var9 = param3.getY() - 3 + var0; var9 <= param3.getY() + var0; ++var9) {
                    int var10 = var9 - (param3.getY() + var0);
                    int var11 = 1 - var10 / 2;

                    for(int var12 = param3.getX() - var11; var12 <= param3.getX() + var11; ++var12) {
                        int var13 = var12 - param3.getX();

                        for(int var14 = param3.getZ() - var11; var14 <= param3.getZ() + var11; ++var14) {
                            int var15 = var14 - param3.getZ();
                            if (Math.abs(var13) != var11 || Math.abs(var15) != var11 || param2.nextInt(2) != 0 && var10 != 0) {
                                BlockPos var16 = new BlockPos(var12, var9, var14);
                                if (isAirOrLeaves(param1, var16) || isReplaceablePlant(param1, var16)) {
                                    this.setBlock(param0, param1, var16, this.leaf, param4);
                                }
                            }
                        }
                    }
                }

                for(int var17 = 0; var17 < var0; ++var17) {
                    if (isAirOrLeaves(param1, param3.above(var17)) || isReplaceablePlant(param1, param3.above(var17))) {
                        this.setBlock(param0, param1, param3.above(var17), this.trunk, param4);
                        if (this.addJungleFeatures && var17 > 0) {
                            if (param2.nextInt(3) > 0 && isAir(param1, param3.offset(-1, var17, 0))) {
                                this.addVine(param1, param3.offset(-1, var17, 0), VineBlock.EAST);
                            }

                            if (param2.nextInt(3) > 0 && isAir(param1, param3.offset(1, var17, 0))) {
                                this.addVine(param1, param3.offset(1, var17, 0), VineBlock.WEST);
                            }

                            if (param2.nextInt(3) > 0 && isAir(param1, param3.offset(0, var17, -1))) {
                                this.addVine(param1, param3.offset(0, var17, -1), VineBlock.SOUTH);
                            }

                            if (param2.nextInt(3) > 0 && isAir(param1, param3.offset(0, var17, 1))) {
                                this.addVine(param1, param3.offset(0, var17, 1), VineBlock.NORTH);
                            }
                        }
                    }
                }

                if (this.addJungleFeatures) {
                    for(int var18 = param3.getY() - 3 + var0; var18 <= param3.getY() + var0; ++var18) {
                        int var19 = var18 - (param3.getY() + var0);
                        int var20 = 2 - var19 / 2;
                        BlockPos.MutableBlockPos var21 = new BlockPos.MutableBlockPos();

                        for(int var22 = param3.getX() - var20; var22 <= param3.getX() + var20; ++var22) {
                            for(int var23 = param3.getZ() - var20; var23 <= param3.getZ() + var20; ++var23) {
                                var21.set(var22, var18, var23);
                                if (isLeaves(param1, var21)) {
                                    BlockPos var24 = var21.west();
                                    BlockPos var25 = var21.east();
                                    BlockPos var26 = var21.north();
                                    BlockPos var27 = var21.south();
                                    if (param2.nextInt(4) == 0 && isAir(param1, var24)) {
                                        this.addHangingVine(param1, var24, VineBlock.EAST);
                                    }

                                    if (param2.nextInt(4) == 0 && isAir(param1, var25)) {
                                        this.addHangingVine(param1, var25, VineBlock.WEST);
                                    }

                                    if (param2.nextInt(4) == 0 && isAir(param1, var26)) {
                                        this.addHangingVine(param1, var26, VineBlock.SOUTH);
                                    }

                                    if (param2.nextInt(4) == 0 && isAir(param1, var27)) {
                                        this.addHangingVine(param1, var27, VineBlock.NORTH);
                                    }
                                }
                            }
                        }
                    }

                    if (param2.nextInt(5) == 0 && var0 > 5) {
                        for(int var28 = 0; var28 < 2; ++var28) {
                            for(Direction var29 : Direction.Plane.HORIZONTAL) {
                                if (param2.nextInt(4 - var28) == 0) {
                                    Direction var30 = var29.getOpposite();
                                    this.placeCocoa(param1, param2.nextInt(3), param3.offset(var30.getStepX(), var0 - 5 + var28, var30.getStepZ()), var29);
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

    protected int getTreeHeight(Random param0) {
        return this.baseHeight + param0.nextInt(3);
    }

    private void placeCocoa(LevelWriter param0, int param1, BlockPos param2, Direction param3) {
        this.setBlock(param0, param2, Blocks.COCOA.defaultBlockState().setValue(CocoaBlock.AGE, Integer.valueOf(param1)).setValue(CocoaBlock.FACING, param3));
    }

    private void addVine(LevelWriter param0, BlockPos param1, BooleanProperty param2) {
        this.setBlock(param0, param1, Blocks.VINE.defaultBlockState().setValue(param2, Boolean.valueOf(true)));
    }

    private void addHangingVine(LevelSimulatedRW param0, BlockPos param1, BooleanProperty param2) {
        this.addVine(param0, param1, param2);
        int var0 = 4;

        for(BlockPos var5 = param1.below(); isAir(param0, var5) && var0 > 0; --var0) {
            this.addVine(param0, var5, param2);
            var5 = var5.below();
        }

    }
}
