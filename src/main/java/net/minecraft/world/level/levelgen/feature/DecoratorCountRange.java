package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;

public class DecoratorCountRange implements DecoratorConfiguration {
    public final int count;
    public final int bottomOffset;
    public final int topOffset;
    public final int maximum;

    public DecoratorCountRange(int param0, int param1, int param2, int param3) {
        this.count = param0;
        this.bottomOffset = param1;
        this.topOffset = param2;
        this.maximum = param3;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        return new Dynamic<>(
            param0,
            param0.createMap(
                ImmutableMap.of(
                    param0.createString("count"),
                    param0.createInt(this.count),
                    param0.createString("bottom_offset"),
                    param0.createInt(this.bottomOffset),
                    param0.createString("top_offset"),
                    param0.createInt(this.topOffset),
                    param0.createString("maximum"),
                    param0.createInt(this.maximum)
                )
            )
        );
    }

    public static DecoratorCountRange deserialize(Dynamic<?> param0) {
        int var0 = param0.get("count").asInt(0);
        int var1 = param0.get("bottom_offset").asInt(0);
        int var2 = param0.get("top_offset").asInt(0);
        int var3 = param0.get("maximum").asInt(0);
        return new DecoratorCountRange(var0, var1, var2, var3);
    }
}
