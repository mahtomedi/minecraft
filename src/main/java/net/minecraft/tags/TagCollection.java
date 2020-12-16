package net.minecraft.tags;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableSet.Builder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
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

    default TagCollection.NetworkPayload serializeToNetwork(Registry<T> param0) {
        Map<ResourceLocation, Tag<T>> var0 = this.getAllTags();
        Map<ResourceLocation, IntList> var1 = Maps.newHashMapWithExpectedSize(var0.size());
        var0.forEach((param2, param3) -> {
            List<T> var0x = param3.getValues();
            IntList var1x = new IntArrayList(var0x.size());

            for(T var2x : var0x) {
                var1x.add(param0.getId((T)var2x));
            }

            var1.put(param2, var1x);
        });
        return new TagCollection.NetworkPayload(var1);
    }

    @OnlyIn(Dist.CLIENT)
    static <T> TagCollection<T> createFromNetwork(TagCollection.NetworkPayload param0, Registry<? extends T> param1) {
        Map<ResourceLocation, Tag<T>> var0 = Maps.newHashMapWithExpectedSize(param0.tags.size());
        param0.tags.forEach((param2, param3) -> {
            Builder<T> var0x = ImmutableSet.builder();

            for(int var1x : param3) {
                var0x.add(param1.byId(var1x));
            }

            var0.put(param2, Tag.fromSet(var0x.build()));
        });
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

    public static class NetworkPayload {
        private final Map<ResourceLocation, IntList> tags;

        private NetworkPayload(Map<ResourceLocation, IntList> param0) {
            this.tags = param0;
        }

        public void write(FriendlyByteBuf param0) {
            param0.writeVarInt(this.tags.size());

            for(Entry<ResourceLocation, IntList> var0 : this.tags.entrySet()) {
                param0.writeResourceLocation(var0.getKey());
                param0.writeVarInt(var0.getValue().size());
                var0.getValue().forEach(param0::writeVarInt);
            }

        }

        public static TagCollection.NetworkPayload read(FriendlyByteBuf param0) {
            Map<ResourceLocation, IntList> var0 = Maps.newHashMap();
            int var1 = param0.readVarInt();

            for(int var2 = 0; var2 < var1; ++var2) {
                ResourceLocation var3 = param0.readResourceLocation();
                int var4 = param0.readVarInt();
                IntList var5 = new IntArrayList(var4);

                for(int var6 = 0; var6 < var4; ++var6) {
                    var5.add(param0.readVarInt());
                }

                var0.put(var3, var5);
            }

            return new TagCollection.NetworkPayload(var0);
        }
    }
}
