package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public abstract class BlockStateProvider {
    public static final Codec<BlockStateProvider> CODEC = Registry.BLOCKSTATE_PROVIDER_TYPES
        .byNameCodec()
        .dispatch(BlockStateProvider::type, BlockStateProviderType::codec);

    public static SimpleStateProvider simple(BlockState param0) {
        return new SimpleStateProvider(param0);
    }

    public static SimpleStateProvider simple(Block param0) {
        return new SimpleStateProvider(param0.defaultBlockState());
    }

    protected abstract BlockStateProviderType<?> type();

    public abstract BlockState getState(RandomSource var1, BlockPos var2);
}
