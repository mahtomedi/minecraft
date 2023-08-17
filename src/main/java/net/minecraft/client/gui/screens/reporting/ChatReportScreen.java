package net.minecraft.client.gui.screens.reporting;

import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.UUID;
import net.minecraft.Optionull;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.CommonLayouts;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.chat.report.ChatReport;
import net.minecraft.client.multiplayer.chat.report.Report;
import net.minecraft.client.multiplayer.chat.report.ReportReason;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ChatReportScreen extends AbstractReportScreen<ChatReport.Builder> {
    private static final int BUTTON_WIDTH = 120;
    private static final Component TITLE = Component.translatable("gui.chatReport.title");
    private static final Component SELECT_CHAT_MESSAGE = Component.translatable("gui.chatReport.select_chat");
    private final LinearLayout layout = LinearLayout.vertical().spacing(8);
    private MultiLineEditBox commentBox;
    private Button sendButton;
    private Button selectMessagesButton;
    private Button selectReasonButton;

    private ChatReportScreen(Screen param0, ReportingContext param1, ChatReport.Builder param2) {
        super(TITLE, param0, param1, param2);
    }

    public ChatReportScreen(Screen param0, ReportingContext param1, UUID param2) {
        this(param0, param1, new ChatReport.Builder(param2, param1.sender().reportLimits()));
    }

    public ChatReportScreen(Screen param0, ReportingContext param1, ChatReport param2) {
        this(param0, param1, new ChatReport.Builder(param2, param1.sender().reportLimits()));
    }

    @Override
    protected void init() {
        this.layout.defaultCellSetting().alignHorizontallyCenter();
        this.layout.addChild(new StringWidget(this.title, this.font));
        this.selectMessagesButton = this.layout
            .addChild(
                Button.builder(
                        SELECT_CHAT_MESSAGE,
                        param0 -> this.minecraft.setScreen(new ChatSelectionScreen(this, this.reportingContext, this.reportBuilder, param0x -> {
                                this.reportBuilder = param0x;
                                this.onReportChanged();
                            }))
                    )
                    .width(280)
                    .build()
            );
        this.selectReasonButton = Button.builder(
                SELECT_REASON, param0 -> this.minecraft.setScreen(new ReportReasonSelectionScreen(this, this.reportBuilder.reason(), param0x -> {
                        this.reportBuilder.setReason(param0x);
                        this.onReportChanged();
                    }))
            )
            .width(280)
            .build();
        this.layout.addChild(CommonLayouts.labeledElement(this.font, this.selectReasonButton, OBSERVED_WHAT_LABEL));
        this.commentBox = this.createCommentBox(280, 9 * 8, param0 -> {
            this.reportBuilder.setComments(param0);
            this.onReportChanged();
        });
        this.layout.addChild(CommonLayouts.labeledElement(this.font, this.commentBox, MORE_COMMENTS_LABEL, param0 -> param0.paddingBottom(12)));
        LinearLayout var0 = this.layout.addChild(LinearLayout.horizontal().spacing(8));
        var0.addChild(Button.builder(CommonComponents.GUI_BACK, param0 -> this.onClose()).width(120).build());
        this.sendButton = var0.addChild(Button.builder(SEND_REPORT, param0 -> this.sendReport()).width(120).build());
        this.layout.visitWidgets(param1 -> {
        });
        this.repositionElements();
        this.onReportChanged();
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
        FrameLayout.centerInRectangle(this.layout, this.getRectangle());
    }

    private void onReportChanged() {
        IntSet var0 = this.reportBuilder.reportedMessages();
        if (var0.isEmpty()) {
            this.selectMessagesButton.setMessage(SELECT_CHAT_MESSAGE);
        } else {
            this.selectMessagesButton.setMessage(Component.translatable("gui.chatReport.selected_chat", var0.size()));
        }

        ReportReason var1 = this.reportBuilder.reason();
        if (var1 != null) {
            this.selectReasonButton.setMessage(var1.title());
        } else {
            this.selectReasonButton.setMessage(SELECT_REASON);
        }

        Report.CannotBuildReason var2 = this.reportBuilder.checkBuildable();
        this.sendButton.active = var2 == null;
        this.sendButton.setTooltip(Optionull.map(var2, Report.CannotBuildReason::tooltip));
    }

    @Override
    public boolean mouseReleased(double param0, double param1, int param2) {
        return super.mouseReleased(param0, param1, param2) ? true : this.commentBox.mouseReleased(param0, param1, param2);
    }
}
