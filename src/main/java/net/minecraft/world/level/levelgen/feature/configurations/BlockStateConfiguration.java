package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.Util;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.OverworldGeneratorSettings;

public class BlockStateConfiguration implements FeatureConfiguration {
    public final BlockState state;

    public BlockStateConfiguration(BlockState param0) {
        this.state = param0;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        return new Dynamic<>(param0, param0.createMap(ImmutableMap.of(param0.createString("state"), BlockState.serialize(param0, this.state).getValue())));
    }

    public static <T> BlockStateConfiguration deserialize(Dynamic<T> param0) {
        BlockState var0 = param0.get("state").map(BlockState::deserialize).orElse(Blocks.AIR.defaultBlockState());
        return new BlockStateConfiguration(var0);
    }

    public static BlockStateConfiguration safeRandom(Random param0) {
        return new BlockStateConfiguration(Util.randomObject(param0, OverworldGeneratorSettings.SAFE_BLOCKS));
    }
}
