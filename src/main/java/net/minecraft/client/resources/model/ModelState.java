package net.minecraft.client.resources.model;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface ModelState {
    default BlockModelRotation getRotation() {
        return BlockModelRotation.X0_Y0;
    }

    default boolean isUvLocked() {
        return false;
    }
}
