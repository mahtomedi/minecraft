package net.minecraft.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Consumer;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Button extends AbstractButton {
    public static final Button.OnTooltip NO_TOOLTIP = (param0, param1, param2, param3) -> {
    };
    public static final int SMALL_WIDTH = 120;
    public static final int DEFAULT_WIDTH = 150;
    public static final int DEFAULT_HEIGHT = 20;
    protected final Button.OnPress onPress;
    protected final Button.OnTooltip onTooltip;

    public Button(int param0, int param1, int param2, int param3, Component param4, Button.OnPress param5) {
        this(param0, param1, param2, param3, param4, param5, NO_TOOLTIP);
    }

    public Button(int param0, int param1, int param2, int param3, Component param4, Button.OnPress param5, Button.OnTooltip param6) {
        super(param0, param1, param2, param3, param4);
        this.onPress = param5;
        this.onTooltip = param6;
    }

    @Override
    public void onPress() {
        this.onPress.onPress(this);
    }

    @Override
    public void renderButton(PoseStack param0, int param1, int param2, float param3) {
        super.renderButton(param0, param1, param2, param3);
        if (this.isHoveredOrFocused()) {
            this.renderToolTip(param0, param1, param2);
        }

    }

    @Override
    public void renderToolTip(PoseStack param0, int param1, int param2) {
        this.onTooltip.onTooltip(this, param0, param1, param2);
    }

    @Override
    public void updateNarration(NarrationElementOutput param0) {
        this.defaultButtonNarrationText(param0);
        this.onTooltip.narrateTooltip(param1 -> param0.add(NarratedElementType.HINT, param1));
    }

    @OnlyIn(Dist.CLIENT)
    public interface OnPress {
        void onPress(Button var1);
    }

    @OnlyIn(Dist.CLIENT)
    public interface OnTooltip {
        void onTooltip(Button var1, PoseStack var2, int var3, int var4);

        default void narrateTooltip(Consumer<Component> param0) {
        }
    }
}
