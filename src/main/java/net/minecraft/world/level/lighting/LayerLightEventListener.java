package net.minecraft.world.level.lighting;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.DataLayer;

public interface LayerLightEventListener extends LightEventListener {
    @Nullable
    DataLayer getDataLayerData(SectionPos var1);

    int getLightValue(BlockPos var1);

    public static enum DummyLightLayerEventListener implements LayerLightEventListener {
        INSTANCE;

        @Nullable
        @Override
        public DataLayer getDataLayerData(SectionPos param0) {
            return null;
        }

        @Override
        public int getLightValue(BlockPos param0) {
            return 0;
        }

        @Override
        public void checkBlock(BlockPos param0) {
        }

        @Override
        public void onBlockEmissionIncrease(BlockPos param0, int param1) {
        }

        @Override
        public boolean hasLightWork() {
            return false;
        }

        @Override
        public int runUpdates(int param0, boolean param1, boolean param2) {
            return param0;
        }

        @Override
        public void updateSectionStatus(SectionPos param0, boolean param1) {
        }

        @Override
        public void enableLightSources(ChunkPos param0, boolean param1) {
        }
    }
}
