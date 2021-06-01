package net.minecraft.world.level.block;

import java.util.function.ToIntFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.VoxelShape;

public interface CaveVines {
    VoxelShape SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0);
    BooleanProperty BERRIES = BlockStateProperties.BERRIES;

    static InteractionResult use(BlockState param0, Level param1, BlockPos param2) {
        if (param0.getValue(BERRIES)) {
            Block.popResource(param1, param2, new ItemStack(Items.GLOW_BERRIES, 1));
            float var0 = Mth.randomBetween(param1.random, 0.8F, 1.2F);
            param1.playSound(null, param2, SoundEvents.CAVE_VINES_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, var0);
            param1.setBlock(param2, param0.setValue(BERRIES, Boolean.valueOf(false)), 2);
            return InteractionResult.sidedSuccess(param1.isClientSide);
        } else {
            return InteractionResult.PASS;
        }
    }

    static boolean hasGlowBerries(BlockState param0) {
        return param0.hasProperty(BERRIES) && param0.getValue(BERRIES);
    }

    static ToIntFunction<BlockState> emission(int param0) {
        return param1 -> param1.getValue(BlockStateProperties.BERRIES) ? param0 : 0;
    }
}
