package net.minecraft.world.level.entity;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import org.slf4j.Logger;

public class EntityLookup<T extends EntityAccess> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Int2ObjectMap<T> byId = new Int2ObjectLinkedOpenHashMap<>();
    private final Map<UUID, T> byUuid = Maps.newHashMap();

    public <U extends T> void getEntities(EntityTypeTest<T, U> param0, Consumer<U> param1) {
        for(T var0 : this.byId.values()) {
            U var1 = (U)param0.tryCast(var0);
            if (var1 != null) {
                param1.accept(var1);
            }
        }

    }

    public Iterable<T> getAllEntities() {
        return Iterables.unmodifiableIterable(this.byId.values());
    }

    public void add(T param0) {
        UUID var0 = param0.getUUID();
        if (this.byUuid.containsKey(var0)) {
            LOGGER.warn("Duplicate entity UUID {}: {}", var0, param0);
        } else {
            this.byUuid.put(var0, param0);
            this.byId.put(param0.getId(), param0);
        }
    }

    public void remove(T param0) {
        this.byUuid.remove(param0.getUUID());
        this.byId.remove(param0.getId());
    }

    @Nullable
    public T getEntity(int param0) {
        return this.byId.get(param0);
    }

    @Nullable
    public T getEntity(UUID param0) {
        return this.byUuid.get(param0);
    }

    public int count() {
        return this.byUuid.size();
    }
}
