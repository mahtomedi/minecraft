package net.minecraft.world.level.block;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;

public class PressurePlateBlock extends BasePressurePlateBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    private final PressurePlateBlock.Sensitivity sensitivity;

    protected PressurePlateBlock(PressurePlateBlock.Sensitivity param0, BlockBehaviour.Properties param1) {
        super(param1);
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
    protected void playOnSound(LevelAccessor param0, BlockPos param1) {
        if (this.material == Material.WOOD) {
            param0.playSound(null, param1, SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_ON, SoundSource.BLOCKS, 0.3F, 0.8F);
        } else {
            param0.playSound(null, param1, SoundEvents.STONE_PRESSURE_PLATE_CLICK_ON, SoundSource.BLOCKS, 0.3F, 0.6F);
        }

    }

    @Override
    protected void playOffSound(LevelAccessor param0, BlockPos param1) {
        if (this.material == Material.WOOD) {
            param0.playSound(null, param1, SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_OFF, SoundSource.BLOCKS, 0.3F, 0.7F);
        } else {
            param0.playSound(null, param1, SoundEvents.STONE_PRESSURE_PLATE_CLICK_OFF, SoundSource.BLOCKS, 0.3F, 0.5F);
        }

    }

    @Override
    protected int getSignalStrength(Level param0, BlockPos param1) {
        AABB var0 = TOUCH_AABB.move(param1);
        List<? extends Entity> var1;
        switch(this.sensitivity) {
            case EVERYTHING:
                var1 = param0.getEntities(null, var0);
                break;
            case MOBS:
                var1 = param0.getEntitiesOfClass(LivingEntity.class, var0);
                break;
            default:
                return 0;
        }

        if (!var1.isEmpty()) {
            for(Entity var4 : var1) {
                if (!var4.isIgnoringBlockTriggers()) {
                    return 15;
                }
            }
        }

        return 0;
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
