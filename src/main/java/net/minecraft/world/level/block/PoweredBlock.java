package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class PoweredBlock extends Block {
    public static final MapCodec<PoweredBlock> CODEC = simpleCodec(PoweredBlock::new);

    @Override
    public MapCodec<PoweredBlock> codec() {
        return CODEC;
    }

    public PoweredBlock(BlockBehaviour.Properties param0) {
        super(param0);
    }

    @Override
    public boolean isSignalSource(BlockState param0) {
        return true;
    }

    @Override
    public int getSignal(BlockState param0, BlockGetter param1, BlockPos param2, Direction param3) {
        return 15;
    }
}
