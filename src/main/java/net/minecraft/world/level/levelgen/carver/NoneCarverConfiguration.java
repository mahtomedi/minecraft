package net.minecraft.world.level.levelgen.carver;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;

public class NoneCarverConfiguration implements CarverConfiguration {
    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        return new Dynamic<>(param0, param0.emptyMap());
    }
}
