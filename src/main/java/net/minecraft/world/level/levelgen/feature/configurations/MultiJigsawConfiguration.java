package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class MultiJigsawConfiguration implements FeatureConfiguration {
    private final List<JigsawConfiguration> configurations;

    public MultiJigsawConfiguration(Map<String, Integer> param0) {
        this.configurations = param0.entrySet()
            .stream()
            .map(param0x -> new JigsawConfiguration(param0x.getKey(), param0x.getValue()))
            .collect(Collectors.toList());
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        return new Dynamic<>(param0, param0.createList(this.configurations.stream().map(param1 -> param1.serialize(param0).getValue())));
    }

    public static <T> MultiJigsawConfiguration deserialize(Dynamic<T> param0) {
        List<JigsawConfiguration> var0 = param0.asList(JigsawConfiguration::deserialize);
        return new MultiJigsawConfiguration(var0.stream().collect(Collectors.toMap(JigsawConfiguration::getStartPool, JigsawConfiguration::getSize)));
    }

    public JigsawConfiguration getRandomPool(Random param0) {
        return this.configurations.get(param0.nextInt(this.configurations.size()));
    }
}
