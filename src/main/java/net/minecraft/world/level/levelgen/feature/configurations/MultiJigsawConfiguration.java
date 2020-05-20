package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import net.minecraft.resources.ResourceLocation;

public class MultiJigsawConfiguration implements FeatureConfiguration {
    public static final Codec<MultiJigsawConfiguration> CODEC = JigsawConfiguration.CODEC
        .listOf()
        .xmap(MultiJigsawConfiguration::new, param0 -> param0.configurations);
    private final List<JigsawConfiguration> configurations;

    public MultiJigsawConfiguration(Map<String, Integer> param0) {
        this(
            param0.entrySet()
                .stream()
                .map(param0x -> new JigsawConfiguration(new ResourceLocation(param0x.getKey()), param0x.getValue()))
                .collect(Collectors.toList())
        );
    }

    private MultiJigsawConfiguration(List<JigsawConfiguration> param0) {
        this.configurations = param0;
    }

    public JigsawConfiguration getRandomPool(Random param0) {
        return this.configurations.get(param0.nextInt(this.configurations.size()));
    }
}
