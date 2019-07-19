package net.minecraft.client.renderer.entity;

import net.minecraft.world.entity.HumanoidArm;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface ArmedModel {
    void translateToHand(float var1, HumanoidArm var2);
}
