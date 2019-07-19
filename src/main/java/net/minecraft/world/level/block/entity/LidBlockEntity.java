package net.minecraft.world.level.block.entity;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface LidBlockEntity {
    float getOpenNess(float var1);
}
