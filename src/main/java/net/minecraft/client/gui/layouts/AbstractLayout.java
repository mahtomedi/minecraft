package net.minecraft.client.gui.layouts;

import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractLayout implements Layout {
    private int x;
    private int y;
    protected int width;
    protected int height;

    public AbstractLayout(int param0, int param1, int param2, int param3) {
        this.x = param0;
        this.y = param1;
        this.width = param2;
        this.height = param3;
    }

    @Override
    public void setX(int param0) {
        this.visitChildren(param1 -> {
            int var0 = param1.getX() + (param0 - this.getX());
            param1.setX(var0);
        });
        this.x = param0;
    }

    @Override
    public void setY(int param0) {
        this.visitChildren(param1 -> {
            int var0 = param1.getY() + (param0 - this.getY());
            param1.setY(var0);
        });
        this.y = param0;
    }

    @Override
    public int getX() {
        return this.x;
    }

    @Override
    public int getY() {
        return this.y;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @OnlyIn(Dist.CLIENT)
    protected abstract static class AbstractChildWrapper {
        public final LayoutElement child;
        public final LayoutSettings.LayoutSettingsImpl layoutSettings;

        protected AbstractChildWrapper(LayoutElement param0, LayoutSettings param1) {
            this.child = param0;
            this.layoutSettings = param1.getExposed();
        }

        public int getHeight() {
            return this.child.getHeight() + this.layoutSettings.paddingTop + this.layoutSettings.paddingBottom;
        }

        public int getWidth() {
            return this.child.getWidth() + this.layoutSettings.paddingLeft + this.layoutSettings.paddingRight;
        }

        public void setX(int param0, int param1) {
            float var0 = (float)this.layoutSettings.paddingLeft;
            float var1 = (float)(param1 - this.child.getWidth() - this.layoutSettings.paddingRight);
            int var2 = (int)Mth.lerp(this.layoutSettings.xAlignment, var0, var1);
            this.child.setX(var2 + param0);
        }

        public void setY(int param0, int param1) {
            float var0 = (float)this.layoutSettings.paddingTop;
            float var1 = (float)(param1 - this.child.getHeight() - this.layoutSettings.paddingBottom);
            int var2 = (int)Mth.lerp(this.layoutSettings.yAlignment, var0, var1);
            this.child.setY(var2 + param0);
        }
    }
}
