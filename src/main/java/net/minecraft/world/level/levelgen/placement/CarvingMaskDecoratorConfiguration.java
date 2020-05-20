package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class CarvingMaskDecoratorConfiguration implements DecoratorConfiguration {
    public static final Codec<CarvingMaskDecoratorConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    GenerationStep.Carving.CODEC.fieldOf("step").forGetter(param0x -> param0x.step),
                    Codec.FLOAT.fieldOf("probability").forGetter(param0x -> param0x.probability)
                )
                .apply(param0, CarvingMaskDecoratorConfiguration::new)
    );
    protected final GenerationStep.Carving step;
    protected final float probability;

    public CarvingMaskDecoratorConfiguration(GenerationStep.Carving param0, float param1) {
        this.step = param0;
        this.probability = param1;
    }
}
