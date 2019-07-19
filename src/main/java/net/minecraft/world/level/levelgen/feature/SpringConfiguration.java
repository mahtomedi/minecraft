package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public class SpringConfiguration implements FeatureConfiguration {
    public final FluidState state;

    public SpringConfiguration(FluidState param0) {
        this.state = param0;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        return new Dynamic<>(param0, param0.createMap(ImmutableMap.of(param0.createString("state"), FluidState.serialize(param0, this.state).getValue())));
    }

    public static <T> SpringConfiguration deserialize(Dynamic<T> param0) {
        FluidState var0 = param0.get("state").map(FluidState::deserialize).orElse(Fluids.EMPTY.defaultFluidState());
        return new SpringConfiguration(var0);
    }
}
