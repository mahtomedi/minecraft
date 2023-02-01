package net.minecraft.client.gui.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.AccessibilityOptionsScreen;
import net.minecraft.client.gui.screens.LanguageSelectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CommonButtons {
    public static TextAndImageButton languageTextAndImage(Minecraft param0, Screen param1) {
        return TextAndImageButton.builder(
                Component.translatable("options.language"),
                Button.WIDGETS_LOCATION,
                param2 -> param0.setScreen(new LanguageSelectScreen(param1, param0.options, param0.getLanguageManager()))
            )
            .texStart(4, 110)
            .offset(65, 3)
            .yDiffTex(20)
            .usedTextureSize(13, 13)
            .textureSize(256, 256)
            .build();
    }

    public static TextAndImageButton accessibilityTextAndImage(Minecraft param0, Screen param1) {
        return TextAndImageButton.builder(
                Component.translatable("options.accessibility.title"),
                Button.ACCESSIBILITY_TEXTURE,
                param2 -> param0.setScreen(new AccessibilityOptionsScreen(param1, param0.options))
            )
            .texStart(3, 3)
            .offset(65, 3)
            .yDiffTex(20)
            .usedTextureSize(15, 15)
            .textureSize(32, 64)
            .build();
    }
}
