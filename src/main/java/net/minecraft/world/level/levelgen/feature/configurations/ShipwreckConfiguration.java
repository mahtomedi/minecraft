package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;

public class ShipwreckConfiguration implements FeatureConfiguration {
    public static final Codec<ShipwreckConfiguration> CODEC = Codec.BOOL
        .fieldOf("is_beached")
        .orElse(false)
        .xmap(ShipwreckConfiguration::new, param0 -> param0.isBeached)
        .codec();
    public final boolean isBeached;

    public ShipwreckConfiguration(boolean param0) {
        this.isBeached = param0;
    }
}
