package net.minecraft.tags;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class StaticTags {
    private static final Map<ResourceLocation, StaticTagHelper<?>> HELPERS = Maps.newHashMap();

    public static <T> StaticTagHelper<T> create(ResourceLocation param0, Function<TagContainer, TagCollection<T>> param1) {
        StaticTagHelper<T> var0 = new StaticTagHelper<>(param1);
        StaticTagHelper<?> var1 = HELPERS.putIfAbsent(param0, var0);
        if (var1 != null) {
            throw new IllegalStateException("Duplicate entry for static tag collection: " + param0);
        } else {
            return var0;
        }
    }

    public static void resetAll(TagContainer param0) {
        HELPERS.values().forEach(param1 -> param1.reset(param0));
    }

    @OnlyIn(Dist.CLIENT)
    public static void resetAllToEmpty() {
        HELPERS.values().forEach(StaticTagHelper::resetToEmpty);
    }

    public static Multimap<ResourceLocation, ResourceLocation> getAllMissingTags(TagContainer param0) {
        Multimap<ResourceLocation, ResourceLocation> var0 = HashMultimap.create();
        HELPERS.forEach((param2, param3) -> var0.putAll(param2, param3.getMissingTags(param0)));
        return var0;
    }

    public static void bootStrap() {
        StaticTagHelper[] var0 = new StaticTagHelper[]{BlockTags.HELPER, ItemTags.HELPER, FluidTags.HELPER, EntityTypeTags.HELPER};
        boolean var1 = Stream.of(var0).anyMatch(param0 -> !HELPERS.containsValue(param0));
        if (var1) {
            throw new IllegalStateException("Missing helper registrations");
        }
    }
}
