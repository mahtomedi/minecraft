package net.minecraft.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StateSwitchingButton extends AbstractWidget {
    protected ResourceLocation resourceLocation;
    protected boolean isStateTriggered;
    protected int xTexStart;
    protected int yTexStart;
    protected int xDiffTex;
    protected int yDiffTex;

    public StateSwitchingButton(int param0, int param1, int param2, int param3, boolean param4) {
        super(param0, param1, param2, param3, CommonComponents.EMPTY);
        this.isStateTriggered = param4;
    }

    public void initTextureValues(int param0, int param1, int param2, int param3, ResourceLocation param4) {
        this.xTexStart = param0;
        this.yTexStart = param1;
        this.xDiffTex = param2;
        this.yDiffTex = param3;
        this.resourceLocation = param4;
    }

    public void setStateTriggered(boolean param0) {
        this.isStateTriggered = param0;
    }

    public boolean isStateTriggered() {
        return this.isStateTriggered;
    }

    public void setPosition(int param0, int param1) {
        this.x = param0;
        this.y = param1;
    }

    @Override
    public void updateNarration(NarrationElementOutput param0) {
        this.defaultButtonNarrationText(param0);
    }

    @Override
    public void renderButton(PoseStack param0, int param1, int param2, float param3) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, this.resourceLocation);
        RenderSystem.disableDepthTest();
        int var0 = this.xTexStart;
        int var1 = this.yTexStart;
        if (this.isStateTriggered) {
            var0 += this.xDiffTex;
        }

        if (this.isHoveredOrFocused()) {
            var1 += this.yDiffTex;
        }

        this.blit(param0, this.x, this.y, var0, var1, this.width, this.height);
        RenderSystem.enableDepthTest();
    }
}
