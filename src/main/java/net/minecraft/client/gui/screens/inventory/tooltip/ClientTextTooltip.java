package net.minecraft.client.gui.screens.inventory.tooltip;

import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class ClientTextTooltip implements ClientTooltipComponent {
    private final FormattedCharSequence text;

    public ClientTextTooltip(FormattedCharSequence param0) {
        this.text = param0;
    }

    @Override
    public int getWidth(Font param0) {
        return param0.width(this.text);
    }

    @Override
    public int getHeight() {
        return 10;
    }

    @Override
    public void renderText(Font param0, int param1, int param2, Matrix4f param3, MultiBufferSource.BufferSource param4) {
        param0.drawInBatch(this.text, (float)param1, (float)param2, -1, true, param3, param4, false, 0, 15728880);
    }
}
