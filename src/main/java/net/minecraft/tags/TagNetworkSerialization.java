package net.minecraft.tags;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.RegistryLayer;

public class TagNetworkSerialization {
    public static Map<ResourceKey<? extends Registry<?>>, TagNetworkSerialization.NetworkPayload> serializeTagsToNetwork(
        LayeredRegistryAccess<RegistryLayer> param0
    ) {
        return RegistrySynchronization.networkSafeRegistries(param0)
            .map(param0x -> Pair.of(param0x.key(), serializeToNetwork(param0x.value())))
            .filter(param0x -> !param0x.getSecond().isEmpty())
            .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
    }

    private static <T> TagNetworkSerialization.NetworkPayload serializeToNetwork(Registry<T> param0) {
        Map<ResourceLocation, IntList> var0 = new HashMap<>();
        param0.getTags().forEach(param2 -> {
            HolderSet<T> var0x = param2.getSecond();
            IntList var1x = new IntArrayList(var0x.size());

            for(Holder<T> var2 : var0x) {
                if (var2.kind() != Holder.Kind.REFERENCE) {
                    throw new IllegalStateException("Can't serialize unregistered value " + var2);
                }

                var1x.add(param0.getId(var2.value()));
            }

            var0.put(param2.getFirst().location(), var1x);
        });
        return new TagNetworkSerialization.NetworkPayload(var0);
    }

    public static <T> void deserializeTagsFromNetwork(
        ResourceKey<? extends Registry<T>> param0,
        Registry<T> param1,
        TagNetworkSerialization.NetworkPayload param2,
        TagNetworkSerialization.TagOutput<T> param3
    ) {
        param2.tags.forEach((param3x, param4) -> {
            TagKey<T> var0x = TagKey.create(param0, param3x);
            List<Holder<T>> var1x = param4.intStream().mapToObj(param1::getHolder).flatMap(Optional::stream).collect(Collectors.toUnmodifiableList());
            param3.accept(var0x, var1x);
        });
    }

    public static final class NetworkPayload {
        final Map<ResourceLocation, IntList> tags;

        NetworkPayload(Map<ResourceLocation, IntList> param0) {
            this.tags = param0;
        }

        public void write(FriendlyByteBuf param0) {
            param0.writeMap(this.tags, FriendlyByteBuf::writeResourceLocation, FriendlyByteBuf::writeIntIdList);
        }

        public static TagNetworkSerialization.NetworkPayload read(FriendlyByteBuf param0) {
            return new TagNetworkSerialization.NetworkPayload(param0.readMap(FriendlyByteBuf::readResourceLocation, FriendlyByteBuf::readIntIdList));
        }

        public boolean isEmpty() {
            return this.tags.isEmpty();
        }
    }

    @FunctionalInterface
    public interface TagOutput<T> {
        void accept(TagKey<T> var1, List<Holder<T>> var2);
    }
}
