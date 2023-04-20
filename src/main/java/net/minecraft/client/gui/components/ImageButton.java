package net.minecraft.client.gui.components;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ImageButton extends Button {
    protected final ResourceLocation resourceLocation;
    protected final int xTexStart;
    protected final int yTexStart;
    protected final int yDiffTex;
    protected final int textureWidth;
    protected final int textureHeight;

    public ImageButton(int param0, int param1, int param2, int param3, int param4, int param5, ResourceLocation param6, Button.OnPress param7) {
        this(param0, param1, param2, param3, param4, param5, param3, param6, 256, 256, param7);
    }

    public ImageButton(int param0, int param1, int param2, int param3, int param4, int param5, int param6, ResourceLocation param7, Button.OnPress param8) {
        this(param0, param1, param2, param3, param4, param5, param6, param7, 256, 256, param8);
    }

    public ImageButton(
        int param0,
        int param1,
        int param2,
        int param3,
        int param4,
        int param5,
        int param6,
        ResourceLocation param7,
        int param8,
        int param9,
        Button.OnPress param10
    ) {
        this(param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10, CommonComponents.EMPTY);
    }

    public ImageButton(
        int param0,
        int param1,
        int param2,
        int param3,
        int param4,
        int param5,
        int param6,
        ResourceLocation param7,
        int param8,
        int param9,
        Button.OnPress param10,
        Component param11
    ) {
        super(param0, param1, param2, param3, param11, param10, DEFAULT_NARRATION);
        this.textureWidth = param8;
        this.textureHeight = param9;
        this.xTexStart = param4;
        this.yTexStart = param5;
        this.yDiffTex = param6;
        this.resourceLocation = param7;
    }

    @Override
    public void renderWidget(GuiGraphics param0, int param1, int param2, float param3) {
        this.renderTexture(
            param0,
            this.resourceLocation,
            this.getX(),
            this.getY(),
            this.xTexStart,
            this.yTexStart,
            this.yDiffTex,
            this.width,
            this.height,
            this.textureWidth,
            this.textureHeight
        );
    }
}
