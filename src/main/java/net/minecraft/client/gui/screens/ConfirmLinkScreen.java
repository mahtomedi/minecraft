package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ConfirmLinkScreen extends ConfirmScreen {
    private static final Component COPY_BUTTON_TEXT = Component.translatable("chat.copy");
    private static final Component WARNING_TEXT = Component.translatable("chat.link.warning");
    private final String url;
    private final boolean showWarning;

    public ConfirmLinkScreen(BooleanConsumer param0, String param1, boolean param2) {
        this(param0, confirmMessage(param2), Component.literal(param1), param1, param2 ? CommonComponents.GUI_CANCEL : CommonComponents.GUI_NO, param2);
    }

    public ConfirmLinkScreen(BooleanConsumer param0, Component param1, String param2, boolean param3) {
        this(param0, param1, param2, param3 ? CommonComponents.GUI_CANCEL : CommonComponents.GUI_NO, param3);
    }

    public ConfirmLinkScreen(BooleanConsumer param0, Component param1, String param2, Component param3, boolean param4) {
        this(param0, param1, confirmMessage(param4, param2), param2, param3, param4);
    }

    public ConfirmLinkScreen(BooleanConsumer param0, Component param1, Component param2, String param3, Component param4, boolean param5) {
        super(param0, param1, param2);
        this.yesButton = (Component)(param5 ? Component.translatable("chat.link.open") : CommonComponents.GUI_YES);
        this.noButton = param4;
        this.showWarning = !param5;
        this.url = param3;
    }

    protected static MutableComponent confirmMessage(boolean param0, String param1) {
        return confirmMessage(param0).append(CommonComponents.SPACE).append(Component.literal(param1));
    }

    protected static MutableComponent confirmMessage(boolean param0) {
        return Component.translatable(param0 ? "chat.link.confirmTrusted" : "chat.link.confirm");
    }

    @Override
    protected void addButtons(int param0) {
        this.addRenderableWidget(
            Button.builder(this.yesButton, param0x -> this.callback.accept(true)).bounds(this.width / 2 - 50 - 105, param0, 100, 20).build()
        );
        this.addRenderableWidget(Button.builder(COPY_BUTTON_TEXT, param0x -> {
            this.copyToClipboard();
            this.callback.accept(false);
        }).bounds(this.width / 2 - 50, param0, 100, 20).build());
        this.addRenderableWidget(
            Button.builder(this.noButton, param0x -> this.callback.accept(false)).bounds(this.width / 2 - 50 + 105, param0, 100, 20).build()
        );
    }

    public void copyToClipboard() {
        this.minecraft.keyboardHandler.setClipboard(this.url);
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        super.render(param0, param1, param2, param3);
        if (this.showWarning) {
            drawCenteredString(param0, this.font, WARNING_TEXT, this.width / 2, 110, 16764108);
        }

    }
}
