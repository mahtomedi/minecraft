package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class FletchingTableBlock extends CraftingTableBlock {
    public static final MapCodec<FletchingTableBlock> CODEC = simpleCodec(FletchingTableBlock::new);

    @Override
    public MapCodec<FletchingTableBlock> codec() {
        return CODEC;
    }

    protected FletchingTableBlock(BlockBehaviour.Properties param0) {
        super(param0);
    }

    @Override
    public InteractionResult use(BlockState param0, Level param1, BlockPos param2, Player param3, InteractionHand param4, BlockHitResult param5) {
        return InteractionResult.PASS;
    }
}
