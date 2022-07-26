package net.minecraft.client.gui.screens.reporting;

import com.mojang.authlib.minecraft.report.AbuseReportLimits;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.GenericWaitingScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.WarningScreen;
import net.minecraft.client.multiplayer.chat.report.ChatReportBuilder;
import net.minecraft.client.multiplayer.chat.report.ReportReason;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ThrowingComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ChatReportScreen extends Screen {
    private static final int BUTTON_WIDTH = 120;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_MARGIN = 20;
    private static final int BUTTON_MARGIN_HALF = 10;
    private static final int LABEL_HEIGHT = 25;
    private static final int SCREEN_WIDTH = 280;
    private static final int SCREEN_HEIGHT = 300;
    private static final Component OBSERVED_WHAT_LABEL = Component.translatable("gui.chatReport.observed_what");
    private static final Component SELECT_REASON = Component.translatable("gui.chatReport.select_reason");
    private static final Component MORE_COMMENTS_LABEL = Component.translatable("gui.chatReport.more_comments");
    private static final Component DESCRIBE_PLACEHOLDER = Component.translatable("gui.chatReport.describe");
    private static final Component REPORT_SENT_MESSAGE = Component.translatable("gui.chatReport.report_sent_msg");
    private static final Component SELECT_CHAT_MESSAGE = Component.translatable("gui.chatReport.select_chat");
    private static final Component REPORT_SENDING_TITLE = Component.translatable("gui.abuseReport.sending.title").withStyle(ChatFormatting.BOLD);
    private static final Component REPORT_SENT_TITLE = Component.translatable("gui.abuseReport.sent.title").withStyle(ChatFormatting.BOLD);
    private static final Component REPORT_ERROR_TITLE = Component.translatable("gui.abuseReport.error.title").withStyle(ChatFormatting.BOLD);
    private static final Component REPORT_SEND_GENERIC_ERROR = Component.translatable("gui.abuseReport.send.generic_error");
    private static final Logger LOGGER = LogUtils.getLogger();
    @Nullable
    final Screen lastScreen;
    private final ReportingContext reportingContext;
    @Nullable
    private MultiLineLabel reasonDescriptionLabel;
    @Nullable
    private MultiLineEditBox commentBox;
    private Button sendButton;
    private ChatReportBuilder reportBuilder;
    @Nullable
    private ChatReportBuilder.CannotBuildReason cannotBuildReason;

    private ChatReportScreen(@Nullable Screen param0, ReportingContext param1, ChatReportBuilder param2) {
        super(Component.translatable("gui.chatReport.title"));
        this.lastScreen = param0;
        this.reportingContext = param1;
        this.reportBuilder = param2;
    }

    public ChatReportScreen(@Nullable Screen param0, ReportingContext param1, UUID param2) {
        this(param0, param1, new ChatReportBuilder(param2, param1.sender().reportLimits()));
    }

    public ChatReportScreen(@Nullable Screen param0, ReportingContext param1, ChatReportBuilder.ChatReport param2) {
        this(param0, param1, new ChatReportBuilder(param2, param1.sender().reportLimits()));
    }

    @Override
    protected void init() {
        AbuseReportLimits var0 = this.reportingContext.sender().reportLimits();
        int var1 = this.width / 2;
        ReportReason var2 = this.reportBuilder.reason();
        if (var2 != null) {
            this.reasonDescriptionLabel = MultiLineLabel.create(this.font, var2.description(), 280);
        } else {
            this.reasonDescriptionLabel = null;
        }

        IntSet var3 = this.reportBuilder.reportedMessages();
        Component var4;
        if (var3.isEmpty()) {
            var4 = SELECT_CHAT_MESSAGE;
        } else {
            var4 = Component.translatable("gui.chatReport.selected_chat", var3.size());
        }

        this.addRenderableWidget(
            Button.builder(var4, param0 -> this.minecraft.setScreen(new ChatSelectionScreen(this, this.reportingContext, this.reportBuilder, param0x -> {
                    this.reportBuilder = param0x;
                    this.onReportChanged();
                }))).bounds(this.contentLeft(), this.selectChatTop(), 280, 20).build()
        );
        Component var6 = Util.mapNullable(var2, ReportReason::title, SELECT_REASON);
        this.addRenderableWidget(
            Button.builder(var6, param0 -> this.minecraft.setScreen(new ReportReasonSelectionScreen(this, this.reportBuilder.reason(), param0x -> {
                    this.reportBuilder.setReason(param0x);
                    this.onReportChanged();
                }))).bounds(this.contentLeft(), this.selectInfoTop(), 280, 20).build()
        );
        this.commentBox = this.addRenderableWidget(
            new MultiLineEditBox(
                this.minecraft.font,
                this.contentLeft(),
                this.commentBoxTop(),
                280,
                this.commentBoxBottom() - this.commentBoxTop(),
                DESCRIBE_PLACEHOLDER,
                Component.translatable("gui.chatReport.comments")
            )
        );
        this.commentBox.setValue(this.reportBuilder.comments());
        this.commentBox.setCharacterLimit(var0.maxOpinionCommentsLength());
        this.commentBox.setValueListener(param0 -> {
            this.reportBuilder.setComments(param0);
            this.onReportChanged();
        });
        this.addRenderableWidget(
            Button.builder(CommonComponents.GUI_BACK, param0 -> this.onClose()).bounds(var1 - 120, this.completeButtonTop(), 120, 20).build()
        );
        this.sendButton = this.addRenderableWidget(
            Button.builder(Component.translatable("gui.chatReport.send"), param0 -> this.sendReport())
                .bounds(var1 + 10, this.completeButtonTop(), 120, 20)
                .build()
        );
        this.onReportChanged();
    }

    private void onReportChanged() {
        this.cannotBuildReason = this.reportBuilder.checkBuildable();
        this.sendButton.active = this.cannotBuildReason == null;
        this.sendButton.setTooltip(Util.mapNullable(this.cannotBuildReason, param0 -> Tooltip.create(param0.message())));
    }

    private void sendReport() {
        this.reportBuilder.build(this.reportingContext).ifLeft(param0 -> {
            CompletableFuture<?> var0 = this.reportingContext.sender().send(param0.id(), param0.report());
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
            this.reportingContext.setChatReportDraft(this.reportBuilder.report().copy());
        }

    }

    void clearDraft() {
        this.reportingContext.setChatReportDraft(null);
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        int var0 = this.width / 2;
        RenderSystem.disableDepthTest();
        this.renderBackground(param0);
        drawCenteredString(param0, this.font, this.title, var0, 10, 16777215);
        drawCenteredString(param0, this.font, OBSERVED_WHAT_LABEL, var0, this.selectChatTop() - 9 - 6, 16777215);
        if (this.reasonDescriptionLabel != null) {
            this.reasonDescriptionLabel.renderLeftAligned(param0, this.contentLeft(), this.selectInfoTop() + 20 + 5, 9, 16777215);
        }

        drawString(param0, this.font, MORE_COMMENTS_LABEL, this.contentLeft(), this.commentBoxTop() - 9 - 6, 16777215);
        super.render(param0, param1, param2, param3);
        RenderSystem.enableDepthTest();
    }

    @Override
    public void tick() {
        this.commentBox.tick();
        super.tick();
    }

    @Override
    public void onClose() {
        if (this.reportBuilder.hasContent()) {
            this.minecraft.setScreen(new ChatReportScreen.DiscardReportWarningScreen());
        } else {
            this.minecraft.setScreen(this.lastScreen);
        }

    }

    @Override
    public void removed() {
        this.saveDraft();
        super.removed();
    }

    @Override
    public boolean mouseReleased(double param0, double param1, int param2) {
        return super.mouseReleased(param0, param1, param2) ? true : this.commentBox.mouseReleased(param0, param1, param2);
    }

    private int contentLeft() {
        return this.width / 2 - 140;
    }

    private int contentRight() {
        return this.width / 2 + 140;
    }

    private int contentTop() {
        return Math.max((this.height - 300) / 2, 0);
    }

    private int contentBottom() {
        return Math.min((this.height + 300) / 2, this.height);
    }

    private int selectChatTop() {
        return this.contentTop() + 40;
    }

    private int selectInfoTop() {
        return this.selectChatTop() + 10 + 20;
    }

    private int commentBoxTop() {
        int var0 = this.selectInfoTop() + 20 + 25;
        if (this.reasonDescriptionLabel != null) {
            var0 += (this.reasonDescriptionLabel.getLineCount() + 1) * 9;
        }

        return var0;
    }

    private int commentBoxBottom() {
        return this.completeButtonTop() - 20;
    }

    private int completeButtonTop() {
        return this.contentBottom() - 20 - 10;
    }

    @OnlyIn(Dist.CLIENT)
    class DiscardReportWarningScreen extends WarningScreen {
        private static final Component TITLE = Component.translatable("gui.chatReport.discard.title").withStyle(ChatFormatting.BOLD);
        private static final Component MESSAGE = Component.translatable("gui.chatReport.discard.content");
        private static final Component RETURN = Component.translatable("gui.chatReport.discard.return");
        private static final Component DRAFT = Component.translatable("gui.chatReport.discard.draft");
        private static final Component DISCARD = Component.translatable("gui.chatReport.discard.discard");

        protected DiscardReportWarningScreen() {
            super(TITLE, MESSAGE, MESSAGE);
        }

        @Override
        protected void initButtons(int param0) {
            int var0 = 150;
            this.addRenderableWidget(Button.builder(RETURN, param0x -> this.onClose()).bounds(this.width / 2 - 155, 100 + param0, 150, 20).build());
            this.addRenderableWidget(Button.builder(DRAFT, param0x -> {
                ChatReportScreen.this.saveDraft();
                this.minecraft.setScreen(ChatReportScreen.this.lastScreen);
            }).bounds(this.width / 2 + 5, 100 + param0, 150, 20).build());
            this.addRenderableWidget(Button.builder(DISCARD, param0x -> {
                ChatReportScreen.this.clearDraft();
                this.minecraft.setScreen(ChatReportScreen.this.lastScreen);
            }).bounds(this.width / 2 - 75, 130 + param0, 150, 20).build());
        }

        @Override
        public void onClose() {
            this.minecraft.setScreen(ChatReportScreen.this);
        }

        @Override
        public boolean shouldCloseOnEsc() {
            return false;
        }

        @Override
        protected void renderTitle(PoseStack param0) {
            drawString(param0, this.font, this.title, this.width / 2 - 155, 30, 16777215);
        }
    }
}
