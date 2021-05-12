package net.minecraft.world.level.lighting;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;

public class BlockLightSectionStorage extends LayerLightSectionStorage<BlockLightSectionStorage.BlockDataLayerStorageMap> {
    protected BlockLightSectionStorage(LightChunkGetter param0) {
        super(LightLayer.BLOCK, param0, new BlockLightSectionStorage.BlockDataLayerStorageMap(new Long2ObjectOpenHashMap<>()));
    }

    @Override
    protected int getLightValue(long param0) {
        long var0 = SectionPos.blockToSection(param0);
        DataLayer var1 = this.getDataLayer(var0, false);
        return var1 == null
            ? 0
            : var1.get(
                SectionPos.sectionRelative(BlockPos.getX(param0)),
                SectionPos.sectionRelative(BlockPos.getY(param0)),
                SectionPos.sectionRelative(BlockPos.getZ(param0))
            );
    }

    protected static final class BlockDataLayerStorageMap extends DataLayerStorageMap<BlockLightSectionStorage.BlockDataLayerStorageMap> {
        public BlockDataLayerStorageMap(Long2ObjectOpenHashMap<DataLayer> param0) {
            super(param0);
        }

        public BlockLightSectionStorage.BlockDataLayerStorageMap copy() {
            return new BlockLightSectionStorage.BlockDataLayerStorageMap(this.map.clone());
        }
    }
}
