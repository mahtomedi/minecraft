package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;

public class CaveVinesBlock extends GrowingPlantHeadBlock implements BonemealableBlock, CaveVines {
    public static final MapCodec<CaveVinesBlock> CODEC = simpleCodec(CaveVinesBlock::new);
    private static final float CHANCE_OF_BERRIES_ON_GROWTH = 0.11F;

    @Override
    public MapCodec<CaveVinesBlock> codec() {
        return CODEC;
    }

    public CaveVinesBlock(BlockBehaviour.Properties param0) {
        super(param0, Direction.DOWN, SHAPE, false, 0.1);
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, Integer.valueOf(0)).setValue(BERRIES, Boolean.valueOf(false)));
    }

    @Override
    protected int getBlocksToGrowWhenBonemealed(RandomSource param0) {
        return 1;
    }

    @Override
    protected boolean canGrowInto(BlockState param0) {
        return param0.isAir();
    }

    @Override
    protected Block getBodyBlock() {
        return Blocks.CAVE_VINES_PLANT;
    }

    @Override
    protected BlockState updateBodyAfterConvertedFromHead(BlockState param0, BlockState param1) {
        return param1.setValue(BERRIES, param0.getValue(BERRIES));
    }

    @Override
    protected BlockState getGrowIntoState(BlockState param0, RandomSource param1) {
        return super.getGrowIntoState(param0, param1).setValue(BERRIES, Boolean.valueOf(param1.nextFloat() < 0.11F));
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader param0, BlockPos param1, BlockState param2) {
        return new ItemStack(Items.GLOW_BERRIES);
    }

    @Override
    public InteractionResult use(BlockState param0, Level param1, BlockPos param2, Player param3, InteractionHand param4, BlockHitResult param5) {
        return CaveVines.use(param3, param0, param1, param2);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        super.createBlockStateDefinition(param0);
        param0.add(BERRIES);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader param0, BlockPos param1, BlockState param2) {
        return !param2.getValue(BERRIES);
    }

    @Override
    public boolean isBonemealSuccess(Level param0, RandomSource param1, BlockPos param2, BlockState param3) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel param0, RandomSource param1, BlockPos param2, BlockState param3) {
        param0.setBlock(param2, param3.setValue(BERRIES, Boolean.valueOf(true)), 2);
    }
}
