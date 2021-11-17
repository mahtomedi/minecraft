package net.minecraft.tags;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class StaticTags {
    private static final Set<ResourceKey<?>> HELPERS_IDS = Sets.newHashSet();
    private static final List<StaticTagHelper<?>> HELPERS = Lists.newArrayList();

    public static <T> StaticTagHelper<T> create(ResourceKey<? extends Registry<T>> param0, String param1) {
        if (!HELPERS_IDS.add(param0)) {
            throw new IllegalStateException("Duplicate entry for static tag collection: " + param0);
        } else {
            StaticTagHelper<T> var0 = new StaticTagHelper<>(param0, param1);
            HELPERS.add(var0);
            return var0;
        }
    }

    public static void resetAll(TagContainer param0) {
        HELPERS.forEach(param1 -> param1.reset(param0));
    }

    public static void resetAllToEmpty() {
        HELPERS.forEach(StaticTagHelper::resetToEmpty);
    }

    public static Multimap<ResourceKey<? extends Registry<?>>, ResourceLocation> getAllMissingTags(TagContainer param0) {
        Multimap<ResourceKey<? extends Registry<?>>, ResourceLocation> var0 = HashMultimap.create();
        HELPERS.forEach(param2 -> var0.putAll(param2.getKey(), param2.getMissingTags(param0)));
        return var0;
    }

    public static void bootStrap() {
        makeSureAllKnownHelpersAreLoaded();
    }

    private static Set<StaticTagHelper<?>> getAllKnownHelpers() {
        return ImmutableSet.of(BlockTags.HELPER, ItemTags.HELPER, FluidTags.HELPER, EntityTypeTags.HELPER, GameEventTags.HELPER);
    }

    private static void makeSureAllKnownHelpersAreLoaded() {
        Set<ResourceKey<?>> var0 = getAllKnownHelpers().stream().map(StaticTagHelper::getKey).collect(Collectors.toSet());
        if (!Sets.difference(HELPERS_IDS, var0).isEmpty()) {
            throw new IllegalStateException("Missing helper registrations");
        }
    }

    public static void visitHelpers(Consumer<StaticTagHelper<?>> param0) {
        HELPERS.forEach(param0);
    }

    public static TagContainer createCollection() {
        TagContainer.Builder var0 = new TagContainer.Builder();
        makeSureAllKnownHelpersAreLoaded();
        HELPERS.forEach(param1 -> param1.addToCollection(var0));
        return var0.build();
    }
}
