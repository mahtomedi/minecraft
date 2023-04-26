package net.minecraft.world.level.lighting;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;

public class LevelLightEngine implements LightEventListener {
    public static final int LIGHT_SECTION_PADDING = 1;
    protected final LevelHeightAccessor levelHeightAccessor;
    @Nullable
    private final LightEngine<?, ?> blockEngine;
    @Nullable
    private final LightEngine<?, ?> skyEngine;

    public LevelLightEngine(LightChunkGetter param0, boolean param1, boolean param2) {
        this.levelHeightAccessor = param0.getLevel();
        this.blockEngine = param1 ? new BlockLightEngine(param0) : null;
        this.skyEngine = param2 ? new SkyLightEngine(param0) : null;
    }

    @Override
    public void checkBlock(BlockPos param0) {
        if (this.blockEngine != null) {
            this.blockEngine.checkBlock(param0);
        }

        if (this.skyEngine != null) {
            this.skyEngine.checkBlock(param0);
        }

    }

    @Override
    public boolean hasLightWork() {
        if (this.skyEngine != null && this.skyEngine.hasLightWork()) {
            return true;
        } else {
            return this.blockEngine != null && this.blockEngine.hasLightWork();
        }
    }

    @Override
    public int runLightUpdates() {
        int var0 = 0;
        if (this.blockEngine != null) {
            var0 += this.blockEngine.runLightUpdates();
        }

        if (this.skyEngine != null) {
            var0 += this.skyEngine.runLightUpdates();
        }

        return var0;
    }

    @Override
    public void updateSectionStatus(SectionPos param0, boolean param1) {
        if (this.blockEngine != null) {
            this.blockEngine.updateSectionStatus(param0, param1);
        }

        if (this.skyEngine != null) {
            this.skyEngine.updateSectionStatus(param0, param1);
        }

    }

    @Override
    public void setLightEnabled(ChunkPos param0, boolean param1) {
        if (this.blockEngine != null) {
            this.blockEngine.setLightEnabled(param0, param1);
        }

        if (this.skyEngine != null) {
            this.skyEngine.setLightEnabled(param0, param1);
        }

    }

    @Override
    public void propagateLightSources(ChunkPos param0) {
        if (this.blockEngine != null) {
            this.blockEngine.propagateLightSources(param0);
        }

        if (this.skyEngine != null) {
            this.skyEngine.propagateLightSources(param0);
        }

    }

    public LayerLightEventListener getLayerListener(LightLayer param0) {
        if (param0 == LightLayer.BLOCK) {
            return (LayerLightEventListener)(this.blockEngine == null ? LayerLightEventListener.DummyLightLayerEventListener.INSTANCE : this.blockEngine);
        } else {
            return (LayerLightEventListener)(this.skyEngine == null ? LayerLightEventListener.DummyLightLayerEventListener.INSTANCE : this.skyEngine);
        }
    }

    public String getDebugData(LightLayer param0, SectionPos param1) {
        if (param0 == LightLayer.BLOCK) {
            if (this.blockEngine != null) {
                return this.blockEngine.getDebugData(param1.asLong());
            }
        } else if (this.skyEngine != null) {
            return this.skyEngine.getDebugData(param1.asLong());
        }

        return "n/a";
    }

    public LayerLightSectionStorage.SectionType getDebugSectionType(LightLayer param0, SectionPos param1) {
        if (param0 == LightLayer.BLOCK) {
            if (this.blockEngine != null) {
                return this.blockEngine.getDebugSectionType(param1.asLong());
            }
        } else if (this.skyEngine != null) {
            return this.skyEngine.getDebugSectionType(param1.asLong());
        }

        return LayerLightSectionStorage.SectionType.EMPTY;
    }

    public void queueSectionData(LightLayer param0, SectionPos param1, @Nullable DataLayer param2) {
        if (param0 == LightLayer.BLOCK) {
            if (this.blockEngine != null) {
                this.blockEngine.queueSectionData(param1.asLong(), param2);
            }
        } else if (this.skyEngine != null) {
            this.skyEngine.queueSectionData(param1.asLong(), param2);
        }

    }

    public void retainData(ChunkPos param0, boolean param1) {
        if (this.blockEngine != null) {
            this.blockEngine.retainData(param0, param1);
        }

        if (this.skyEngine != null) {
            this.skyEngine.retainData(param0, param1);
        }

    }

    public int getRawBrightness(BlockPos param0, int param1) {
        int var0 = this.skyEngine == null ? 0 : this.skyEngine.getLightValue(param0) - param1;
        int var1 = this.blockEngine == null ? 0 : this.blockEngine.getLightValue(param0);
        return Math.max(var1, var0);
    }

    public boolean lightOnInSection(SectionPos param0) {
        long var0 = param0.asLong();
        return this.blockEngine == null
            || this.blockEngine.storage.lightOnInSection(var0) && (this.skyEngine == null || this.skyEngine.storage.lightOnInSection(var0));
    }

    public int getLightSectionCount() {
        return this.levelHeightAccessor.getSectionsCount() + 2;
    }

    public int getMinLightSection() {
        return this.levelHeightAccessor.getMinSection() - 1;
    }

    public int getMaxLightSection() {
        return this.getMinLightSection() + this.getLightSectionCount();
    }
}
