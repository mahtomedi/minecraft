package net.minecraft.world.flag;

public class FeatureFlag {
    final FeatureFlagUniverse universe;
    final long mask;

    FeatureFlag(FeatureFlagUniverse param0, int param1) {
        this.universe = param0;
        this.mask = 1L << param1;
    }
}
