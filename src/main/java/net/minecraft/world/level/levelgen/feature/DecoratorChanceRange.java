package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;

public class DecoratorChanceRange implements DecoratorConfiguration {
    public final float chance;
    public final int bottomOffset;
    public final int topOffset;
    public final int top;

    public DecoratorChanceRange(float param0, int param1, int param2, int param3) {
        this.chance = param0;
        this.bottomOffset = param1;
        this.topOffset = param2;
        this.top = param3;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        return new Dynamic<>(
            param0,
            param0.createMap(
                ImmutableMap.of(
                    param0.createString("chance"),
                    param0.createFloat(this.chance),
                    param0.createString("bottom_offset"),
                    param0.createInt(this.bottomOffset),
                    param0.createString("top_offset"),
                    param0.createInt(this.topOffset),
                    param0.createString("top"),
                    param0.createInt(this.top)
                )
            )
        );
    }

    public static DecoratorChanceRange deserialize(Dynamic<?> param0) {
        float var0 = param0.get("chance").asFloat(0.0F);
        int var1 = param0.get("bottom_offset").asInt(0);
        int var2 = param0.get("top_offset").asInt(0);
        int var3 = param0.get("top").asInt(0);
        return new DecoratorChanceRange(var0, var1, var2, var3);
    }
}
