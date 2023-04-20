package net.minecraft.client.gui.screens.social;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.reporting.ChatReportScreen;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PlayerEntry extends ContainerObjectSelectionList.Entry<PlayerEntry> {
    private static final ResourceLocation REPORT_BUTTON_LOCATION = new ResourceLocation("textures/gui/report_button.png");
    private static final int TOOLTIP_DELAY = 10;
    private final Minecraft minecraft;
    private final List<AbstractWidget> children;
    private final UUID id;
    private final String playerName;
    private final Supplier<ResourceLocation> skinGetter;
    private boolean isRemoved;
    private boolean hasRecentMessages;
    private final boolean reportingEnabled;
    private final boolean playerReportable;
    private final boolean hasDraftReport;
    @Nullable
    private Button hideButton;
    @Nullable
    private Button showButton;
    @Nullable
    private Button reportButton;
    private float tooltipHoverTime;
    private static final Component HIDDEN = Component.translatable("gui.socialInteractions.status_hidden").withStyle(ChatFormatting.ITALIC);
    private static final Component BLOCKED = Component.translatable("gui.socialInteractions.status_blocked").withStyle(ChatFormatting.ITALIC);
    private static final Component OFFLINE = Component.translatable("gui.socialInteractions.status_offline").withStyle(ChatFormatting.ITALIC);
    private static final Component HIDDEN_OFFLINE = Component.translatable("gui.socialInteractions.status_hidden_offline").withStyle(ChatFormatting.ITALIC);
    private static final Component BLOCKED_OFFLINE = Component.translatable("gui.socialInteractions.status_blocked_offline").withStyle(ChatFormatting.ITALIC);
    private static final Component REPORT_DISABLED_TOOLTIP = Component.translatable("gui.socialInteractions.tooltip.report.disabled");
    private static final Component NOT_REPORTABLE_TOOLTIP = Component.translatable("gui.socialInteractions.tooltip.report.not_reportable");
    private static final Component HIDE_TEXT_TOOLTIP = Component.translatable("gui.socialInteractions.tooltip.hide");
    private static final Component SHOW_TEXT_TOOLTIP = Component.translatable("gui.socialInteractions.tooltip.show");
    private static final Component REPORT_PLAYER_TOOLTIP = Component.translatable("gui.socialInteractions.tooltip.report");
    private static final int SKIN_SIZE = 24;
    private static final int PADDING = 4;
    private static final int CHAT_TOGGLE_ICON_SIZE = 20;
    private static final int CHAT_TOGGLE_ICON_X = 0;
    private static final int CHAT_TOGGLE_ICON_Y = 38;
    public static final int SKIN_SHADE = FastColor.ARGB32.color(190, 0, 0, 0);
    public static final int BG_FILL = FastColor.ARGB32.color(255, 74, 74, 74);
    public static final int BG_FILL_REMOVED = FastColor.ARGB32.color(255, 48, 48, 48);
    public static final int PLAYERNAME_COLOR = FastColor.ARGB32.color(255, 255, 255, 255);
    public static final int PLAYER_STATUS_COLOR = FastColor.ARGB32.color(140, 255, 255, 255);

    public PlayerEntry(Minecraft param0, SocialInteractionsScreen param1, UUID param2, String param3, Supplier<ResourceLocation> param4, boolean param5) {
        this.minecraft = param0;
        this.id = param2;
        this.playerName = param3;
        this.skinGetter = param4;
        ReportingContext var0 = param0.getReportingContext();
        this.reportingEnabled = var0.sender().isEnabled();
        this.playerReportable = param5;
        this.hasDraftReport = var0.hasDraftReportFor(param2);
        Component var1 = Component.translatable("gui.socialInteractions.narration.hide", param3);
        Component var2 = Component.translatable("gui.socialInteractions.narration.show", param3);
        PlayerSocialManager var3 = param0.getPlayerSocialManager();
        boolean var4 = param0.getChatStatus().isChatAllowed(param0.isLocalServer());
        boolean var5 = !param0.player.getUUID().equals(param2);
        if (var5 && var4 && !var3.isBlocked(param2)) {
            this.reportButton = new ImageButton(
                0,
                0,
                20,
                20,
                0,
                0,
                20,
                REPORT_BUTTON_LOCATION,
                64,
                64,
                param4x -> var0.draftReportHandled(param0, param1, () -> param0.setScreen(new ChatReportScreen(param1, var0, param2)), false),
                Component.translatable("gui.socialInteractions.report")
            ) {
                @Override
                protected MutableComponent createNarrationMessage() {
                    return PlayerEntry.this.getEntryNarationMessage(super.createNarrationMessage());
                }
            };
            this.reportButton.setTooltip(this.createReportButtonTooltip());
            this.reportButton.setTooltipDelay(10);
            this.hideButton = new ImageButton(0, 0, 20, 20, 0, 38, 20, SocialInteractionsScreen.SOCIAL_INTERACTIONS_LOCATION, 256, 256, param3x -> {
                var3.hidePlayer(param2);
                this.onHiddenOrShown(true, Component.translatable("gui.socialInteractions.hidden_in_chat", param3));
            }, Component.translatable("gui.socialInteractions.hide")) {
                @Override
                protected MutableComponent createNarrationMessage() {
                    return PlayerEntry.this.getEntryNarationMessage(super.createNarrationMessage());
                }
            };
            this.hideButton.setTooltip(Tooltip.create(HIDE_TEXT_TOOLTIP, var1));
            this.hideButton.setTooltipDelay(10);
            this.showButton = new ImageButton(0, 0, 20, 20, 20, 38, 20, SocialInteractionsScreen.SOCIAL_INTERACTIONS_LOCATION, 256, 256, param3x -> {
                var3.showPlayer(param2);
                this.onHiddenOrShown(false, Component.translatable("gui.socialInteractions.shown_in_chat", param3));
            }, Component.translatable("gui.socialInteractions.show")) {
                @Override
                protected MutableComponent createNarrationMessage() {
                    return PlayerEntry.this.getEntryNarationMessage(super.createNarrationMessage());
                }
            };
            this.showButton.setTooltip(Tooltip.create(SHOW_TEXT_TOOLTIP, var2));
            this.showButton.setTooltipDelay(10);
            this.reportButton.active = false;
            this.children = new ArrayList<>();
            this.children.add(this.hideButton);
            this.children.add(this.reportButton);
            this.updateHideAndShowButton(var3.isHidden(this.id));
        } else {
            this.children = ImmutableList.of();
        }

    }

    private Tooltip createReportButtonTooltip() {
        if (!this.playerReportable) {
            return Tooltip.create(NOT_REPORTABLE_TOOLTIP);
        } else if (!this.reportingEnabled) {
            return Tooltip.create(REPORT_DISABLED_TOOLTIP);
        } else {
            return !this.hasRecentMessages
                ? Tooltip.create(Component.translatable("gui.socialInteractions.tooltip.report.no_messages", this.playerName))
                : Tooltip.create(REPORT_PLAYER_TOOLTIP, Component.translatable("gui.socialInteractions.narration.report", this.playerName));
        }
    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9) {
        int var0 = param3 + 4;
        int var1 = param2 + (param5 - 24) / 2;
        int var2 = var0 + 24 + 4;
        Component var3 = this.getStatusComponent();
        int var4;
        if (var3 == CommonComponents.EMPTY) {
            param0.fill(param3, param2, param3 + param4, param2 + param5, BG_FILL);
            var4 = param2 + (param5 - 9) / 2;
        } else {
            param0.fill(param3, param2, param3 + param4, param2 + param5, BG_FILL_REMOVED);
            var4 = param2 + (param5 - (9 + 9)) / 2;
            param0.drawString(this.minecraft.font, var3, var2, var4 + 12, PLAYER_STATUS_COLOR, false);
        }

        PlayerFaceRenderer.draw(param0, this.skinGetter.get(), var0, var1, 24);
        param0.drawString(this.minecraft.font, this.playerName, var2, var4, PLAYERNAME_COLOR, false);
        if (this.isRemoved) {
            param0.fill(var0, var1, var0 + 24, var1 + 24, SKIN_SHADE);
        }

        if (this.hideButton != null && this.showButton != null && this.reportButton != null) {
            float var6 = this.tooltipHoverTime;
            this.hideButton.setX(param3 + (param4 - this.hideButton.getWidth() - 4) - 20 - 4);
            this.hideButton.setY(param2 + (param5 - this.hideButton.getHeight()) / 2);
            this.hideButton.render(param0, param6, param7, param9);
            this.showButton.setX(param3 + (param4 - this.showButton.getWidth() - 4) - 20 - 4);
            this.showButton.setY(param2 + (param5 - this.showButton.getHeight()) / 2);
            this.showButton.render(param0, param6, param7, param9);
            this.reportButton.setX(param3 + (param4 - this.showButton.getWidth() - 4));
            this.reportButton.setY(param2 + (param5 - this.showButton.getHeight()) / 2);
            this.reportButton.render(param0, param6, param7, param9);
            if (var6 == this.tooltipHoverTime) {
                this.tooltipHoverTime = 0.0F;
            }
        }

        if (this.hasDraftReport && this.reportButton != null) {
            param0.blit(AbstractWidget.WIDGETS_LOCATION, this.reportButton.getX() + 5, this.reportButton.getY() + 1, 182.0F, 24.0F, 15, 15, 256, 256);
        }

    }

    @Override
    public List<? extends GuiEventListener> children() {
        return this.children;
    }

    @Override
    public List<? extends NarratableEntry> narratables() {
        return this.children;
    }

    public String getPlayerName() {
        return this.playerName;
    }

    public UUID getPlayerId() {
        return this.id;
    }

    public void setRemoved(boolean param0) {
        this.isRemoved = param0;
    }

    public boolean isRemoved() {
        return this.isRemoved;
    }

    public void setHasRecentMessages(boolean param0) {
        this.hasRecentMessages = param0;
        if (this.reportButton != null) {
            this.reportButton.active = this.reportingEnabled && this.playerReportable && param0;
            this.reportButton.setTooltip(this.createReportButtonTooltip());
        }

    }

    public boolean hasRecentMessages() {
        return this.hasRecentMessages;
    }

    private void onHiddenOrShown(boolean param0, Component param1) {
        this.updateHideAndShowButton(param0);
        this.minecraft.gui.getChat().addMessage(param1);
        this.minecraft.getNarrator().sayNow(param1);
    }

    private void updateHideAndShowButton(boolean param0) {
        this.showButton.visible = param0;
        this.hideButton.visible = !param0;
        this.children.set(0, param0 ? this.showButton : this.hideButton);
    }

    MutableComponent getEntryNarationMessage(MutableComponent param0) {
        Component var0 = this.getStatusComponent();
        return var0 == CommonComponents.EMPTY
            ? Component.literal(this.playerName).append(", ").append(param0)
            : Component.literal(this.playerName).append(", ").append(var0).append(", ").append(param0);
    }

    private Component getStatusComponent() {
        boolean var0 = this.minecraft.getPlayerSocialManager().isHidden(this.id);
        boolean var1 = this.minecraft.getPlayerSocialManager().isBlocked(this.id);
        if (var1 && this.isRemoved) {
            return BLOCKED_OFFLINE;
        } else if (var0 && this.isRemoved) {
            return HIDDEN_OFFLINE;
        } else if (var1) {
            return BLOCKED;
        } else if (var0) {
            return HIDDEN;
        } else {
            return this.isRemoved ? OFFLINE : CommonComponents.EMPTY;
        }
    }
}
