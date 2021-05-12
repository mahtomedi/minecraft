package net.minecraft.world.level.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.util.CsvOutput;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PersistentEntitySectionManager<T extends EntityAccess> implements AutoCloseable {
    static final Logger LOGGER = LogManager.getLogger();
    final Set<UUID> knownUuids = Sets.newHashSet();
    final LevelCallback<T> callbacks;
    private final EntityPersistentStorage<T> permanentStorage;
    private final EntityLookup<T> visibleEntityStorage;
    final EntitySectionStorage<T> sectionStorage;
    private final LevelEntityGetter<T> entityGetter;
    private final Long2ObjectMap<Visibility> chunkVisibility = new Long2ObjectOpenHashMap<>();
    private final Long2ObjectMap<PersistentEntitySectionManager.ChunkLoadStatus> chunkLoadStatuses = new Long2ObjectOpenHashMap<>();
    private final LongSet chunksToUnload = new LongOpenHashSet();
    private final Queue<ChunkEntities<T>> loadingInbox = Queues.newConcurrentLinkedQueue();

    public PersistentEntitySectionManager(Class<T> param0, LevelCallback<T> param1, EntityPersistentStorage<T> param2) {
        this.visibleEntityStorage = new EntityLookup<>();
        this.sectionStorage = new EntitySectionStorage<>(param0, this.chunkVisibility);
        this.chunkVisibility.defaultReturnValue(Visibility.HIDDEN);
        this.chunkLoadStatuses.defaultReturnValue(PersistentEntitySectionManager.ChunkLoadStatus.FRESH);
        this.callbacks = param1;
        this.permanentStorage = param2;
        this.entityGetter = new LevelEntityGetterAdapter<>(this.visibleEntityStorage, this.sectionStorage);
    }

    void removeSectionIfEmpty(long param0, EntitySection<T> param1) {
        if (param1.isEmpty()) {
            this.sectionStorage.remove(param0);
        }

    }

    private boolean addEntityUuid(T param0) {
        if (!this.knownUuids.add(param0.getUUID())) {
            LOGGER.warn("UUID of added entity already exists: {}", param0);
            return false;
        } else {
            return true;
        }
    }

    public boolean addNewEntity(T param0) {
        return this.addEntity(param0, false);
    }

    private boolean addEntity(T param0, boolean param1) {
        if (!this.addEntityUuid(param0)) {
            return false;
        } else {
            long var0 = SectionPos.asLong(param0.blockPosition());
            EntitySection<T> var1 = this.sectionStorage.getOrCreateSection(var0);
            var1.add(param0);
            param0.setLevelCallback(new PersistentEntitySectionManager.Callback(param0, var0, var1));
            if (!param1) {
                this.callbacks.onCreated(param0);
            }

            Visibility var2 = getEffectiveStatus(param0, var1.getStatus());
            if (var2.isAccessible()) {
                this.startTracking(param0);
            }

            if (var2.isTicking()) {
                this.startTicking(param0);
            }

            return true;
        }
    }

    static <T extends EntityAccess> Visibility getEffectiveStatus(T param0, Visibility param1) {
        return param0.isAlwaysTicking() ? Visibility.TICKING : param1;
    }

    public void addLegacyChunkEntities(Stream<T> param0) {
        param0.forEach(param0x -> this.addEntity(param0x, true));
    }

    public void addWorldGenChunkEntities(Stream<T> param0) {
        param0.forEach(param0x -> this.addEntity(param0x, false));
    }

    void startTicking(T param0) {
        this.callbacks.onTickingStart(param0);
    }

    void stopTicking(T param0) {
        this.callbacks.onTickingEnd(param0);
    }

    void startTracking(T param0) {
        this.visibleEntityStorage.add(param0);
        this.callbacks.onTrackingStart(param0);
    }

    void stopTracking(T param0) {
        this.callbacks.onTrackingEnd(param0);
        this.visibleEntityStorage.remove(param0);
    }

    public void updateChunkStatus(ChunkPos param0, ChunkHolder.FullChunkStatus param1) {
        Visibility var0 = Visibility.fromFullChunkStatus(param1);
        this.updateChunkStatus(param0, var0);
    }

    public void updateChunkStatus(ChunkPos param0, Visibility param1) {
        long var0 = param0.toLong();
        if (param1 == Visibility.HIDDEN) {
            this.chunkVisibility.remove(var0);
            this.chunksToUnload.add(var0);
        } else {
            this.chunkVisibility.put(var0, param1);
            this.chunksToUnload.remove(var0);
            this.ensureChunkQueuedForLoad(var0);
        }

        this.sectionStorage.getExistingSectionsInChunk(var0).forEach(param1x -> {
            Visibility var0x = param1x.updateChunkStatus(param1);
            boolean var1x = var0x.isAccessible();
            boolean var2x = param1.isAccessible();
            boolean var3x = var0x.isTicking();
            boolean var4 = param1.isTicking();
            if (var3x && !var4) {
                param1x.getEntities().filter(param0x -> !param0x.isAlwaysTicking()).forEach(this::stopTicking);
            }

            if (var1x && !var2x) {
                param1x.getEntities().filter(param0x -> !param0x.isAlwaysTicking()).forEach(this::stopTracking);
            } else if (!var1x && var2x) {
                param1x.getEntities().filter(param0x -> !param0x.isAlwaysTicking()).forEach(this::startTracking);
            }

            if (!var3x && var4) {
                param1x.getEntities().filter(param0x -> !param0x.isAlwaysTicking()).forEach(this::startTicking);
            }

        });
    }

    private void ensureChunkQueuedForLoad(long param0) {
        PersistentEntitySectionManager.ChunkLoadStatus var0 = this.chunkLoadStatuses.get(param0);
        if (var0 == PersistentEntitySectionManager.ChunkLoadStatus.FRESH) {
            this.requestChunkLoad(param0);
        }

    }

    private boolean storeChunkSections(long param0, Consumer<T> param1) {
        PersistentEntitySectionManager.ChunkLoadStatus var0 = this.chunkLoadStatuses.get(param0);
        if (var0 == PersistentEntitySectionManager.ChunkLoadStatus.PENDING) {
            return false;
        } else {
            List<T> var1 = this.sectionStorage
                .getExistingSectionsInChunk(param0)
                .flatMap(param0x -> param0x.getEntities().filter(EntityAccess::shouldBeSaved))
                .collect(Collectors.toList());
            if (var1.isEmpty()) {
                if (var0 == PersistentEntitySectionManager.ChunkLoadStatus.LOADED) {
                    this.permanentStorage.storeEntities(new ChunkEntities<>(new ChunkPos(param0), ImmutableList.of()));
                }

                return true;
            } else if (var0 == PersistentEntitySectionManager.ChunkLoadStatus.FRESH) {
                this.requestChunkLoad(param0);
                return false;
            } else {
                this.permanentStorage.storeEntities(new ChunkEntities<>(new ChunkPos(param0), var1));
                var1.forEach(param1);
                return true;
            }
        }
    }

    private void requestChunkLoad(long param0) {
        this.chunkLoadStatuses.put(param0, PersistentEntitySectionManager.ChunkLoadStatus.PENDING);
        ChunkPos var0 = new ChunkPos(param0);
        this.permanentStorage.loadEntities(var0).thenAccept(this.loadingInbox::add).exceptionally(param1 -> {
            LOGGER.error("Failed to read chunk {}", var0, param1);
            return null;
        });
    }

    private boolean processChunkUnload(long param0) {
        boolean var0 = this.storeChunkSections(param0, param0x -> param0x.getPassengersAndSelf().forEach(this::unloadEntity));
        if (!var0) {
            return false;
        } else {
            this.chunkLoadStatuses.remove(param0);
            return true;
        }
    }

    private void unloadEntity(EntityAccess param0) {
        param0.setRemoved(Entity.RemovalReason.UNLOADED_TO_CHUNK);
        param0.setLevelCallback(EntityInLevelCallback.NULL);
    }

    private void processUnloads() {
        this.chunksToUnload.removeIf(param0 -> this.chunkVisibility.get(param0) != Visibility.HIDDEN ? true : this.processChunkUnload(param0));
    }

    private void processPendingLoads() {
        ChunkEntities<T> var0;
        while((var0 = this.loadingInbox.poll()) != null) {
            var0.getEntities().forEach(param0 -> this.addEntity(param0, true));
            this.chunkLoadStatuses.put(var0.getPos().toLong(), PersistentEntitySectionManager.ChunkLoadStatus.LOADED);
        }

    }

    public void tick() {
        this.processPendingLoads();
        this.processUnloads();
    }

    private LongSet getAllChunksToSave() {
        LongSet var0 = this.sectionStorage.getAllChunksWithExistingSections();

        for(Entry<PersistentEntitySectionManager.ChunkLoadStatus> var1 : Long2ObjectMaps.fastIterable(this.chunkLoadStatuses)) {
            if (var1.getValue() == PersistentEntitySectionManager.ChunkLoadStatus.LOADED) {
                var0.add(var1.getLongKey());
            }
        }

        return var0;
    }

    public void autoSave() {
        this.getAllChunksToSave().forEach(param0 -> {
            boolean var0 = this.chunkVisibility.get(param0) == Visibility.HIDDEN;
            if (var0) {
                this.processChunkUnload(param0);
            } else {
                this.storeChunkSections(param0, param0x -> {
                });
            }

        });
    }

    public void saveAll() {
        LongSet var0 = this.getAllChunksToSave();

        while(!var0.isEmpty()) {
            this.permanentStorage.flush();
            this.processPendingLoads();
            var0.removeIf(param0 -> {
                boolean var0x = this.chunkVisibility.get(param0) == Visibility.HIDDEN;
                return var0x ? this.processChunkUnload(param0) : this.storeChunkSections(param0, param0x -> {
                });
            });
        }

    }

    @Override
    public void close() throws IOException {
        this.saveAll();
        this.permanentStorage.close();
    }

    public boolean isLoaded(UUID param0) {
        return this.knownUuids.contains(param0);
    }

    public LevelEntityGetter<T> getEntityGetter() {
        return this.entityGetter;
    }

    public void dumpSections(Writer param0) throws IOException {
        CsvOutput var0 = CsvOutput.builder()
            .addColumn("x")
            .addColumn("y")
            .addColumn("z")
            .addColumn("visibility")
            .addColumn("load_status")
            .addColumn("entity_count")
            .build(param0);
        this.sectionStorage.getAllChunksWithExistingSections().forEach(param1 -> {
            PersistentEntitySectionManager.ChunkLoadStatus var0x = this.chunkLoadStatuses.get(param1);
            this.sectionStorage.getExistingSectionPositionsInChunk(param1).forEach(param2 -> {
                EntitySection<T> var0xx = this.sectionStorage.getSection(param2);
                if (var0xx != null) {
                    try {
                        var0.writeRow(SectionPos.x(param2), SectionPos.y(param2), SectionPos.z(param2), var0xx.getStatus(), var0x, var0xx.size());
                    } catch (IOException var7) {
                        throw new UncheckedIOException(var7);
                    }
                }

            });
        });
    }

    @VisibleForDebug
    public String gatherStats() {
        return this.knownUuids.size()
            + ","
            + this.visibleEntityStorage.count()
            + ","
            + this.sectionStorage.count()
            + ","
            + this.chunkLoadStatuses.size()
            + ","
            + this.chunkVisibility.size()
            + ","
            + this.loadingInbox.size()
            + ","
            + this.chunksToUnload.size();
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
                    PersistentEntitySectionManager.LOGGER
                        .warn("Entity {} wasn't found in section {} (moving to {})", this.entity, SectionPos.of(this.currentSectionKey), var1);
                }

                PersistentEntitySectionManager.this.removeSectionIfEmpty(this.currentSectionKey, this.currentSection);
                EntitySection<T> var3 = PersistentEntitySectionManager.this.sectionStorage.getOrCreateSection(var1);
                var3.add(this.entity);
                this.currentSection = var3;
                this.currentSectionKey = var1;
                this.updateStatus(var2, var3.getStatus());
            }

        }

        private void updateStatus(Visibility param0, Visibility param1) {
            Visibility var0 = PersistentEntitySectionManager.getEffectiveStatus(this.entity, param0);
            Visibility var1 = PersistentEntitySectionManager.getEffectiveStatus(this.entity, param1);
            if (var0 != var1) {
                boolean var2 = var0.isAccessible();
                boolean var3 = var1.isAccessible();
                if (var2 && !var3) {
                    PersistentEntitySectionManager.this.stopTracking(this.entity);
                } else if (!var2 && var3) {
                    PersistentEntitySectionManager.this.startTracking(this.entity);
                }

                boolean var4 = var0.isTicking();
                boolean var5 = var1.isTicking();
                if (var4 && !var5) {
                    PersistentEntitySectionManager.this.stopTicking(this.entity);
                } else if (!var4 && var5) {
                    PersistentEntitySectionManager.this.startTicking(this.entity);
                }

            }
        }

        @Override
        public void onRemove(Entity.RemovalReason param0) {
            if (!this.currentSection.remove(this.entity)) {
                PersistentEntitySectionManager.LOGGER
                    .warn("Entity {} wasn't found in section {} (destroying due to {})", this.entity, SectionPos.of(this.currentSectionKey), param0);
            }

            Visibility var0 = PersistentEntitySectionManager.getEffectiveStatus(this.entity, this.currentSection.getStatus());
            if (var0.isTicking()) {
                PersistentEntitySectionManager.this.stopTicking(this.entity);
            }

            if (var0.isAccessible()) {
                PersistentEntitySectionManager.this.stopTracking(this.entity);
            }

            if (param0.shouldDestroy()) {
                PersistentEntitySectionManager.this.callbacks.onDestroyed(this.entity);
            }

            PersistentEntitySectionManager.this.knownUuids.remove(this.entity.getUUID());
            this.entity.setLevelCallback(NULL);
            PersistentEntitySectionManager.this.removeSectionIfEmpty(this.currentSectionKey, this.currentSection);
        }
    }

    static enum ChunkLoadStatus {
        FRESH,
        PENDING,
        LOADED;
    }
}
