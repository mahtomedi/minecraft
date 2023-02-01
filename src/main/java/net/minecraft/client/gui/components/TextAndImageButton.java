package net.minecraft.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TextAndImageButton extends ImageButton {
    private static final int TEXT_OVERFLOW_PADDING = 5;
    private final int xOffset;
    private final int yOffset;
    private final int usedTextureWidth;
    private final int usedTextureHeight;

    TextAndImageButton(
        Component param0,
        int param1,
        int param2,
        int param3,
        int param4,
        int param5,
        int param6,
        int param7,
        int param8,
        int param9,
        ResourceLocation param10,
        Button.OnPress param11
    ) {
        super(0, 0, 150, 20, param1, param2, param5, param10, param8, param9, param11, param0);
        this.xOffset = param3;
        this.yOffset = param4;
        this.usedTextureWidth = param6;
        this.usedTextureHeight = param7;
    }

    @Override
    public void renderWidget(PoseStack param0, int param1, int param2, float param3) {
        this.renderButton(param0, param1, param2);
        this.renderTexture(
            param0,
            this.resourceLocation,
            this.getXOffset(),
            this.getYOffset(),
            this.xTexStart,
            this.yTexStart,
            this.yDiffTex,
            this.usedTextureWidth,
            this.usedTextureHeight,
            this.textureWidth,
            this.textureHeight
        );
    }

    @Override
    public void renderString(PoseStack param0, Font param1, int param2, int param3, int param4) {
        FormattedCharSequence var0 = this.getMessage().getVisualOrderText();
        int var1 = param1.width(var0);
        int var2 = param2 - var1 / 2;
        int var3 = var2 + var1;
        int var4 = this.getX() + this.width - this.usedTextureWidth - 5;
        if (var3 >= var4) {
            var2 -= var3 - var4;
        }

        drawString(param0, param1, var0, var2, param3, param4);
    }

    private int getXOffset() {
        return this.getX() + (this.width / 2 - this.usedTextureWidth / 2) + this.xOffset;
    }

    private int getYOffset() {
        return this.getY() + this.yOffset;
    }

    public static TextAndImageButton.Builder builder(Component param0, ResourceLocation param1, Button.OnPress param2) {
        return new TextAndImageButton.Builder(param0, param1, param2);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Builder {
        private final Component message;
        private final ResourceLocation resourceLocation;
        private final Button.OnPress onPress;
        private int xTexStart;
        private int yTexStart;
        private int yDiffTex;
        private int usedTextureWidth;
        private int usedTextureHeight;
        private int textureWidth;
        private int textureHeight;
        private int xOffset;
        private int yOffset;

        public Builder(Component param0, ResourceLocation param1, Button.OnPress param2) {
            this.message = param0;
            this.resourceLocation = param1;
            this.onPress = param2;
        }

        public TextAndImageButton.Builder texStart(int param0, int param1) {
            this.xTexStart = param0;
            this.yTexStart = param1;
            return this;
        }

        public TextAndImageButton.Builder offset(int param0, int param1) {
            this.xOffset = param0;
            this.yOffset = param1;
            return this;
        }

        public TextAndImageButton.Builder yDiffTex(int param0) {
            this.yDiffTex = param0;
            return this;
        }

        public TextAndImageButton.Builder usedTextureSize(int param0, int param1) {
            this.usedTextureWidth = param0;
            this.usedTextureHeight = param1;
            return this;
        }

        public TextAndImageButton.Builder textureSize(int param0, int param1) {
            this.textureWidth = param0;
            this.textureHeight = param1;
            return this;
        }

        public TextAndImageButton build() {
            return new TextAndImageButton(
                this.message,
                this.xTexStart,
                this.yTexStart,
                this.xOffset,
                this.yOffset,
                this.yDiffTex,
                this.usedTextureWidth,
                this.usedTextureHeight,
                this.textureWidth,
                this.textureHeight,
                this.resourceLocation,
                this.onPress
            );
        }
    }
}
