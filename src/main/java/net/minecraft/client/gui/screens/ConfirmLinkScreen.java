package net.minecraft.client.gui.screens;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ConfirmLinkScreen extends ConfirmScreen {
    private final String warning;
    private final String copyButton;
    private final String url;
    private final boolean showWarning;

    public ConfirmLinkScreen(BooleanConsumer param0, String param1, boolean param2) {
        super(param0, new TranslatableComponent(param2 ? "chat.link.confirmTrusted" : "chat.link.confirm"), new TextComponent(param1));
        this.yesButton = I18n.get(param2 ? "chat.link.open" : "gui.yes");
        this.noButton = I18n.get(param2 ? "gui.cancel" : "gui.no");
        this.copyButton = I18n.get("chat.copy");
        this.warning = I18n.get("chat.link.warning");
        this.showWarning = !param2;
        this.url = param1;
    }

    @Override
    protected void init() {
        super.init();
        this.buttons.clear();
        this.children.clear();
        this.addButton(new Button(this.width / 2 - 50 - 105, this.height / 6 + 96, 100, 20, this.yesButton, param0 -> this.callback.accept(true)));
        this.addButton(new Button(this.width / 2 - 50, this.height / 6 + 96, 100, 20, this.copyButton, param0 -> {
            this.copyToClipboard();
            this.callback.accept(false);
        }));
        this.addButton(new Button(this.width / 2 - 50 + 105, this.height / 6 + 96, 100, 20, this.noButton, param0 -> this.callback.accept(false)));
    }

    public void copyToClipboard() {
        this.minecraft.keyboardHandler.setClipboard(this.url);
    }

    @Override
    public void render(int param0, int param1, float param2) {
        super.render(param0, param1, param2);
        if (this.showWarning) {
            this.drawCenteredString(this.font, this.warning, this.width / 2, 110, 16764108);
        }

    }
}
