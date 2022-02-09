package net.minecraft.core;

import com.mojang.serialization.Lifecycle;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class DefaultedRegistry<T> extends MappedRegistry<T> {
    private final ResourceLocation defaultKey;
    private Holder<T> defaultValue;

    public DefaultedRegistry(String param0, ResourceKey<? extends Registry<T>> param1, Lifecycle param2, @Nullable Function<T, Holder.Reference<T>> param3) {
        super(param1, param2, param3);
        this.defaultKey = new ResourceLocation(param0);
    }

    @Override
    public Holder<T> registerMapping(int param0, ResourceKey<T> param1, T param2, Lifecycle param3) {
        Holder<T> var0 = super.registerMapping(param0, param1, param2, param3);
        if (this.defaultKey.equals(param1.location())) {
            this.defaultValue = var0;
        }

        return var0;
    }

    @Override
    public int getId(@Nullable T param0) {
        int var0 = super.getId(param0);
        return var0 == -1 ? super.getId(this.defaultValue.value()) : var0;
    }

    @Nonnull
    @Override
    public ResourceLocation getKey(T param0) {
        ResourceLocation var0 = super.getKey(param0);
        return var0 == null ? this.defaultKey : var0;
    }

    @Nonnull
    @Override
    public T get(@Nullable ResourceLocation param0) {
        T var0 = super.get(param0);
        return (T)(var0 == null ? this.defaultValue.value() : var0);
    }

    @Override
    public Optional<T> getOptional(@Nullable ResourceLocation param0) {
        return Optional.ofNullable(super.get(param0));
    }

    @Nonnull
    @Override
    public T byId(int param0) {
        T var0 = super.byId(param0);
        return (T)(var0 == null ? this.defaultValue.value() : var0);
    }

    @Override
    public Optional<Holder<T>> getRandom(Random param0) {
        return super.getRandom(param0).or(() -> Optional.of(this.defaultValue));
    }

    public ResourceLocation getDefaultKey() {
        return this.defaultKey;
    }
}
