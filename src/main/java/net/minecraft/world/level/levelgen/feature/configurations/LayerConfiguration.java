package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class LayerConfiguration implements FeatureConfiguration {
    public final int height;
    public final BlockState state;

    public LayerConfiguration(int param0, BlockState param1) {
        this.height = param0;
        this.state = param1;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        return new Dynamic<>(
            param0,
            param0.createMap(
                ImmutableMap.of(
                    param0.createString("height"),
                    param0.createInt(this.height),
                    param0.createString("state"),
                    BlockState.serialize(param0, this.state).getValue()
                )
            )
        );
    }

    public static <T> LayerConfiguration deserialize(Dynamic<T> param0) {
        int var0 = param0.get("height").asInt(0);
        BlockState var1 = param0.get("state").map(BlockState::deserialize).orElse(Blocks.AIR.defaultBlockState());
        return new LayerConfiguration(var0, var1);
    }

    public static LayerConfiguration random(Random param0) {
        return new LayerConfiguration(param0.nextInt(5), Registry.BLOCK.getRandom(param0).defaultBlockState());
    }
}
