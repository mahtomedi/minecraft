package net.minecraft.world.level.levelgen.placement;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.DecoratorConfiguration;

public class DecoratorCarvingMaskConfig implements DecoratorConfiguration {
    protected final GenerationStep.Carving step;
    protected final float probability;

    public DecoratorCarvingMaskConfig(GenerationStep.Carving param0, float param1) {
        this.step = param0;
        this.probability = param1;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        return new Dynamic<>(
            param0,
            param0.createMap(
                ImmutableMap.of(
                    param0.createString("step"),
                    param0.createString(this.step.toString()),
                    param0.createString("probability"),
                    param0.createFloat(this.probability)
                )
            )
        );
    }

    public static DecoratorCarvingMaskConfig deserialize(Dynamic<?> param0) {
        GenerationStep.Carving var0 = GenerationStep.Carving.valueOf(param0.get("step").asString(""));
        float var1 = param0.get("probability").asFloat(0.0F);
        return new DecoratorCarvingMaskConfig(var0, var1);
    }
}
