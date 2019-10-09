package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.state.BlockState;

public class SimpleStateProvider extends BlockStateProvider {
    private final BlockState state;

    public SimpleStateProvider(BlockState param0) {
        super(BlockStateProviderType.SIMPLE_STATE_PROVIDER);
        this.state = param0;
    }

    public <T> SimpleStateProvider(Dynamic<T> param0) {
        this(BlockState.deserialize(param0.get("state").orElseEmptyMap()));
    }

    @Override
    public BlockState getState(Random param0, BlockPos param1) {
        return this.state;
    }

    @Override
    public <T> T serialize(DynamicOps<T> param0) {
        Builder<T, T> var0 = ImmutableMap.builder();
        var0.put(param0.createString("type"), param0.createString(Registry.BLOCKSTATE_PROVIDER_TYPES.getKey(this.type).toString()))
            .put(param0.createString("state"), BlockState.serialize(param0, this.state).getValue());
        return new Dynamic<>(param0, param0.createMap(var0.build())).getValue();
    }
}
