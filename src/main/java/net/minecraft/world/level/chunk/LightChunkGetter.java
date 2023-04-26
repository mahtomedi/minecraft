package net.minecraft.world.level.chunk;

import javax.annotation.Nullable;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LightLayer;

public interface LightChunkGetter {
    @Nullable
    LightChunk getChunkForLighting(int var1, int var2);

    default void onLightUpdate(LightLayer param0, SectionPos param1) {
    }

    BlockGetter getLevel();
}
