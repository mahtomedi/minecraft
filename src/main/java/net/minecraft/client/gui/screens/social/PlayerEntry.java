package net.minecraft.client.gui.screens.social;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.reporting.ChatReportScreen;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PlayerEntry extends ContainerObjectSelectionList.Entry<PlayerEntry> {
    private static final int TOOLTIP_DELAY = 10;
    private static final int TOOLTIP_MAX_WIDTH = 150;
    private final Minecraft minecraft;
    private final List<AbstractWidget> children;
    private final UUID id;
    private final String playerName;
    private final Supplier<ResourceLocation> skinGetter;
    private boolean isRemoved;
    @Nullable
    private Button hideButton;
    @Nullable
    private Button showButton;
    @Nullable
    private Button reportButton;
    final Component hideText;
    final Component showText;
    final Component reportText;
    final List<FormattedCharSequence> hideTooltip;
    final List<FormattedCharSequence> showTooltip;
    final List<FormattedCharSequence> reportTooltip;
    float tooltipHoverTime;
    private static final Component HIDDEN = Component.translatable("gui.socialInteractions.status_hidden").withStyle(ChatFormatting.ITALIC);
    private static final Component BLOCKED = Component.translatable("gui.socialInteractions.status_blocked").withStyle(ChatFormatting.ITALIC);
    private static final Component OFFLINE = Component.translatable("gui.socialInteractions.status_offline").withStyle(ChatFormatting.ITALIC);
    private static final Component HIDDEN_OFFLINE = Component.translatable("gui.socialInteractions.status_hidden_offline").withStyle(ChatFormatting.ITALIC);
    private static final Component BLOCKED_OFFLINE = Component.translatable("gui.socialInteractions.status_blocked_offline").withStyle(ChatFormatting.ITALIC);
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

    public PlayerEntry(final Minecraft param0, final SocialInteractionsScreen param1, UUID param2, String param3, Supplier<ResourceLocation> param4) {
        this.minecraft = param0;
        this.id = param2;
        this.playerName = param3;
        this.skinGetter = param4;
        this.hideText = Component.translatable("gui.socialInteractions.tooltip.hide", param3);
        this.showText = Component.translatable("gui.socialInteractions.tooltip.show", param3);
        this.reportText = Component.translatable("gui.socialInteractions.tooltip.report", param3);
        this.hideTooltip = param0.font.split(this.hideText, 150);
        this.showTooltip = param0.font.split(this.showText, 150);
        this.reportTooltip = param0.font.split(this.reportText, 150);
        PlayerSocialManager var0 = param0.getPlayerSocialManager();
        boolean var1 = param0.getChatStatus().isChatAllowed(param0.isLocalServer());
        boolean var2 = !param0.player.getUUID().equals(param2);
        if (var2 && var1 && !var0.isBlocked(param2)) {
            ReportingContext var3 = param0.getReportingContext();
            this.reportButton = new ImageButton(
                0,
                0,
                20,
                20,
                40,
                38,
                20,
                SocialInteractionsScreen.SOCIAL_INTERACTIONS_LOCATION,
                256,
                256,
                param3x -> param0.setScreen(new ChatReportScreen(param0.screen, var3, param2)),
                new Button.OnTooltip() {
                    @Override
                    public void onTooltip(Button param0x, PoseStack param1x, int param2, int param3) {
                        PlayerEntry.this.tooltipHoverTime += param0.getDeltaFrameTime();
                        if (PlayerEntry.this.tooltipHoverTime >= 10.0F) {
                            param1.setPostRenderRunnable(() -> PlayerEntry.postRenderTooltip(param1, param1, PlayerEntry.this.reportTooltip, param2, param3));
                        }
    
                    }
    
                    @Override
                    public void narrateTooltip(Consumer<Component> param0x) {
                        param0.accept(PlayerEntry.this.reportText);
                    }
                },
                Component.translatable("gui.socialInteractions.report")
            ) {
                @Override
                protected MutableComponent createNarrationMessage() {
                    return PlayerEntry.this.getEntryNarationMessage(super.createNarrationMessage());
                }
            };
            this.hideButton = new ImageButton(0, 0, 20, 20, 0, 38, 20, SocialInteractionsScreen.SOCIAL_INTERACTIONS_LOCATION, 256, 256, param3x -> {
                var0.hidePlayer(param2);
                this.onHiddenOrShown(true, Component.translatable("gui.socialInteractions.hidden_in_chat", param3));
            }, new Button.OnTooltip() {
                @Override
                public void onTooltip(Button param0x, PoseStack param1x, int param2, int param3) {
                    PlayerEntry.this.tooltipHoverTime += param0.getDeltaFrameTime();
                    if (PlayerEntry.this.tooltipHoverTime >= 10.0F) {
                        param1.setPostRenderRunnable(() -> PlayerEntry.postRenderTooltip(param1, param1, PlayerEntry.this.hideTooltip, param2, param3));
                    }

                }

                @Override
                public void narrateTooltip(Consumer<Component> param0x) {
                    param0.accept(PlayerEntry.this.hideText);
                }
            }, Component.translatable("gui.socialInteractions.hide")) {
                @Override
                protected MutableComponent createNarrationMessage() {
                    return PlayerEntry.this.getEntryNarationMessage(super.createNarrationMessage());
                }
            };
            this.showButton = new ImageButton(0, 0, 20, 20, 20, 38, 20, SocialInteractionsScreen.SOCIAL_INTERACTIONS_LOCATION, 256, 256, param3x -> {
                var0.showPlayer(param2);
                this.onHiddenOrShown(false, Component.translatable("gui.socialInteractions.shown_in_chat", param3));
            }, new Button.OnTooltip() {
                @Override
                public void onTooltip(Button param0x, PoseStack param1x, int param2, int param3) {
                    PlayerEntry.this.tooltipHoverTime += param0.getDeltaFrameTime();
                    if (PlayerEntry.this.tooltipHoverTime >= 10.0F) {
                        param1.setPostRenderRunnable(() -> PlayerEntry.postRenderTooltip(param1, param1, PlayerEntry.this.showTooltip, param2, param3));
                    }

                }

                @Override
                public void narrateTooltip(Consumer<Component> param0x) {
                    param0.accept(PlayerEntry.this.showText);
                }
            }, Component.translatable("gui.socialInteractions.show")) {
                @Override
                protected MutableComponent createNarrationMessage() {
                    return PlayerEntry.this.getEntryNarationMessage(super.createNarrationMessage());
                }
            };
            this.showButton.visible = var0.isHidden(param2);
            this.hideButton.visible = !this.showButton.visible;
            this.reportButton.active = var3.sender().isEnabled();
            this.children = ImmutableList.of(this.hideButton, this.showButton, this.reportButton);
        } else {
            this.children = ImmutableList.of();
        }

    }

    @Override
    public void render(PoseStack param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9) {
        int var0 = param3 + 4;
        int var1 = param2 + (param5 - 24) / 2;
        int var2 = var0 + 24 + 4;
        Component var3 = this.getStatusComponent();
        int var4;
        if (var3 == CommonComponents.EMPTY) {
            GuiComponent.fill(param0, param3, param2, param3 + param4, param2 + param5, BG_FILL);
            var4 = param2 + (param5 - 9) / 2;
        } else {
            GuiComponent.fill(param0, param3, param2, param3 + param4, param2 + param5, BG_FILL_REMOVED);
            var4 = param2 + (param5 - (9 + 9)) / 2;
            this.minecraft.font.draw(param0, var3, (float)var2, (float)(var4 + 12), PLAYER_STATUS_COLOR);
        }

        RenderSystem.setShaderTexture(0, this.skinGetter.get());
        PlayerFaceRenderer.draw(param0, var0, var1, 24);
        this.minecraft.font.draw(param0, this.playerName, (float)var2, (float)var4, PLAYERNAME_COLOR);
        if (this.isRemoved) {
            GuiComponent.fill(param0, var0, var1, var0 + 24, var1 + 24, SKIN_SHADE);
        }

        if (this.hideButton != null && this.showButton != null && this.reportButton != null) {
            float var6 = this.tooltipHoverTime;
            this.hideButton.x = param3 + (param4 - this.hideButton.getWidth() - 4) - 20 - 4;
            this.hideButton.y = param2 + (param5 - this.hideButton.getHeight()) / 2;
            this.hideButton.render(param0, param6, param7, param9);
            this.showButton.x = param3 + (param4 - this.showButton.getWidth() - 4) - 20 - 4;
            this.showButton.y = param2 + (param5 - this.showButton.getHeight()) / 2;
            this.showButton.render(param0, param6, param7, param9);
            this.reportButton.x = param3 + (param4 - this.showButton.getWidth() - 4);
            this.reportButton.y = param2 + (param5 - this.showButton.getHeight()) / 2;
            this.reportButton.render(param0, param6, param7, param9);
            if (var6 == this.tooltipHoverTime) {
                this.tooltipHoverTime = 0.0F;
            }
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

    private void onHiddenOrShown(boolean param0, Component param1) {
        this.showButton.visible = param0;
        this.hideButton.visible = !param0;
        this.minecraft.gui.getChat().addMessage(param1);
        NarratorChatListener.INSTANCE.sayNow(param1);
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

    static void postRenderTooltip(SocialInteractionsScreen param0, PoseStack param1, List<FormattedCharSequence> param2, int param3, int param4) {
        param0.renderTooltip(param1, param2, param3, param4);
        param0.setPostRenderRunnable(null);
    }
}
