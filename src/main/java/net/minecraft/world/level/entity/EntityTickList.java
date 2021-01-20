package net.minecraft.world.level.entity;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;

public class EntityTickList {
    private Int2ObjectMap<Entity> active = new Int2ObjectLinkedOpenHashMap<>();
    private Int2ObjectMap<Entity> passive = new Int2ObjectLinkedOpenHashMap<>();
    @Nullable
    private Int2ObjectMap<Entity> iterated;

    private void ensureActiveIsNotIterated() {
        if (this.iterated == this.active) {
            this.passive.clear();

            for(Entry<Entity> var0 : Int2ObjectMaps.fastIterable(this.active)) {
                this.passive.put(var0.getIntKey(), var0.getValue());
            }

            Int2ObjectMap<Entity> var1 = this.active;
            this.active = this.passive;
            this.passive = var1;
        }

    }

    public void add(Entity param0) {
        this.ensureActiveIsNotIterated();
        this.active.put(param0.getId(), param0);
    }

    public void remove(Entity param0) {
        this.ensureActiveIsNotIterated();
        this.active.remove(param0.getId());
    }

    public boolean contains(Entity param0) {
        return this.active.containsKey(param0.getId());
    }

    public void forEach(Consumer<Entity> param0) {
        if (this.iterated != null) {
            throw new UnsupportedOperationException("Only one concurrent iteration supported");
        } else {
            this.iterated = this.active;

            try {
                for(Entity var0 : this.active.values()) {
                    param0.accept(var0);
                }
            } finally {
                this.iterated = null;
            }

        }
    }
}
