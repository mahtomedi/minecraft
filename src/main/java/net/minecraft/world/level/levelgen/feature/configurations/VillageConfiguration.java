package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.List;
import java.util.Random;
import net.minecraft.resources.ResourceLocation;

public class VillageConfiguration implements FeatureConfiguration {
    private static final List<String> STARTS = ImmutableList.of(
        "village/plains/town_centers",
        "village/desert/town_centers",
        "village/savanna/town_centers",
        "village/snowy/town_centers",
        "village/taiga/town_centers"
    );
    public final ResourceLocation startPool;
    public final int size;

    public VillageConfiguration(String param0, int param1) {
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

    public static <T> VillageConfiguration deserialize(Dynamic<T> param0) {
        String var0 = param0.get("start_pool").asString("");
        int var1 = param0.get("size").asInt(6);
        return new VillageConfiguration(var0, var1);
    }

    public static VillageConfiguration random(Random param0) {
        return new VillageConfiguration(STARTS.get(param0.nextInt(STARTS.size())), param0.nextInt(10));
    }
}
