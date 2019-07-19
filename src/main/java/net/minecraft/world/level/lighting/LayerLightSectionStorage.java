package net.minecraft.world.level.lighting;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
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
    protected final Long2ObjectMap<DataLayer> queuedSections = new Long2ObjectOpenHashMap<>();
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

        for(int var2 = -1; var2 <= 1; ++var2) {
            for(int var3 = -1; var3 <= 1; ++var3) {
                for(int var4 = -1; var4 <= 1; ++var4) {
                    this.sectionsAffectedByLightUpdates.add(SectionPos.blockToSection(BlockPos.offset(param0, var3, var4, var2)));
                }
            }
        }

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

                for(int var1 = -1; var1 <= 1; ++var1) {
                    for(int var2 = -1; var2 <= 1; ++var2) {
                        for(int var3 = -1; var3 <= 1; ++var3) {
                            this.sectionsAffectedByLightUpdates.add(SectionPos.blockToSection(BlockPos.offset(param0, var2, var3, var1)));
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
                    if (this.storingLightForSection(var7)) {
                        int var8 = SectionPos.sectionToBlockCoord(SectionPos.x(var7));
                        int var9 = SectionPos.sectionToBlockCoord(SectionPos.y(var7));
                        int var10 = SectionPos.sectionToBlockCoord(SectionPos.z(var7));

                        for(Direction var11 : DIRECTIONS) {
                            long var12 = SectionPos.offset(var7, var11);
                            if (!this.queuedSections.containsKey(var12) && this.storingLightForSection(var12)) {
                                for(int var13 = 0; var13 < 16; ++var13) {
                                    for(int var14 = 0; var14 < 16; ++var14) {
                                        long var15;
                                        long var16;
                                        switch(var11) {
                                            case DOWN:
                                                var15 = BlockPos.asLong(var8 + var14, var9, var10 + var13);
                                                var16 = BlockPos.asLong(var8 + var14, var9 - 1, var10 + var13);
                                                break;
                                            case UP:
                                                var15 = BlockPos.asLong(var8 + var14, var9 + 16 - 1, var10 + var13);
                                                var16 = BlockPos.asLong(var8 + var14, var9 + 16, var10 + var13);
                                                break;
                                            case NORTH:
                                                var15 = BlockPos.asLong(var8 + var13, var9 + var14, var10);
                                                var16 = BlockPos.asLong(var8 + var13, var9 + var14, var10 - 1);
                                                break;
                                            case SOUTH:
                                                var15 = BlockPos.asLong(var8 + var13, var9 + var14, var10 + 16 - 1);
                                                var16 = BlockPos.asLong(var8 + var13, var9 + var14, var10 + 16);
                                                break;
                                            case WEST:
                                                var15 = BlockPos.asLong(var8, var9 + var13, var10 + var14);
                                                var16 = BlockPos.asLong(var8 - 1, var9 + var13, var10 + var14);
                                                break;
                                            default:
                                                var15 = BlockPos.asLong(var8 + 16 - 1, var9 + var13, var10 + var14);
                                                var16 = BlockPos.asLong(var8 + 16, var9 + var13, var10 + var14);
                                        }

                                        param0.checkEdge(var15, var16, param0.computeLevelFromNeighbor(var15, var16, param0.getLevel(var15)), false);
                                        param0.checkEdge(var16, var15, param0.computeLevelFromNeighbor(var16, var15, param0.getLevel(var16)), false);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            ObjectIterator<Entry<DataLayer>> var27 = this.queuedSections.long2ObjectEntrySet().iterator();

            while(var27.hasNext()) {
                Entry<DataLayer> var28 = var27.next();
                long var29 = var28.getLongKey();
                if (this.storingLightForSection(var29)) {
                    var27.remove();
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

    protected void queueSectionData(long param0, @Nullable DataLayer param1) {
        if (param1 != null) {
            this.queuedSections.put(param0, param1);
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
