package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class GrassConfiguration implements FeatureConfiguration {
    public final BlockState state;

    public GrassConfiguration(BlockState param0) {
        this.state = param0;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        return new Dynamic<>(param0, param0.createMap(ImmutableMap.of(param0.createString("state"), BlockState.serialize(param0, this.state).getValue())));
    }

    public static <T> GrassConfiguration deserialize(Dynamic<T> param0) {
        BlockState var0 = param0.get("state").map(BlockState::deserialize).orElse(Blocks.AIR.defaultBlockState());
        return new GrassConfiguration(var0);
    }
}
