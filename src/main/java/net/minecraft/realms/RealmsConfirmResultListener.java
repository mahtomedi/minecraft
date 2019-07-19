package net.minecraft.realms;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface RealmsConfirmResultListener {
    void confirmResult(boolean var1, int var2);
}
