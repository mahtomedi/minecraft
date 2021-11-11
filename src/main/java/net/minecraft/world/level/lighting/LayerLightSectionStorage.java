package net.minecraft.world.level.lighting;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.SectionTracker;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;

public abstract class LayerLightSectionStorage<M extends DataLayerStorageMap<M>> extends SectionTracker {
    protected static final int LIGHT_AND_DATA = 0;
    protected static final int LIGHT_ONLY = 1;
    protected static final int EMPTY = 2;
    protected static final DataLayer EMPTY_DATA = new DataLayer();
    private static final Direction[] DIRECTIONS = Direction.values();
    private final LightLayer layer;
    private final LightChunkGetter chunkSource;
    protected final LongSet dataSectionSet = new LongOpenHashSet();
    protected final LongSet toMarkNoData = new LongOpenHashSet();
    protected final LongSet toMarkData = new LongOpenHashSet();
    protected volatile M visibleSectionData;
    protected final M updatingSectionData;
    protected final LongSet changedSections = new LongOpenHashSet();
    protected final LongSet sectionsAffectedByLightUpdates = new LongOpenHashSet();
    protected final Long2ObjectMap<DataLayer> queuedSections = Long2ObjectMaps.synchronize(new Long2ObjectOpenHashMap<>());
    private final LongSet untrustedSections = new LongOpenHashSet();
    private final LongSet columnsToRetainQueuedDataFor = new LongOpenHashSet();
    private final LongSet toRemove = new LongOpenHashSet();
    protected volatile boolean hasToRemove;

    protected LayerLightSectionStorage(LightLayer param0, LightChunkGetter param1, M param2) {
        super(3, 16, 256);
        this.layer = param0;
        this.chunkSource = param1;
        this.updatingSectionData = param2;
        this.visibleSectionData = param2.copy();
        this.visibleSectionData.disableCache();
    }

    protected boolean storingLightForSection(long param0) {
        return this.getDataLayer(param0, true) != null;
    }

    @Nullable
    protected DataLayer getDataLayer(long param0, boolean param1) {
        return this.getDataLayer((M)(param1 ? this.updatingSectionData : this.visibleSectionData), param0);
    }

    @Nullable
    protected DataLayer getDataLayer(M param0, long param1) {
        return param0.getLayer(param1);
    }

    @Nullable
    public DataLayer getDataLayerData(long param0) {
        DataLayer var0 = this.queuedSections.get(param0);
        return var0 != null ? var0 : this.getDataLayer(param0, false);
    }

    protected abstract int getLightValue(long var1);

    protected int getStoredLevel(long param0) {
        long var0 = SectionPos.blockToSection(param0);
        DataLayer var1 = this.getDataLayer(var0, true);
        return var1.get(
            SectionPos.sectionRelative(BlockPos.getX(param0)),
            SectionPos.sectionRelative(BlockPos.getY(param0)),
            SectionPos.sectionRelative(BlockPos.getZ(param0))
        );
    }

    protected void setStoredLevel(long param0, int param1) {
        long var0 = SectionPos.blockToSection(param0);
        if (this.changedSections.add(var0)) {
            this.updatingSectionData.copyDataLayer(var0);
        }

        DataLayer var1 = this.getDataLayer(var0, true);
        var1.set(
            SectionPos.sectionRelative(BlockPos.getX(param0)),
            SectionPos.sectionRelative(BlockPos.getY(param0)),
            SectionPos.sectionRelative(BlockPos.getZ(param0)),
            param1
        );
        SectionPos.aroundAndAtBlockPos(param0, this.sectionsAffectedByLightUpdates::add);
    }

    @Override
    protected int getLevel(long param0) {
        if (param0 == Long.MAX_VALUE) {
            return 2;
        } else if (this.dataSectionSet.contains(param0)) {
            return 0;
        } else {
            return !this.toRemove.contains(param0) && this.updatingSectionData.hasLayer(param0) ? 1 : 2;
        }
    }

    @Override
    protected int getLevelFromSource(long param0) {
        if (this.toMarkNoData.contains(param0)) {
            return 2;
        } else {
            return !this.dataSectionSet.contains(param0) && !this.toMarkData.contains(param0) ? 2 : 0;
        }
    }

    @Override
    protected void setLevel(long param0, int param1) {
        int var0 = this.getLevel(param0);
        if (var0 != 0 && param1 == 0) {
            this.dataSectionSet.add(param0);
            this.toMarkData.remove(param0);
        }

        if (var0 == 0 && param1 != 0) {
            this.dataSectionSet.remove(param0);
            this.toMarkNoData.remove(param0);
        }

        if (var0 >= 2 && param1 != 2) {
            if (this.toRemove.contains(param0)) {
                this.toRemove.remove(param0);
            } else {
                this.updatingSectionData.setLayer(param0, this.createDataLayer(param0));
                this.changedSections.add(param0);
                this.onNodeAdded(param0);
                int var1 = SectionPos.x(param0);
                int var2 = SectionPos.y(param0);
                int var3 = SectionPos.z(param0);

                for(int var4 = -1; var4 <= 1; ++var4) {
                    for(int var5 = -1; var5 <= 1; ++var5) {
                        for(int var6 = -1; var6 <= 1; ++var6) {
                            this.sectionsAffectedByLightUpdates.add(SectionPos.asLong(var1 + var5, var2 + var6, var3 + var4));
                        }
                    }
                }
            }
        }

        if (var0 != 2 && param1 >= 2) {
            this.toRemove.add(param0);
        }

        this.hasToRemove = !this.toRemove.isEmpty();
    }

    protected DataLayer createDataLayer(long param0) {
        DataLayer var0 = this.queuedSections.get(param0);
        return var0 != null ? var0 : new DataLayer();
    }

    protected void clearQueuedSectionBlocks(LayerLightEngine<?, ?> param0, long param1) {
        if (param0.getQueueSize() != 0) {
            if (param0.getQueueSize() < 8192) {
                param0.removeIf(param1x -> SectionPos.blockToSection(param1x) == param1);
            } else {
                int var0 = SectionPos.sectionToBlockCoord(SectionPos.x(param1));
                int var1 = SectionPos.sectionToBlockCoord(SectionPos.y(param1));
                int var2 = SectionPos.sectionToBlockCoord(SectionPos.z(param1));

                for(int var3 = 0; var3 < 16; ++var3) {
                    for(int var4 = 0; var4 < 16; ++var4) {
                        for(int var5 = 0; var5 < 16; ++var5) {
                            long var6 = BlockPos.asLong(var0 + var3, var1 + var4, var2 + var5);
                            param0.removeFromQueue(var6);
                        }
                    }
                }

            }
        }
    }

    protected boolean hasInconsistencies() {
        return this.hasToRemove;
    }

    protected void markNewInconsistencies(LayerLightEngine<M, ?> param0, boolean param1, boolean param2) {
        if (this.hasInconsistencies() || !this.queuedSections.isEmpty()) {
            for(long var0 : this.toRemove) {
                this.clearQueuedSectionBlocks(param0, var0);
                DataLayer var1 = this.queuedSections.remove(var0);
                DataLayer var2 = this.updatingSectionData.removeLayer(var0);
                if (this.columnsToRetainQueuedDataFor.contains(SectionPos.getZeroNode(var0))) {
                    if (var1 != null) {
                        this.queuedSections.put(var0, var1);
                    } else if (var2 != null) {
                        this.queuedSections.put(var0, var2);
                    }
                }
            }

            this.updatingSectionData.clearCache();

            for(long var3 : this.toRemove) {
                this.onNodeRemoved(var3);
            }

            this.toRemove.clear();
            this.hasToRemove = false;

            for(Entry<DataLayer> var4 : this.queuedSections.long2ObjectEntrySet()) {
                long var5 = var4.getLongKey();
                if (this.storingLightForSection(var5)) {
                    DataLayer var6 = var4.getValue();
                    if (this.updatingSectionData.getLayer(var5) != var6) {
                        this.clearQueuedSectionBlocks(param0, var5);
                        this.updatingSectionData.setLayer(var5, var6);
                        this.changedSections.add(var5);
                    }
                }
            }

            this.updatingSectionData.clearCache();
            if (!param2) {
                for(long var7 : this.queuedSections.keySet()) {
                    this.checkEdgesForSection(param0, var7);
                }
            } else {
                for(long var8 : this.untrustedSections) {
                    this.checkEdgesForSection(param0, var8);
                }
            }

            this.untrustedSections.clear();
            ObjectIterator<Entry<DataLayer>> var9 = this.queuedSections.long2ObjectEntrySet().iterator();

            while(var9.hasNext()) {
                Entry<DataLayer> var10 = var9.next();
                long var11 = var10.getLongKey();
                if (this.storingLightForSection(var11)) {
                    var9.remove();
                }
            }

        }
    }

    private void checkEdgesForSection(LayerLightEngine<M, ?> param0, long param1) {
        if (this.storingLightForSection(param1)) {
            int var0 = SectionPos.sectionToBlockCoord(SectionPos.x(param1));
            int var1 = SectionPos.sectionToBlockCoord(SectionPos.y(param1));
            int var2 = SectionPos.sectionToBlockCoord(SectionPos.z(param1));

            for(Direction var3 : DIRECTIONS) {
                long var4 = SectionPos.offset(param1, var3);
                if (!this.queuedSections.containsKey(var4) && this.storingLightForSection(var4)) {
                    for(int var5 = 0; var5 < 16; ++var5) {
                        for(int var6 = 0; var6 < 16; ++var6) {
                            long var7;
                            long var8;
                            switch(var3) {
                                case DOWN:
                                    var7 = BlockPos.asLong(var0 + var6, var1, var2 + var5);
                                    var8 = BlockPos.asLong(var0 + var6, var1 - 1, var2 + var5);
                                    break;
                                case UP:
                                    var7 = BlockPos.asLong(var0 + var6, var1 + 16 - 1, var2 + var5);
                                    var8 = BlockPos.asLong(var0 + var6, var1 + 16, var2 + var5);
                                    break;
                                case NORTH:
                                    var7 = BlockPos.asLong(var0 + var5, var1 + var6, var2);
                                    var8 = BlockPos.asLong(var0 + var5, var1 + var6, var2 - 1);
                                    break;
                                case SOUTH:
                                    var7 = BlockPos.asLong(var0 + var5, var1 + var6, var2 + 16 - 1);
                                    var8 = BlockPos.asLong(var0 + var5, var1 + var6, var2 + 16);
                                    break;
                                case WEST:
                                    var7 = BlockPos.asLong(var0, var1 + var5, var2 + var6);
                                    var8 = BlockPos.asLong(var0 - 1, var1 + var5, var2 + var6);
                                    break;
                                default:
                                    var7 = BlockPos.asLong(var0 + 16 - 1, var1 + var5, var2 + var6);
                                    var8 = BlockPos.asLong(var0 + 16, var1 + var5, var2 + var6);
                            }

                            param0.checkEdge(var7, var8, param0.computeLevelFromNeighbor(var7, var8, param0.getLevel(var7)), false);
                            param0.checkEdge(var8, var7, param0.computeLevelFromNeighbor(var8, var7, param0.getLevel(var8)), false);
                        }
                    }
                }
            }

        }
    }

    protected void onNodeAdded(long param0) {
    }

    protected void onNodeRemoved(long param0) {
    }

    protected void enableLightSources(long param0, boolean param1) {
    }

    public void retainData(long param0, boolean param1) {
        if (param1) {
            this.columnsToRetainQueuedDataFor.add(param0);
        } else {
            this.columnsToRetainQueuedDataFor.remove(param0);
        }

    }

    protected void queueSectionData(long param0, @Nullable DataLayer param1, boolean param2) {
        if (param1 != null) {
            this.queuedSections.put(param0, param1);
            if (!param2) {
                this.untrustedSections.add(param0);
            }
        } else {
            this.queuedSections.remove(param0);
        }

    }

    protected void updateSectionStatus(long param0, boolean param1) {
        boolean var0 = this.dataSectionSet.contains(param0);
        if (!var0 && !param1) {
            this.toMarkData.add(param0);
            this.checkEdge(Long.MAX_VALUE, param0, 0, true);
        }

        if (var0 && param1) {
            this.toMarkNoData.add(param0);
            this.checkEdge(Long.MAX_VALUE, param0, 2, false);
        }

    }

    protected void runAllUpdates() {
        if (this.hasWork()) {
            this.runUpdates(Integer.MAX_VALUE);
        }

    }

    protected void swapSectionMap() {
        if (!this.changedSections.isEmpty()) {
            M var0 = this.updatingSectionData.copy();
            var0.disableCache();
            this.visibleSectionData = var0;
            this.changedSections.clear();
        }

        if (!this.sectionsAffectedByLightUpdates.isEmpty()) {
            LongIterator var1 = this.sectionsAffectedByLightUpdates.iterator();

            while(var1.hasNext()) {
                long var2 = var1.nextLong();
                this.chunkSource.onLightUpdate(this.layer, SectionPos.of(var2));
            }

            this.sectionsAffectedByLightUpdates.clear();
        }

    }
}
