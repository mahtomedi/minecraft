package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.ai.behavior.WeightedList;
import net.minecraft.world.level.block.state.BlockState;

public class WeightedStateProvider extends BlockStateProvider {
    private final WeightedList<BlockState> weightedList;

    private WeightedStateProvider(WeightedList<BlockState> param0) {
        super(BlockStateProviderType.WEIGHTED_STATE_PROVIDER);
        this.weightedList = param0;
    }

    public WeightedStateProvider() {
        this(new WeightedList<>());
    }

    public <T> WeightedStateProvider(Dynamic<T> param0) {
        this(new WeightedList<>(param0.get("entries").orElseEmptyList(), BlockState::deserialize));
    }

    public WeightedStateProvider add(BlockState param0, int param1) {
        this.weightedList.add(param0, param1);
        return this;
    }

    @Override
    public BlockState getState(Random param0, BlockPos param1) {
        return this.weightedList.getOne(param0);
    }

    @Override
    public <T> T serialize(DynamicOps<T> param0) {
        Builder<T, T> var0 = ImmutableMap.builder();
        var0.put(param0.createString("type"), param0.createString(Registry.BLOCKSTATE_PROVIDER_TYPES.getKey(this.type).toString()))
            .put(param0.createString("entries"), this.weightedList.serialize(param0, param1 -> BlockState.serialize(param0, param1)));
        return new Dynamic<>(param0, param0.createMap(var0.build())).getValue();
    }
}
