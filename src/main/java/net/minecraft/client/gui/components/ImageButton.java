package net.minecraft.client.gui.components;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ImageButton extends Button {
    protected final WidgetSprites sprites;

    public ImageButton(int param0, int param1, int param2, int param3, WidgetSprites param4, Button.OnPress param5) {
        this(param0, param1, param2, param3, param4, param5, CommonComponents.EMPTY);
    }

    public ImageButton(int param0, int param1, int param2, int param3, WidgetSprites param4, Button.OnPress param5, Component param6) {
        super(param0, param1, param2, param3, param6, param5, DEFAULT_NARRATION);
        this.sprites = param4;
    }

    public ImageButton(int param0, int param1, WidgetSprites param2, Button.OnPress param3, Component param4) {
        this(0, 0, param0, param1, param2, param3, param4);
    }

    @Override
    public void renderWidget(GuiGraphics param0, int param1, int param2, float param3) {
        ResourceLocation var0 = this.sprites.get(this.isActive(), this.isHoveredOrFocused());
        param0.blitSprite(var0, this.getX(), this.getY(), this.width, this.height);
    }
}
