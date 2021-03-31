package net.minecraft.world.level.newbiome.layer.traits;

import net.minecraft.world.level.newbiome.layer.LayerBiomes;

public interface DimensionTransformer extends LayerBiomes {
    int getParentX(int var1);

    int getParentY(int var1);
}
