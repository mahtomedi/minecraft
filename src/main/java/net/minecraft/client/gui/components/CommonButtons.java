package net.minecraft.client.gui.components;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CommonButtons {
    public static SpriteIconButton language(int param0, Button.OnPress param1, boolean param2) {
        return SpriteIconButton.builder(Component.translatable("options.language"), param1, param2)
            .width(param0)
            .sprite(new ResourceLocation("icon/language"), 15, 15)
            .build();
    }

    public static SpriteIconButton accessibility(int param0, Button.OnPress param1, boolean param2) {
        return SpriteIconButton.builder(Component.translatable("options.accessibility"), param1, param2)
            .width(param0)
            .sprite(new ResourceLocation("icon/accessibility"), 15, 15)
            .build();
    }
}
