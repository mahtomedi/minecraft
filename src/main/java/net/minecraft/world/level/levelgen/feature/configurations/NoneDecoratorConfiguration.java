package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;

public class NoneDecoratorConfiguration implements DecoratorConfiguration {
    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        return new Dynamic<>(param0, param0.emptyMap());
    }

    public static NoneDecoratorConfiguration deserialize(Dynamic<?> param0) {
        return new NoneDecoratorConfiguration();
    }

    public static NoneDecoratorConfiguration random(Random param0) {
        return DecoratorConfiguration.NONE;
    }
}
