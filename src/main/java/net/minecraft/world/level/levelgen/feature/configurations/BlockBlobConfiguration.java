package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class BlockBlobConfiguration implements FeatureConfiguration {
    public final BlockState state;
    public final int startRadius;

    public BlockBlobConfiguration(BlockState param0, int param1) {
        this.state = param0;
        this.startRadius = param1;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        return new Dynamic<>(
            param0,
            param0.createMap(
                ImmutableMap.of(
                    param0.createString("state"),
                    BlockState.serialize(param0, this.state).getValue(),
                    param0.createString("start_radius"),
                    param0.createInt(this.startRadius)
                )
            )
        );
    }

    public static <T> BlockBlobConfiguration deserialize(Dynamic<T> param0) {
        BlockState var0 = param0.get("state").map(BlockState::deserialize).orElse(Blocks.AIR.defaultBlockState());
        int var1 = param0.get("start_radius").asInt(0);
        return new BlockBlobConfiguration(var0, var1);
    }
}
