package net.minecraft.world.level.lighting;

import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
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
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;

public abstract class LayerLightSectionStorage<M extends DataLayerStorageMap<M>> {
    private final LightLayer layer;
    protected final LightChunkGetter chunkSource;
    protected final Long2ByteMap sectionStates = new Long2ByteOpenHashMap();
    private final LongSet columnsWithSources = new LongOpenHashSet();
    protected volatile M visibleSectionData;
    protected final M updatingSectionData;
    protected final LongSet changedSections = new LongOpenHashSet();
    protected final LongSet sectionsAffectedByLightUpdates = new LongOpenHashSet();
    protected final Long2ObjectMap<DataLayer> queuedSections = Long2ObjectMaps.synchronize(new Long2ObjectOpenHashMap<>());
    private final LongSet columnsToRetainQueuedDataFor = new LongOpenHashSet();
    private final LongSet toRemove = new LongOpenHashSet();
    protected volatile boolean hasInconsistencies;

    protected LayerLightSectionStorage(LightLayer param0, LightChunkGetter param1, M param2) {
        this.layer = param0;
        this.chunkSource = param1;
        this.updatingSectionData = param2;
        this.visibleSectionData = param2.copy();
        this.visibleSectionData.disableCache();
        this.sectionStates.defaultReturnValue((byte)0);
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
    protected DataLayer getDataLayerToWrite(long param0) {
        DataLayer var0 = this.updatingSectionData.getLayer(param0);
        if (var0 == null) {
            return null;
        } else {
            if (this.changedSections.add(param0)) {
                var0 = var0.copy();
                this.updatingSectionData.setLayer(param0, var0);
                this.updatingSectionData.clearCache();
            }

            return var0;
        }
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
        DataLayer var1;
        if (this.changedSections.add(var0)) {
            var1 = this.updatingSectionData.copyDataLayer(var0);
        } else {
            var1 = this.getDataLayer(var0, true);
        }

        var1.set(
            SectionPos.sectionRelative(BlockPos.getX(param0)),
            SectionPos.sectionRelative(BlockPos.getY(param0)),
            SectionPos.sectionRelative(BlockPos.getZ(param0)),
            param1
        );
        SectionPos.aroundAndAtBlockPos(param0, this.sectionsAffectedByLightUpdates::add);
    }

    protected void markSectionAndNeighborsAsAffected(long param0) {
        int var0 = SectionPos.x(param0);
        int var1 = SectionPos.y(param0);
        int var2 = SectionPos.z(param0);

        for(int var3 = -1; var3 <= 1; ++var3) {
            for(int var4 = -1; var4 <= 1; ++var4) {
                for(int var5 = -1; var5 <= 1; ++var5) {
                    this.sectionsAffectedByLightUpdates.add(SectionPos.asLong(var0 + var4, var1 + var5, var2 + var3));
                }
            }
        }

    }

    protected DataLayer createDataLayer(long param0) {
        DataLayer var0 = this.queuedSections.get(param0);
        return var0 != null ? var0 : new DataLayer();
    }

    protected boolean hasInconsistencies() {
        return this.hasInconsistencies;
    }

    protected void markNewInconsistencies(LightEngine<M, ?> param0) {
        if (this.hasInconsistencies) {
            this.hasInconsistencies = false;

            for(long var0 : this.toRemove) {
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
                this.changedSections.add(var3);
            }

            this.toRemove.clear();
            ObjectIterator<Entry<DataLayer>> var4 = Long2ObjectMaps.fastIterator(this.queuedSections);

            while(var4.hasNext()) {
                Entry<DataLayer> var5 = var4.next();
                long var6 = var5.getLongKey();
                if (this.storingLightForSection(var6)) {
                    DataLayer var7 = var5.getValue();
                    if (this.updatingSectionData.getLayer(var6) != var7) {
                        param0.clearQueuedSectionBlocks(var6);
                        this.updatingSectionData.setLayer(var6, var7);
                        this.changedSections.add(var6);
                    }

                    var4.remove();
                }
            }

            this.updatingSectionData.clearCache();
        }
    }

    protected void onNodeAdded(long param0) {
    }

    protected void onNodeRemoved(long param0) {
    }

    protected void setLightEnabled(long param0, boolean param1) {
        if (param1) {
            this.columnsWithSources.add(param0);
        } else {
            this.columnsWithSources.remove(param0);
        }

    }

    protected boolean lightOnInSection(long param0) {
        long var0 = SectionPos.getZeroNode(param0);
        return this.columnsWithSources.contains(var0);
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
            this.hasInconsistencies = true;
        } else {
            this.queuedSections.remove(param0);
        }

    }

    protected void updateSectionStatus(long param0, boolean param1) {
        byte var0 = this.sectionStates.get(param0);
        byte var1 = LayerLightSectionStorage.SectionState.hasData(var0, !param1);
        if (var0 != var1) {
            this.putSectionState(param0, var1);
            int var2 = param1 ? -1 : 1;

            for(int var3 = -1; var3 <= 1; ++var3) {
                for(int var4 = -1; var4 <= 1; ++var4) {
                    for(int var5 = -1; var5 <= 1; ++var5) {
                        if (var3 != 0 || var4 != 0 || var5 != 0) {
                            long var6 = SectionPos.offset(param0, var3, var4, var5);
                            byte var7 = this.sectionStates.get(var6);
                            this.putSectionState(
                                var6,
                                LayerLightSectionStorage.SectionState.neighborCount(var7, LayerLightSectionStorage.SectionState.neighborCount(var7) + var2)
                            );
                        }
                    }
                }
            }

        }
    }

    protected void putSectionState(long param0, byte param1) {
        if (param1 != 0) {
            if (this.sectionStates.put(param0, param1) == 0) {
                this.initializeSection(param0);
            }
        } else if (this.sectionStates.remove(param0) != 0) {
            this.removeSection(param0);
        }

    }

    private void initializeSection(long param0) {
        if (!this.toRemove.remove(param0)) {
            this.updatingSectionData.setLayer(param0, this.createDataLayer(param0));
            this.changedSections.add(param0);
            this.onNodeAdded(param0);
            this.markSectionAndNeighborsAsAffected(param0);
            this.hasInconsistencies = true;
        }

    }

    private void removeSection(long param0) {
        this.toRemove.add(param0);
        this.hasInconsistencies = true;
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

    public LayerLightSectionStorage.SectionType getDebugSectionType(long param0) {
        return LayerLightSectionStorage.SectionState.type(this.sectionStates.get(param0));
    }

    protected static class SectionState {
        public static final byte EMPTY = 0;
        private static final int MIN_NEIGHBORS = 0;
        private static final int MAX_NEIGHBORS = 26;
        private static final byte HAS_DATA_BIT = 32;
        private static final byte NEIGHBOR_COUNT_BITS = 31;

        public static byte hasData(byte param0, boolean param1) {
            return (byte)(param1 ? param0 | 32 : param0 & -33);
        }

        public static byte neighborCount(byte param0, int param1) {
            if (param1 >= 0 && param1 <= 26) {
                return (byte)(param0 & -32 | param1 & 31);
            } else {
                throw new IllegalArgumentException("Neighbor count was not within range [0; 26]");
            }
        }

        public static boolean hasData(byte param0) {
            return (param0 & 32) != 0;
        }

        public static int neighborCount(byte param0) {
            return param0 & 31;
        }

        public static LayerLightSectionStorage.SectionType type(byte param0) {
            if (param0 == 0) {
                return LayerLightSectionStorage.SectionType.EMPTY;
            } else {
                return hasData(param0) ? LayerLightSectionStorage.SectionType.LIGHT_AND_DATA : LayerLightSectionStorage.SectionType.LIGHT_ONLY;
            }
        }
    }

    public static enum SectionType {
        EMPTY("2"),
        LIGHT_ONLY("1"),
        LIGHT_AND_DATA("0");

        private final String display;

        private SectionType(String param0) {
            this.display = param0;
        }

        public String display() {
            return this.display;
        }
    }
}
