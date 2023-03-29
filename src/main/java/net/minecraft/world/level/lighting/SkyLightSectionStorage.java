package net.minecraft.world.level.lighting;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Arrays;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;

public class SkyLightSectionStorage extends LayerLightSectionStorage<SkyLightSectionStorage.SkyDataLayerStorageMap> {
    private static final Direction[] HORIZONTALS = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
    private final LongSet sectionsWithSources = new LongOpenHashSet();
    private final LongSet sectionsToAddSourcesTo = new LongOpenHashSet();
    private final LongSet sectionsToRemoveSourcesFrom = new LongOpenHashSet();
    private final LongSet columnsWithSkySources = new LongOpenHashSet();
    private volatile boolean hasSourceInconsistencies;

    protected SkyLightSectionStorage(LightChunkGetter param0) {
        super(
            LightLayer.SKY,
            param0,
            new SkyLightSectionStorage.SkyDataLayerStorageMap(new Long2ObjectOpenHashMap<>(), new Long2IntOpenHashMap(), Integer.MAX_VALUE)
        );
    }

    @Override
    protected int getLightValue(long param0) {
        return this.getLightValue(param0, false);
    }

    protected int getLightValue(long param0, boolean param1) {
        long var0 = SectionPos.blockToSection(param0);
        int var1 = SectionPos.y(var0);
        SkyLightSectionStorage.SkyDataLayerStorageMap var2 = param1 ? this.updatingSectionData : this.visibleSectionData;
        int var3 = var2.topSections.get(SectionPos.getZeroNode(var0));
        if (var3 != var2.currentLowestY && var1 < var3) {
            DataLayer var4 = this.getDataLayer(var2, var0);
            if (var4 == null) {
                for(param0 = BlockPos.getFlatIndex(param0); var4 == null; var4 = this.getDataLayer(var2, var0)) {
                    if (++var1 >= var3) {
                        return 15;
                    }

                    var0 = SectionPos.offset(var0, Direction.UP);
                }
            }

            return var4.get(
                SectionPos.sectionRelative(BlockPos.getX(param0)),
                SectionPos.sectionRelative(BlockPos.getY(param0)),
                SectionPos.sectionRelative(BlockPos.getZ(param0))
            );
        } else {
            return param1 && !this.lightOnInSection(var0) ? 0 : 15;
        }
    }

    @Override
    protected void onNodeAdded(long param0) {
        int var0 = SectionPos.y(param0);
        if (this.updatingSectionData.currentLowestY > var0) {
            this.updatingSectionData.currentLowestY = var0;
            this.updatingSectionData.topSections.defaultReturnValue(this.updatingSectionData.currentLowestY);
        }

        long var1 = SectionPos.getZeroNode(param0);
        int var2 = this.updatingSectionData.topSections.get(var1);
        if (var2 < var0 + 1) {
            this.updatingSectionData.topSections.put(var1, var0 + 1);
            if (this.columnsWithSkySources.contains(var1)) {
                this.queueAddSource(param0);
                if (var2 > this.updatingSectionData.currentLowestY) {
                    long var3 = SectionPos.asLong(SectionPos.x(param0), var2 - 1, SectionPos.z(param0));
                    this.queueRemoveSource(var3);
                }

                this.recheckInconsistencyFlag();
            }
        }

    }

    private void queueRemoveSource(long param0) {
        this.sectionsToRemoveSourcesFrom.add(param0);
        this.sectionsToAddSourcesTo.remove(param0);
    }

    private void queueAddSource(long param0) {
        this.sectionsToAddSourcesTo.add(param0);
        this.sectionsToRemoveSourcesFrom.remove(param0);
    }

    private void recheckInconsistencyFlag() {
        this.hasSourceInconsistencies = !this.sectionsToAddSourcesTo.isEmpty() || !this.sectionsToRemoveSourcesFrom.isEmpty();
    }

    @Override
    protected void onNodeRemoved(long param0) {
        long var0 = SectionPos.getZeroNode(param0);
        boolean var1 = this.columnsWithSkySources.contains(var0);
        if (var1) {
            this.queueRemoveSource(param0);
        }

        int var2 = SectionPos.y(param0);
        if (this.updatingSectionData.topSections.get(var0) == var2 + 1) {
            long var3;
            for(var3 = param0; !this.storingLightForSection(var3) && this.hasLightDataAtOrBelow(var2); var3 = SectionPos.offset(var3, Direction.DOWN)) {
                --var2;
            }

            if (this.storingLightForSection(var3)) {
                this.updatingSectionData.topSections.put(var0, var2 + 1);
                if (var1) {
                    this.queueAddSource(var3);
                }
            } else {
                this.updatingSectionData.topSections.remove(var0);
            }
        }

        if (var1) {
            this.recheckInconsistencyFlag();
        }

    }

    @Override
    protected void enableLightSources(long param0, boolean param1) {
        this.runAllUpdates();
        if (param1 && this.columnsWithSkySources.add(param0)) {
            int var0 = this.updatingSectionData.topSections.get(param0);
            if (var0 != this.updatingSectionData.currentLowestY) {
                long var1 = SectionPos.asLong(SectionPos.x(param0), var0 - 1, SectionPos.z(param0));
                this.queueAddSource(var1);
                this.recheckInconsistencyFlag();
            }
        } else if (!param1) {
            this.columnsWithSkySources.remove(param0);
        }

    }

    @Override
    protected boolean hasInconsistencies() {
        return super.hasInconsistencies() || this.hasSourceInconsistencies;
    }

    @Override
    protected DataLayer createDataLayer(long param0) {
        DataLayer var0 = this.queuedSections.get(param0);
        if (var0 != null) {
            return var0;
        } else {
            long var1 = SectionPos.offset(param0, Direction.UP);
            int var2 = this.updatingSectionData.topSections.get(SectionPos.getZeroNode(param0));
            if (var2 != this.updatingSectionData.currentLowestY && SectionPos.y(var1) < var2) {
                DataLayer var3;
                while((var3 = this.getDataLayer(var1, true)) == null) {
                    var1 = SectionPos.offset(var1, Direction.UP);
                }

                return repeatFirstLayer(var3);
            } else {
                return new DataLayer();
            }
        }
    }

    private static DataLayer repeatFirstLayer(DataLayer param0) {
        if (param0.isEmpty()) {
            return new DataLayer();
        } else {
            byte[] var0 = param0.getData();
            byte[] var1 = new byte[2048];

            for(int var2 = 0; var2 < 16; ++var2) {
                System.arraycopy(var0, 0, var1, var2 * 128, 128);
            }

            return new DataLayer(var1);
        }
    }

    @Override
    protected void markNewInconsistencies(LayerLightEngine<SkyLightSectionStorage.SkyDataLayerStorageMap, ?> param0, boolean param1, boolean param2) {
        super.markNewInconsistencies(param0, param1, param2);
        if (param1) {
            if (!this.sectionsToAddSourcesTo.isEmpty()) {
                for(long var0 : this.sectionsToAddSourcesTo) {
                    int var1 = this.getLevel(var0);
                    if (var1 != 2 && !this.sectionsToRemoveSourcesFrom.contains(var0) && this.sectionsWithSources.add(var0)) {
                        if (var1 == 1) {
                            this.clearQueuedSectionBlocks(param0, var0);
                            if (this.changedSections.add(var0)) {
                                this.updatingSectionData.copyDataLayer(var0);
                            }

                            Arrays.fill(this.getDataLayer(var0, true).getData(), (byte)-1);
                            int var2 = SectionPos.sectionToBlockCoord(SectionPos.x(var0));
                            int var3 = SectionPos.sectionToBlockCoord(SectionPos.y(var0));
                            int var4 = SectionPos.sectionToBlockCoord(SectionPos.z(var0));

                            for(Direction var5 : HORIZONTALS) {
                                long var6 = SectionPos.offset(var0, var5);
                                if ((
                                        this.sectionsToRemoveSourcesFrom.contains(var6)
                                            || !this.sectionsWithSources.contains(var6) && !this.sectionsToAddSourcesTo.contains(var6)
                                    )
                                    && this.storingLightForSection(var6)) {
                                    for(int var7 = 0; var7 < 16; ++var7) {
                                        for(int var8 = 0; var8 < 16; ++var8) {
                                            long var9;
                                            long var10;
                                            switch(var5) {
                                                case NORTH:
                                                    var9 = BlockPos.asLong(var2 + var7, var3 + var8, var4);
                                                    var10 = BlockPos.asLong(var2 + var7, var3 + var8, var4 - 1);
                                                    break;
                                                case SOUTH:
                                                    var9 = BlockPos.asLong(var2 + var7, var3 + var8, var4 + 16 - 1);
                                                    var10 = BlockPos.asLong(var2 + var7, var3 + var8, var4 + 16);
                                                    break;
                                                case WEST:
                                                    var9 = BlockPos.asLong(var2, var3 + var7, var4 + var8);
                                                    var10 = BlockPos.asLong(var2 - 1, var3 + var7, var4 + var8);
                                                    break;
                                                default:
                                                    var9 = BlockPos.asLong(var2 + 16 - 1, var3 + var7, var4 + var8);
                                                    var10 = BlockPos.asLong(var2 + 16, var3 + var7, var4 + var8);
                                            }

                                            param0.checkEdge(var9, var10, param0.computeLevelFromNeighbor(var9, var10, 0), true);
                                        }
                                    }
                                }
                            }

                            for(int var17 = 0; var17 < 16; ++var17) {
                                for(int var18 = 0; var18 < 16; ++var18) {
                                    long var19 = BlockPos.asLong(
                                        SectionPos.sectionToBlockCoord(SectionPos.x(var0), var17),
                                        SectionPos.sectionToBlockCoord(SectionPos.y(var0)),
                                        SectionPos.sectionToBlockCoord(SectionPos.z(var0), var18)
                                    );
                                    long var20 = BlockPos.asLong(
                                        SectionPos.sectionToBlockCoord(SectionPos.x(var0), var17),
                                        SectionPos.sectionToBlockCoord(SectionPos.y(var0)) - 1,
                                        SectionPos.sectionToBlockCoord(SectionPos.z(var0), var18)
                                    );
                                    param0.checkEdge(var19, var20, param0.computeLevelFromNeighbor(var19, var20, 0), true);
                                }
                            }
                        } else {
                            for(int var21 = 0; var21 < 16; ++var21) {
                                for(int var22 = 0; var22 < 16; ++var22) {
                                    long var23 = BlockPos.asLong(
                                        SectionPos.sectionToBlockCoord(SectionPos.x(var0), var21),
                                        SectionPos.sectionToBlockCoord(SectionPos.y(var0), 15),
                                        SectionPos.sectionToBlockCoord(SectionPos.z(var0), var22)
                                    );
                                    param0.checkEdge(Long.MAX_VALUE, var23, 0, true);
                                }
                            }
                        }
                    }
                }
            }

            this.sectionsToAddSourcesTo.clear();
            if (!this.sectionsToRemoveSourcesFrom.isEmpty()) {
                for(long var24 : this.sectionsToRemoveSourcesFrom) {
                    if (this.sectionsWithSources.remove(var24) && this.storingLightForSection(var24)) {
                        for(int var25 = 0; var25 < 16; ++var25) {
                            for(int var26 = 0; var26 < 16; ++var26) {
                                long var27 = BlockPos.asLong(
                                    SectionPos.sectionToBlockCoord(SectionPos.x(var24), var25),
                                    SectionPos.sectionToBlockCoord(SectionPos.y(var24), 15),
                                    SectionPos.sectionToBlockCoord(SectionPos.z(var24), var26)
                                );
                                param0.checkEdge(Long.MAX_VALUE, var27, 15, false);
                            }
                        }
                    }
                }
            }

            this.sectionsToRemoveSourcesFrom.clear();
            this.hasSourceInconsistencies = false;
        }
    }

    protected boolean hasLightDataAtOrBelow(int param0) {
        return param0 >= this.updatingSectionData.currentLowestY;
    }

    protected boolean isAboveData(long param0) {
        long var0 = SectionPos.getZeroNode(param0);
        int var1 = this.updatingSectionData.topSections.get(var0);
        return var1 == this.updatingSectionData.currentLowestY || SectionPos.y(param0) >= var1;
    }

    protected boolean lightOnInSection(long param0) {
        long var0 = SectionPos.getZeroNode(param0);
        return this.columnsWithSkySources.contains(var0);
    }

    protected static final class SkyDataLayerStorageMap extends DataLayerStorageMap<SkyLightSectionStorage.SkyDataLayerStorageMap> {
        int currentLowestY;
        final Long2IntOpenHashMap topSections;

        public SkyDataLayerStorageMap(Long2ObjectOpenHashMap<DataLayer> param0, Long2IntOpenHashMap param1, int param2) {
            super(param0);
            this.topSections = param1;
            param1.defaultReturnValue(param2);
            this.currentLowestY = param2;
        }

        public SkyLightSectionStorage.SkyDataLayerStorageMap copy() {
            return new SkyLightSectionStorage.SkyDataLayerStorageMap(this.map.clone(), this.topSections.clone(), this.currentLowestY);
        }
    }
}
