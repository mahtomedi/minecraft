package net.minecraft.world.level.levelgen.feature.blockplacers;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class SimpleBlockPlacer extends BlockPlacer {
    public static final Codec<SimpleBlockPlacer> CODEC = Codec.unit(() -> SimpleBlockPlacer.INSTANCE);
    public static final SimpleBlockPlacer INSTANCE = new SimpleBlockPlacer();

    @Override
    protected BlockPlacerType<?> type() {
        return BlockPlacerType.SIMPLE_BLOCK_PLACER;
    }

    @Override
    public void place(LevelAccessor param0, BlockPos param1, BlockState param2, Random param3) {
        param0.setBlock(param1, param2, 2);
    }
}
