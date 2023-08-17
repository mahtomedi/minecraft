package net.minecraft.client.gui.screens.reporting;

import com.mojang.authlib.minecraft.report.AbuseReportLimits;
import com.mojang.logging.LogUtils;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.screens.GenericWaitingScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.WarningScreen;
import net.minecraft.client.multiplayer.chat.report.Report;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ThrowingComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractReportScreen<B extends Report.Builder<?>> extends Screen {
    private static final Component REPORT_SENT_MESSAGE = Component.translatable("gui.abuseReport.report_sent_msg");
    private static final Component REPORT_SENDING_TITLE = Component.translatable("gui.abuseReport.sending.title").withStyle(ChatFormatting.BOLD);
    private static final Component REPORT_SENT_TITLE = Component.translatable("gui.abuseReport.sent.title").withStyle(ChatFormatting.BOLD);
    private static final Component REPORT_ERROR_TITLE = Component.translatable("gui.abuseReport.error.title").withStyle(ChatFormatting.BOLD);
    private static final Component REPORT_SEND_GENERIC_ERROR = Component.translatable("gui.abuseReport.send.generic_error");
    protected static final Component SEND_REPORT = Component.translatable("gui.abuseReport.send");
    protected static final Component OBSERVED_WHAT_LABEL = Component.translatable("gui.abuseReport.observed_what");
    protected static final Component SELECT_REASON = Component.translatable("gui.abuseReport.select_reason");
    private static final Component DESCRIBE_PLACEHOLDER = Component.translatable("gui.abuseReport.describe");
    protected static final Component MORE_COMMENTS_LABEL = Component.translatable("gui.abuseReport.more_comments");
    private static final Component MORE_COMMENTS_NARRATION = Component.translatable("gui.abuseReport.comments");
    protected static final int MARGIN = 20;
    protected static final int SCREEN_WIDTH = 280;
    protected static final int SPACING = 8;
    private static final Logger LOGGER = LogUtils.getLogger();
    protected final Screen lastScreen;
    protected final ReportingContext reportingContext;
    protected B reportBuilder;

    protected AbstractReportScreen(Component param0, Screen param1, ReportingContext param2, B param3) {
        super(param0);
        this.lastScreen = param1;
        this.reportingContext = param2;
        this.reportBuilder = param3;
    }

    protected MultiLineEditBox createCommentBox(int param0, int param1, Consumer<String> param2) {
        AbuseReportLimits var0 = this.reportingContext.sender().reportLimits();
        MultiLineEditBox var1 = new MultiLineEditBox(this.font, 0, 0, param0, param1, DESCRIBE_PLACEHOLDER, MORE_COMMENTS_NARRATION);
        var1.setValue(this.reportBuilder.comments());
        var1.setCharacterLimit(var0.maxOpinionCommentsLength());
        var1.setValueListener(param2);
        return var1;
    }

    protected void sendReport() {
        this.reportBuilder.build(this.reportingContext).ifLeft(param0 -> {
            CompletableFuture<?> var0 = this.reportingContext.sender().send(param0.id(), param0.reportType(), param0.report());
            this.minecraft.setScreen(GenericWaitingScreen.createWaiting(REPORT_SENDING_TITLE, CommonComponents.GUI_CANCEL, () -> {
                this.minecraft.setScreen(this);
                var0.cancel(true);
            }));
            var0.handleAsync((param0x, param1) -> {
                if (param1 == null) {
                    this.onReportSendSuccess();
                } else {
                    if (param1 instanceof CancellationException) {
                        return null;
                    }

                    this.onReportSendError(param1);
                }

                return null;
            }, this.minecraft);
        }).ifRight(param0 -> this.displayReportSendError(param0.message()));
    }

    private void onReportSendSuccess() {
        this.clearDraft();
        this.minecraft
            .setScreen(
                GenericWaitingScreen.createCompleted(REPORT_SENT_TITLE, REPORT_SENT_MESSAGE, CommonComponents.GUI_DONE, () -> this.minecraft.setScreen(null))
            );
    }

    private void onReportSendError(Throwable param0) {
        LOGGER.error("Encountered error while sending abuse report", param0);
        Throwable var4 = param0.getCause();
        Component var1;
        if (var4 instanceof ThrowingComponent var0) {
            var1 = var0.getComponent();
        } else {
            var1 = REPORT_SEND_GENERIC_ERROR;
        }

        this.displayReportSendError(var1);
    }

    private void displayReportSendError(Component param0) {
        Component var0 = param0.copy().withStyle(ChatFormatting.RED);
        this.minecraft
            .setScreen(GenericWaitingScreen.createCompleted(REPORT_ERROR_TITLE, var0, CommonComponents.GUI_BACK, () -> this.minecraft.setScreen(this)));
    }

    void saveDraft() {
        if (this.reportBuilder.hasContent()) {
            this.reportingContext.setReportDraft(this.reportBuilder.report().copy());
        }

    }

    void clearDraft() {
        this.reportingContext.setReportDraft(null);
    }

    @Override
    public void onClose() {
        if (this.reportBuilder.hasContent()) {
            this.minecraft.setScreen(new AbstractReportScreen.DiscardReportWarningScreen());
        } else {
            this.minecraft.setScreen(this.lastScreen);
        }

    }

    @Override
    public void removed() {
        this.saveDraft();
        super.removed();
    }

    @OnlyIn(Dist.CLIENT)
    class DiscardReportWarningScreen extends WarningScreen {
        private static final int BUTTON_MARGIN = 20;
        private static final Component TITLE = Component.translatable("gui.abuseReport.discard.title").withStyle(ChatFormatting.BOLD);
        private static final Component MESSAGE = Component.translatable("gui.abuseReport.discard.content");
        private static final Component RETURN = Component.translatable("gui.abuseReport.discard.return");
        private static final Component DRAFT = Component.translatable("gui.abuseReport.discard.draft");
        private static final Component DISCARD = Component.translatable("gui.abuseReport.discard.discard");

        protected DiscardReportWarningScreen() {
            super(TITLE, MESSAGE, MESSAGE);
        }

        @Override
        protected void initButtons(int param0) {
            this.addRenderableWidget(Button.builder(RETURN, param0x -> this.onClose()).pos(this.width / 2 - 155, 100 + param0).build());
            this.addRenderableWidget(Button.builder(DRAFT, param0x -> {
                AbstractReportScreen.this.saveDraft();
                this.minecraft.setScreen(AbstractReportScreen.this.lastScreen);
            }).pos(this.width / 2 + 5, 100 + param0).build());
            this.addRenderableWidget(Button.builder(DISCARD, param0x -> {
                AbstractReportScreen.this.clearDraft();
                this.minecraft.setScreen(AbstractReportScreen.this.lastScreen);
            }).pos(this.width / 2 - 75, 130 + param0).build());
        }

        @Override
        public void onClose() {
            this.minecraft.setScreen(AbstractReportScreen.this);
        }

        @Override
        public boolean shouldCloseOnEsc() {
            return false;
        }

        @Override
        protected void renderTitle(GuiGraphics param0) {
            param0.drawString(this.font, this.title, this.width / 2 - 155, 30, -1);
        }
    }
}
