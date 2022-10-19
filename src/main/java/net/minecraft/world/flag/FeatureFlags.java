package net.minecraft.world.flag;

import com.mojang.serialization.Codec;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.resources.ResourceLocation;

public class FeatureFlags {
    public static final FeatureFlag VANILLA;
    public static final FeatureFlag BUNDLE;
    public static final FeatureFlag UPDATE_1_20;
    public static final FeatureFlagRegistry REGISTRY;
    public static final Codec<FeatureFlagSet> CODEC = REGISTRY.codec();
    public static final FeatureFlagSet VANILLA_SET = FeatureFlagSet.of(VANILLA);
    public static final FeatureFlagSet DEFAULT_FLAGS = VANILLA_SET;

    public static String printMissingFlags(FeatureFlagSet param0, FeatureFlagSet param1) {
        return printMissingFlags(REGISTRY, param0, param1);
    }

    public static String printMissingFlags(FeatureFlagRegistry param0, FeatureFlagSet param1, FeatureFlagSet param2) {
        Set<ResourceLocation> var0 = param0.toNames(param2);
        Set<ResourceLocation> var1 = param0.toNames(param1);
        return var0.stream().filter(param1x -> !var1.contains(param1x)).map(ResourceLocation::toString).collect(Collectors.joining(", "));
    }

    public static boolean isExperimental(FeatureFlagSet param0) {
        return !param0.isSubsetOf(VANILLA_SET);
    }

    static {
        FeatureFlagRegistry.Builder var0 = new FeatureFlagRegistry.Builder("main");
        VANILLA = var0.createVanilla("vanilla");
        BUNDLE = var0.createVanilla("bundle");
        UPDATE_1_20 = var0.createVanilla("update_1_20");
        REGISTRY = var0.build();
    }
}
