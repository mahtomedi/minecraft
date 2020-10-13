package net.minecraft.client.gui.screens.social;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collection;
import java.util.Locale;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SocialInteractionsScreen extends Screen {
    protected static final ResourceLocation SOCIAL_INTERACTIONS_LOCATION = new ResourceLocation("textures/gui/social_interactions.png");
    private static final Component TAB_ALL = new TranslatableComponent("gui.socialInteractions.tab_all");
    private static final Component TAB_HIDDEN = new TranslatableComponent("gui.socialInteractions.tab_hidden");
    private static final Component TAB_ALL_SELECTED = TAB_ALL.plainCopy().withStyle(ChatFormatting.GRAY);
    private static final Component TAB_HIDDEN_SELECTED = TAB_HIDDEN.plainCopy().withStyle(ChatFormatting.GRAY);
    private static final Component SEARCH_HINT = new TranslatableComponent("gui.socialInteractions.search_hint")
        .withStyle(ChatFormatting.ITALIC)
        .withStyle(ChatFormatting.GRAY);
    private static final Component EMPTY_HIDDEN = new TranslatableComponent("gui.socialInteractions.empty_hidden").withStyle(ChatFormatting.GRAY);
    private SocialInteractionsPlayerList socialInteractionsPlayerList;
    private EditBox searchBox;
    private String lastSearch = "";
    private SocialInteractionsScreen.Page page = SocialInteractionsScreen.Page.ALL;
    private Button allButton;
    private Button hiddenButton;
    @Nullable
    private Component serverLabel;
    private int playerCount;
    private boolean showHiddenExclaim;
    private boolean initialized;
    @Nullable
    private Runnable postRenderRunnable;

    public SocialInteractionsScreen() {
        super(new TranslatableComponent("gui.socialInteractions.title"));
        this.updateServerLabel(Minecraft.getInstance());
    }

    private int windowHeight() {
        return Math.max(0, this.height - 128 - 16);
    }

    private int backgroundUnits() {
        return this.windowHeight() / 16;
    }

    private int listEnd() {
        return 80 + this.backgroundUnits() * 16 - 8;
    }

    private int marginX() {
        return (this.width - 238) / 2;
    }

    @Override
    public String getNarrationMessage() {
        return super.getNarrationMessage() + ". " + this.serverLabel.getString();
    }

    @Override
    protected void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        if (this.initialized) {
            this.socialInteractionsPlayerList.updateSize(this.width, this.height, 88, this.listEnd());
        } else {
            this.socialInteractionsPlayerList = new SocialInteractionsPlayerList(this, this.minecraft, this.width, this.height, 88, this.listEnd(), 36);
        }

        this.allButton = this.addButton(
            new Button(
                this.socialInteractionsPlayerList.getRowLeft() + this.socialInteractionsPlayerList.getRowWidth() / 4 - 30,
                45,
                60,
                20,
                TAB_ALL,
                param0 -> this.showPage(SocialInteractionsScreen.Page.ALL)
            )
        );
        this.hiddenButton = this.addButton(
            new Button(
                this.socialInteractionsPlayerList.getRowLeft() + this.socialInteractionsPlayerList.getRowWidth() / 4 * 3 - 30,
                45,
                60,
                20,
                TAB_HIDDEN,
                param0 -> this.showPage(SocialInteractionsScreen.Page.HIDDEN)
            )
        );
        String var0 = this.searchBox != null ? this.searchBox.getValue() : "";
        this.searchBox = new EditBox(this.font, this.marginX() + 28, 78, 220, 16, SEARCH_HINT);
        this.searchBox.setMaxLength(50);
        this.searchBox.setBordered(false);
        this.searchBox.setVisible(true);
        this.searchBox.setTextColor(16777215);
        this.searchBox.setValue(var0);
        this.searchBox.setResponder(this::checkSearchStringUpdate);
        this.children.add(this.searchBox);
        this.children.add(this.socialInteractionsPlayerList);
        this.initialized = true;
        this.showPage(this.page);
    }

    private void showPage(SocialInteractionsScreen.Page param0) {
        this.page = param0;
        this.allButton.setMessage(TAB_ALL);
        this.hiddenButton.setMessage(TAB_HIDDEN);
        Collection<UUID> var0;
        switch(param0) {
            case ALL:
                this.allButton.setMessage(TAB_ALL_SELECTED);
                var0 = this.minecraft.player.connection.getOnlinePlayerIds();
                break;
            case HIDDEN:
                this.hiddenButton.setMessage(TAB_HIDDEN_SELECTED);
                var0 = this.minecraft.getPlayerSocialManager().getHiddenPlayers();
                this.showHiddenExclaim = false;
                if (var0.isEmpty()) {
                    NarratorChatListener.INSTANCE.sayNow(EMPTY_HIDDEN.getString());
                }
                break;
            default:
                var0 = ImmutableList.of();
        }

        this.page = param0;
        this.socialInteractionsPlayerList.showPage(param0, var0, this.socialInteractionsPlayerList.getScrollAmount());
    }

    @Override
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
    }

    @Override
    public void renderBackground(PoseStack param0) {
        int var0 = this.marginX() + 3;
        super.renderBackground(param0);
        this.minecraft.getTextureManager().bind(SOCIAL_INTERACTIONS_LOCATION);
        this.blit(param0, var0, 64, 1, 1, 236, 8);
        int var1 = this.backgroundUnits();

        for(int var2 = 0; var2 < var1; ++var2) {
            this.blit(param0, var0, 72 + 16 * var2, 1, 10, 236, 16);
        }

        this.blit(param0, var0, 72 + 16 * var1, 1, 27, 236, 8);
        this.blit(param0, var0 + 10, 76, 243, 1, 12, 12);
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.updateServerLabel(this.minecraft);
        this.renderBackground(param0);
        if (this.serverLabel != null) {
            drawString(param0, this.minecraft.font, this.serverLabel, this.marginX() + 8, 35, -1);
        }

        if (!this.socialInteractionsPlayerList.isEmpty()) {
            this.socialInteractionsPlayerList.render(param0, param1, param2, param3);
        } else if (this.page == SocialInteractionsScreen.Page.HIDDEN) {
            drawCenteredString(param0, this.minecraft.font, EMPTY_HIDDEN, this.width / 2, (78 + this.listEnd()) / 2, -1);
        }

        if (!this.searchBox.isFocused() && this.searchBox.getValue().isEmpty()) {
            drawString(param0, this.minecraft.font, SEARCH_HINT, this.searchBox.x, this.searchBox.y, -1);
        } else {
            this.searchBox.render(param0, param1, param2, param3);
        }

        super.render(param0, param1, param2, param3);
        if (this.showHiddenExclaim) {
            this.minecraft.getTextureManager().bind(SOCIAL_INTERACTIONS_LOCATION);
            this.blit(param0, this.hiddenButton.x + this.hiddenButton.getWidth() - 8, 44, 249, 14, 6, 22);
        }

        if (this.postRenderRunnable != null) {
            this.postRenderRunnable.run();
        }

    }

    @Override
    public boolean mouseClicked(double param0, double param1, int param2) {
        return super.mouseClicked(param0, param1, param2) || this.socialInteractionsPlayerList.mouseClicked(param0, param1, param2);
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        if (!this.searchBox.isFocused() && this.minecraft.options.keySocialInteractions.matches(param0, param1)) {
            this.minecraft.setScreen(null);
            return true;
        } else {
            return super.keyPressed(param0, param1, param2);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void checkSearchStringUpdate(String param0) {
        param0 = param0.toLowerCase(Locale.ROOT);
        if (!param0.equals(this.lastSearch)) {
            this.socialInteractionsPlayerList.setFilter(param0);
            this.lastSearch = param0;
            this.showPage(this.page);
        }

    }

    private void updateServerLabel(Minecraft param0) {
        int var0 = param0.getConnection().getOnlinePlayers().size();
        if (this.playerCount != var0) {
            String var1 = "";
            ServerData var2 = param0.getCurrentServer();
            if (var2 != null) {
                var1 = var2.name;
            } else if (param0.isLocalServer()) {
                var1 = param0.getSingleplayerServer().getMotd();
            }

            this.serverLabel = new TranslatableComponent("gui.socialInteractions.server_label", var1, var0);
            this.playerCount = var0;
        }

    }

    public void onHiddenOrShown() {
        this.showHiddenExclaim = this.page != SocialInteractionsScreen.Page.HIDDEN && this.minecraft.getPlayerSocialManager().getHiddenPlayers().size() > 0;
    }

    public void onAddPlayer(PlayerInfo param0) {
        this.socialInteractionsPlayerList.addPlayer(param0, this.page);
    }

    public void onRemovePlayer(UUID param0) {
        this.socialInteractionsPlayerList.removePlayer(param0);
    }

    public void setPostRenderRunnable(@Nullable Runnable param0) {
        this.postRenderRunnable = param0;
    }

    @OnlyIn(Dist.CLIENT)
    public static enum Page {
        ALL,
        HIDDEN;
    }
}
