package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class RainbowBlockProvider extends BlockStateProvider {
    private final List<BlockState> blocks;

    public RainbowBlockProvider(Dynamic<?> param0) {
        this(param0.get("states").asStream().map(BlockState::deserialize).collect(ImmutableList.toImmutableList()));
    }

    public RainbowBlockProvider(List<BlockState> param0) {
        super(BlockStateProviderType.RAINBOW_BLOCK_PROVIDER);
        this.blocks = param0;
    }

    @Override
    public BlockState getState(Random param0, BlockPos param1) {
        int var0 = Math.abs(param1.getX() + param1.getY() + param1.getZ());
        return this.blocks.get(var0 % this.blocks.size());
    }

    @Override
    public <T> T serialize(DynamicOps<T> param0) {
        return param0.createMap(
            ImmutableMap.of(
                param0.createString("states"), param0.createList(this.blocks.stream().map(param1 -> BlockState.serialize(param0, param1).getValue()))
            )
        );
    }
}
