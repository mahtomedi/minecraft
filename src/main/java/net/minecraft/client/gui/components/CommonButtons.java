package net.minecraft.client.gui.components;

import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CommonButtons {
    public static TextAndImageButton languageTextAndImage(Button.OnPress param0) {
        return TextAndImageButton.builder(Component.translatable("options.language"), Button.WIDGETS_LOCATION, param0)
            .texStart(4, 110)
            .offset(65, 3)
            .yDiffTex(20)
            .usedTextureSize(13, 13)
            .textureSize(256, 256)
            .build();
    }

    public static TextAndImageButton accessibilityTextAndImage(Button.OnPress param0) {
        return TextAndImageButton.builder(Component.translatable("options.accessibility.title"), Button.ACCESSIBILITY_TEXTURE, param0)
            .texStart(3, 3)
            .offset(65, 3)
            .yDiffTex(20)
            .usedTextureSize(15, 15)
            .textureSize(32, 64)
            .build();
    }
}
