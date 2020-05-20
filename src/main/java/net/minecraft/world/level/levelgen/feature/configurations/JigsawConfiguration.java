package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public class JigsawConfiguration implements FeatureConfiguration {
    public static final Codec<JigsawConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    ResourceLocation.CODEC.fieldOf("start_pool").forGetter(JigsawConfiguration::getStartPool),
                    Codec.INT.fieldOf("size").forGetter(JigsawConfiguration::getSize)
                )
                .apply(param0, JigsawConfiguration::new)
    );
    public final ResourceLocation startPool;
    public final int size;

    public JigsawConfiguration(ResourceLocation param0, int param1) {
        this.startPool = param0;
        this.size = param1;
    }

    public int getSize() {
        return this.size;
    }

    public ResourceLocation getStartPool() {
        return this.startPool;
    }
}
