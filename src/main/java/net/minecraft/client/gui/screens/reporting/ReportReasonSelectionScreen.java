package net.minecraft.client.gui.screens.reporting;

import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.Optionull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
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
    private static final Component READ_INFO_LABEL = Component.translatable("gui.abuseReport.read_info");
    private static final int FOOTER_HEIGHT = 95;
    private static final int BUTTON_WIDTH = 150;
    private static final int BUTTON_HEIGHT = 20;
    private static final int CONTENT_WIDTH = 320;
    private static final int PADDING = 4;
    @Nullable
    private final Screen lastScreen;
    @Nullable
    private ReportReasonSelectionScreen.ReasonSelectionList reasonSelectionList;
    @Nullable
    ReportReason currentlySelectedReason;
    private final Consumer<ReportReason> onSelectedReason;

    public ReportReasonSelectionScreen(@Nullable Screen param0, @Nullable ReportReason param1, Consumer<ReportReason> param2) {
        super(REASON_TITLE);
        this.lastScreen = param0;
        this.currentlySelectedReason = param1;
        this.onSelectedReason = param2;
    }

    @Override
    protected void init() {
        this.reasonSelectionList = this.addRenderableWidget(new ReportReasonSelectionScreen.ReasonSelectionList(this.minecraft));
        ReportReasonSelectionScreen.ReasonSelectionList.Entry var0 = Optionull.map(this.currentlySelectedReason, this.reasonSelectionList::findEntry);
        this.reasonSelectionList.setSelected(var0);
        int var1 = this.width / 2 - 150 - 5;
        this.addRenderableWidget(
            Button.builder(READ_INFO_LABEL, ConfirmLinkScreen.confirmLink(this, "https://aka.ms/aboutjavareporting"))
                .bounds(var1, this.buttonTop(), 150, 20)
                .build()
        );
        int var2 = this.width / 2 + 5;
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, param0 -> {
            ReportReasonSelectionScreen.ReasonSelectionList.Entry var0x = this.reasonSelectionList.getSelected();
            if (var0x != null) {
                this.onSelectedReason.accept(var0x.getReason());
            }

            this.minecraft.setScreen(this.lastScreen);
        }).bounds(var2, this.buttonTop(), 150, 20).build());
        super.init();
    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        super.render(param0, param1, param2, param3);
        param0.drawCenteredString(this.font, this.title, this.width / 2, 16, 16777215);
        param0.fill(this.contentLeft(), this.descriptionTop(), this.contentRight(), this.descriptionBottom(), 2130706432);
        param0.drawString(this.font, REASON_DESCRIPTION, this.contentLeft() + 4, this.descriptionTop() + 4, -8421505);
        ReportReasonSelectionScreen.ReasonSelectionList.Entry var0 = this.reasonSelectionList.getSelected();
        if (var0 != null) {
            int var1 = this.contentLeft() + 4 + 16;
            int var2 = this.contentRight() - 4;
            int var3 = this.descriptionTop() + 4 + 9 + 2;
            int var4 = this.descriptionBottom() - 4;
            int var5 = var2 - var1;
            int var6 = var4 - var3;
            int var7 = this.font.wordWrapHeight(var0.reason.description(), var5);
            param0.drawWordWrap(this.font, var0.reason.description(), var1, var3 + (var6 - var7) / 2, var5, -1);
        }

    }

    @Override
    public void renderBackground(GuiGraphics param0, int param1, int param2, float param3) {
        this.renderDirtBackground(param0);
    }

    private int buttonTop() {
        return this.height - 20 - 4;
    }

    private int contentLeft() {
        return (this.width - 320) / 2;
    }

    private int contentRight() {
        return (this.width + 320) / 2;
    }

    private int descriptionTop() {
        return this.height - 95 + 4;
    }

    private int descriptionBottom() {
        return this.buttonTop() - 4;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
    }

    @OnlyIn(Dist.CLIENT)
    public class ReasonSelectionList extends ObjectSelectionList<ReportReasonSelectionScreen.ReasonSelectionList.Entry> {
        public ReasonSelectionList(Minecraft param1) {
            super(param1, ReportReasonSelectionScreen.this.width, ReportReasonSelectionScreen.this.height - 95 - 40, 40, 18);

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
            return 320;
        }

        @Override
        protected int getScrollbarPosition() {
            return this.getRowRight() - 2;
        }

        public void setSelected(@Nullable ReportReasonSelectionScreen.ReasonSelectionList.Entry param0) {
            super.setSelected(param0);
            ReportReasonSelectionScreen.this.currentlySelectedReason = param0 != null ? param0.getReason() : null;
        }

        @OnlyIn(Dist.CLIENT)
        public class Entry extends ObjectSelectionList.Entry<ReportReasonSelectionScreen.ReasonSelectionList.Entry> {
            final ReportReason reason;

            public Entry(ReportReason param1) {
                this.reason = param1;
            }

            @Override
            public void render(
                GuiGraphics param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9
            ) {
                int var0 = param3 + 1;
                int var1 = param2 + (param5 - 9) / 2 + 1;
                param0.drawString(ReportReasonSelectionScreen.this.font, this.reason.title(), var0, var1, -1);
            }

            @Override
            public Component getNarration() {
                return Component.translatable("gui.abuseReport.reason.narration", this.reason.title(), this.reason.description());
            }

            @Override
            public boolean mouseClicked(double param0, double param1, int param2) {
                ReasonSelectionList.this.setSelected(this);
                return true;
            }

            public ReportReason getReason() {
                return this.reason;
            }
        }
    }
}
