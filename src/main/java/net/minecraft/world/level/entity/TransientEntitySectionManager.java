package net.minecraft.world.level.entity;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import org.slf4j.Logger;

public class TransientEntitySectionManager<T extends EntityAccess> {
    static final Logger LOGGER = LogUtils.getLogger();
    final LevelCallback<T> callbacks;
    final EntityLookup<T> entityStorage;
    final EntitySectionStorage<T> sectionStorage;
    private final LongSet tickingChunks = new LongOpenHashSet();
    private final LevelEntityGetter<T> entityGetter;

    public TransientEntitySectionManager(Class<T> param0, LevelCallback<T> param1) {
        this.entityStorage = new EntityLookup<>();
        this.sectionStorage = new EntitySectionStorage<>(param0, param0x -> this.tickingChunks.contains(param0x) ? Visibility.TICKING : Visibility.TRACKED);
        this.callbacks = param1;
        this.entityGetter = new LevelEntityGetterAdapter<>(this.entityStorage, this.sectionStorage);
    }

    public void startTicking(ChunkPos param0) {
        long var0 = param0.toLong();
        this.tickingChunks.add(var0);
        this.sectionStorage.getExistingSectionsInChunk(var0).forEach(param0x -> {
            Visibility var0x = param0x.updateChunkStatus(Visibility.TICKING);
            if (!var0x.isTicking()) {
                param0x.getEntities().filter(param0xx -> !param0xx.isAlwaysTicking()).forEach(this.callbacks::onTickingStart);
            }

        });
    }

    public void stopTicking(ChunkPos param0) {
        long var0 = param0.toLong();
        this.tickingChunks.remove(var0);
        this.sectionStorage.getExistingSectionsInChunk(var0).forEach(param0x -> {
            Visibility var0x = param0x.updateChunkStatus(Visibility.TRACKED);
            if (var0x.isTicking()) {
                param0x.getEntities().filter(param0xx -> !param0xx.isAlwaysTicking()).forEach(this.callbacks::onTickingEnd);
            }

        });
    }

    public LevelEntityGetter<T> getEntityGetter() {
        return this.entityGetter;
    }

    public void addEntity(T param0) {
        this.entityStorage.add(param0);
        long var0 = SectionPos.asLong(param0.blockPosition());
        EntitySection<T> var1 = this.sectionStorage.getOrCreateSection(var0);
        var1.add(param0);
        param0.setLevelCallback(new TransientEntitySectionManager.Callback(param0, var0, var1));
        this.callbacks.onCreated(param0);
        this.callbacks.onTrackingStart(param0);
        if (param0.isAlwaysTicking() || var1.getStatus().isTicking()) {
            this.callbacks.onTickingStart(param0);
        }

    }

    @VisibleForDebug
    public int count() {
        return this.entityStorage.count();
    }

    void removeSectionIfEmpty(long param0, EntitySection<T> param1) {
        if (param1.isEmpty()) {
            this.sectionStorage.remove(param0);
        }

    }

    @VisibleForDebug
    public String gatherStats() {
        return this.entityStorage.count() + "," + this.sectionStorage.count() + "," + this.tickingChunks.size();
    }

    class Callback implements EntityInLevelCallback {
        private final T entity;
        private long currentSectionKey;
        private EntitySection<T> currentSection;

        Callback(T param0, long param1, EntitySection<T> param2) {
            this.entity = param0;
            this.currentSectionKey = param1;
            this.currentSection = param2;
        }

        @Override
        public void onMove() {
            BlockPos var0 = this.entity.blockPosition();
            long var1 = SectionPos.asLong(var0);
            if (var1 != this.currentSectionKey) {
                Visibility var2 = this.currentSection.getStatus();
                if (!this.currentSection.remove(this.entity)) {
                    TransientEntitySectionManager.LOGGER
                        .warn("Entity {} wasn't found in section {} (moving to {})", this.entity, SectionPos.of(this.currentSectionKey), var1);
                }

                TransientEntitySectionManager.this.removeSectionIfEmpty(this.currentSectionKey, this.currentSection);
                EntitySection<T> var3 = TransientEntitySectionManager.this.sectionStorage.getOrCreateSection(var1);
                var3.add(this.entity);
                this.currentSection = var3;
                this.currentSectionKey = var1;
                if (!this.entity.isAlwaysTicking()) {
                    boolean var4 = var2.isTicking();
                    boolean var5 = var3.getStatus().isTicking();
                    if (var4 && !var5) {
                        TransientEntitySectionManager.this.callbacks.onTickingEnd(this.entity);
                    } else if (!var4 && var5) {
                        TransientEntitySectionManager.this.callbacks.onTickingStart(this.entity);
                    }
                }
            }

        }

        @Override
        public void onRemove(Entity.RemovalReason param0) {
            if (!this.currentSection.remove(this.entity)) {
                TransientEntitySectionManager.LOGGER
                    .warn("Entity {} wasn't found in section {} (destroying due to {})", this.entity, SectionPos.of(this.currentSectionKey), param0);
            }

            Visibility var0 = this.currentSection.getStatus();
            if (var0.isTicking() || this.entity.isAlwaysTicking()) {
                TransientEntitySectionManager.this.callbacks.onTickingEnd(this.entity);
            }

            TransientEntitySectionManager.this.callbacks.onTrackingEnd(this.entity);
            TransientEntitySectionManager.this.callbacks.onDestroyed(this.entity);
            TransientEntitySectionManager.this.entityStorage.remove(this.entity);
            this.entity.setLevelCallback(NULL);
            TransientEntitySectionManager.this.removeSectionIfEmpty(this.currentSectionKey, this.currentSection);
        }
    }
}
