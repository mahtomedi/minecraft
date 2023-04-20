package net.minecraft.client.gui.screens.telemetry;

import java.nio.file.Path;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TelemetryInfoScreen extends Screen {
    private static final int PADDING = 8;
    private static final Component TITLE = Component.translatable("telemetry_info.screen.title");
    private static final Component DESCRIPTION = Component.translatable("telemetry_info.screen.description").withStyle(ChatFormatting.GRAY);
    private static final Component BUTTON_GIVE_FEEDBACK = Component.translatable("telemetry_info.button.give_feedback");
    private static final Component BUTTON_SHOW_DATA = Component.translatable("telemetry_info.button.show_data");
    private final Screen lastScreen;
    private final Options options;
    private TelemetryEventWidget telemetryEventWidget;
    private double savedScroll;

    public TelemetryInfoScreen(Screen param0, Options param1) {
        super(TITLE);
        this.lastScreen = param0;
        this.options = param1;
    }

    @Override
    public Component getNarrationMessage() {
        return CommonComponents.joinForNarration(super.getNarrationMessage(), DESCRIPTION);
    }

    @Override
    protected void init() {
        FrameLayout var0 = new FrameLayout();
        var0.defaultChildLayoutSetting().padding(8);
        var0.setMinHeight(this.height);
        GridLayout var1 = var0.addChild(new GridLayout(), var0.newChildLayoutSettings().align(0.5F, 0.0F));
        var1.defaultCellSetting().alignHorizontallyCenter().paddingBottom(8);
        GridLayout.RowHelper var2 = var1.createRowHelper(1);
        var2.addChild(new StringWidget(this.getTitle(), this.font));
        var2.addChild(new MultiLineTextWidget(DESCRIPTION, this.font).setMaxWidth(this.width - 16).setCentered(true));
        GridLayout var3 = this.twoButtonContainer(
            Button.builder(BUTTON_GIVE_FEEDBACK, this::openFeedbackLink).build(), Button.builder(BUTTON_SHOW_DATA, this::openDataFolder).build()
        );
        var2.addChild(var3);
        GridLayout var4 = this.twoButtonContainer(this.createTelemetryButton(), Button.builder(CommonComponents.GUI_DONE, this::openLastScreen).build());
        var0.addChild(var4, var0.newChildLayoutSettings().align(0.5F, 1.0F));
        var0.arrangeElements();
        this.telemetryEventWidget = new TelemetryEventWidget(0, 0, this.width - 40, var4.getY() - (var3.getY() + var3.getHeight()) - 16, this.minecraft.font);
        this.telemetryEventWidget.setScrollAmount(this.savedScroll);
        this.telemetryEventWidget.setOnScrolledListener(param0 -> this.savedScroll = param0);
        this.setInitialFocus(this.telemetryEventWidget);
        var2.addChild(this.telemetryEventWidget);
        var0.arrangeElements();
        FrameLayout.alignInRectangle(var0, 0, 0, this.width, this.height, 0.5F, 0.0F);
        var0.visitWidgets(param1 -> {
        });
    }

    private AbstractWidget createTelemetryButton() {
        AbstractWidget var0 = this.options
            .telemetryOptInExtra()
            .createButton(this.options, 0, 0, 150, param0 -> this.telemetryEventWidget.onOptInChanged(param0));
        var0.active = this.minecraft.extraTelemetryAvailable();
        return var0;
    }

    private void openLastScreen(Button param0) {
        this.minecraft.setScreen(this.lastScreen);
    }

    private void openFeedbackLink(Button param0) {
        this.minecraft.setScreen(new ConfirmLinkScreen(param0x -> {
            if (param0x) {
                Util.getPlatform().openUri("https://aka.ms/javafeedback?ref=game");
            }

            this.minecraft.setScreen(this);
        }, "https://aka.ms/javafeedback?ref=game", true));
    }

    private void openDataFolder(Button param0) {
        Path var0x = this.minecraft.getTelemetryManager().getLogDirectory();
        Util.getPlatform().openUri(var0x.toUri());
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        this.renderDirtBackground(param0);
        super.render(param0, param1, param2, param3);
    }

    private GridLayout twoButtonContainer(AbstractWidget param0, AbstractWidget param1) {
        GridLayout var0 = new GridLayout();
        var0.defaultCellSetting().alignHorizontallyCenter().paddingHorizontal(4);
        var0.addChild(param0, 0, 0);
        var0.addChild(param1, 0, 1);
        return var0;
    }
}
