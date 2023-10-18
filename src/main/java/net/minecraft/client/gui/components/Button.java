package net.minecraft.client.gui.components;

import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Button extends AbstractButton {
    public static final int SMALL_WIDTH = 120;
    public static final int DEFAULT_WIDTH = 150;
    public static final int DEFAULT_HEIGHT = 20;
    public static final int DEFAULT_SPACING = 8;
    protected static final Button.CreateNarration DEFAULT_NARRATION = param0 -> param0.get();
    protected final Button.OnPress onPress;
    protected final Button.CreateNarration createNarration;

    public static Button.Builder builder(Component param0, Button.OnPress param1) {
        return new Button.Builder(param0, param1);
    }

    protected Button(int param0, int param1, int param2, int param3, Component param4, Button.OnPress param5, Button.CreateNarration param6) {
        super(param0, param1, param2, param3, param4);
        this.onPress = param5;
        this.createNarration = param6;
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
    public void updateWidgetNarration(NarrationElementOutput param0) {
        this.defaultButtonNarrationText(param0);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Builder {
        private final Component message;
        private final Button.OnPress onPress;
        @Nullable
        private Tooltip tooltip;
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

        public Button.Builder tooltip(@Nullable Tooltip param0) {
            this.tooltip = param0;
            return this;
        }

        public Button.Builder createNarration(Button.CreateNarration param0) {
            this.createNarration = param0;
            return this;
        }

        public Button build() {
            Button var0 = new Button(this.x, this.y, this.width, this.height, this.message, this.onPress, this.createNarration);
            var0.setTooltip(this.tooltip);
            return var0;
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
}
