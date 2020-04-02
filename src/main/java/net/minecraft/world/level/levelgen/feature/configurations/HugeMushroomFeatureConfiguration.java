package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;

public class HugeMushroomFeatureConfiguration implements FeatureConfiguration {
    public final BlockStateProvider capProvider;
    public final BlockStateProvider stemProvider;
    public final int foliageRadius;

    public HugeMushroomFeatureConfiguration(BlockStateProvider param0, BlockStateProvider param1, int param2) {
        this.capProvider = param0;
        this.stemProvider = param1;
        this.foliageRadius = param2;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        Builder<T, T> var0 = ImmutableMap.builder();
        var0.put(param0.createString("cap_provider"), this.capProvider.serialize(param0))
            .put(param0.createString("stem_provider"), this.stemProvider.serialize(param0))
            .put(param0.createString("foliage_radius"), param0.createInt(this.foliageRadius));
        return new Dynamic<>(param0, param0.createMap(var0.build()));
    }

    public static <T> HugeMushroomFeatureConfiguration deserialize(Dynamic<T> param0) {
        BlockStateProviderType<?> var0 = Registry.BLOCKSTATE_PROVIDER_TYPES
            .get(new ResourceLocation(param0.get("cap_provider").get("type").asString().orElseThrow(RuntimeException::new)));
        BlockStateProviderType<?> var1 = Registry.BLOCKSTATE_PROVIDER_TYPES
            .get(new ResourceLocation(param0.get("stem_provider").get("type").asString().orElseThrow(RuntimeException::new)));
        return new HugeMushroomFeatureConfiguration(
            var0.deserialize(param0.get("cap_provider").orElseEmptyMap()),
            var1.deserialize(param0.get("stem_provider").orElseEmptyMap()),
            param0.get("foliage_radius").asInt(2)
        );
    }
}
