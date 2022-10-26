package net.minecraft.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PlainTextButton extends Button {
    private final Font font;
    private final Component message;
    private final Component underlinedMessage;

    public PlainTextButton(int param0, int param1, int param2, int param3, Component param4, Button.OnPress param5, Font param6) {
        super(param0, param1, param2, param3, param4, param5, NO_TOOLTIP, DEFAULT_NARRATION);
        this.font = param6;
        this.message = param4;
        this.underlinedMessage = ComponentUtils.mergeStyles(param4.copy(), Style.EMPTY.withUnderlined(true));
    }

    @Override
    public void renderButton(PoseStack param0, int param1, int param2, float param3) {
        Component var0 = this.isHoveredOrFocused() ? this.underlinedMessage : this.message;
        drawString(param0, this.font, var0, this.getX(), this.getY(), 16777215 | Mth.ceil(this.alpha * 255.0F) << 24);
    }
}
