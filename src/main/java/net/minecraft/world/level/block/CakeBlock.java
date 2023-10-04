package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CakeBlock extends Block {
    public static final MapCodec<CakeBlock> CODEC = simpleCodec(CakeBlock::new);
    public static final int MAX_BITES = 6;
    public static final IntegerProperty BITES = BlockStateProperties.BITES;
    public static final int FULL_CAKE_SIGNAL = getOutputSignal(0);
    protected static final float AABB_OFFSET = 1.0F;
    protected static final float AABB_SIZE_PER_BITE = 2.0F;
    protected static final VoxelShape[] SHAPE_BY_BITE = new VoxelShape[]{
        Block.box(1.0, 0.0, 1.0, 15.0, 8.0, 15.0),
        Block.box(3.0, 0.0, 1.0, 15.0, 8.0, 15.0),
        Block.box(5.0, 0.0, 1.0, 15.0, 8.0, 15.0),
        Block.box(7.0, 0.0, 1.0, 15.0, 8.0, 15.0),
        Block.box(9.0, 0.0, 1.0, 15.0, 8.0, 15.0),
        Block.box(11.0, 0.0, 1.0, 15.0, 8.0, 15.0),
        Block.box(13.0, 0.0, 1.0, 15.0, 8.0, 15.0)
    };

    @Override
    public MapCodec<CakeBlock> codec() {
        return CODEC;
    }

    protected CakeBlock(BlockBehaviour.Properties param0) {
        super(param0);
        this.registerDefaultState(this.stateDefinition.any().setValue(BITES, Integer.valueOf(0)));
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return SHAPE_BY_BITE[param0.getValue(BITES)];
    }

    @Override
    public InteractionResult use(BlockState param0, Level param1, BlockPos param2, Player param3, InteractionHand param4, BlockHitResult param5) {
        ItemStack var0 = param3.getItemInHand(param4);
        Item var1 = var0.getItem();
        if (var0.is(ItemTags.CANDLES) && param0.getValue(BITES) == 0) {
            Block var2 = Block.byItem(var1);
            if (var2 instanceof CandleBlock) {
                if (!param3.isCreative()) {
                    var0.shrink(1);
                }

                param1.playSound(null, param2, SoundEvents.CAKE_ADD_CANDLE, SoundSource.BLOCKS, 1.0F, 1.0F);
                param1.setBlockAndUpdate(param2, CandleCakeBlock.byCandle(var2));
                param1.gameEvent(param3, GameEvent.BLOCK_CHANGE, param2);
                param3.awardStat(Stats.ITEM_USED.get(var1));
                return InteractionResult.SUCCESS;
            }
        }

        if (param1.isClientSide) {
            if (eat(param1, param2, param0, param3).consumesAction()) {
                return InteractionResult.SUCCESS;
            }

            if (var0.isEmpty()) {
                return InteractionResult.CONSUME;
            }
        }

        return eat(param1, param2, param0, param3);
    }

    protected static InteractionResult eat(LevelAccessor param0, BlockPos param1, BlockState param2, Player param3) {
        if (!param3.canEat(false)) {
            return InteractionResult.PASS;
        } else {
            param3.awardStat(Stats.EAT_CAKE_SLICE);
            param3.getFoodData().eat(2, 0.1F);
            int var0 = param2.getValue(BITES);
            param0.gameEvent(param3, GameEvent.EAT, param1);
            if (var0 < 6) {
                param0.setBlock(param1, param2.setValue(BITES, Integer.valueOf(var0 + 1)), 3);
            } else {
                param0.removeBlock(param1, false);
                param0.gameEvent(param3, GameEvent.BLOCK_DESTROY, param1);
            }

            return InteractionResult.SUCCESS;
        }
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        return param1 == Direction.DOWN && !param0.canSurvive(param3, param4)
            ? Blocks.AIR.defaultBlockState()
            : super.updateShape(param0, param1, param2, param3, param4, param5);
    }

    @Override
    public boolean canSurvive(BlockState param0, LevelReader param1, BlockPos param2) {
        return param1.getBlockState(param2.below()).isSolid();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(BITES);
    }

    @Override
    public int getAnalogOutputSignal(BlockState param0, Level param1, BlockPos param2) {
        return getOutputSignal(param0.getValue(BITES));
    }

    public static int getOutputSignal(int param0) {
        return (7 - param0) * 2;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState param0) {
        return true;
    }

    @Override
    public boolean isPathfindable(BlockState param0, BlockGetter param1, BlockPos param2, PathComputationType param3) {
        return false;
    }
}
