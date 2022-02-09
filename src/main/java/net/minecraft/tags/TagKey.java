package net.minecraft.tags;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public record TagKey<T>(ResourceKey<? extends Registry<T>> registry, ResourceLocation location) {
    private static final Interner<TagKey<?>> VALUES = Interners.newStrongInterner();

    @Deprecated
    public TagKey(ResourceKey<? extends Registry<T>> param0, ResourceLocation param1) {
        this.registry = param0;
        this.location = param1;
    }

    public static <T> Codec<TagKey<T>> codec(ResourceKey<? extends Registry<T>> param0) {
        return ResourceLocation.CODEC.xmap(param1 -> create(param0, param1), TagKey::location);
    }

    public static <T> Codec<TagKey<T>> hashedCodec(ResourceKey<? extends Registry<T>> param0) {
        return Codec.STRING
            .comapFlatMap(
                param1 -> param1.startsWith("#")
                        ? ResourceLocation.read(param1.substring(1)).map(param1x -> create(param0, param1x))
                        : DataResult.error("Not a tag id"),
                param0x -> "#" + param0x.location
            );
    }

    public static <T> TagKey<T> create(ResourceKey<? extends Registry<T>> param0, ResourceLocation param1) {
        return (TagKey<T>)VALUES.intern(new TagKey<>(param0, param1));
    }

    @Override
    public String toString() {
        return "TagKey[" + this.registry.location() + " / " + this.location + "]";
    }
}
