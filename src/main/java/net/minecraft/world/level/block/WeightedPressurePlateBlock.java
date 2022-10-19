package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class WeightedPressurePlateBlock extends BasePressurePlateBlock {
    public static final IntegerProperty POWER = BlockStateProperties.POWER;
    private final int maxWeight;
    private final SoundEvent soundOff;
    private final SoundEvent soundOn;

    protected WeightedPressurePlateBlock(int param0, BlockBehaviour.Properties param1, SoundEvent param2, SoundEvent param3) {
        super(param1);
        this.registerDefaultState(this.stateDefinition.any().setValue(POWER, Integer.valueOf(0)));
        this.maxWeight = param0;
        this.soundOff = param2;
        this.soundOn = param3;
    }

    @Override
    protected int getSignalStrength(Level param0, BlockPos param1) {
        int var0 = Math.min(param0.getEntitiesOfClass(Entity.class, TOUCH_AABB.move(param1)).size(), this.maxWeight);
        if (var0 > 0) {
            float var1 = (float)Math.min(this.maxWeight, var0) / (float)this.maxWeight;
            return Mth.ceil(var1 * 15.0F);
        } else {
            return 0;
        }
    }

    @Override
    protected void playOnSound(LevelAccessor param0, BlockPos param1) {
        param0.playSound(null, param1, this.soundOn, SoundSource.BLOCKS);
    }

    @Override
    protected void playOffSound(LevelAccessor param0, BlockPos param1) {
        param0.playSound(null, param1, this.soundOff, SoundSource.BLOCKS);
    }

    @Override
    protected int getSignalForState(BlockState param0) {
        return param0.getValue(POWER);
    }

    @Override
    protected BlockState setSignalForState(BlockState param0, int param1) {
        return param0.setValue(POWER, Integer.valueOf(param1));
    }

    @Override
    protected int getPressedTime() {
        return 10;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(POWER);
    }
}
