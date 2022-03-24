package net.minecraft.world.level.block.piston;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PistonBaseBlock extends DirectionalBlock {
    public static final BooleanProperty EXTENDED = BlockStateProperties.EXTENDED;
    public static final int TRIGGER_EXTEND = 0;
    public static final int TRIGGER_CONTRACT = 1;
    public static final int TRIGGER_DROP = 2;
    public static final float PLATFORM_THICKNESS = 4.0F;
    protected static final VoxelShape EAST_AABB = Block.box(0.0, 0.0, 0.0, 12.0, 16.0, 16.0);
    protected static final VoxelShape WEST_AABB = Block.box(4.0, 0.0, 0.0, 16.0, 16.0, 16.0);
    protected static final VoxelShape SOUTH_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 12.0);
    protected static final VoxelShape NORTH_AABB = Block.box(0.0, 0.0, 4.0, 16.0, 16.0, 16.0);
    protected static final VoxelShape UP_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0);
    protected static final VoxelShape DOWN_AABB = Block.box(0.0, 4.0, 0.0, 16.0, 16.0, 16.0);
    private final boolean isSticky;

    public PistonBaseBlock(boolean param0, BlockBehaviour.Properties param1) {
        super(param1);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(EXTENDED, Boolean.valueOf(false)));
        this.isSticky = param0;
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
        if (!param3.is(param0.getBlock())) {
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
            if (var3.is(Blocks.MOVING_PISTON) && var3.getValue(FACING) == var0) {
                BlockEntity var5 = param0.getBlockEntity(var2);
                if (var5 instanceof PistonMovingBlockEntity var6
                    && var6.isExtending()
                    && (var6.getProgress(0.0F) < 0.5F || param0.getGameTime() == var6.getLastTicked() || ((ServerLevel)param0).isHandlingTick())) {
                    var4 = 2;
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
            param1.gameEvent(null, GameEvent.PISTON_EXTEND, param2);
        } else if (param3 == 1 || param3 == 2) {
            BlockEntity var2 = param1.getBlockEntity(param2.relative(var0));
            if (var2 instanceof PistonMovingBlockEntity) {
                ((PistonMovingBlockEntity)var2).finalTick();
            }

            BlockState var3 = Blocks.MOVING_PISTON
                .defaultBlockState()
                .setValue(MovingPistonBlock.FACING, var0)
                .setValue(MovingPistonBlock.TYPE, this.isSticky ? PistonType.STICKY : PistonType.DEFAULT);
            param1.setBlock(param2, var3, 20);
            param1.setBlockEntity(
                MovingPistonBlock.newMovingBlockEntity(
                    param2, var3, this.defaultBlockState().setValue(FACING, Direction.from3DDataValue(param4 & 7)), var0, false, true
                )
            );
            param1.blockUpdated(param2, var3.getBlock());
            var3.updateNeighbourShapes(param1, param2, 2);
            if (this.isSticky) {
                BlockPos var4 = param2.offset(var0.getStepX() * 2, var0.getStepY() * 2, var0.getStepZ() * 2);
                BlockState var5 = param1.getBlockState(var4);
                boolean var6 = false;
                if (var5.is(Blocks.MOVING_PISTON)) {
                    BlockEntity var7 = param1.getBlockEntity(var4);
                    if (var7 instanceof PistonMovingBlockEntity var8 && var8.getDirection() == var0 && var8.isExtending()) {
                        var8.finalTick();
                        var6 = true;
                    }
                }

                if (!var6) {
                    if (param3 != 1
                        || var5.isAir()
                        || !isPushable(var5, param1, var4, var0.getOpposite(), false, var0)
                        || var5.getPistonPushReaction() != PushReaction.NORMAL && !var5.is(Blocks.PISTON) && !var5.is(Blocks.STICKY_PISTON)) {
                        param1.removeBlock(param2.relative(var0), false);
                    } else {
                        this.moveBlocks(param1, param2, var0, false);
                    }
                }
            } else {
                param1.removeBlock(param2.relative(var0), false);
            }

            param1.playSound(null, param2, SoundEvents.PISTON_CONTRACT, SoundSource.BLOCKS, 0.5F, param1.random.nextFloat() * 0.15F + 0.6F);
            param1.gameEvent(null, GameEvent.PISTON_CONTRACT, param2);
        }

        return true;
    }

    public static boolean isPushable(BlockState param0, Level param1, BlockPos param2, Direction param3, boolean param4, Direction param5) {
        if (param2.getY() < param1.getMinBuildHeight() || param2.getY() > param1.getMaxBuildHeight() - 1 || !param1.getWorldBorder().isWithinBounds(param2)) {
            return false;
        } else if (param0.isAir()) {
            return true;
        } else if (param0.is(Blocks.OBSIDIAN) || param0.is(Blocks.CRYING_OBSIDIAN) || param0.is(Blocks.RESPAWN_ANCHOR)) {
            return false;
        } else if (param3 == Direction.DOWN && param2.getY() == param1.getMinBuildHeight()) {
            return false;
        } else if (param3 == Direction.UP && param2.getY() == param1.getMaxBuildHeight() - 1) {
            return false;
        } else {
            if (!param0.is(Blocks.PISTON) && !param0.is(Blocks.STICKY_PISTON)) {
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

            return !param0.hasBlockEntity();
        }
    }

    private boolean moveBlocks(Level param0, BlockPos param1, Direction param2, boolean param3) {
        BlockPos var0 = param1.relative(param2);
        if (!param3 && param0.getBlockState(var0).is(Blocks.PISTON_HEAD)) {
            param0.setBlock(var0, Blocks.AIR.defaultBlockState(), 20);
        }

        PistonStructureResolver var1 = new PistonStructureResolver(param0, param1, param2, param3);
        if (!var1.resolve()) {
            return false;
        } else {
            Map<BlockPos, BlockState> var2 = Maps.newHashMap();
            List<BlockPos> var3 = var1.getToPush();
            List<BlockState> var4 = Lists.newArrayList();

            for(int var5 = 0; var5 < var3.size(); ++var5) {
                BlockPos var6 = var3.get(var5);
                BlockState var7 = param0.getBlockState(var6);
                var4.add(var7);
                var2.put(var6, var7);
            }

            List<BlockPos> var8 = var1.getToDestroy();
            BlockState[] var9 = new BlockState[var3.size() + var8.size()];
            Direction var10 = param3 ? param2 : param2.getOpposite();
            int var11 = 0;

            for(int var12 = var8.size() - 1; var12 >= 0; --var12) {
                BlockPos var13 = var8.get(var12);
                BlockState var14 = param0.getBlockState(var13);
                BlockEntity var15 = var14.hasBlockEntity() ? param0.getBlockEntity(var13) : null;
                dropResources(var14, param0, var13, var15);
                param0.setBlock(var13, Blocks.AIR.defaultBlockState(), 18);
                if (!var14.is(BlockTags.FIRE)) {
                    param0.addDestroyBlockEffect(var13, var14);
                }

                var9[var11++] = var14;
            }

            for(int var16 = var3.size() - 1; var16 >= 0; --var16) {
                BlockPos var17 = var3.get(var16);
                BlockState var18 = param0.getBlockState(var17);
                var17 = var17.relative(var10);
                var2.remove(var17);
                BlockState var19 = Blocks.MOVING_PISTON.defaultBlockState().setValue(FACING, param2);
                param0.setBlock(var17, var19, 68);
                param0.setBlockEntity(MovingPistonBlock.newMovingBlockEntity(var17, var19, var4.get(var16), param2, param3, false));
                var9[var11++] = var18;
            }

            if (param3) {
                PistonType var20 = this.isSticky ? PistonType.STICKY : PistonType.DEFAULT;
                BlockState var21 = Blocks.PISTON_HEAD.defaultBlockState().setValue(PistonHeadBlock.FACING, param2).setValue(PistonHeadBlock.TYPE, var20);
                BlockState var22 = Blocks.MOVING_PISTON
                    .defaultBlockState()
                    .setValue(MovingPistonBlock.FACING, param2)
                    .setValue(MovingPistonBlock.TYPE, this.isSticky ? PistonType.STICKY : PistonType.DEFAULT);
                var2.remove(var0);
                param0.setBlock(var0, var22, 68);
                param0.setBlockEntity(MovingPistonBlock.newMovingBlockEntity(var0, var22, var21, param2, true, true));
            }

            BlockState var23 = Blocks.AIR.defaultBlockState();

            for(BlockPos var24 : var2.keySet()) {
                param0.setBlock(var24, var23, 82);
            }

            for(Entry<BlockPos, BlockState> var25 : var2.entrySet()) {
                BlockPos var26 = var25.getKey();
                BlockState var27 = var25.getValue();
                var27.updateIndirectNeighbourShapes(param0, var26, 2);
                var23.updateNeighbourShapes(param0, var26, 2);
                var23.updateIndirectNeighbourShapes(param0, var26, 2);
            }

            var11 = 0;

            for(int var28 = var8.size() - 1; var28 >= 0; --var28) {
                BlockState var29 = var9[var11++];
                BlockPos var30 = var8.get(var28);
                var29.updateIndirectNeighbourShapes(param0, var30, 2);
                param0.updateNeighborsAt(var30, var29.getBlock());
            }

            for(int var31 = var3.size() - 1; var31 >= 0; --var31) {
                param0.updateNeighborsAt(var3.get(var31), var9[var11++].getBlock());
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
