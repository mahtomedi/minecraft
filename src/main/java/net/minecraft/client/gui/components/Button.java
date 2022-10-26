package net.minecraft.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Button extends AbstractButton {
    public static final Button.OnTooltip NO_TOOLTIP = (param0, param1, param2, param3) -> {
    };
    public static final int SMALL_WIDTH = 120;
    public static final int DEFAULT_WIDTH = 150;
    public static final int DEFAULT_HEIGHT = 20;
    protected static final Button.CreateNarration DEFAULT_NARRATION = param0 -> param0.get();
    protected final Button.OnPress onPress;
    protected final Button.OnTooltip onTooltip;
    protected final Button.CreateNarration createNarration;

    public static Button.Builder builder(Component param0, Button.OnPress param1) {
        return new Button.Builder(param0, param1);
    }

    protected Button(
        int param0, int param1, int param2, int param3, Component param4, Button.OnPress param5, Button.OnTooltip param6, Button.CreateNarration param7
    ) {
        super(param0, param1, param2, param3, param4);
        this.onPress = param5;
        this.onTooltip = param6;
        this.createNarration = param7;
    }

    @Override
    public void onPress() {
        this.onPress.onPress(this);
    }

    @Override
    protected MutableComponent createNarrationMessage() {
        return this.createNarration.createNarrationMessage(() -> super.createNarrationMessage());
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
    public static class Builder {
        private final Component message;
        private final Button.OnPress onPress;
        private Button.OnTooltip onTooltip = Button.NO_TOOLTIP;
        private int x;
        private int y;
        private int width = 150;
        private int height = 20;
        private Button.CreateNarration createNarration = Button.DEFAULT_NARRATION;

        public Builder(Component param0, Button.OnPress param1) {
            this.message = param0;
            this.onPress = param1;
        }

        public Button.Builder pos(int param0, int param1) {
            this.x = param0;
            this.y = param1;
            return this;
        }

        public Button.Builder width(int param0) {
            this.width = param0;
            return this;
        }

        public Button.Builder size(int param0, int param1) {
            this.width = param0;
            this.height = param1;
            return this;
        }

        public Button.Builder bounds(int param0, int param1, int param2, int param3) {
            return this.pos(param0, param1).size(param2, param3);
        }

        public Button.Builder tooltip(Button.OnTooltip param0) {
            this.onTooltip = param0;
            return this;
        }

        public Button.Builder createNarration(Button.CreateNarration param0) {
            this.createNarration = param0;
            return this;
        }

        public Button build() {
            return new Button(this.x, this.y, this.width, this.height, this.message, this.onPress, this.onTooltip, this.createNarration);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public interface CreateNarration {
        MutableComponent createNarrationMessage(Supplier<MutableComponent> var1);
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
