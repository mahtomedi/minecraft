package net.minecraft.client.gui.components;

import javax.annotation.Nullable;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class SpriteIconButton extends Button {
    protected final ResourceLocation sprite;
    protected final int spriteWidth;
    protected final int spriteHeight;

    SpriteIconButton(int param0, int param1, Component param2, int param3, int param4, ResourceLocation param5, Button.OnPress param6) {
        super(0, 0, param0, param1, param2, param6, DEFAULT_NARRATION);
        this.spriteWidth = param3;
        this.spriteHeight = param4;
        this.sprite = param5;
    }

    public static SpriteIconButton.Builder builder(Component param0, Button.OnPress param1, boolean param2) {
        return new SpriteIconButton.Builder(param0, param1, param2);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Builder {
        private final Component message;
        private final Button.OnPress onPress;
        private final boolean iconOnly;
        private int width = 150;
        private int height = 20;
        @Nullable
        private ResourceLocation sprite;
        private int spriteWidth;
        private int spriteHeight;

        public Builder(Component param0, Button.OnPress param1, boolean param2) {
            this.message = param0;
            this.onPress = param1;
            this.iconOnly = param2;
        }

        public SpriteIconButton.Builder width(int param0) {
            this.width = param0;
            return this;
        }

        public SpriteIconButton.Builder size(int param0, int param1) {
            this.width = param0;
            this.height = param1;
            return this;
        }

        public SpriteIconButton.Builder sprite(ResourceLocation param0, int param1, int param2) {
            this.sprite = param0;
            this.spriteWidth = param1;
            this.spriteHeight = param2;
            return this;
        }

        public SpriteIconButton build() {
            if (this.sprite == null) {
                throw new IllegalStateException("Sprite not set");
            } else {
                return (SpriteIconButton)(this.iconOnly
                    ? new SpriteIconButton.CenteredIcon(this.width, this.height, this.message, this.spriteWidth, this.spriteHeight, this.sprite, this.onPress)
                    : new SpriteIconButton.TextAndIcon(this.width, this.height, this.message, this.spriteWidth, this.spriteHeight, this.sprite, this.onPress));
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class CenteredIcon extends SpriteIconButton {
        protected CenteredIcon(int param0, int param1, Component param2, int param3, int param4, ResourceLocation param5, Button.OnPress param6) {
            super(param0, param1, param2, param3, param4, param5, param6);
        }

        @Override
        public void renderWidget(GuiGraphics param0, int param1, int param2, float param3) {
            super.renderWidget(param0, param1, param2, param3);
            int var0 = this.getX() + this.getWidth() / 2 - this.spriteWidth / 2;
            int var1 = this.getY() + this.getHeight() / 2 - this.spriteHeight / 2;
            param0.blitSprite(this.sprite, var0, var1, this.spriteWidth, this.spriteHeight);
        }

        @Override
        public void renderString(GuiGraphics param0, Font param1, int param2) {
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class TextAndIcon extends SpriteIconButton {
        protected TextAndIcon(int param0, int param1, Component param2, int param3, int param4, ResourceLocation param5, Button.OnPress param6) {
            super(param0, param1, param2, param3, param4, param5, param6);
        }

        @Override
        public void renderWidget(GuiGraphics param0, int param1, int param2, float param3) {
            super.renderWidget(param0, param1, param2, param3);
            int var0 = this.getX() + this.getWidth() - this.spriteWidth - 2;
            int var1 = this.getY() + this.getHeight() / 2 - this.spriteHeight / 2;
            param0.blitSprite(this.sprite, var0, var1, this.spriteWidth, this.spriteHeight);
        }

        @Override
        public void renderString(GuiGraphics param0, Font param1, int param2) {
            int var0 = this.getX() + 2;
            int var1 = this.getX() + this.getWidth() - this.spriteWidth - 4;
            int var2 = this.getX() + this.getWidth() / 2;
            renderScrollingString(param0, param1, this.getMessage(), var2, var0, this.getY(), var1, this.getY() + this.getHeight(), param2);
        }
    }
}
