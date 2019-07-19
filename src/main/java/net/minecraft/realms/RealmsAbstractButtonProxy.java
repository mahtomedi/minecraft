package net.minecraft.realms;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface RealmsAbstractButtonProxy<T extends AbstractRealmsButton<?>> {
    T getButton();

    boolean active();

    void active(boolean var1);

    boolean isVisible();

    void setVisible(boolean var1);
}
