package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class PressurePlateBlock extends BasePressurePlateBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    private final PressurePlateBlock.Sensitivity sensitivity;

    protected PressurePlateBlock(PressurePlateBlock.Sensitivity param0, BlockBehaviour.Properties param1, BlockSetType param2) {
        super(param1, param2);
        this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, Boolean.valueOf(false)));
        this.sensitivity = param0;
    }

    @Override
    protected int getSignalForState(BlockState param0) {
        return param0.getValue(POWERED) ? 15 : 0;
    }

    @Override
    protected BlockState setSignalForState(BlockState param0, int param1) {
        return param0.setValue(POWERED, Boolean.valueOf(param1 > 0));
    }

    @Override
    protected int getSignalStrength(Level param0, BlockPos param1) {
        Class var0 = switch(this.sensitivity) {
            case EVERYTHING -> Entity.class;
            case MOBS -> LivingEntity.class;
        };
        return getEntityCount(param0, TOUCH_AABB.move(param1), var0) > 0 ? 15 : 0;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(POWERED);
    }

    public static enum Sensitivity {
        EVERYTHING,
        MOBS;
    }
}
