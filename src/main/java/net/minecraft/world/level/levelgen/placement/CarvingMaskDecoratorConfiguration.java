package net.minecraft.world.level.levelgen.placement;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.Util;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class CarvingMaskDecoratorConfiguration implements DecoratorConfiguration {
    protected final GenerationStep.Carving step;
    protected final float probability;

    public CarvingMaskDecoratorConfiguration(GenerationStep.Carving param0, float param1) {
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

    public static CarvingMaskDecoratorConfiguration deserialize(Dynamic<?> param0) {
        GenerationStep.Carving var0 = GenerationStep.Carving.valueOf(param0.get("step").asString(""));
        float var1 = param0.get("probability").asFloat(0.0F);
        return new CarvingMaskDecoratorConfiguration(var0, var1);
    }

    public static CarvingMaskDecoratorConfiguration random(Random param0) {
        return new CarvingMaskDecoratorConfiguration(Util.randomEnum(GenerationStep.Carving.class, param0), param0.nextFloat() / 2.0F);
    }
}
