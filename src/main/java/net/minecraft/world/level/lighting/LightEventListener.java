package net.minecraft.world.level.lighting;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;

public interface LightEventListener {
    default void updateSectionStatus(BlockPos param0, boolean param1) {
        this.updateSectionStatus(SectionPos.of(param0), param1);
    }

    void updateSectionStatus(SectionPos var1, boolean var2);
}
