package net.minecraft.client.gui.screens.telemetry;

import java.nio.file.Path;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LinearLayout;
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
    private static final Component BUTTON_PRIVACY_STATEMENT = Component.translatable("telemetry_info.button.privacy_statement");
    private static final Component BUTTON_GIVE_FEEDBACK = Component.translatable("telemetry_info.button.give_feedback");
    private static final Component BUTTON_SHOW_DATA = Component.translatable("telemetry_info.button.show_data");
    private static final Component CHECKBOX_OPT_IN = Component.translatable("telemetry_info.opt_in.description");
    private final Screen lastScreen;
    private final Options options;
    @Nullable
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
        LinearLayout var1 = var0.addChild(LinearLayout.vertical(), var0.newChildLayoutSettings().align(0.5F, 0.0F));
        var1.defaultCellSetting().alignHorizontallyCenter().paddingBottom(8);
        var1.addChild(new StringWidget(this.getTitle(), this.font));
        var1.addChild(new MultiLineTextWidget(DESCRIPTION, this.font).setMaxWidth(this.width - 16).setCentered(true));
        GridLayout var2 = this.twoButtonContainer(
            Button.builder(BUTTON_PRIVACY_STATEMENT, this::openPrivacyStatementLink).build(),
            Button.builder(BUTTON_GIVE_FEEDBACK, this::openFeedbackLink).build()
        );
        var1.addChild(var2);
        Layout var3 = this.createLowerSection();
        var0.arrangeElements();
        var3.arrangeElements();
        int var4 = var2.getY() + var2.getHeight();
        int var5 = var3.getHeight();
        int var6 = this.height - var4 - var5 - 16;
        this.telemetryEventWidget = new TelemetryEventWidget(0, 0, this.width - 40, var6, this.minecraft.font);
        this.telemetryEventWidget.setScrollAmount(this.savedScroll);
        this.telemetryEventWidget.setOnScrolledListener(param0 -> this.savedScroll = param0);
        this.setInitialFocus(this.telemetryEventWidget);
        var1.addChild(this.telemetryEventWidget);
        var1.addChild(var3);
        var0.arrangeElements();
        FrameLayout.alignInRectangle(var0, 0, 0, this.width, this.height, 0.5F, 0.0F);
        var0.visitWidgets(param1 -> {
        });
    }

    private Layout createLowerSection() {
        LinearLayout var0 = LinearLayout.vertical();
        var0.defaultCellSetting().alignHorizontallyCenter().paddingBottom(4);
        if (this.minecraft.extraTelemetryAvailable()) {
            var0.addChild(this.createTelemetryCheckbox());
        }

        var0.addChild(
            this.twoButtonContainer(
                Button.builder(BUTTON_SHOW_DATA, this::openDataFolder).build(), Button.builder(CommonComponents.GUI_DONE, this::openLastScreen).build()
            )
        );
        return var0;
    }

    private AbstractWidget createTelemetryCheckbox() {
        OptionInstance<Boolean> var0 = this.options.telemetryOptInExtra();
        Checkbox var1 = Checkbox.builder(CHECKBOX_OPT_IN, this.minecraft.font).selected(var0).onValueChange(this::onOptInChanged).build();
        var1.active = this.minecraft.extraTelemetryAvailable();
        return var1;
    }

    private void onOptInChanged(AbstractWidget param0, boolean param1) {
        if (this.telemetryEventWidget != null) {
            this.telemetryEventWidget.onOptInChanged(param1);
        }

    }

    private void openLastScreen(Button param0) {
        this.minecraft.setScreen(this.lastScreen);
    }

    private void openPrivacyStatementLink(Button param0) {
        ConfirmLinkScreen.confirmLinkNow(this, "http://go.microsoft.com/fwlink/?LinkId=521839");
    }

    private void openFeedbackLink(Button param0) {
        ConfirmLinkScreen.confirmLinkNow(this, "https://aka.ms/javafeedback?ref=game");
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
    public void renderBackground(GuiGraphics param0, int param1, int param2, float param3) {
        this.renderDirtBackground(param0);
    }

    private GridLayout twoButtonContainer(AbstractWidget param0, AbstractWidget param1) {
        GridLayout var0 = new GridLayout();
        var0.defaultCellSetting().alignHorizontallyCenter().paddingHorizontal(4);
        var0.addChild(param0, 0, 0);
        var0.addChild(param1, 0, 1);
        return var0;
    }
}
