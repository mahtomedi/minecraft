package net.minecraft.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StateSwitchingButton extends AbstractWidget {
    @Nullable
    protected WidgetSprites sprites;
    protected boolean isStateTriggered;

    public StateSwitchingButton(int param0, int param1, int param2, int param3, boolean param4) {
        super(param0, param1, param2, param3, CommonComponents.EMPTY);
        this.isStateTriggered = param4;
    }

    public void initTextureValues(WidgetSprites param0) {
        this.sprites = param0;
    }

    public void setStateTriggered(boolean param0) {
        this.isStateTriggered = param0;
    }

    public boolean isStateTriggered() {
        return this.isStateTriggered;
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput param0) {
        this.defaultButtonNarrationText(param0);
    }

    @Override
    public void renderWidget(GuiGraphics param0, int param1, int param2, float param3) {
        if (this.sprites != null) {
            RenderSystem.disableDepthTest();
            param0.blitSprite(this.sprites.get(this.isStateTriggered, this.isHoveredOrFocused()), this.getX(), this.getY(), this.width, this.height);
            RenderSystem.enableDepthTest();
        }
    }
}
