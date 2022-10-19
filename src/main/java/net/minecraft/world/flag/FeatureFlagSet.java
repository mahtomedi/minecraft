package net.minecraft.world.flag;

import it.unimi.dsi.fastutil.HashCommon;
import java.util.Arrays;
import java.util.Collection;
import javax.annotation.Nullable;

public final class FeatureFlagSet {
    private static final FeatureFlagSet EMPTY = new FeatureFlagSet(null, 0L);
    public static final int MAX_CONTAINER_SIZE = 64;
    @Nullable
    private final FeatureFlagUniverse universe;
    private final long mask;

    private FeatureFlagSet(@Nullable FeatureFlagUniverse param0, long param1) {
        this.universe = param0;
        this.mask = param1;
    }

    static FeatureFlagSet create(FeatureFlagUniverse param0, Collection<FeatureFlag> param1) {
        if (param1.isEmpty()) {
            return EMPTY;
        } else {
            long var0 = computeMask(param0, 0L, param1);
            return new FeatureFlagSet(param0, var0);
        }
    }

    public static FeatureFlagSet of() {
        return EMPTY;
    }

    public static FeatureFlagSet of(FeatureFlag param0) {
        return new FeatureFlagSet(param0.universe, param0.mask);
    }

    public static FeatureFlagSet of(FeatureFlag param0, FeatureFlag... param1) {
        long var0 = param1.length == 0 ? param0.mask : computeMask(param0.universe, param0.mask, Arrays.asList(param1));
        return new FeatureFlagSet(param0.universe, var0);
    }

    private static long computeMask(FeatureFlagUniverse param0, long param1, Iterable<FeatureFlag> param2) {
        for(FeatureFlag var0 : param2) {
            if (param0 != var0.universe) {
                throw new IllegalStateException("Mismatched feature universe, expected '" + param0 + "', but got '" + var0.universe + "'");
            }

            param1 |= var0.mask;
        }

        return param1;
    }

    public boolean contains(FeatureFlag param0) {
        if (this.universe != param0.universe) {
            return false;
        } else {
            return (this.mask & param0.mask) != 0L;
        }
    }

    public boolean isSubsetOf(FeatureFlagSet param0) {
        if (this.universe == null) {
            return true;
        } else if (this.universe != param0.universe) {
            return false;
        } else {
            return (this.mask & ~param0.mask) == 0L;
        }
    }

    public FeatureFlagSet join(FeatureFlagSet param0) {
        if (this.universe == null) {
            return param0;
        } else if (param0.universe == null) {
            return this;
        } else if (this.universe != param0.universe) {
            throw new IllegalArgumentException("Mismatched set elements: '" + this.universe + "' != '" + param0.universe + "'");
        } else {
            return new FeatureFlagSet(this.universe, this.mask | param0.mask);
        }
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else {
            if (param0 instanceof FeatureFlagSet var0 && this.universe == var0.universe && this.mask == var0.mask) {
                return true;
            }

            return false;
        }
    }

    @Override
    public int hashCode() {
        return (int)HashCommon.mix(this.mask);
    }
}
