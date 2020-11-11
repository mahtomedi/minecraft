package net.minecraft.world.level.material;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum FogType {
    LAVA,
    WATER,
    POWDER_SNOW,
    NONE;
}
