package net.minecraft.client.gui.spectator;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface SpectatorMenuItem {
    void selectItem(SpectatorMenu var1);

    Component getName();

    void renderIcon(PoseStack var1, float var2, int var3);

    boolean isEnabled();
}
