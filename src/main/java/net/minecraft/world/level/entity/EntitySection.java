package net.minecraft.world.level.entity;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.util.ClassInstanceMultiMap;
import net.minecraft.util.VisibleForDebug;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EntitySection<T> {
    protected static final Logger LOGGER = LogManager.getLogger();
    private final ClassInstanceMultiMap<T> storage;
    private Visibility chunkStatus;

    public EntitySection(Class<T> param0, Visibility param1) {
        this.chunkStatus = param1;
        this.storage = new ClassInstanceMultiMap<>(param0);
    }

    public void add(T param0) {
        this.storage.add(param0);
    }

    public boolean remove(T param0) {
        return this.storage.remove(param0);
    }

    public void getEntities(Predicate<? super T> param0, Consumer<T> param1) {
        for(T var0 : this.storage) {
            if (param0.test(var0)) {
                param1.accept(var0);
            }
        }

    }

    public <U extends T> void getEntities(EntityTypeTest<T, U> param0, Predicate<? super U> param1, Consumer<? super U> param2) {
        for(T var0 : this.storage.find(param0.getBaseClass())) {
            U var1 = (U)param0.tryCast(var0);
            if (var1 != null && param1.test(var1)) {
                param2.accept((T)var1);
            }
        }

    }

    public boolean isEmpty() {
        return this.storage.isEmpty();
    }

    public Stream<T> getEntities() {
        return this.storage.stream();
    }

    public Visibility getStatus() {
        return this.chunkStatus;
    }

    public Visibility updateChunkStatus(Visibility param0) {
        Visibility var0 = this.chunkStatus;
        this.chunkStatus = param0;
        return var0;
    }

    @VisibleForDebug
    public int size() {
        return this.storage.size();
    }
}
