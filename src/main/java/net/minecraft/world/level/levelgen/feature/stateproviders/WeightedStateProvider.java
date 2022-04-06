package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.block.state.BlockState;

public class WeightedStateProvider extends BlockStateProvider {
    public static final Codec<WeightedStateProvider> CODEC = SimpleWeightedRandomList.wrappedCodec(BlockState.CODEC)
        .comapFlatMap(WeightedStateProvider::create, param0 -> param0.weightedList)
        .fieldOf("entries")
        .codec();
    private final SimpleWeightedRandomList<BlockState> weightedList;

    private static DataResult<WeightedStateProvider> create(SimpleWeightedRandomList<BlockState> param0) {
        return param0.isEmpty() ? DataResult.error("WeightedStateProvider with no states") : DataResult.success(new WeightedStateProvider(param0));
    }

    public WeightedStateProvider(SimpleWeightedRandomList<BlockState> param0) {
        this.weightedList = param0;
    }

    public WeightedStateProvider(SimpleWeightedRandomList.Builder<BlockState> param0) {
        this(param0.build());
    }

    @Override
    protected BlockStateProviderType<?> type() {
        return BlockStateProviderType.WEIGHTED_STATE_PROVIDER;
    }

    @Override
    public BlockState getState(RandomSource param0, BlockPos param1) {
        return this.weightedList.getRandomValue(param0).orElseThrow(IllegalStateException::new);
    }
}
