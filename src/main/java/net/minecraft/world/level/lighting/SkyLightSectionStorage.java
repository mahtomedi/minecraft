package net.minecraft.world.level.lighting;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;

public class SkyLightSectionStorage extends LayerLightSectionStorage<SkyLightSectionStorage.SkyDataLayerStorageMap> {
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
        }

    }

    @Override
    protected void onNodeRemoved(long param0) {
        long var0 = SectionPos.getZeroNode(param0);
        int var1 = SectionPos.y(param0);
        if (this.updatingSectionData.topSections.get(var0) == var1 + 1) {
            long var2;
            for(var2 = param0; !this.storingLightForSection(var2) && this.hasLightDataAtOrBelow(var1); var2 = SectionPos.offset(var2, Direction.DOWN)) {
                --var1;
            }

            if (this.storingLightForSection(var2)) {
                this.updatingSectionData.topSections.put(var0, var1 + 1);
            } else {
                this.updatingSectionData.topSections.remove(var0);
            }
        }

    }

    @Override
    protected DataLayer createDataLayer(long param0) {
        DataLayer var0 = this.queuedSections.get(param0);
        if (var0 != null) {
            return var0;
        } else {
            int var1 = this.updatingSectionData.topSections.get(SectionPos.getZeroNode(param0));
            if (var1 != this.updatingSectionData.currentLowestY && SectionPos.y(param0) < var1) {
                long var2 = SectionPos.offset(param0, Direction.UP);

                DataLayer var3;
                while((var3 = this.getDataLayer(var2, true)) == null) {
                    var2 = SectionPos.offset(var2, Direction.UP);
                }

                return repeatFirstLayer(var3);
            } else {
                return this.lightOnInSection(param0) ? new DataLayer(15) : new DataLayer();
            }
        }
    }

    private static DataLayer repeatFirstLayer(DataLayer param0) {
        if (param0.isDefinitelyHomogenous()) {
            return param0.copy();
        } else {
            byte[] var0 = param0.getData();
            byte[] var1 = new byte[2048];

            for(int var2 = 0; var2 < 16; ++var2) {
                System.arraycopy(var0, 0, var1, var2 * 128, 128);
            }

            return new DataLayer(var1);
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

    protected int getTopSectionY(long param0) {
        return this.updatingSectionData.topSections.get(param0);
    }

    protected int getBottomSectionY() {
        return this.updatingSectionData.currentLowestY;
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
