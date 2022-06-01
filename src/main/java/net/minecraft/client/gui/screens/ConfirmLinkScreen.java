package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ConfirmLinkScreen extends ConfirmScreen {
    private static final Component COPY_BUTTON_TEXT = Component.translatable("chat.copy");
    private static final Component WARNING_TEXT = Component.translatable("chat.link.warning");
    private final String url;
    private final boolean showWarning;

    public ConfirmLinkScreen(BooleanConsumer param0, Component param1, String param2, boolean param3) {
        super(param0, param1, Component.translatable(param3 ? "chat.link.confirmTrusted" : "chat.link.confirm").append(" ").append(Component.literal(param2)));
        this.yesButton = (Component)(param3 ? Component.translatable("chat.link.open") : CommonComponents.GUI_YES);
        this.noButton = param3 ? CommonComponents.GUI_CANCEL : CommonComponents.GUI_NO;
        this.showWarning = !param3;
        this.url = param2;
    }

    public ConfirmLinkScreen(BooleanConsumer param0, String param1, boolean param2) {
        super(param0, Component.translatable(param2 ? "chat.link.confirmTrusted" : "chat.link.confirm"), Component.literal(param1));
        this.yesButton = (Component)(param2 ? Component.translatable("chat.link.open") : CommonComponents.GUI_YES);
        this.noButton = param2 ? CommonComponents.GUI_CANCEL : CommonComponents.GUI_NO;
        this.showWarning = !param2;
        this.url = param1;
    }

    @Override
    protected void addButtons(int param0) {
        this.addRenderableWidget(new Button(this.width / 2 - 50 - 105, this.height / 6 + 96, 100, 20, this.yesButton, param0x -> this.callback.accept(true)));
        this.addRenderableWidget(new Button(this.width / 2 - 50, this.height / 6 + 96, 100, 20, COPY_BUTTON_TEXT, param0x -> {
            this.copyToClipboard();
            this.callback.accept(false);
        }));
        this.addRenderableWidget(new Button(this.width / 2 - 50 + 105, this.height / 6 + 96, 100, 20, this.noButton, param0x -> this.callback.accept(false)));
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
