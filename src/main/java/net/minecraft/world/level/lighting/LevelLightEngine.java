package net.minecraft.world.level.lighting;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class LevelLightEngine implements LightEventListener {
    @Nullable
    private final LayerLightEngine<?, ?> blockEngine;
    @Nullable
    private final LayerLightEngine<?, ?> skyEngine;

    public LevelLightEngine(LightChunkGetter param0, boolean param1, boolean param2) {
        this.blockEngine = param1 ? new BlockLightEngine(param0) : null;
        this.skyEngine = param2 ? new SkyLightEngine(param0) : null;
    }

    public void checkBlock(BlockPos param0) {
        if (this.blockEngine != null) {
            this.blockEngine.checkBlock(param0);
        }

        if (this.skyEngine != null) {
            this.skyEngine.checkBlock(param0);
        }

    }

    public void onBlockEmissionIncrease(BlockPos param0, int param1) {
        if (this.blockEngine != null) {
            this.blockEngine.onBlockEmissionIncrease(param0, param1);
        }

    }

    public boolean hasLightWork() {
        if (this.skyEngine != null && this.skyEngine.hasLightWork()) {
            return true;
        } else {
            return this.blockEngine != null && this.blockEngine.hasLightWork();
        }
    }

    public int runUpdates(int param0, boolean param1, boolean param2) {
        if (this.blockEngine != null && this.skyEngine != null) {
            int var0 = param0 / 2;
            int var1 = this.blockEngine.runUpdates(var0, param1, param2);
            int var2 = param0 - var0 + var1;
            int var3 = this.skyEngine.runUpdates(var2, param1, param2);
            return var1 == 0 && var3 > 0 ? this.blockEngine.runUpdates(var3, param1, param2) : var3;
        } else if (this.blockEngine != null) {
            return this.blockEngine.runUpdates(param0, param1, param2);
        } else {
            return this.skyEngine != null ? this.skyEngine.runUpdates(param0, param1, param2) : param0;
        }
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

    public void enableLightSources(ChunkPos param0, boolean param1) {
        if (this.blockEngine != null) {
            this.blockEngine.enableLightSources(param0, param1);
        }

        if (this.skyEngine != null) {
            this.skyEngine.enableLightSources(param0, param1);
        }

    }

    public LayerLightEventListener getLayerListener(LightLayer param0) {
        if (param0 == LightLayer.BLOCK) {
            return (LayerLightEventListener)(this.blockEngine == null ? LayerLightEventListener.DummyLightLayerEventListener.INSTANCE : this.blockEngine);
        } else {
            return (LayerLightEventListener)(this.skyEngine == null ? LayerLightEventListener.DummyLightLayerEventListener.INSTANCE : this.skyEngine);
        }
    }

    @OnlyIn(Dist.CLIENT)
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
}
