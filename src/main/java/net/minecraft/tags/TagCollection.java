package net.minecraft.tags;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableSet.Builder;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface TagCollection<T> {
    Map<ResourceLocation, Tag<T>> getAllTags();

    @Nullable
    default Tag<T> getTag(ResourceLocation param0) {
        return this.getAllTags().get(param0);
    }

    Tag<T> getTagOrEmpty(ResourceLocation var1);

    @Nullable
    ResourceLocation getId(Tag<T> var1);

    default ResourceLocation getIdOrThrow(Tag<T> param0) {
        ResourceLocation var0 = this.getId(param0);
        if (var0 == null) {
            throw new IllegalStateException("Unrecognized tag");
        } else {
            return var0;
        }
    }

    default Collection<ResourceLocation> getAvailableTags() {
        return this.getAllTags().keySet();
    }

    @OnlyIn(Dist.CLIENT)
    default Collection<ResourceLocation> getMatchingTags(T param0) {
        List<ResourceLocation> var0 = Lists.newArrayList();

        for(Entry<ResourceLocation, Tag<T>> var1 : this.getAllTags().entrySet()) {
            if (var1.getValue().contains(param0)) {
                var0.add(var1.getKey());
            }
        }

        return var0;
    }

    default void serializeToNetwork(FriendlyByteBuf param0, DefaultedRegistry<T> param1) {
        Map<ResourceLocation, Tag<T>> var0 = this.getAllTags();
        param0.writeVarInt(var0.size());

        for(Entry<ResourceLocation, Tag<T>> var1 : var0.entrySet()) {
            param0.writeResourceLocation(var1.getKey());
            param0.writeVarInt(var1.getValue().getValues().size());

            for(T var2 : var1.getValue().getValues()) {
                param0.writeVarInt(param1.getId(var2));
            }
        }

    }

    static <T> TagCollection<T> loadFromNetwork(FriendlyByteBuf param0, Registry<T> param1) {
        Map<ResourceLocation, Tag<T>> var0 = Maps.newHashMap();
        int var1 = param0.readVarInt();

        for(int var2 = 0; var2 < var1; ++var2) {
            ResourceLocation var3 = param0.readResourceLocation();
            int var4 = param0.readVarInt();
            Builder<T> var5 = ImmutableSet.builder();

            for(int var6 = 0; var6 < var4; ++var6) {
                var5.add(param1.byId(param0.readVarInt()));
            }

            var0.put(var3, Tag.fromSet(var5.build()));
        }

        return of(var0);
    }

    static <T> TagCollection<T> empty() {
        return of(ImmutableBiMap.of());
    }

    static <T> TagCollection<T> of(Map<ResourceLocation, Tag<T>> param0) {
        final BiMap<ResourceLocation, Tag<T>> var0 = ImmutableBiMap.copyOf(param0);
        return new TagCollection<T>() {
            private final Tag<T> empty = SetTag.empty();

            @Override
            public Tag<T> getTagOrEmpty(ResourceLocation param0) {
                return var0.getOrDefault(param0, this.empty);
            }

            @Nullable
            @Override
            public ResourceLocation getId(Tag<T> param0) {
                return param0 instanceof Tag.Named ? ((Tag.Named)param0).getName() : var0.inverse().get(param0);
            }

            @Override
            public Map<ResourceLocation, Tag<T>> getAllTags() {
                return var0;
            }
        };
    }
}
