package net.minecraft.world.flag;

import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public class FeatureFlagRegistry {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final FeatureFlagUniverse universe;
    private final Map<ResourceLocation, FeatureFlag> names;
    private final FeatureFlagSet allFlags;

    FeatureFlagRegistry(FeatureFlagUniverse param0, FeatureFlagSet param1, Map<ResourceLocation, FeatureFlag> param2) {
        this.universe = param0;
        this.names = param2;
        this.allFlags = param1;
    }

    public boolean isSubset(FeatureFlagSet param0) {
        return param0.isSubsetOf(this.allFlags);
    }

    public FeatureFlagSet allFlags() {
        return this.allFlags;
    }

    public FeatureFlagSet fromNames(Iterable<ResourceLocation> param0) {
        return this.fromNames(param0, param0x -> LOGGER.warn("Unknown feature flag: {}", param0x));
    }

    public FeatureFlagSet subset(FeatureFlag... param0) {
        return FeatureFlagSet.create(this.universe, Arrays.asList(param0));
    }

    public FeatureFlagSet fromNames(Iterable<ResourceLocation> param0, Consumer<ResourceLocation> param1) {
        Set<FeatureFlag> var0 = Sets.newIdentityHashSet();

        for(ResourceLocation var1 : param0) {
            FeatureFlag var2 = this.names.get(var1);
            if (var2 == null) {
                param1.accept(var1);
            } else {
                var0.add(var2);
            }
        }

        return FeatureFlagSet.create(this.universe, var0);
    }

    public Set<ResourceLocation> toNames(FeatureFlagSet param0) {
        Set<ResourceLocation> var0 = new HashSet<>();
        this.names.forEach((param2, param3) -> {
            if (param0.contains(param3)) {
                var0.add(param2);
            }

        });
        return var0;
    }

    public Codec<FeatureFlagSet> codec() {
        return ResourceLocation.CODEC.listOf().comapFlatMap(param0 -> {
            Set<ResourceLocation> var0 = new HashSet<>();
            FeatureFlagSet var1 = this.fromNames(param0, var0::add);
            return !var0.isEmpty() ? DataResult.error(() -> "Unknown feature ids: " + var0, var1) : DataResult.success(var1);
        }, param0 -> List.copyOf(this.toNames(param0)));
    }

    public static class Builder {
        private final FeatureFlagUniverse universe;
        private int id;
        private final Map<ResourceLocation, FeatureFlag> flags = new LinkedHashMap<>();

        public Builder(String param0) {
            this.universe = new FeatureFlagUniverse(param0);
        }

        public FeatureFlag createVanilla(String param0) {
            return this.create(new ResourceLocation("minecraft", param0));
        }

        public FeatureFlag create(ResourceLocation param0) {
            if (this.id >= 64) {
                throw new IllegalStateException("Too many feature flags");
            } else {
                FeatureFlag var0 = new FeatureFlag(this.universe, this.id++);
                FeatureFlag var1 = this.flags.put(param0, var0);
                if (var1 != null) {
                    throw new IllegalStateException("Duplicate feature flag " + param0);
                } else {
                    return var0;
                }
            }
        }

        public FeatureFlagRegistry build() {
            FeatureFlagSet var0 = FeatureFlagSet.create(this.universe, this.flags.values());
            return new FeatureFlagRegistry(this.universe, var0, Map.copyOf(this.flags));
        }
    }
}
