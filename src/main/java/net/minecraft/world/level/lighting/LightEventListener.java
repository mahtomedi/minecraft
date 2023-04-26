package net.minecraft.world.level.lighting;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;

public interface LightEventListener {
    void checkBlock(BlockPos var1);

    boolean hasLightWork();

    int runLightUpdates();

    default void updateSectionStatus(BlockPos param0, boolean param1) {
        this.updateSectionStatus(SectionPos.of(param0), param1);
    }

    void updateSectionStatus(SectionPos var1, boolean var2);

    void setLightEnabled(ChunkPos var1, boolean var2);

    void propagateLightSources(ChunkPos var1);
}
