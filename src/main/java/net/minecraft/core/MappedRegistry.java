package net.minecraft.core;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CrudeIncrementalIntIdentityHashBiMap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MappedRegistry<T> extends WritableRegistry<T> {
    protected static final Logger LOGGER = LogManager.getLogger();
    protected final CrudeIncrementalIntIdentityHashBiMap<T> map = new CrudeIncrementalIntIdentityHashBiMap<>(256);
    protected final BiMap<ResourceLocation, T> storage = HashBiMap.create();
    protected Object[] randomCache;
    private int nextId;

    @Override
    public <V extends T> V registerMapping(int param0, ResourceLocation param1, V param2) {
        this.map.addMapping((T)param2, param0);
        Validate.notNull(param1);
        Validate.notNull((T)param2);
        this.randomCache = null;
        if (this.storage.containsKey(param1)) {
            LOGGER.debug("Adding duplicate key '{}' to registry", param1);
        }

        this.storage.put(param1, (T)param2);
        if (this.nextId <= param0) {
            this.nextId = param0 + 1;
        }

        return param2;
    }

    @Override
    public <V extends T> V register(ResourceLocation param0, V param1) {
        return this.registerMapping(this.nextId, param0, param1);
    }

    @Nullable
    @Override
    public ResourceLocation getKey(T param0) {
        return this.storage.inverse().get(param0);
    }

    @Override
    public int getId(@Nullable T param0) {
        return this.map.getId(param0);
    }

    @Nullable
    @Override
    public T byId(int param0) {
        return this.map.byId(param0);
    }

    @Override
    public Iterator<T> iterator() {
        return this.map.iterator();
    }

    @Nullable
    @Override
    public T get(@Nullable ResourceLocation param0) {
        return this.storage.get(param0);
    }

    @Override
    public Optional<T> getOptional(@Nullable ResourceLocation param0) {
        return Optional.ofNullable(this.storage.get(param0));
    }

    @Override
    public Set<ResourceLocation> keySet() {
        return Collections.unmodifiableSet(this.storage.keySet());
    }

    @Override
    public boolean isEmpty() {
        return this.storage.isEmpty();
    }

    @Nullable
    public T getRandom(Random param0) {
        if (this.randomCache == null) {
            Collection<?> var0 = this.storage.values();
            if (var0.isEmpty()) {
                return null;
            }

            this.randomCache = var0.toArray(new Object[var0.size()]);
        }

        return Util.getRandom((T[])this.randomCache, param0);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean containsKey(ResourceLocation param0) {
        return this.storage.containsKey(param0);
    }
}
