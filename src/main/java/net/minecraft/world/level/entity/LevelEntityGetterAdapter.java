package net.minecraft.world.level.entity;

import java.util.UUID;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.world.phys.AABB;

public class LevelEntityGetterAdapter<T extends EntityAccess> implements LevelEntityGetter<T> {
    private final EntityLookup<T> visibleEntities;
    private final EntitySectionStorage<T> sectionStorage;

    public LevelEntityGetterAdapter(EntityLookup<T> param0, EntitySectionStorage<T> param1) {
        this.visibleEntities = param0;
        this.sectionStorage = param1;
    }

    @Nullable
    @Override
    public T get(int param0) {
        return this.visibleEntities.getEntity(param0);
    }

    @Nullable
    @Override
    public T get(UUID param0) {
        return this.visibleEntities.getEntity(param0);
    }

    @Override
    public Iterable<T> getAll() {
        return this.visibleEntities.getAllEntities();
    }

    @Override
    public <U extends T> void get(EntityTypeTest<T, U> param0, Consumer<U> param1) {
        this.visibleEntities.getEntities(param0, param1);
    }

    @Override
    public void get(AABB param0, Consumer<T> param1) {
        this.sectionStorage.getEntities(param0, param1);
    }

    @Override
    public <U extends T> void get(EntityTypeTest<T, U> param0, AABB param1, Consumer<U> param2) {
        this.sectionStorage.getEntities(param0, param1, param2);
    }
}
