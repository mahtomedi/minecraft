package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.behavior.WeightedList;
import net.minecraft.world.level.block.state.BlockState;

public class WeightedStateProvider extends BlockStateProvider {
    public static final Codec<WeightedStateProvider> CODEC = WeightedList.codec(BlockState.CODEC)
        .comapFlatMap(WeightedStateProvider::create, param0 -> param0.weightedList)
        .fieldOf("entries")
        .codec();
    private final WeightedList<BlockState> weightedList;

    private static DataResult<WeightedStateProvider> create(WeightedList<BlockState> param0) {
        return param0.isEmpty() ? DataResult.error("WeightedStateProvider with no states") : DataResult.success(new WeightedStateProvider(param0));
    }

    private WeightedStateProvider(WeightedList<BlockState> param0) {
        this.weightedList = param0;
    }

    @Override
    protected BlockStateProviderType<?> type() {
        return BlockStateProviderType.WEIGHTED_STATE_PROVIDER;
    }

    public WeightedStateProvider() {
        this(new WeightedList<>());
    }

    public WeightedStateProvider add(BlockState param0, int param1) {
        this.weightedList.add(param0, param1);
        return this;
    }

    @Override
    public BlockState getState(Random param0, BlockPos param1) {
        return this.weightedList.getOne(param0);
    }
}
