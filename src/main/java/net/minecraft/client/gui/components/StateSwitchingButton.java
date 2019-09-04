package net.minecraft.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
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
        super(param0, param1, param2, param3, "");
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
    public void renderButton(int param0, int param1, float param2) {
        Minecraft var0 = Minecraft.getInstance();
        var0.getTextureManager().bind(this.resourceLocation);
        RenderSystem.disableDepthTest();
        int var1 = this.xTexStart;
        int var2 = this.yTexStart;
        if (this.isStateTriggered) {
            var1 += this.xDiffTex;
        }

        if (this.isHovered()) {
            var2 += this.yDiffTex;
        }

        this.blit(this.x, this.y, var1, var2, this.width, this.height);
        RenderSystem.enableDepthTest();
    }
}
