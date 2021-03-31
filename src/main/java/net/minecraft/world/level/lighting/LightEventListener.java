package net.minecraft.world.level.lighting;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;

public interface LightEventListener {
    void checkBlock(BlockPos var1);

    void onBlockEmissionIncrease(BlockPos var1, int var2);

    boolean hasLightWork();

    int runUpdates(int var1, boolean var2, boolean var3);

    default void updateSectionStatus(BlockPos param0, boolean param1) {
        this.updateSectionStatus(SectionPos.of(param0), param1);
    }

    void updateSectionStatus(SectionPos var1, boolean var2);

    void enableLightSources(ChunkPos var1, boolean var2);
}
