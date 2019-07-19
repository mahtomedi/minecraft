package net.minecraft.core;

import java.util.Random;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;

public class DefaultedRegistry<T> extends MappedRegistry<T> {
    private final ResourceLocation defaultKey;
    private T defaultValue;

    public DefaultedRegistry(String param0) {
        this.defaultKey = new ResourceLocation(param0);
    }

    @Override
    public <V extends T> V registerMapping(int param0, ResourceLocation param1, V param2) {
        if (this.defaultKey.equals(param1)) {
            this.defaultValue = (T)param2;
        }

        return super.registerMapping(param0, param1, param2);
    }

    @Override
    public int getId(@Nullable T param0) {
        int var0 = super.getId(param0);
        return var0 == -1 ? super.getId(this.defaultValue) : var0;
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
        return (T)(var0 == null ? this.defaultValue : var0);
    }

    @Nonnull
    @Override
    public T byId(int param0) {
        T var0 = super.byId(param0);
        return (T)(var0 == null ? this.defaultValue : var0);
    }

    @Nonnull
    @Override
    public T getRandom(Random param0) {
        T var0 = super.getRandom(param0);
        return (T)(var0 == null ? this.defaultValue : var0);
    }

    public ResourceLocation getDefaultKey() {
        return this.defaultKey;
    }
}
