package net.minecraft.realms.pluginapi;

import net.minecraft.realms.RealmsScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface LoadedRealmsPlugin {
    RealmsScreen getMainScreen(RealmsScreen var1);

    RealmsScreen getNotificationsScreen(RealmsScreen var1);
}
