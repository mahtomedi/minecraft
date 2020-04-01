package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class ReplaceBlockConfiguration implements FeatureConfiguration {
    public final BlockState target;
    public final BlockState state;

    public ReplaceBlockConfiguration(BlockState param0, BlockState param1) {
        this.target = param0;
        this.state = param1;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        return new Dynamic<>(
            param0,
            param0.createMap(
                ImmutableMap.of(
                    param0.createString("target"),
                    BlockState.serialize(param0, this.target).getValue(),
                    param0.createString("state"),
                    BlockState.serialize(param0, this.state).getValue()
                )
            )
        );
    }

    public static <T> ReplaceBlockConfiguration deserialize(Dynamic<T> param0) {
        BlockState var0 = param0.get("target").map(BlockState::deserialize).orElse(Blocks.AIR.defaultBlockState());
        BlockState var1 = param0.get("state").map(BlockState::deserialize).orElse(Blocks.AIR.defaultBlockState());
        return new ReplaceBlockConfiguration(var0, var1);
    }

    public static ReplaceBlockConfiguration random(Random param0) {
        return new ReplaceBlockConfiguration(Registry.BLOCK.getRandom(param0).defaultBlockState(), Registry.BLOCK.getRandom(param0).defaultBlockState());
    }
}
