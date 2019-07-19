package net.minecraft.world.level.block.piston;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PistonBaseBlock extends DirectionalBlock {
    public static final BooleanProperty EXTENDED = BlockStateProperties.EXTENDED;
    protected static final VoxelShape EAST_AABB = Block.box(0.0, 0.0, 0.0, 12.0, 16.0, 16.0);
    protected static final VoxelShape WEST_AABB = Block.box(4.0, 0.0, 0.0, 16.0, 16.0, 16.0);
    protected static final VoxelShape SOUTH_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 12.0);
    protected static final VoxelShape NORTH_AABB = Block.box(0.0, 0.0, 4.0, 16.0, 16.0, 16.0);
    protected static final VoxelShape UP_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0);
    protected static final VoxelShape DOWN_AABB = Block.box(0.0, 4.0, 0.0, 16.0, 16.0, 16.0);
    private final boolean isSticky;

    public PistonBaseBlock(boolean param0, Block.Properties param1) {
        super(param1);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(EXTENDED, Boolean.valueOf(false)));
        this.isSticky = param0;
    }

    @Override
    public boolean isViewBlocking(BlockState param0, BlockGetter param1, BlockPos param2) {
        return !param0.getValue(EXTENDED);
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        if (param0.getValue(EXTENDED)) {
            switch((Direction)param0.getValue(FACING)) {
                case DOWN:
                    return DOWN_AABB;
                case UP:
                default:
                    return UP_AABB;
                case NORTH:
                    return NORTH_AABB;
                case SOUTH:
                    return SOUTH_AABB;
                case WEST:
                    return WEST_AABB;
                case EAST:
                    return EAST_AABB;
            }
        } else {
            return Shapes.block();
        }
    }

    @Override
    public boolean isRedstoneConductor(BlockState param0, BlockGetter param1, BlockPos param2) {
        return false;
    }

    @Override
    public void setPlacedBy(Level param0, BlockPos param1, BlockState param2, LivingEntity param3, ItemStack param4) {
        if (!param0.isClientSide) {
            this.checkIfExtend(param0, param1, param2);
        }

    }

    @Override
    public void neighborChanged(BlockState param0, Level param1, BlockPos param2, Block param3, BlockPos param4, boolean param5) {
        if (!param1.isClientSide) {
            this.checkIfExtend(param1, param2, param0);
        }

    }

    @Override
    public void onPlace(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
        if (param3.getBlock() != param0.getBlock()) {
            if (!param1.isClientSide && param1.getBlockEntity(param2) == null) {
                this.checkIfExtend(param1, param2, param0);
            }

        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        return this.defaultBlockState().setValue(FACING, param0.getNearestLookingDirection().getOpposite()).setValue(EXTENDED, Boolean.valueOf(false));
    }

    private void checkIfExtend(Level param0, BlockPos param1, BlockState param2) {
        Direction var0 = param2.getValue(FACING);
        boolean var1 = this.getNeighborSignal(param0, param1, var0);
        if (var1 && !param2.getValue(EXTENDED)) {
            if (new PistonStructureResolver(param0, param1, var0, true).resolve()) {
                param0.blockEvent(param1, this, 0, var0.get3DDataValue());
            }
        } else if (!var1 && param2.getValue(EXTENDED)) {
            BlockPos var2 = param1.relative(var0, 2);
            BlockState var3 = param0.getBlockState(var2);
            int var4 = 1;
            if (var3.getBlock() == Blocks.MOVING_PISTON && var3.getValue(FACING) == var0) {
                BlockEntity var5 = param0.getBlockEntity(var2);
                if (var5 instanceof PistonMovingBlockEntity) {
                    PistonMovingBlockEntity var6 = (PistonMovingBlockEntity)var5;
                    if (var6.isExtending()
                        && (var6.getProgress(0.0F) < 0.5F || param0.getGameTime() == var6.getLastTicked() || ((ServerLevel)param0).isHandlingTick())) {
                        var4 = 2;
                    }
                }
            }

            param0.blockEvent(param1, this, var4, var0.get3DDataValue());
        }

    }

    private boolean getNeighborSignal(Level param0, BlockPos param1, Direction param2) {
        for(Direction var0 : Direction.values()) {
            if (var0 != param2 && param0.hasSignal(param1.relative(var0), var0)) {
                return true;
            }
        }

        if (param0.hasSignal(param1, Direction.DOWN)) {
            return true;
        } else {
            BlockPos var1 = param1.above();

            for(Direction var2 : Direction.values()) {
                if (var2 != Direction.DOWN && param0.hasSignal(var1.relative(var2), var2)) {
                    return true;
                }
            }

            return false;
        }
    }

    @Override
    public boolean triggerEvent(BlockState param0, Level param1, BlockPos param2, int param3, int param4) {
        Direction var0 = param0.getValue(FACING);
        if (!param1.isClientSide) {
            boolean var1 = this.getNeighborSignal(param1, param2, var0);
            if (var1 && (param3 == 1 || param3 == 2)) {
                param1.setBlock(param2, param0.setValue(EXTENDED, Boolean.valueOf(true)), 2);
                return false;
            }

            if (!var1 && param3 == 0) {
                return false;
            }
        }

        if (param3 == 0) {
            if (!this.moveBlocks(param1, param2, var0, true)) {
                return false;
            }

            param1.setBlock(param2, param0.setValue(EXTENDED, Boolean.valueOf(true)), 67);
            param1.playSound(null, param2, SoundEvents.PISTON_EXTEND, SoundSource.BLOCKS, 0.5F, param1.random.nextFloat() * 0.25F + 0.6F);
        } else if (param3 == 1 || param3 == 2) {
            BlockEntity var2 = param1.getBlockEntity(param2.relative(var0));
            if (var2 instanceof PistonMovingBlockEntity) {
                ((PistonMovingBlockEntity)var2).finalTick();
            }

            param1.setBlock(
                param2,
                Blocks.MOVING_PISTON
                    .defaultBlockState()
                    .setValue(MovingPistonBlock.FACING, var0)
                    .setValue(MovingPistonBlock.TYPE, this.isSticky ? PistonType.STICKY : PistonType.DEFAULT),
                3
            );
            param1.setBlockEntity(
                param2,
                MovingPistonBlock.newMovingBlockEntity(this.defaultBlockState().setValue(FACING, Direction.from3DDataValue(param4 & 7)), var0, false, true)
            );
            if (this.isSticky) {
                BlockPos var3 = param2.offset(var0.getStepX() * 2, var0.getStepY() * 2, var0.getStepZ() * 2);
                BlockState var4 = param1.getBlockState(var3);
                Block var5 = var4.getBlock();
                boolean var6 = false;
                if (var5 == Blocks.MOVING_PISTON) {
                    BlockEntity var7 = param1.getBlockEntity(var3);
                    if (var7 instanceof PistonMovingBlockEntity) {
                        PistonMovingBlockEntity var8 = (PistonMovingBlockEntity)var7;
                        if (var8.getDirection() == var0 && var8.isExtending()) {
                            var8.finalTick();
                            var6 = true;
                        }
                    }
                }

                if (!var6) {
                    if (param3 != 1
                        || var4.isAir()
                        || !isPushable(var4, param1, var3, var0.getOpposite(), false, var0)
                        || var4.getPistonPushReaction() != PushReaction.NORMAL && var5 != Blocks.PISTON && var5 != Blocks.STICKY_PISTON) {
                        param1.removeBlock(param2.relative(var0), false);
                    } else {
                        this.moveBlocks(param1, param2, var0, false);
                    }
                }
            } else {
                param1.removeBlock(param2.relative(var0), false);
            }

            param1.playSound(null, param2, SoundEvents.PISTON_CONTRACT, SoundSource.BLOCKS, 0.5F, param1.random.nextFloat() * 0.15F + 0.6F);
        }

        return true;
    }

    public static boolean isPushable(BlockState param0, Level param1, BlockPos param2, Direction param3, boolean param4, Direction param5) {
        Block var0 = param0.getBlock();
        if (var0 == Blocks.OBSIDIAN) {
            return false;
        } else if (!param1.getWorldBorder().isWithinBounds(param2)) {
            return false;
        } else if (param2.getY() >= 0 && (param3 != Direction.DOWN || param2.getY() != 0)) {
            if (param2.getY() <= param1.getMaxBuildHeight() - 1 && (param3 != Direction.UP || param2.getY() != param1.getMaxBuildHeight() - 1)) {
                if (var0 != Blocks.PISTON && var0 != Blocks.STICKY_PISTON) {
                    if (param0.getDestroySpeed(param1, param2) == -1.0F) {
                        return false;
                    }

                    switch(param0.getPistonPushReaction()) {
                        case BLOCK:
                            return false;
                        case DESTROY:
                            return param4;
                        case PUSH_ONLY:
                            return param3 == param5;
                    }
                } else if (param0.getValue(EXTENDED)) {
                    return false;
                }

                return !var0.isEntityBlock();
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean moveBlocks(Level param0, BlockPos param1, Direction param2, boolean param3) {
        BlockPos var0 = param1.relative(param2);
        if (!param3 && param0.getBlockState(var0).getBlock() == Blocks.PISTON_HEAD) {
            param0.setBlock(var0, Blocks.AIR.defaultBlockState(), 20);
        }

        PistonStructureResolver var1 = new PistonStructureResolver(param0, param1, param2, param3);
        if (!var1.resolve()) {
            return false;
        } else {
            List<BlockPos> var2 = var1.getToPush();
            List<BlockState> var3 = Lists.newArrayList();

            for(int var4 = 0; var4 < var2.size(); ++var4) {
                BlockPos var5 = var2.get(var4);
                var3.add(param0.getBlockState(var5));
            }

            List<BlockPos> var6 = var1.getToDestroy();
            int var7 = var2.size() + var6.size();
            BlockState[] var8 = new BlockState[var7];
            Direction var9 = param3 ? param2 : param2.getOpposite();
            Set<BlockPos> var10 = Sets.newHashSet(var2);

            for(int var11 = var6.size() - 1; var11 >= 0; --var11) {
                BlockPos var12 = var6.get(var11);
                BlockState var13 = param0.getBlockState(var12);
                BlockEntity var14 = var13.getBlock().isEntityBlock() ? param0.getBlockEntity(var12) : null;
                dropResources(var13, param0, var12, var14);
                param0.setBlock(var12, Blocks.AIR.defaultBlockState(), 18);
                --var7;
                var8[var7] = var13;
            }

            for(int var15 = var2.size() - 1; var15 >= 0; --var15) {
                BlockPos var16 = var2.get(var15);
                BlockState var17 = param0.getBlockState(var16);
                var16 = var16.relative(var9);
                var10.remove(var16);
                param0.setBlock(var16, Blocks.MOVING_PISTON.defaultBlockState().setValue(FACING, param2), 68);
                param0.setBlockEntity(var16, MovingPistonBlock.newMovingBlockEntity(var3.get(var15), param2, param3, false));
                --var7;
                var8[var7] = var17;
            }

            if (param3) {
                PistonType var18 = this.isSticky ? PistonType.STICKY : PistonType.DEFAULT;
                BlockState var19 = Blocks.PISTON_HEAD.defaultBlockState().setValue(PistonHeadBlock.FACING, param2).setValue(PistonHeadBlock.TYPE, var18);
                BlockState var20 = Blocks.MOVING_PISTON
                    .defaultBlockState()
                    .setValue(MovingPistonBlock.FACING, param2)
                    .setValue(MovingPistonBlock.TYPE, this.isSticky ? PistonType.STICKY : PistonType.DEFAULT);
                var10.remove(var0);
                param0.setBlock(var0, var20, 68);
                param0.setBlockEntity(var0, MovingPistonBlock.newMovingBlockEntity(var19, param2, true, true));
            }

            for(BlockPos var21 : var10) {
                param0.setBlock(var21, Blocks.AIR.defaultBlockState(), 66);
            }

            for(int var22 = var6.size() - 1; var22 >= 0; --var22) {
                BlockState var23 = var8[var7++];
                BlockPos var24 = var6.get(var22);
                var23.updateIndirectNeighbourShapes(param0, var24, 2);
                param0.updateNeighborsAt(var24, var23.getBlock());
            }

            for(int var25 = var2.size() - 1; var25 >= 0; --var25) {
                param0.updateNeighborsAt(var2.get(var25), var8[var7++].getBlock());
            }

            if (param3) {
                param0.updateNeighborsAt(var0, Blocks.PISTON_HEAD);
            }

            return true;
        }
    }

    @Override
    public BlockState rotate(BlockState param0, Rotation param1) {
        return param0.setValue(FACING, param1.rotate(param0.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState param0, Mirror param1) {
        return param0.rotate(param1.getRotation(param0.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(FACING, EXTENDED);
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState param0) {
        return param0.getValue(EXTENDED);
    }

    @Override
    public boolean isPathfindable(BlockState param0, BlockGetter param1, BlockPos param2, PathComputationType param3) {
        return false;
    }
}
