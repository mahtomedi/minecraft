package net.minecraft.client.gui.screens.reporting;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.chat.report.ReportReason;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ReportReasonSelectionScreen extends Screen {
    private static final Component REASON_TITLE = Component.translatable("gui.abuseReport.reason.title");
    private static final Component REASON_DESCRIPTION = Component.translatable("gui.abuseReport.reason.description");
    private static final int FOOTER_HEIGHT = 80;
    @Nullable
    private final Screen lastScreen;
    @Nullable
    private ReportReasonSelectionScreen.ReasonSelectionList reasonSelectionList;
    @Nullable
    private final ReportReason selectedReasonOnInit;
    private final Consumer<ReportReason> onSelectedReason;

    public ReportReasonSelectionScreen(@Nullable Screen param0, @Nullable ReportReason param1, Consumer<ReportReason> param2) {
        super(REASON_TITLE);
        this.lastScreen = param0;
        this.selectedReasonOnInit = param1;
        this.onSelectedReason = param2;
    }

    @Override
    protected void init() {
        this.reasonSelectionList = new ReportReasonSelectionScreen.ReasonSelectionList(this.minecraft);
        this.reasonSelectionList.setRenderBackground(false);
        this.addWidget(this.reasonSelectionList);
        ReportReasonSelectionScreen.ReasonSelectionList.Entry var0 = Util.mapNullable(this.selectedReasonOnInit, this.reasonSelectionList::findEntry);
        this.reasonSelectionList.setSelected(var0);
        this.addRenderableWidget(new Button(this.width / 2 - 155 + 160, this.height - 32, 150, 20, CommonComponents.GUI_DONE, param0 -> {
            ReportReasonSelectionScreen.ReasonSelectionList.Entry var0x = this.reasonSelectionList.getSelected();
            if (var0x != null) {
                this.onSelectedReason.accept(var0x.getReason());
            }

            this.minecraft.setScreen(this.lastScreen);
        }));
        super.init();
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        this.reasonSelectionList.render(param0, param1, param2, param3);
        drawCenteredString(param0, this.font, this.title, this.width / 2, 16, 16777215);
        super.render(param0, param1, param2, param3);
        int var0 = this.height - 80;
        int var1 = this.height - 35;
        int var2 = this.width / 2 - 160;
        int var3 = this.width / 2 + 160;
        fill(param0, var2, var0, var3, var1, 2130706432);
        drawString(param0, this.font, REASON_DESCRIPTION, var2 + 2, var0 + 2, -8421505);
        ReportReasonSelectionScreen.ReasonSelectionList.Entry var4 = this.reasonSelectionList.getSelected();
        if (var4 != null) {
            int var5 = this.font.wordWrapHeight(var4.reason.description(), 280);
            int var6 = var1 - var0 + 10;
            this.font.drawWordWrap(var4.reason.description(), var2 + 20, var0 + (var6 - var5) / 2, var3 - var2 - 40, -1);
        }

    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
    }

    @OnlyIn(Dist.CLIENT)
    public class ReasonSelectionList extends ObjectSelectionList<ReportReasonSelectionScreen.ReasonSelectionList.Entry> {
        public ReasonSelectionList(Minecraft param1) {
            super(param1, ReportReasonSelectionScreen.this.width, ReportReasonSelectionScreen.this.height, 40, ReportReasonSelectionScreen.this.height - 80, 18);

            for(ReportReason var0 : ReportReason.values()) {
                this.addEntry(new ReportReasonSelectionScreen.ReasonSelectionList.Entry(var0));
            }

        }

        @Nullable
        public ReportReasonSelectionScreen.ReasonSelectionList.Entry findEntry(ReportReason param0) {
            return this.children().stream().filter(param1 -> param1.reason == param0).findFirst().orElse(null);
        }

        @Override
        public int getRowWidth() {
            return 280;
        }

        @OnlyIn(Dist.CLIENT)
        public class Entry extends ObjectSelectionList.Entry<ReportReasonSelectionScreen.ReasonSelectionList.Entry> {
            final ReportReason reason;

            public Entry(ReportReason param1) {
                this.reason = param1;
            }

            @Override
            public void render(
                PoseStack param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9
            ) {
                GuiComponent.drawString(param0, ReportReasonSelectionScreen.this.font, this.reason.title(), param3, param2 + 1, -1);
            }

            @Override
            public Component getNarration() {
                return Component.translatable("gui.abuseReport.reason.narration", this.reason.title(), this.reason.description());
            }

            @Override
            public boolean mouseClicked(double param0, double param1, int param2) {
                if (param2 == 0) {
                    ReasonSelectionList.this.setSelected(this);
                    return true;
                } else {
                    return false;
                }
            }

            public ReportReason getReason() {
                return this.reason;
            }
        }
    }
}
