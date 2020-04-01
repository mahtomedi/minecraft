package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;

public class BlockPileConfiguration implements FeatureConfiguration {
    public final BlockStateProvider stateProvider;

    public BlockPileConfiguration(BlockStateProvider param0) {
        this.stateProvider = param0;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        Builder<T, T> var0 = ImmutableMap.builder();
        var0.put(param0.createString("state_provider"), this.stateProvider.serialize(param0));
        return new Dynamic<>(param0, param0.createMap(var0.build()));
    }

    public static <T> BlockPileConfiguration deserialize(Dynamic<T> param0) {
        BlockStateProviderType<?> var0 = Registry.BLOCKSTATE_PROVIDER_TYPES
            .get(new ResourceLocation(param0.get("state_provider").get("type").asString().orElseThrow(RuntimeException::new)));
        return new BlockPileConfiguration(var0.deserialize(param0.get("state_provider").orElseEmptyMap()));
    }

    public static BlockPileConfiguration random(Random param0) {
        return new BlockPileConfiguration(BlockStateProvider.random(param0));
    }
}
