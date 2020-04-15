package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.resources.ResourceLocation;

public class JigsawConfiguration implements FeatureConfiguration {
    public final ResourceLocation startPool;
    public final int size;

    public JigsawConfiguration(String param0, int param1) {
        this.startPool = new ResourceLocation(param0);
        this.size = param1;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        return new Dynamic<>(
            param0,
            param0.createMap(
                ImmutableMap.of(
                    param0.createString("start_pool"), param0.createString(this.startPool.toString()), param0.createString("size"), param0.createInt(this.size)
                )
            )
        );
    }

    public static <T> JigsawConfiguration deserialize(Dynamic<T> param0) {
        String var0 = param0.get("start_pool").asString("");
        int var1 = param0.get("size").asInt(6);
        return new JigsawConfiguration(var0, var1);
    }

    public int getSize() {
        return this.size;
    }

    public String getStartPool() {
        return this.startPool.toString();
    }
}
