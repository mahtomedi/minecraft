package net.minecraft.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MultiLineTextWidget extends AbstractWidget {
    private final MultiLineLabel multiLineLabel;
    private final int lineHeight;
    private final boolean centered;

    protected MultiLineTextWidget(MultiLineLabel param0, Font param1, Component param2, boolean param3) {
        super(0, 0, param0.getWidth(), param0.getLineCount() * 9, param2);
        this.multiLineLabel = param0;
        this.lineHeight = 9;
        this.centered = param3;
        this.active = false;
    }

    public static MultiLineTextWidget createCentered(int param0, Font param1, Component param2) {
        MultiLineLabel var0 = MultiLineLabel.create(param1, param2, param0);
        return new MultiLineTextWidget(var0, param1, param2, true);
    }

    public static MultiLineTextWidget create(int param0, Font param1, Component param2) {
        MultiLineLabel var0 = MultiLineLabel.create(param1, param2, param0);
        return new MultiLineTextWidget(var0, param1, param2, false);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput param0) {
    }

    @Override
    public void renderButton(PoseStack param0, int param1, int param2, float param3) {
        if (this.centered) {
            this.multiLineLabel.renderCentered(param0, this.getX() + this.getWidth() / 2, this.getY(), this.lineHeight, 16777215);
        } else {
            this.multiLineLabel.renderLeftAligned(param0, this.getX(), this.getY(), this.lineHeight, 16777215);
        }

    }
}
