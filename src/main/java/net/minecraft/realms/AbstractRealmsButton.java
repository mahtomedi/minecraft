package net.minecraft.realms;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractRealmsButton<P extends AbstractWidget & RealmsAbstractButtonProxy<?>> {
    public abstract P getProxy();

    public boolean active() {
        return this.getProxy().active();
    }

    public void active(boolean param0) {
        this.getProxy().active(param0);
    }

    public boolean isVisible() {
        return this.getProxy().isVisible();
    }

    public void setVisible(boolean param0) {
        this.getProxy().setVisible(param0);
    }

    public void render(int param0, int param1, float param2) {
        this.getProxy().render(param0, param1, param2);
    }

    public void blit(int param0, int param1, int param2, int param3, int param4, int param5) {
        this.getProxy().blit(param0, param1, param2, param3, param4, param5);
    }

    public void tick() {
    }
}
