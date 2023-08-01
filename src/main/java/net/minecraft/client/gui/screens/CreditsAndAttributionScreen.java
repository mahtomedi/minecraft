package net.minecraft.client.gui.screens;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CreditsAndAttributionScreen extends Screen {
    private static final int BUTTON_SPACING = 8;
    private static final int BUTTON_WIDTH = 210;
    private static final Component TITLE = Component.translatable("credits_and_attribution.screen.title");
    private static final Component CREDITS_BUTTON = Component.translatable("credits_and_attribution.button.credits");
    private static final Component ATTRIBUTION_BUTTON = Component.translatable("credits_and_attribution.button.attribution");
    private static final Component LICENSES_BUTTON = Component.translatable("credits_and_attribution.button.licenses");
    private final Screen lastScreen;
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);

    public CreditsAndAttributionScreen(Screen param0) {
        super(TITLE);
        this.lastScreen = param0;
    }

    @Override
    protected void init() {
        this.layout.addToHeader(new StringWidget(this.getTitle(), this.font));
        LinearLayout var0 = this.layout.addToContents(LinearLayout.vertical()).spacing(8);
        var0.defaultCellSetting().alignHorizontallyCenter();
        var0.addChild(Button.builder(CREDITS_BUTTON, param0 -> this.openCreditsScreen()).width(210).build());
        var0.addChild(
            Button.builder(ATTRIBUTION_BUTTON, ConfirmLinkScreen.confirmLink("https://aka.ms/MinecraftJavaAttribution", this, true)).width(210).build()
        );
        var0.addChild(Button.builder(LICENSES_BUTTON, ConfirmLinkScreen.confirmLink("https://aka.ms/MinecraftJavaLicenses", this, true)).width(210).build());
        this.layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, param0 -> this.onClose()).build());
        this.layout.arrangeElements();
        this.layout.visitWidgets(this::addRenderableWidget);
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
    }

    private void openCreditsScreen() {
        this.minecraft.setScreen(new WinScreen(false, () -> this.minecraft.setScreen(this)));
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
    }
}
