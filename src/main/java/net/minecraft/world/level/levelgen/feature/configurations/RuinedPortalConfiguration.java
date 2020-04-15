package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.world.level.levelgen.feature.RuinedPortalFeature;

public class RuinedPortalConfiguration implements FeatureConfiguration {
    public final RuinedPortalFeature.Type portalType;

    public RuinedPortalConfiguration() {
        this(RuinedPortalFeature.Type.STANDARD);
    }

    public RuinedPortalConfiguration(RuinedPortalFeature.Type param0) {
        this.portalType = param0;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        return new Dynamic<>(param0, param0.createMap(ImmutableMap.of(param0.createString("portal_type"), param0.createString(this.portalType.getName()))));
    }

    public static <T> RuinedPortalConfiguration deserialize(Dynamic<T> param0) {
        RuinedPortalFeature.Type var0 = RuinedPortalFeature.Type.byName(param0.get("portal_type").asString(""));
        return new RuinedPortalConfiguration(var0);
    }
}
