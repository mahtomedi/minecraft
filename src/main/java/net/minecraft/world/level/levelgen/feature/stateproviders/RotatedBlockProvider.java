package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

public class RotatedBlockProvider extends BlockStateProvider {
    private final Block block;

    public RotatedBlockProvider(Block param0) {
        super(BlockStateProviderType.SIMPLE_STATE_PROVIDER);
        this.block = param0;
    }

    public <T> RotatedBlockProvider(Dynamic<T> param0) {
        this(BlockState.deserialize(param0.get("state").orElseEmptyMap()).getBlock());
    }

    @Override
    public BlockState getState(Random param0, BlockPos param1) {
        Direction.Axis var0 = Direction.Axis.getRandom(param0);
        return this.block.defaultBlockState().setValue(RotatedPillarBlock.AXIS, var0);
    }

    @Override
    public <T> T serialize(DynamicOps<T> param0) {
        Builder<T, T> var0 = ImmutableMap.builder();
        var0.put(param0.createString("type"), param0.createString(Registry.BLOCKSTATE_PROVIDER_TYPES.getKey(this.type).toString()))
            .put(param0.createString("state"), BlockState.serialize(param0, this.block.defaultBlockState()).getValue());
        return new Dynamic<>(param0, param0.createMap(var0.build())).getValue();
    }
}
