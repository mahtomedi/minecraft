package net.minecraft.world.level.block;

import java.util.Collection;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;

public class SculkVeinBlock extends MultifaceBlock implements SculkBehaviour, SimpleWaterloggedBlock {
    private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private final MultifaceSpreader veinSpreader = new MultifaceSpreader(new SculkVeinBlock.SculkVeinSpreaderConfig(MultifaceSpreader.DEFAULT_SPREAD_ORDER));
    private final MultifaceSpreader sameSpaceSpreader = new MultifaceSpreader(
        new SculkVeinBlock.SculkVeinSpreaderConfig(MultifaceSpreader.SpreadType.SAME_POSITION)
    );

    public SculkVeinBlock(BlockBehaviour.Properties param0) {
        super(param0);
        this.registerDefaultState(this.defaultBlockState().setValue(WATERLOGGED, Boolean.valueOf(false)));
    }

    @Override
    public MultifaceSpreader getSpreader() {
        return this.veinSpreader;
    }

    public MultifaceSpreader getSameSpaceSpreader() {
        return this.sameSpaceSpreader;
    }

    public static boolean regrow(LevelAccessor param0, BlockPos param1, BlockState param2, Collection<Direction> param3) {
        boolean var0 = false;
        BlockState var1 = Blocks.SCULK_VEIN.defaultBlockState();

        for(Direction var2 : param3) {
            BlockPos var3 = param1.relative(var2);
            if (canAttachTo(param0, var2, var3, param0.getBlockState(var3))) {
                var1 = var1.setValue(getFaceProperty(var2), Boolean.valueOf(true));
                var0 = true;
            }
        }

        if (!var0) {
            return false;
        } else {
            if (!param2.getFluidState().isEmpty()) {
                var1 = var1.setValue(WATERLOGGED, Boolean.valueOf(true));
            }

            param0.setBlock(param1, var1, 3);
            return true;
        }
    }

    @Override
    public void onDischarged(LevelAccessor param0, BlockState param1, BlockPos param2, Random param3) {
        if (param1.is(this)) {
            for(Direction var0 : DIRECTIONS) {
                BooleanProperty var1 = getFaceProperty(var0);
                if (param1.getValue(var1) && param0.getBlockState(param2.relative(var0)).is(Blocks.SCULK)) {
                    param1 = param1.setValue(var1, Boolean.valueOf(false));
                }
            }

            if (!hasAnyFace(param1)) {
                FluidState var2 = param0.getFluidState(param2);
                param1 = (var2.isEmpty() ? Blocks.AIR : Blocks.WATER).defaultBlockState();
            }

            param0.setBlock(param2, param1, 3);
            SculkBehaviour.super.onDischarged(param0, param1, param2, param3);
        }
    }

    @Override
    public int attemptUseCharge(SculkSpreader.ChargeCursor param0, LevelAccessor param1, BlockPos param2, Random param3, SculkSpreader param4, boolean param5) {
        if (param5 && this.attemptPlaceSculk(param4, param1, param0.getPos(), param3)) {
            return param0.getCharge() - 1;
        } else {
            return param3.nextInt(param4.chargeDecayRate()) == 0 ? Mth.floor((float)param0.getCharge() * 0.5F) : param0.getCharge();
        }
    }

    private boolean attemptPlaceSculk(SculkSpreader param0, LevelAccessor param1, BlockPos param2, Random param3) {
        BlockState var0 = param1.getBlockState(param2);
        TagKey<Block> var1 = param0.replaceableBlocks();

        for(Direction var2 : Direction.allShuffled(param3)) {
            if (hasFace(var0, var2)) {
                BlockPos var3 = param2.relative(var2);
                if (param1.getBlockState(var3).is(var1)) {
                    BlockState var4 = Blocks.SCULK.defaultBlockState();
                    param1.setBlock(var3, var4, 3);
                    param1.playSound(null, var3, SoundEvents.SCULK_BLOCK_SPREAD, SoundSource.BLOCKS, 1.0F, 1.0F);
                    this.veinSpreader.spreadAll(var4, param1, var3, param0.isWorldGeneration());
                    Direction var5 = var2.getOpposite();

                    for(Direction var6 : DIRECTIONS) {
                        if (var6 != var5) {
                            BlockPos var7 = var3.relative(var6);
                            BlockState var8 = param1.getBlockState(var7);
                            if (var8.is(this)) {
                                this.onDischarged(param1, var8, var7, param3);
                            }
                        }
                    }

                    return true;
                }
            }
        }

        return false;
    }

    public static boolean hasSubstrateAccess(LevelAccessor param0, BlockState param1, BlockPos param2) {
        if (!param1.is(Blocks.SCULK_VEIN)) {
            return false;
        } else {
            for(Direction var0 : DIRECTIONS) {
                if (hasFace(param1, var0) && param0.getBlockState(param2.relative(var0)).is(BlockTags.SCULK_REPLACEABLE)) {
                    return true;
                }
            }

            return false;
        }
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        if (param0.getValue(WATERLOGGED)) {
            param3.scheduleTick(param4, Fluids.WATER, Fluids.WATER.getTickDelay(param3));
        }

        return super.updateShape(param0, param1, param2, param3, param4, param5);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        super.createBlockStateDefinition(param0);
        param0.add(WATERLOGGED);
    }

    @Override
    public boolean canBeReplaced(BlockState param0, BlockPlaceContext param1) {
        return !param1.getItemInHand().is(Items.SCULK_VEIN) || super.canBeReplaced(param0, param1);
    }

    @Override
    public FluidState getFluidState(BlockState param0) {
        return param0.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(param0);
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState param0) {
        return PushReaction.DESTROY;
    }

    class SculkVeinSpreaderConfig extends MultifaceSpreader.DefaultSpreaderConfig {
        private final MultifaceSpreader.SpreadType[] spreadTypes;

        public SculkVeinSpreaderConfig(MultifaceSpreader.SpreadType... param0) {
            super(SculkVeinBlock.this);
            this.spreadTypes = param0;
        }

        @Override
        public boolean stateCanBeReplaced(BlockGetter param0, BlockPos param1, BlockPos param2, Direction param3, BlockState param4) {
            BlockState var0 = param0.getBlockState(param2.relative(param3));
            if (!var0.is(Blocks.SCULK) && !var0.is(Blocks.SCULK_CATALYST) && !var0.is(Blocks.MOVING_PISTON)) {
                if (param1.distManhattan(param2) == 2) {
                    BlockPos var1 = param1.relative(param3.getOpposite());
                    if (param0.getBlockState(var1).isFaceSturdy(param0, var1, param3)) {
                        return false;
                    }
                }

                FluidState var2 = param4.getFluidState();
                if (!var2.isEmpty() && !var2.is(Fluids.WATER)) {
                    return false;
                } else {
                    return param4.getMaterial().isReplaceable() || super.stateCanBeReplaced(param0, param1, param2, param3, param4);
                }
            } else {
                return false;
            }
        }

        @Override
        public MultifaceSpreader.SpreadType[] getSpreadTypes() {
            return this.spreadTypes;
        }

        @Override
        public boolean isOtherBlockValidAsSource(BlockState param0) {
            return !param0.is(Blocks.SCULK_VEIN);
        }
    }
}
