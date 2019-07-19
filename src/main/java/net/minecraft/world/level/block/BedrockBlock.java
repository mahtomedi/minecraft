package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public class BedrockBlock extends Block {
    public BedrockBlock(Block.Properties param0) {
        super(param0);
    }

    @Override
    public boolean isValidSpawn(BlockState param0, BlockGetter param1, BlockPos param2, EntityType<?> param3) {
        return false;
    }
}
