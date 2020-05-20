package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class RotatedBlockProvider extends BlockStateProvider {
    public static final Codec<RotatedBlockProvider> CODEC = BlockState.CODEC
        .fieldOf("state")
        .xmap(BlockBehaviour.BlockStateBase::getBlock, Block::defaultBlockState)
        .xmap(RotatedBlockProvider::new, param0 -> param0.block)
        .codec();
    private final Block block;

    public RotatedBlockProvider(Block param0) {
        this.block = param0;
    }

    @Override
    protected BlockStateProviderType<?> type() {
        return BlockStateProviderType.ROTATED_BLOCK_PROVIDER;
    }

    @Override
    public BlockState getState(Random param0, BlockPos param1) {
        Direction.Axis var0 = Direction.Axis.getRandom(param0);
        return this.block.defaultBlockState().setValue(RotatedPillarBlock.AXIS, var0);
    }
}
