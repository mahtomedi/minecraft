package net.minecraft.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ImageButton extends Button {
    private final ResourceLocation resourceLocation;
    private final int xTexStart;
    private final int yTexStart;
    private final int yDiffTex;
    private final int textureWidth;
    private final int textureHeight;

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
        this(param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10, TextComponent.EMPTY);
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
        this(param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10, NO_TOOLTIP, param11);
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
        Button.OnTooltip param11,
        Component param12
    ) {
        super(param0, param1, param2, param3, param12, param10, param11);
        this.textureWidth = param8;
        this.textureHeight = param9;
        this.xTexStart = param4;
        this.yTexStart = param5;
        this.yDiffTex = param6;
        this.resourceLocation = param7;
    }

    public void setPosition(int param0, int param1) {
        this.x = param0;
        this.y = param1;
    }

    @Override
    public void renderButton(PoseStack param0, int param1, int param2, float param3) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, this.resourceLocation);
        int var0 = this.yTexStart;
        if (this.isHovered()) {
            var0 += this.yDiffTex;
        }

        RenderSystem.enableDepthTest();
        blit(param0, this.x, this.y, (float)this.xTexStart, (float)var0, this.width, this.height, this.textureWidth, this.textureHeight);
        if (this.isHovered()) {
            this.renderToolTip(param0, param1, param2);
        }

    }
}
