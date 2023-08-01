package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ChorusFlowerBlock extends Block {
    public static final int DEAD_AGE = 5;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_5;
    protected static final VoxelShape BLOCK_SUPPORT_SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 15.0, 15.0);
    private final ChorusPlantBlock plant;

    protected ChorusFlowerBlock(ChorusPlantBlock param0, BlockBehaviour.Properties param1) {
        super(param1);
        this.plant = param0;
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, Integer.valueOf(0)));
    }

    @Override
    public void tick(BlockState param0, ServerLevel param1, BlockPos param2, RandomSource param3) {
        if (!param0.canSurvive(param1, param2)) {
            param1.destroyBlock(param2, true);
        }

    }

    @Override
    public boolean isRandomlyTicking(BlockState param0) {
        return param0.getValue(AGE) < 5;
    }

    @Override
    public VoxelShape getBlockSupportShape(BlockState param0, BlockGetter param1, BlockPos param2) {
        return BLOCK_SUPPORT_SHAPE;
    }

    @Override
    public void randomTick(BlockState param0, ServerLevel param1, BlockPos param2, RandomSource param3) {
        BlockPos var0 = param2.above();
        if (param1.isEmptyBlock(var0) && var0.getY() < param1.getMaxBuildHeight()) {
            int var1 = param0.getValue(AGE);
            if (var1 < 5) {
                boolean var2 = false;
                boolean var3 = false;
                BlockState var4 = param1.getBlockState(param2.below());
                if (var4.is(Blocks.END_STONE)) {
                    var2 = true;
                } else if (var4.is(this.plant)) {
                    int var5 = 1;

                    for(int var6 = 0; var6 < 4; ++var6) {
                        BlockState var7 = param1.getBlockState(param2.below(var5 + 1));
                        if (!var7.is(this.plant)) {
                            if (var7.is(Blocks.END_STONE)) {
                                var3 = true;
                            }
                            break;
                        }

                        ++var5;
                    }

                    if (var5 < 2 || var5 <= param3.nextInt(var3 ? 5 : 4)) {
                        var2 = true;
                    }
                } else if (var4.isAir()) {
                    var2 = true;
                }

                if (var2 && allNeighborsEmpty(param1, var0, null) && param1.isEmptyBlock(param2.above(2))) {
                    param1.setBlock(param2, this.plant.getStateForPlacement(param1, param2), 2);
                    this.placeGrownFlower(param1, var0, var1);
                } else if (var1 < 4) {
                    int var8 = param3.nextInt(4);
                    if (var3) {
                        ++var8;
                    }

                    boolean var9 = false;

                    for(int var10 = 0; var10 < var8; ++var10) {
                        Direction var11 = Direction.Plane.HORIZONTAL.getRandomDirection(param3);
                        BlockPos var12 = param2.relative(var11);
                        if (param1.isEmptyBlock(var12) && param1.isEmptyBlock(var12.below()) && allNeighborsEmpty(param1, var12, var11.getOpposite())) {
                            this.placeGrownFlower(param1, var12, var1 + 1);
                            var9 = true;
                        }
                    }

                    if (var9) {
                        param1.setBlock(param2, this.plant.getStateForPlacement(param1, param2), 2);
                    } else {
                        this.placeDeadFlower(param1, param2);
                    }
                } else {
                    this.placeDeadFlower(param1, param2);
                }

            }
        }
    }

    private void placeGrownFlower(Level param0, BlockPos param1, int param2) {
        param0.setBlock(param1, this.defaultBlockState().setValue(AGE, Integer.valueOf(param2)), 2);
        param0.levelEvent(1033, param1, 0);
    }

    private void placeDeadFlower(Level param0, BlockPos param1) {
        param0.setBlock(param1, this.defaultBlockState().setValue(AGE, Integer.valueOf(5)), 2);
        param0.levelEvent(1034, param1, 0);
    }

    private static boolean allNeighborsEmpty(LevelReader param0, BlockPos param1, @Nullable Direction param2) {
        for(Direction var0 : Direction.Plane.HORIZONTAL) {
            if (var0 != param2 && !param0.isEmptyBlock(param1.relative(var0))) {
                return false;
            }
        }

        return true;
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        if (param1 != Direction.UP && !param0.canSurvive(param3, param4)) {
            param3.scheduleTick(param4, this, 1);
        }

        return super.updateShape(param0, param1, param2, param3, param4, param5);
    }

    @Override
    public boolean canSurvive(BlockState param0, LevelReader param1, BlockPos param2) {
        BlockState var0 = param1.getBlockState(param2.below());
        if (!var0.is(this.plant) && !var0.is(Blocks.END_STONE)) {
            if (!var0.isAir()) {
                return false;
            } else {
                boolean var1 = false;

                for(Direction var2 : Direction.Plane.HORIZONTAL) {
                    BlockState var3 = param1.getBlockState(param2.relative(var2));
                    if (var3.is(this.plant)) {
                        if (var1) {
                            return false;
                        }

                        var1 = true;
                    } else if (!var3.isAir()) {
                        return false;
                    }
                }

                return var1;
            }
        } else {
            return true;
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(AGE);
    }

    public static void generatePlant(LevelAccessor param0, BlockPos param1, RandomSource param2, int param3) {
        param0.setBlock(param1, ((ChorusPlantBlock)Blocks.CHORUS_PLANT).getStateForPlacement(param0, param1), 2);
        growTreeRecursive(param0, param1, param2, param1, param3, 0);
    }

    private static void growTreeRecursive(LevelAccessor param0, BlockPos param1, RandomSource param2, BlockPos param3, int param4, int param5) {
        ChorusPlantBlock var0 = (ChorusPlantBlock)Blocks.CHORUS_PLANT;
        int var1 = param2.nextInt(4) + 1;
        if (param5 == 0) {
            ++var1;
        }

        for(int var2 = 0; var2 < var1; ++var2) {
            BlockPos var3 = param1.above(var2 + 1);
            if (!allNeighborsEmpty(param0, var3, null)) {
                return;
            }

            param0.setBlock(var3, var0.getStateForPlacement(param0, var3), 2);
            param0.setBlock(var3.below(), var0.getStateForPlacement(param0, var3.below()), 2);
        }

        boolean var4 = false;
        if (param5 < 4) {
            int var5 = param2.nextInt(4);
            if (param5 == 0) {
                ++var5;
            }

            for(int var6 = 0; var6 < var5; ++var6) {
                Direction var7 = Direction.Plane.HORIZONTAL.getRandomDirection(param2);
                BlockPos var8 = param1.above(var1).relative(var7);
                if (Math.abs(var8.getX() - param3.getX()) < param4
                    && Math.abs(var8.getZ() - param3.getZ()) < param4
                    && param0.isEmptyBlock(var8)
                    && param0.isEmptyBlock(var8.below())
                    && allNeighborsEmpty(param0, var8, var7.getOpposite())) {
                    var4 = true;
                    param0.setBlock(var8, var0.getStateForPlacement(param0, var8), 2);
                    param0.setBlock(var8.relative(var7.getOpposite()), var0.getStateForPlacement(param0, var8.relative(var7.getOpposite())), 2);
                    growTreeRecursive(param0, var8, param2, param3, param4, param5 + 1);
                }
            }
        }

        if (!var4) {
            param0.setBlock(param1.above(var1), Blocks.CHORUS_FLOWER.defaultBlockState().setValue(AGE, Integer.valueOf(5)), 2);
        }

    }

    @Override
    public void onProjectileHit(Level param0, BlockState param1, BlockHitResult param2, Projectile param3) {
        BlockPos var0 = param2.getBlockPos();
        if (!param0.isClientSide && param3.mayInteract(param0, var0) && param3.getType().is(EntityTypeTags.IMPACT_PROJECTILES)) {
            param0.destroyBlock(var0, true, param3);
        }

    }
}
