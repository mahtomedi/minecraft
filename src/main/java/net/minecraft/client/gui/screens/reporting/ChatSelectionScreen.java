package net.minecraft.client.gui.screens.reporting;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.report.AbuseReportLimits;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.chat.ChatTrustLevel;
import net.minecraft.client.multiplayer.chat.LoggedChatMessage;
import net.minecraft.client.multiplayer.chat.report.ChatReportBuilder;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ChatSelectionScreen extends Screen {
    private static final Component TITLE = Component.translatable("gui.chatSelection.title");
    private static final Component CONTEXT_INFO = Component.translatable("gui.chatSelection.context").withStyle(ChatFormatting.GRAY);
    @Nullable
    private final Screen lastScreen;
    private final ReportingContext reportingContext;
    private Button confirmSelectedButton;
    private MultiLineLabel contextInfoLabel;
    @Nullable
    private ChatSelectionScreen.ChatSelectionList chatSelectionList;
    final ChatReportBuilder report;
    private final Consumer<ChatReportBuilder> onSelected;
    private ChatSelectionLogFiller chatLogFiller;
    @Nullable
    private List<FormattedCharSequence> tooltip;

    public ChatSelectionScreen(@Nullable Screen param0, ReportingContext param1, ChatReportBuilder param2, Consumer<ChatReportBuilder> param3) {
        super(TITLE);
        this.lastScreen = param0;
        this.reportingContext = param1;
        this.report = param2.copy();
        this.onSelected = param3;
    }

    @Override
    protected void init() {
        this.chatLogFiller = new ChatSelectionLogFiller(this.reportingContext, this::canReport);
        this.contextInfoLabel = MultiLineLabel.create(this.font, CONTEXT_INFO, this.width - 16);
        this.chatSelectionList = new ChatSelectionScreen.ChatSelectionList(this.minecraft, (this.contextInfoLabel.getLineCount() + 1) * 9);
        this.chatSelectionList.setRenderBackground(false);
        this.addWidget(this.chatSelectionList);
        this.addRenderableWidget(
            Button.builder(CommonComponents.GUI_BACK, param0 -> this.onClose()).bounds(this.width / 2 - 155, this.height - 32, 150, 20).build()
        );
        this.confirmSelectedButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, param0 -> {
            this.onSelected.accept(this.report);
            this.onClose();
        }).bounds(this.width / 2 - 155 + 160, this.height - 32, 150, 20).build());
        this.updateConfirmSelectedButton();
        this.extendLog();
        this.chatSelectionList.setScrollAmount((double)this.chatSelectionList.getMaxScroll());
    }

    private boolean canReport(LoggedChatMessage param0) {
        return param0.canReport(this.report.reportedProfileId());
    }

    private void extendLog() {
        int var0 = this.chatSelectionList.getMaxVisibleEntries();
        this.chatLogFiller.fillNextPage(var0, this.chatSelectionList);
    }

    void onReachedScrollTop() {
        this.extendLog();
    }

    void updateConfirmSelectedButton() {
        this.confirmSelectedButton.active = !this.report.reportedMessages().isEmpty();
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        this.chatSelectionList.render(param0, param1, param2, param3);
        drawCenteredString(param0, this.font, this.title, this.width / 2, 16, 16777215);
        AbuseReportLimits var0 = this.reportingContext.sender().reportLimits();
        int var1 = this.report.reportedMessages().size();
        int var2 = var0.maxReportedMessageCount();
        Component var3 = Component.translatable("gui.chatSelection.selected", var1, var2);
        drawCenteredString(param0, this.font, var3, this.width / 2, 16 + 9 * 3 / 2, 10526880);
        this.contextInfoLabel.renderCentered(param0, this.width / 2, this.chatSelectionList.getFooterTop());
        super.render(param0, param1, param2, param3);
        if (this.tooltip != null) {
            this.renderTooltip(param0, this.tooltip, param1, param2);
            this.tooltip = null;
        }

    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
    }

    @Override
    public Component getNarrationMessage() {
        return CommonComponents.joinForNarration(super.getNarrationMessage(), CONTEXT_INFO);
    }

    void setTooltip(@Nullable List<FormattedCharSequence> param0) {
        this.tooltip = param0;
    }

    @OnlyIn(Dist.CLIENT)
    public class ChatSelectionList extends ObjectSelectionList<ChatSelectionScreen.ChatSelectionList.Entry> implements ChatSelectionLogFiller.Output {
        @Nullable
        private ChatSelectionScreen.ChatSelectionList.Heading previousHeading;

        public ChatSelectionList(Minecraft param1, int param2) {
            super(param1, ChatSelectionScreen.this.width, ChatSelectionScreen.this.height, 40, ChatSelectionScreen.this.height - 40 - param2, 16);
        }

        @Override
        public void setScrollAmount(double param0) {
            double var0 = this.getScrollAmount();
            super.setScrollAmount(param0);
            if ((float)this.getMaxScroll() > 1.0E-5F && param0 <= 1.0E-5F && !Mth.equal(param0, var0)) {
                ChatSelectionScreen.this.onReachedScrollTop();
            }

        }

        @Override
        public void acceptMessage(int param0, LoggedChatMessage.Player param1) {
            boolean var0 = param1.canReport(ChatSelectionScreen.this.report.reportedProfileId());
            ChatTrustLevel var1 = param1.trustLevel();
            GuiMessageTag var2 = var1.createTag(param1.message());
            ChatSelectionScreen.ChatSelectionList.Entry var3 = new ChatSelectionScreen.ChatSelectionList.MessageEntry(
                param0, param1.toContentComponent(), param1.toNarrationComponent(), var2, var0, true
            );
            this.addEntryToTop(var3);
            this.updateHeading(param1, var0);
        }

        private void updateHeading(LoggedChatMessage.Player param0, boolean param1) {
            ChatSelectionScreen.ChatSelectionList.Entry var0 = new ChatSelectionScreen.ChatSelectionList.MessageHeadingEntry(
                param0.profile(), param0.toHeadingComponent(), param1
            );
            this.addEntryToTop(var0);
            ChatSelectionScreen.ChatSelectionList.Heading var1 = new ChatSelectionScreen.ChatSelectionList.Heading(param0.profileId(), var0);
            if (this.previousHeading != null && this.previousHeading.canCombine(var1)) {
                this.removeEntryFromTop(this.previousHeading.entry());
            }

            this.previousHeading = var1;
        }

        @Override
        public void acceptDivider(Component param0) {
            this.addEntryToTop(new ChatSelectionScreen.ChatSelectionList.PaddingEntry());
            this.addEntryToTop(new ChatSelectionScreen.ChatSelectionList.DividerEntry(param0));
            this.addEntryToTop(new ChatSelectionScreen.ChatSelectionList.PaddingEntry());
            this.previousHeading = null;
        }

        @Override
        protected int getScrollbarPosition() {
            return (this.width + this.getRowWidth()) / 2;
        }

        @Override
        public int getRowWidth() {
            return Math.min(350, this.width - 50);
        }

        public int getMaxVisibleEntries() {
            return Mth.positiveCeilDiv(this.y1 - this.y0, this.itemHeight);
        }

        @Override
        protected void renderItem(PoseStack param0, int param1, int param2, float param3, int param4, int param5, int param6, int param7, int param8) {
            ChatSelectionScreen.ChatSelectionList.Entry var0 = this.getEntry(param4);
            if (this.shouldHighlightEntry(var0)) {
                boolean var1 = this.getSelected() == var0;
                int var2 = this.isFocused() && var1 ? -1 : -8355712;
                this.renderSelection(param0, param6, param7, param8, var2, -16777216);
            }

            var0.render(param0, param4, param6, param5, param7, param8, param1, param2, this.getHovered() == var0, param3);
        }

        private boolean shouldHighlightEntry(ChatSelectionScreen.ChatSelectionList.Entry param0) {
            if (param0.canSelect()) {
                boolean var0 = this.getSelected() == param0;
                boolean var1 = this.getSelected() == null;
                boolean var2 = this.getHovered() == param0;
                return var0 || var1 && var2 && param0.canReport();
            } else {
                return false;
            }
        }

        @Override
        protected void moveSelection(AbstractSelectionList.SelectionDirection param0) {
            if (!this.moveSelectableSelection(param0) && param0 == AbstractSelectionList.SelectionDirection.UP) {
                ChatSelectionScreen.this.onReachedScrollTop();
                this.moveSelectableSelection(param0);
            }

        }

        private boolean moveSelectableSelection(AbstractSelectionList.SelectionDirection param0) {
            return this.moveSelection(param0, ChatSelectionScreen.ChatSelectionList.Entry::canSelect);
        }

        @Override
        public boolean keyPressed(int param0, int param1, int param2) {
            ChatSelectionScreen.ChatSelectionList.Entry var0 = this.getSelected();
            if (var0 != null && var0.keyPressed(param0, param1, param2)) {
                return true;
            } else {
                this.setFocused(null);
                return super.keyPressed(param0, param1, param2);
            }
        }

        public int getFooterTop() {
            return this.y1 + 9;
        }

        @Override
        protected boolean isFocused() {
            return ChatSelectionScreen.this.getFocused() == this;
        }

        @OnlyIn(Dist.CLIENT)
        public class DividerEntry extends ChatSelectionScreen.ChatSelectionList.Entry {
            private static final int COLOR = -6250336;
            private final Component text;

            public DividerEntry(Component param1) {
                this.text = param1;
            }

            @Override
            public void render(
                PoseStack param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9
            ) {
                int var0 = param2 + param5 / 2;
                int var1 = param3 + param4 - 8;
                int var2 = ChatSelectionScreen.this.font.width(this.text);
                int var3 = (param3 + var1 - var2) / 2;
                int var4 = var0 - 9 / 2;
                GuiComponent.drawString(param0, ChatSelectionScreen.this.font, this.text, var3, var4, -6250336);
            }

            @Override
            public Component getNarration() {
                return this.text;
            }
        }

        @OnlyIn(Dist.CLIENT)
        public abstract class Entry extends ObjectSelectionList.Entry<ChatSelectionScreen.ChatSelectionList.Entry> {
            @Override
            public Component getNarration() {
                return CommonComponents.EMPTY;
            }

            public boolean isSelected() {
                return false;
            }

            public boolean canSelect() {
                return false;
            }

            public boolean canReport() {
                return this.canSelect();
            }
        }

        @OnlyIn(Dist.CLIENT)
        static record Heading(UUID sender, ChatSelectionScreen.ChatSelectionList.Entry entry) {
            public boolean canCombine(ChatSelectionScreen.ChatSelectionList.Heading param0) {
                return param0.sender.equals(this.sender);
            }
        }

        @OnlyIn(Dist.CLIENT)
        public class MessageEntry extends ChatSelectionScreen.ChatSelectionList.Entry {
            private static final ResourceLocation CHECKMARK_TEXTURE = new ResourceLocation("minecraft", "textures/gui/checkmark.png");
            private static final int CHECKMARK_WIDTH = 9;
            private static final int CHECKMARK_HEIGHT = 8;
            private static final int INDENT_AMOUNT = 11;
            private static final int TAG_MARGIN_LEFT = 4;
            private final int chatId;
            private final FormattedText text;
            private final Component narration;
            @Nullable
            private final List<FormattedCharSequence> hoverText;
            @Nullable
            private final GuiMessageTag.Icon tagIcon;
            @Nullable
            private final List<FormattedCharSequence> tagHoverText;
            private final boolean canReport;
            private final boolean playerMessage;

            public MessageEntry(int param1, Component param2, @Nullable Component param3, GuiMessageTag param4, boolean param5, boolean param6) {
                this.chatId = param1;
                this.tagIcon = Util.mapNullable(param4, GuiMessageTag::icon);
                this.tagHoverText = param4 != null && param4.text() != null
                    ? ChatSelectionScreen.this.font.split(param4.text(), ChatSelectionList.this.getRowWidth())
                    : null;
                this.canReport = param5;
                this.playerMessage = param6;
                FormattedText var0 = ChatSelectionScreen.this.font
                    .substrByWidth(param2, this.getMaximumTextWidth() - ChatSelectionScreen.this.font.width(CommonComponents.ELLIPSIS));
                if (param2 != var0) {
                    this.text = FormattedText.composite(var0, CommonComponents.ELLIPSIS);
                    this.hoverText = ChatSelectionScreen.this.font.split(param2, ChatSelectionList.this.getRowWidth());
                } else {
                    this.text = param2;
                    this.hoverText = null;
                }

                this.narration = param3;
            }

            @Override
            public void render(
                PoseStack param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9
            ) {
                if (this.isSelected() && this.canReport) {
                    this.renderSelectedCheckmark(param0, param2, param3, param5);
                }

                int var0 = param3 + this.getTextIndent();
                int var1 = param2 + 1 + (param5 - 9) / 2;
                GuiComponent.drawString(
                    param0, ChatSelectionScreen.this.font, Language.getInstance().getVisualOrder(this.text), var0, var1, this.canReport ? -1 : -1593835521
                );
                if (this.hoverText != null && param8) {
                    ChatSelectionScreen.this.setTooltip(this.hoverText);
                }

                int var2 = ChatSelectionScreen.this.font.width(this.text);
                this.renderTag(param0, var0 + var2 + 4, param2, param5, param6, param7);
            }

            private void renderTag(PoseStack param0, int param1, int param2, int param3, int param4, int param5) {
                if (this.tagIcon != null) {
                    int var0 = param2 + (param3 - this.tagIcon.height) / 2;
                    this.tagIcon.draw(param0, param1, var0);
                    if (this.tagHoverText != null
                        && param4 >= param1
                        && param4 <= param1 + this.tagIcon.width
                        && param5 >= var0
                        && param5 <= var0 + this.tagIcon.height) {
                        ChatSelectionScreen.this.setTooltip(this.tagHoverText);
                    }
                }

            }

            private void renderSelectedCheckmark(PoseStack param0, int param1, int param2, int param3) {
                int var1 = param1 + (param3 - 8) / 2;
                RenderSystem.setShaderTexture(0, CHECKMARK_TEXTURE);
                RenderSystem.enableBlend();
                GuiComponent.blit(param0, param2, var1, 0.0F, 0.0F, 9, 8, 9, 8);
                RenderSystem.disableBlend();
            }

            private int getMaximumTextWidth() {
                int var0 = this.tagIcon != null ? this.tagIcon.width + 4 : 0;
                return ChatSelectionList.this.getRowWidth() - this.getTextIndent() - 4 - var0;
            }

            private int getTextIndent() {
                return this.playerMessage ? 11 : 0;
            }

            @Override
            public Component getNarration() {
                return (Component)(this.isSelected() ? Component.translatable("narrator.select", this.narration) : this.narration);
            }

            @Override
            public boolean mouseClicked(double param0, double param1, int param2) {
                if (param2 == 0) {
                    ChatSelectionList.this.setSelected(null);
                    return this.toggleReport();
                } else {
                    return false;
                }
            }

            @Override
            public boolean keyPressed(int param0, int param1, int param2) {
                return param0 != 257 && param0 != 32 && param0 != 335 ? false : this.toggleReport();
            }

            @Override
            public boolean isSelected() {
                return ChatSelectionScreen.this.report.isReported(this.chatId);
            }

            @Override
            public boolean canSelect() {
                return true;
            }

            @Override
            public boolean canReport() {
                return this.canReport;
            }

            private boolean toggleReport() {
                if (this.canReport) {
                    ChatSelectionScreen.this.report.toggleReported(this.chatId);
                    ChatSelectionScreen.this.updateConfirmSelectedButton();
                    return true;
                } else {
                    return false;
                }
            }
        }

        @OnlyIn(Dist.CLIENT)
        public class MessageHeadingEntry extends ChatSelectionScreen.ChatSelectionList.Entry {
            private static final int FACE_SIZE = 12;
            private final Component heading;
            private final ResourceLocation skin;
            private final boolean canReport;

            public MessageHeadingEntry(GameProfile param1, Component param2, boolean param3) {
                this.heading = param2;
                this.canReport = param3;
                this.skin = ChatSelectionList.this.minecraft.getSkinManager().getInsecureSkinLocation(param1);
            }

            @Override
            public void render(
                PoseStack param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9
            ) {
                int var0 = param3 - 12 - 4;
                int var1 = param2 + (param5 - 12) / 2;
                this.renderFace(param0, var0, var1, this.skin);
                int var2 = param2 + 1 + (param5 - 9) / 2;
                GuiComponent.drawString(param0, ChatSelectionScreen.this.font, this.heading, param3, var2, this.canReport ? -1 : -1593835521);
            }

            private void renderFace(PoseStack param0, int param1, int param2, ResourceLocation param3) {
                RenderSystem.setShaderTexture(0, param3);
                PlayerFaceRenderer.draw(param0, param1, param2, 12);
            }
        }

        @OnlyIn(Dist.CLIENT)
        public class PaddingEntry extends ChatSelectionScreen.ChatSelectionList.Entry {
            @Override
            public void render(
                PoseStack param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9
            ) {
            }
        }
    }
}
