package net.minecraft.client.gui.screens.social;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PlayerEntry extends ContainerObjectSelectionList.Entry<PlayerEntry> {
    private final Minecraft minecraft;
    private final List<GuiEventListener> children;
    private final UUID id;
    private final String playerName;
    private final Supplier<ResourceLocation> skinGetter;
    private boolean isRemoved;
    @Nullable
    private Button hideButton;
    @Nullable
    private Button showButton;
    private final List<FormattedCharSequence> hideTooltip;
    private final List<FormattedCharSequence> showTooltip;
    private float tooltipHoverTime;
    private static final Component HIDDEN = new TranslatableComponent("gui.socialInteractions.status_hidden").withStyle(ChatFormatting.ITALIC);
    private static final Component BLOCKED = new TranslatableComponent("gui.socialInteractions.status_blocked").withStyle(ChatFormatting.ITALIC);
    private static final Component OFFLINE = new TranslatableComponent("gui.socialInteractions.status_offline").withStyle(ChatFormatting.ITALIC);
    private static final Component HIDDEN_OFFLINE = new TranslatableComponent("gui.socialInteractions.status_hidden_offline").withStyle(ChatFormatting.ITALIC);
    private static final Component BLOCKED_OFFLINE = new TranslatableComponent("gui.socialInteractions.status_blocked_offline")
        .withStyle(ChatFormatting.ITALIC);
    public static final int SKIN_SHADE = FastColor.ARGB32.color(190, 0, 0, 0);
    public static final int BG_FILL = FastColor.ARGB32.color(255, 74, 74, 74);
    public static final int BG_FILL_REMOVED = FastColor.ARGB32.color(255, 48, 48, 48);
    public static final int PLAYERNAME_COLOR = FastColor.ARGB32.color(255, 255, 255, 255);
    public static final int PLAYER_STATUS_COLOR = FastColor.ARGB32.color(140, 255, 255, 255);

    public PlayerEntry(Minecraft param0, SocialInteractionsScreen param1, UUID param2, String param3, Supplier<ResourceLocation> param4) {
        this.minecraft = param0;
        this.id = param2;
        this.playerName = param3;
        this.skinGetter = param4;
        this.hideTooltip = param0.font.split(new TranslatableComponent("gui.socialInteractions.tooltip.hide", param3), 150);
        this.showTooltip = param0.font.split(new TranslatableComponent("gui.socialInteractions.tooltip.show", param3), 150);
        PlayerSocialManager var0 = param0.getPlayerSocialManager();
        if (!param0.player.getGameProfile().getId().equals(param2) && !var0.isBlocked(param2)) {
            this.hideButton = new ImageButton(0, 0, 20, 20, 0, 38, 20, SocialInteractionsScreen.SOCIAL_INTERACTIONS_LOCATION, 256, 256, param3x -> {
                var0.hidePlayer(param2);
                this.onHiddenOrShown(true, new TranslatableComponent("gui.socialInteractions.hidden_in_chat", param3));
            }, (param2x, param3x, param4x, param5) -> {
                this.tooltipHoverTime += param0.getDeltaFrameTime();
                if (this.tooltipHoverTime >= 10.0F) {
                    param1.setPostRenderRunnable(() -> postRenderTooltip(param1, param3x, this.hideTooltip, param4x, param5));
                }

            }, new TranslatableComponent("gui.socialInteractions.hide")) {
                @Override
                protected MutableComponent createNarrationMessage() {
                    return PlayerEntry.this.getEntryNarationMessage(super.createNarrationMessage());
                }
            };
            this.showButton = new ImageButton(0, 0, 20, 20, 20, 38, 20, SocialInteractionsScreen.SOCIAL_INTERACTIONS_LOCATION, 256, 256, param3x -> {
                var0.showPlayer(param2);
                this.onHiddenOrShown(false, new TranslatableComponent("gui.socialInteractions.shown_in_chat", param3));
            }, (param2x, param3x, param4x, param5) -> {
                this.tooltipHoverTime += param0.getDeltaFrameTime();
                if (this.tooltipHoverTime >= 10.0F) {
                    param1.setPostRenderRunnable(() -> postRenderTooltip(param1, param3x, this.showTooltip, param4x, param5));
                }

            }, new TranslatableComponent("gui.socialInteractions.show")) {
                @Override
                protected MutableComponent createNarrationMessage() {
                    return PlayerEntry.this.getEntryNarationMessage(super.createNarrationMessage());
                }
            };
            this.showButton.visible = var0.isHidden(param2);
            this.hideButton.visible = !this.showButton.visible;
            this.children = ImmutableList.of(this.hideButton, this.showButton);
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
        if (var3 == TextComponent.EMPTY) {
            GuiComponent.fill(param0, param3, param2, param3 + param4, param2 + param5, BG_FILL);
            var4 = param2 + (param5 - 9) / 2;
        } else {
            GuiComponent.fill(param0, param3, param2, param3 + param4, param2 + param5, BG_FILL_REMOVED);
            var4 = param2 + (param5 - (9 + 9)) / 2;
            this.minecraft.font.draw(param0, var3, (float)var2, (float)(var4 + 12), PLAYER_STATUS_COLOR);
        }

        RenderSystem.setShaderTexture(0, this.skinGetter.get());
        GuiComponent.blit(param0, var0, var1, 24, 24, 8.0F, 8.0F, 8, 8, 64, 64);
        RenderSystem.enableBlend();
        GuiComponent.blit(param0, var0, var1, 24, 24, 40.0F, 8.0F, 8, 8, 64, 64);
        RenderSystem.disableBlend();
        this.minecraft.font.draw(param0, this.playerName, (float)var2, (float)var4, PLAYERNAME_COLOR);
        if (this.isRemoved) {
            GuiComponent.fill(param0, var0, var1, var0 + 24, var1 + 24, SKIN_SHADE);
        }

        if (this.hideButton != null && this.showButton != null) {
            float var6 = this.tooltipHoverTime;
            this.hideButton.x = param3 + (param4 - this.hideButton.getWidth() - 4);
            this.hideButton.y = param2 + (param5 - this.hideButton.getHeight()) / 2;
            this.hideButton.render(param0, param6, param7, param9);
            this.showButton.x = param3 + (param4 - this.showButton.getWidth() - 4);
            this.showButton.y = param2 + (param5 - this.showButton.getHeight()) / 2;
            this.showButton.render(param0, param6, param7, param9);
            if (var6 == this.tooltipHoverTime) {
                this.tooltipHoverTime = 0.0F;
            }
        }

    }

    @Override
    public List<? extends GuiEventListener> children() {
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
        NarratorChatListener.INSTANCE.sayNow(param1.getString());
    }

    private MutableComponent getEntryNarationMessage(MutableComponent param0) {
        Component var0 = this.getStatusComponent();
        return var0 == TextComponent.EMPTY
            ? new TextComponent(this.playerName).append(", ").append(param0)
            : new TextComponent(this.playerName).append(", ").append(var0).append(", ").append(param0);
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
            return this.isRemoved ? OFFLINE : TextComponent.EMPTY;
        }
    }

    private static void postRenderTooltip(SocialInteractionsScreen param0, PoseStack param1, List<FormattedCharSequence> param2, int param3, int param4) {
        param0.renderTooltip(param1, param2, param3, param4);
        param0.setPostRenderRunnable(null);
    }
}
