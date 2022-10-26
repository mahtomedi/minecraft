package net.minecraft.client.gui.screens.inventory.tooltip;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.tooltip.BundleTooltip;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public interface ClientTooltipComponent {
    static ClientTooltipComponent create(FormattedCharSequence param0) {
        return new ClientTextTooltip(param0);
    }

    static ClientTooltipComponent create(TooltipComponent param0) {
        if (param0 instanceof BundleTooltip) {
            return new ClientBundleTooltip((BundleTooltip)param0);
        } else {
            throw new IllegalArgumentException("Unknown TooltipComponent");
        }
    }

    int getHeight();

    int getWidth(Font var1);

    default void renderText(Font param0, int param1, int param2, Matrix4f param3, MultiBufferSource.BufferSource param4) {
    }

    default void renderImage(Font param0, int param1, int param2, PoseStack param3, ItemRenderer param4, int param5) {
    }
}
