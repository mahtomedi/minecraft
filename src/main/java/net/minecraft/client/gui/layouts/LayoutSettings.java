package net.minecraft.client.gui.layouts;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface LayoutSettings {
    LayoutSettings padding(int var1);

    LayoutSettings padding(int var1, int var2);

    LayoutSettings padding(int var1, int var2, int var3, int var4);

    LayoutSettings paddingLeft(int var1);

    LayoutSettings paddingTop(int var1);

    LayoutSettings paddingRight(int var1);

    LayoutSettings paddingBottom(int var1);

    LayoutSettings paddingHorizontal(int var1);

    LayoutSettings paddingVertical(int var1);

    LayoutSettings align(float var1, float var2);

    LayoutSettings alignHorizontally(float var1);

    LayoutSettings alignVertically(float var1);

    default LayoutSettings alignHorizontallyLeft() {
        return this.alignHorizontally(0.0F);
    }

    default LayoutSettings alignHorizontallyCenter() {
        return this.alignHorizontally(0.5F);
    }

    default LayoutSettings alignHorizontallyRight() {
        return this.alignHorizontally(1.0F);
    }

    default LayoutSettings alignVerticallyTop() {
        return this.alignVertically(0.0F);
    }

    default LayoutSettings alignVerticallyMiddle() {
        return this.alignVertically(0.5F);
    }

    default LayoutSettings alignVerticallyBottom() {
        return this.alignVertically(1.0F);
    }

    LayoutSettings copy();

    LayoutSettings.LayoutSettingsImpl getExposed();

    static LayoutSettings defaults() {
        return new LayoutSettings.LayoutSettingsImpl();
    }

    @OnlyIn(Dist.CLIENT)
    public static class LayoutSettingsImpl implements LayoutSettings {
        public int paddingLeft;
        public int paddingTop;
        public int paddingRight;
        public int paddingBottom;
        public float xAlignment;
        public float yAlignment;

        public LayoutSettingsImpl() {
        }

        public LayoutSettingsImpl(LayoutSettings.LayoutSettingsImpl param0) {
            this.paddingLeft = param0.paddingLeft;
            this.paddingTop = param0.paddingTop;
            this.paddingRight = param0.paddingRight;
            this.paddingBottom = param0.paddingBottom;
            this.xAlignment = param0.xAlignment;
            this.yAlignment = param0.yAlignment;
        }

        public LayoutSettings.LayoutSettingsImpl padding(int param0) {
            return this.padding(param0, param0);
        }

        public LayoutSettings.LayoutSettingsImpl padding(int param0, int param1) {
            return this.paddingHorizontal(param0).paddingVertical(param1);
        }

        public LayoutSettings.LayoutSettingsImpl padding(int param0, int param1, int param2, int param3) {
            return this.paddingLeft(param0).paddingRight(param2).paddingTop(param1).paddingBottom(param3);
        }

        public LayoutSettings.LayoutSettingsImpl paddingLeft(int param0) {
            this.paddingLeft = param0;
            return this;
        }

        public LayoutSettings.LayoutSettingsImpl paddingTop(int param0) {
            this.paddingTop = param0;
            return this;
        }

        public LayoutSettings.LayoutSettingsImpl paddingRight(int param0) {
            this.paddingRight = param0;
            return this;
        }

        public LayoutSettings.LayoutSettingsImpl paddingBottom(int param0) {
            this.paddingBottom = param0;
            return this;
        }

        public LayoutSettings.LayoutSettingsImpl paddingHorizontal(int param0) {
            return this.paddingLeft(param0).paddingRight(param0);
        }

        public LayoutSettings.LayoutSettingsImpl paddingVertical(int param0) {
            return this.paddingTop(param0).paddingBottom(param0);
        }

        public LayoutSettings.LayoutSettingsImpl align(float param0, float param1) {
            this.xAlignment = param0;
            this.yAlignment = param1;
            return this;
        }

        public LayoutSettings.LayoutSettingsImpl alignHorizontally(float param0) {
            this.xAlignment = param0;
            return this;
        }

        public LayoutSettings.LayoutSettingsImpl alignVertically(float param0) {
            this.yAlignment = param0;
            return this;
        }

        public LayoutSettings.LayoutSettingsImpl copy() {
            return new LayoutSettings.LayoutSettingsImpl(this);
        }

        @Override
        public LayoutSettings.LayoutSettingsImpl getExposed() {
            return this;
        }
    }
}
