package net.minecraft.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
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
        super(param0, param1, param2, param3, param11, param10);
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
        Minecraft var0 = Minecraft.getInstance();
        var0.getTextureManager().bind(this.resourceLocation);
        RenderSystem.disableDepthTest();
        int var1 = this.yTexStart;
        if (this.isHovered()) {
            var1 += this.yDiffTex;
        }

        blit(param0, this.x, this.y, (float)this.xTexStart, (float)var1, this.width, this.height, this.textureWidth, this.textureHeight);
        RenderSystem.enableDepthTest();
    }
}
