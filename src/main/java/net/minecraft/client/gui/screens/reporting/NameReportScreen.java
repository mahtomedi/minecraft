package net.minecraft.client.gui.screens.reporting;

import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.Optionull;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.CommonLayouts;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.chat.report.NameReport;
import net.minecraft.client.multiplayer.chat.report.Report;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class NameReportScreen extends AbstractReportScreen<NameReport.Builder> {
    private static final int BUTTON_WIDTH = 120;
    private static final Component TITLE = Component.translatable("gui.abuseReport.name.title");
    private final LinearLayout layout = LinearLayout.vertical().spacing(8);
    private MultiLineEditBox commentBox;
    private Button sendButton;

    private NameReportScreen(Screen param0, ReportingContext param1, NameReport.Builder param2) {
        super(TITLE, param0, param1, param2);
    }

    public NameReportScreen(Screen param0, ReportingContext param1, UUID param2, String param3) {
        this(param0, param1, new NameReport.Builder(param2, param3, param1.sender().reportLimits()));
    }

    public NameReportScreen(Screen param0, ReportingContext param1, NameReport param2) {
        this(param0, param1, new NameReport.Builder(param2, param1.sender().reportLimits()));
    }

    @Override
    protected void init() {
        this.layout.defaultCellSetting().alignHorizontallyCenter();
        this.layout.addChild(new StringWidget(this.title, this.font));
        Component var0 = Component.literal(this.reportBuilder.report().getReportedName()).withStyle(ChatFormatting.YELLOW);
        this.layout
            .addChild(
                new StringWidget(Component.translatable("gui.abuseReport.name.reporting", var0), this.font),
                param0 -> param0.alignHorizontallyLeft().padding(0, 8)
            );
        this.commentBox = this.createCommentBox(280, 9 * 8, param0 -> {
            this.reportBuilder.setComments(param0);
            this.onReportChanged();
        });
        this.layout.addChild(CommonLayouts.labeledElement(this.font, this.commentBox, MORE_COMMENTS_LABEL, param0 -> param0.paddingBottom(12)));
        LinearLayout var1 = this.layout.addChild(LinearLayout.horizontal().spacing(8));
        var1.addChild(Button.builder(CommonComponents.GUI_BACK, param0 -> this.onClose()).width(120).build());
        this.sendButton = var1.addChild(Button.builder(SEND_REPORT, param0 -> this.sendReport()).width(120).build());
        this.onReportChanged();
        this.layout.visitWidgets(param1 -> {
        });
        this.repositionElements();
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
        FrameLayout.centerInRectangle(this.layout, this.getRectangle());
    }

    private void onReportChanged() {
        Report.CannotBuildReason var0 = this.reportBuilder.checkBuildable();
        this.sendButton.active = var0 == null;
        this.sendButton.setTooltip(Optionull.map(var0, Report.CannotBuildReason::tooltip));
    }

    @Override
    public boolean mouseReleased(double param0, double param1, int param2) {
        return super.mouseReleased(param0, param1, param2) ? true : this.commentBox.mouseReleased(param0, param1, param2);
    }
}
