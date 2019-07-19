package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;

public class NoneDecoratorConfiguration implements DecoratorConfiguration {
    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        return new Dynamic<>(param0, param0.emptyMap());
    }

    public static NoneDecoratorConfiguration deserialize(Dynamic<?> param0) {
        return new NoneDecoratorConfiguration();
    }
}
