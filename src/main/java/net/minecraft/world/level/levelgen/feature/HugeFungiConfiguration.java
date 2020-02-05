package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class HugeFungiConfiguration implements FeatureConfiguration {
    public final BlockState stemState;
    public final BlockState hatState;
    public final BlockState decorState;
    public final boolean planted;

    public HugeFungiConfiguration(BlockState param0, BlockState param1, BlockState param2, boolean param3) {
        this.stemState = param0;
        this.hatState = param1;
        this.decorState = param2;
        this.planted = param3;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        return new Dynamic<>(
            param0,
            param0.createMap(
                ImmutableMap.of(
                    param0.createString("stem_state"),
                    BlockState.serialize(param0, this.stemState).getValue(),
                    param0.createString("hat_state"),
                    BlockState.serialize(param0, this.hatState).getValue(),
                    param0.createString("decor_state"),
                    BlockState.serialize(param0, this.decorState).getValue(),
                    param0.createString("planted"),
                    param0.createBoolean(this.planted)
                )
            )
        );
    }

    public static <T> HugeFungiConfiguration deserialize(Dynamic<T> param0) {
        BlockState var0 = param0.get("stem_state").map(BlockState::deserialize).orElse(Blocks.AIR.defaultBlockState());
        BlockState var1 = param0.get("hat_state").map(BlockState::deserialize).orElse(Blocks.AIR.defaultBlockState());
        BlockState var2 = param0.get("decor_state").map(BlockState::deserialize).orElse(Blocks.AIR.defaultBlockState());
        boolean var3 = param0.get("planted").asBoolean(false);
        return new HugeFungiConfiguration(var0, var1, var2, var3);
    }
}
