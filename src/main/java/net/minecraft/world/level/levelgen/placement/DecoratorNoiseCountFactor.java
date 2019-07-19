package net.minecraft.world.level.levelgen.placement;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.DecoratorConfiguration;

public class DecoratorNoiseCountFactor implements DecoratorConfiguration {
    public final int noiseToCountRatio;
    public final double noiseFactor;
    public final double noiseOffset;
    public final Heightmap.Types heightmap;

    public DecoratorNoiseCountFactor(int param0, double param1, double param2, Heightmap.Types param3) {
        this.noiseToCountRatio = param0;
        this.noiseFactor = param1;
        this.noiseOffset = param2;
        this.heightmap = param3;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        return new Dynamic<>(
            param0,
            param0.createMap(
                ImmutableMap.of(
                    param0.createString("noise_to_count_ratio"),
                    param0.createInt(this.noiseToCountRatio),
                    param0.createString("noise_factor"),
                    param0.createDouble(this.noiseFactor),
                    param0.createString("noise_offset"),
                    param0.createDouble(this.noiseOffset),
                    param0.createString("heightmap"),
                    param0.createString(this.heightmap.getSerializationKey())
                )
            )
        );
    }

    public static DecoratorNoiseCountFactor deserialize(Dynamic<?> param0) {
        int var0 = param0.get("noise_to_count_ratio").asInt(10);
        double var1 = param0.get("noise_factor").asDouble(80.0);
        double var2 = param0.get("noise_offset").asDouble(0.0);
        Heightmap.Types var3 = Heightmap.Types.getFromKey(param0.get("heightmap").asString("OCEAN_FLOOR_WG"));
        return new DecoratorNoiseCountFactor(var0, var1, var2, var3);
    }
}
