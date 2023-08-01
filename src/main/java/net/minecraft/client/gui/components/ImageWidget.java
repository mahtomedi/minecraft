package net.minecraft.client.gui.components;

import javax.annotation.Nullable;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class ImageWidget extends AbstractWidget {
    ImageWidget(int param0, int param1, int param2, int param3) {
        super(param0, param1, param2, param3, CommonComponents.EMPTY);
    }

    public static ImageWidget texture(int param0, int param1, ResourceLocation param2, int param3, int param4) {
        return new ImageWidget.Texture(0, 0, param0, param1, param2, param3, param4);
    }

    public static ImageWidget sprite(int param0, int param1, ResourceLocation param2) {
        return new ImageWidget.Sprite(0, 0, param0, param1, param2);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput param0) {
    }

    @Override
    public void playDownSound(SoundManager param0) {
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Nullable
    @Override
    public ComponentPath nextFocusPath(FocusNavigationEvent param0) {
        return null;
    }

    @OnlyIn(Dist.CLIENT)
    static class Sprite extends ImageWidget {
        private final ResourceLocation sprite;

        public Sprite(int param0, int param1, int param2, int param3, ResourceLocation param4) {
            super(param0, param1, param2, param3);
            this.sprite = param4;
        }

        @Override
        public void renderWidget(GuiGraphics param0, int param1, int param2, float param3) {
            param0.blitSprite(this.sprite, this.getX(), this.getY(), this.getWidth(), this.getHeight());
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class Texture extends ImageWidget {
        private final ResourceLocation texture;
        private final int textureWidth;
        private final int textureHeight;

        public Texture(int param0, int param1, int param2, int param3, ResourceLocation param4, int param5, int param6) {
            super(param0, param1, param2, param3);
            this.texture = param4;
            this.textureWidth = param5;
            this.textureHeight = param6;
        }

        @Override
        protected void renderWidget(GuiGraphics param0, int param1, int param2, float param3) {
            param0.blit(
                this.texture,
                this.getX(),
                this.getY(),
                this.getWidth(),
                this.getHeight(),
                0.0F,
                0.0F,
                this.getWidth(),
                this.getHeight(),
                this.textureWidth,
                this.textureHeight
            );
        }
    }
}
