package net.minecraft.client.gui.screens.social;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
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
    private final ResourceLocation skin;
    private boolean isRemoved;
    @Nullable
    private Button hideButton;
    @Nullable
    private Button showButton;
    private final List<FormattedCharSequence> hideTooltip;
    private final List<FormattedCharSequence> showTooltip;
    private float tooltipHoverTime;
    public static final int SKIN_SHADE = FastColor.ARGB32.color(190, 0, 0, 0);
    public static final int BG_FILL = FastColor.ARGB32.color(255, 74, 74, 74);
    public static final int BG_FILL_REMOVED = FastColor.ARGB32.color(255, 48, 48, 48);
    public static final int PLAYERNAME_COLOR = FastColor.ARGB32.color(255, 255, 255, 255);

    public PlayerEntry(
        Minecraft param0, SocialInteractionsScreen param1, UUID param2, String param3, ResourceLocation param4, SocialInteractionsScreen.Page param5
    ) {
        this.minecraft = param0;
        this.id = param2;
        this.playerName = param3;
        this.skin = param4;
        this.hideTooltip = param0.font.split(new TranslatableComponent("gui.socialInteractions.tooltip.hide", param3), 150);
        this.showTooltip = param0.font.split(new TranslatableComponent("gui.socialInteractions.tooltip.show", param3), 150);
        if (!param0.player.getGameProfile().getId().equals(param2)) {
            this.hideButton = new ImageButton(0, 0, 20, 20, 0, 38, 20, SocialInteractionsScreen.SOCIAL_INTERACTIONS_LOCATION, 256, 256, param4x -> {
                param0.getPlayerSocialManager().hidePlayer(param2);
                this.onHiddenOrShown(true, new TranslatableComponent("gui.socialInteractions.hidden_in_chat", param3));
                param1.onHiddenOrShown();
            }, (param2x, param3x, param4x, param5x) -> {
                this.tooltipHoverTime += param0.getDeltaFrameTime();
                if (this.tooltipHoverTime >= 20.0F) {
                    param1.setPostRenderRunnable(() -> postRenderTooltip(param1, param3x, this.hideTooltip, param4x, param5x));
                }

            }, new TranslatableComponent("gui.socialInteractions.hide", param3));
            this.showButton = new ImageButton(0, 0, 20, 20, 20, 38, 20, SocialInteractionsScreen.SOCIAL_INTERACTIONS_LOCATION, 256, 256, param4x -> {
                param0.getPlayerSocialManager().showPlayer(param2);
                this.onHiddenOrShown(false, new TranslatableComponent("gui.socialInteractions.shown_in_chat", param3));
                param1.onHiddenOrShown();
            }, (param2x, param3x, param4x, param5x) -> {
                this.tooltipHoverTime += param0.getDeltaFrameTime();
                if (this.tooltipHoverTime >= 20.0F) {
                    param1.setPostRenderRunnable(() -> postRenderTooltip(param1, param3x, this.showTooltip, param4x, param5x));
                }

            }, new TranslatableComponent("gui.socialInteractions.show", param3));
            this.showButton.visible = param0.getPlayerSocialManager().isHidden(param2);
            this.hideButton.visible = !this.showButton.visible;
            this.children = ImmutableList.of(this.hideButton, this.showButton);
        } else {
            this.children = ImmutableList.of();
        }

    }

    @Override
    public void render(PoseStack param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9) {
        GuiComponent.fill(param0, param3, param2, param3 + param4, param2 + param5, this.isRemoved ? BG_FILL_REMOVED : BG_FILL);
        int var0 = param3 + 4;
        int var1 = param2 + (param5 - 24) / 2;
        this.minecraft.getTextureManager().bind(this.skin);
        GuiComponent.blit(param0, var0, var1, 24, 24, 8.0F, 8.0F, 8, 8, 64, 64);
        RenderSystem.enableBlend();
        GuiComponent.blit(param0, var0, var1, 24, 24, 40.0F, 8.0F, 8, 8, 64, 64);
        RenderSystem.disableBlend();
        boolean var2 = this.minecraft.getPlayerSocialManager().isHidden(this.id);
        if (this.isRemoved || var2) {
            GuiComponent.fill(param0, var0, var1, var0 + 24, var1 + 24, SKIN_SHADE);
            if (var2) {
                this.minecraft.getTextureManager().bind(SocialInteractionsScreen.SOCIAL_INTERACTIONS_LOCATION);
                RenderSystem.enableBlend();
                GuiComponent.blit(param0, var0 + 5, var1 + 8, 14, 14, 241.0F, 37.0F, 14, 14, 256, 256);
                RenderSystem.disableBlend();
            }
        }

        int var3 = var0 + 24 + 4;
        int var4 = param2 + (param5 - (9 + 9)) / 2;
        this.minecraft.font.draw(param0, this.playerName, (float)var3, (float)var4, PLAYERNAME_COLOR);
        if (this.hideButton != null && this.showButton != null) {
            float var5 = this.tooltipHoverTime;
            this.hideButton.x = param3 + (param4 - this.hideButton.getWidth() - 4);
            this.hideButton.y = param2 + (param5 - this.hideButton.getHeight()) / 2;
            this.hideButton.render(param0, param6, param7, param9);
            this.showButton.x = param3 + (param4 - this.showButton.getWidth() - 4);
            this.showButton.y = param2 + (param5 - this.showButton.getHeight()) / 2;
            this.showButton.render(param0, param6, param7, param9);
            if (var5 == this.tooltipHoverTime) {
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

    private static void postRenderTooltip(SocialInteractionsScreen param0, PoseStack param1, List<FormattedCharSequence> param2, int param3, int param4) {
        param0.renderTooltip(param1, param2, param3, param4);
        param0.setPostRenderRunnable(null);
    }
}
