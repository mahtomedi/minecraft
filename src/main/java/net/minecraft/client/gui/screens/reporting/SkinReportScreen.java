package net.minecraft.client.gui.screens.reporting;

import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.Optionull;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.PlayerSkinWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.CommonLayouts;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.chat.report.Report;
import net.minecraft.client.multiplayer.chat.report.ReportReason;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import net.minecraft.client.multiplayer.chat.report.SkinReport;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SkinReportScreen extends AbstractReportScreen<SkinReport.Builder> {
    private static final int BUTTON_WIDTH = 120;
    private static final int SKIN_WIDTH = 85;
    private static final int FORM_WIDTH = 178;
    private static final Component TITLE = Component.translatable("gui.abuseReport.skin.title");
    private final LinearLayout layout = LinearLayout.vertical().spacing(8);
    private MultiLineEditBox commentBox;
    private Button sendButton;
    private Button selectReasonButton;

    private SkinReportScreen(Screen param0, ReportingContext param1, SkinReport.Builder param2) {
        super(TITLE, param0, param1, param2);
    }

    public SkinReportScreen(Screen param0, ReportingContext param1, UUID param2, Supplier<PlayerSkin> param3) {
        this(param0, param1, new SkinReport.Builder(param2, param3, param1.sender().reportLimits()));
    }

    public SkinReportScreen(Screen param0, ReportingContext param1, SkinReport param2) {
        this(param0, param1, new SkinReport.Builder(param2, param1.sender().reportLimits()));
    }

    @Override
    protected void init() {
        this.layout.defaultCellSetting().alignHorizontallyCenter();
        this.layout.addChild(new StringWidget(this.title, this.font));
        LinearLayout var0 = this.layout.addChild(LinearLayout.horizontal().spacing(8));
        var0.defaultCellSetting().alignVerticallyMiddle();
        var0.addChild(new PlayerSkinWidget(85, 120, this.minecraft.getEntityModels(), this.reportBuilder.report().getSkinGetter()));
        LinearLayout var1 = var0.addChild(LinearLayout.vertical().spacing(8));
        this.selectReasonButton = Button.builder(
                SELECT_REASON, param0 -> this.minecraft.setScreen(new ReportReasonSelectionScreen(this, this.reportBuilder.reason(), param0x -> {
                        this.reportBuilder.setReason(param0x);
                        this.onReportChanged();
                    }))
            )
            .width(178)
            .build();
        var1.addChild(CommonLayouts.labeledElement(this.font, this.selectReasonButton, OBSERVED_WHAT_LABEL));
        this.commentBox = this.createCommentBox(178, 9 * 8, param0 -> {
            this.reportBuilder.setComments(param0);
            this.onReportChanged();
        });
        var1.addChild(CommonLayouts.labeledElement(this.font, this.commentBox, MORE_COMMENTS_LABEL, param0 -> param0.paddingBottom(12)));
        LinearLayout var2 = this.layout.addChild(LinearLayout.horizontal().spacing(8));
        var2.addChild(Button.builder(CommonComponents.GUI_BACK, param0 -> this.onClose()).width(120).build());
        this.sendButton = var2.addChild(Button.builder(SEND_REPORT, param0 -> this.sendReport()).width(120).build());
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
        ReportReason var0 = this.reportBuilder.reason();
        if (var0 != null) {
            this.selectReasonButton.setMessage(var0.title());
        } else {
            this.selectReasonButton.setMessage(SELECT_REASON);
        }

        Report.CannotBuildReason var1 = this.reportBuilder.checkBuildable();
        this.sendButton.active = var1 == null;
        this.sendButton.setTooltip(Optionull.map(var1, Report.CannotBuildReason::tooltip));
    }

    @Override
    public boolean mouseReleased(double param0, double param1, int param2) {
        return super.mouseReleased(param0, param1, param2) ? true : this.commentBox.mouseReleased(param0, param1, param2);
    }
}
