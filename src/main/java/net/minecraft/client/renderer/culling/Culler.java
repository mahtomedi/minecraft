package net.minecraft.client.renderer.culling;

import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface Culler {
    boolean isVisible(AABB var1);

    void prepare(double var1, double var3, double var5);
}
