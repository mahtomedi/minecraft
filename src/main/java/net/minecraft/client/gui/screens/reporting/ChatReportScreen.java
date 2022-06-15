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
    private static final Component REPORT_SENDING_TITLE = Component.translatable("gui.abuseReport.sending.title");
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
    private ChatReportBuilder report;
    @Nullable
    ChatReportBuilder.CannotBuildReason cannotBuildReason;

    public ChatReportScreen(Screen param0, ReportingContext param1, UUID param2) {
        super(Component.translatable("gui.chatReport.title"));
        this.lastScreen = param0;
        this.reportingContext = param1;
        this.report = new ChatReportBuilder(param2, param1.sender().reportLimits());
    }

    @Override
    protected void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        AbuseReportLimits var0 = this.reportingContext.sender().reportLimits();
        int var1 = this.width / 2;
        ReportReason var2 = this.report.reason();
        if (var2 != null) {
            this.reasonDescriptionLabel = MultiLineLabel.create(this.font, var2.description(), 280);
        } else {
            this.reasonDescriptionLabel = null;
        }

        IntSet var3 = this.report.reportedMessages();
        Component var4;
        if (var3.isEmpty()) {
            var4 = SELECT_CHAT_MESSAGE;
        } else {
            var4 = Component.translatable("gui.chatReport.selected_chat", var3.size());
        }

        this.addRenderableWidget(
            new Button(
                this.contentLeft(),
                this.selectChatTop(),
                280,
                20,
                var4,
                param0 -> this.minecraft.setScreen(new ChatSelectionScreen(this, this.reportingContext, this.report, param0x -> {
                        this.report = param0x;
                        this.onReportChanged();
                    }))
            )
        );
        Component var6 = Util.mapNullable(var2, ReportReason::title, SELECT_REASON);
        this.addRenderableWidget(
            new Button(
                this.contentLeft(),
                this.selectInfoTop(),
                280,
                20,
                var6,
                param0 -> this.minecraft.setScreen(new ReportReasonSelectionScreen(this, this.report.reason(), param0x -> {
                        this.report.setReason(param0x);
                        this.onReportChanged();
                    }))
            )
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
        this.commentBox.setValue(this.report.comments());
        this.commentBox.setCharacterLimit(var0.maxOpinionCommentsLength());
        this.commentBox.setValueListener(param0 -> {
            this.report.setComments(param0);
            this.onReportChanged();
        });
        this.addRenderableWidget(new Button(var1 - 120, this.completeButtonTop(), 120, 20, CommonComponents.GUI_BACK, param0 -> this.onClose()));
        this.sendButton = this.addRenderableWidget(
            new Button(
                var1 + 10,
                this.completeButtonTop(),
                120,
                20,
                Component.translatable("gui.chatReport.send"),
                param0 -> this.sendReport(),
                new ChatReportScreen.SubmitButtonTooltip()
            )
        );
        this.onReportChanged();
    }

    private void onReportChanged() {
        this.cannotBuildReason = this.report.checkBuildable();
        this.sendButton.active = this.cannotBuildReason == null;
    }

    private void sendReport() {
        this.report.build(this.reportingContext).left().ifPresent(param0 -> {
            CompletableFuture<?> var0 = this.reportingContext.sender().send(param0.id(), param0.report());
            GenericWaitingScreen var1 = new GenericWaitingScreen(REPORT_SENDING_TITLE, CommonComponents.GUI_CANCEL, () -> {
                this.minecraft.setScreen(this);
                var0.cancel(true);
            });
            this.minecraft.setScreen(var1);
            var0.handleAsync((param1, param2) -> {
                if (param2 == null) {
                    this.onReportSendSuccess(var1);
                } else {
                    if (param2 instanceof CancellationException) {
                        return null;
                    }

                    this.onReportSendError(var1, param2);
                }

                return null;
            }, this.minecraft);
        });
    }

    private void onReportSendSuccess(GenericWaitingScreen param0) {
        param0.update(REPORT_SENT_MESSAGE, CommonComponents.GUI_DONE, () -> this.minecraft.setScreen(null));
    }

    private void onReportSendError(GenericWaitingScreen param0, Throwable param1) {
        LOGGER.error("Encountered error while sending abuse report", param1);
        Throwable var5 = param1.getCause();
        Component var1;
        if (var5 instanceof ThrowingComponent var0) {
            var1 = var0.getComponent();
        } else {
            var1 = REPORT_SEND_GENERIC_ERROR;
        }

        Component var3 = var1.copy().withStyle(ChatFormatting.RED);
        param0.update(var3, CommonComponents.GUI_BACK, () -> this.minecraft.setScreen(this));
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
        if (!this.commentBox.getValue().isEmpty()) {
            this.minecraft.setScreen(new ChatReportScreen.DiscardReportWarningScreen());
        } else {
            this.minecraft.setScreen(this.lastScreen);
        }

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
        protected DiscardReportWarningScreen() {
            super(
                Component.translatable("gui.chatReport.discard.title"),
                Component.translatable("gui.chatReport.discard.content"),
                Component.translatable("gui.chatReport.discard.content")
            );
        }

        @Override
        protected void initButtons(int param0) {
            this.addRenderableWidget(
                new Button(this.width / 2 - 155, 100 + param0, 150, 20, Component.translatable("gui.chatReport.discard.return"), param0x -> this.onClose())
            );
            this.addRenderableWidget(
                new Button(
                    this.width / 2 + 5,
                    100 + param0,
                    150,
                    20,
                    Component.translatable("gui.chatReport.discard.discard"),
                    param0x -> this.minecraft.setScreen(ChatReportScreen.this.lastScreen)
                )
            );
        }

        @Override
        public void onClose() {
            this.minecraft.setScreen(ChatReportScreen.this);
        }

        @Override
        public boolean shouldCloseOnEsc() {
            return false;
        }
    }

    @OnlyIn(Dist.CLIENT)
    class SubmitButtonTooltip implements Button.OnTooltip {
        @Override
        public void onTooltip(Button param0, PoseStack param1, int param2, int param3) {
            if (ChatReportScreen.this.cannotBuildReason != null) {
                Component var0 = ChatReportScreen.this.cannotBuildReason.message();
                ChatReportScreen.this.renderTooltip(
                    param1, ChatReportScreen.this.font.split(var0, Math.max(ChatReportScreen.this.width / 2 - 43, 170)), param2, param3
                );
            }

        }
    }
}
