package net.minecraft.client.gui.screens.social;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collection;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SocialInteractionsScreen extends Screen {
    protected static final ResourceLocation SOCIAL_INTERACTIONS_LOCATION = new ResourceLocation("textures/gui/social_interactions.png");
    private static final Component TAB_ALL = new TranslatableComponent("gui.socialInteractions.tab_all");
    private static final Component TAB_HIDDEN = new TranslatableComponent("gui.socialInteractions.tab_hidden");
    private static final Component TAB_BLOCKED = new TranslatableComponent("gui.socialInteractions.tab_blocked");
    private static final Component TAB_ALL_SELECTED = TAB_ALL.plainCopy().withStyle(ChatFormatting.UNDERLINE);
    private static final Component TAB_HIDDEN_SELECTED = TAB_HIDDEN.plainCopy().withStyle(ChatFormatting.UNDERLINE);
    private static final Component TAB_BLOCKED_SELECTED = TAB_BLOCKED.plainCopy().withStyle(ChatFormatting.UNDERLINE);
    private static final Component SEARCH_HINT = new TranslatableComponent("gui.socialInteractions.search_hint")
        .withStyle(ChatFormatting.ITALIC)
        .withStyle(ChatFormatting.GRAY);
    private static final Component EMPTY_SEARCH = new TranslatableComponent("gui.socialInteractions.search_empty").withStyle(ChatFormatting.GRAY);
    private static final Component EMPTY_HIDDEN = new TranslatableComponent("gui.socialInteractions.empty_hidden").withStyle(ChatFormatting.GRAY);
    private static final Component EMPTY_BLOCKED = new TranslatableComponent("gui.socialInteractions.empty_blocked").withStyle(ChatFormatting.GRAY);
    private static final Component BLOCKING_HINT = new TranslatableComponent("gui.socialInteractions.blocking_hint");
    private static final String BLOCK_LINK = "https://aka.ms/javablocking";
    private static final int BG_BORDER_SIZE = 8;
    private static final int BG_UNITS = 16;
    private static final int BG_WIDTH = 236;
    private static final int SEARCH_HEIGHT = 16;
    private static final int MARGIN_Y = 64;
    public static final int LIST_START = 88;
    public static final int SEARCH_START = 78;
    private static final int IMAGE_WIDTH = 238;
    private static final int BUTTON_HEIGHT = 20;
    private static final int ITEM_HEIGHT = 36;
    private SocialInteractionsPlayerList socialInteractionsPlayerList;
    private EditBox searchBox;
    private String lastSearch = "";
    private SocialInteractionsScreen.Page page = SocialInteractionsScreen.Page.ALL;
    private Button allButton;
    private Button hiddenButton;
    private Button blockedButton;
    private Button blockingHintButton;
    @Nullable
    private Component serverLabel;
    private int playerCount;
    private boolean initialized;
    @Nullable
    private Runnable postRenderRunnable;

    public SocialInteractionsScreen() {
        super(new TranslatableComponent("gui.socialInteractions.title"));
        this.updateServerLabel(Minecraft.getInstance());
    }

    private int windowHeight() {
        return Math.max(52, this.height - 128 - 16);
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
    public void tick() {
        super.tick();
        this.searchBox.tick();
    }

    @Override
    protected void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        if (this.initialized) {
            this.socialInteractionsPlayerList.updateSize(this.width, this.height, 88, this.listEnd());
        } else {
            this.socialInteractionsPlayerList = new SocialInteractionsPlayerList(this, this.minecraft, this.width, this.height, 88, this.listEnd(), 36);
        }

        int var0 = this.socialInteractionsPlayerList.getRowWidth() / 3;
        int var1 = this.socialInteractionsPlayerList.getRowLeft();
        int var2 = this.socialInteractionsPlayerList.getRowRight();
        int var3 = this.font.width(BLOCKING_HINT) + 40;
        int var4 = 64 + 16 * this.backgroundUnits();
        int var5 = (this.width - var3) / 2;
        this.allButton = this.addButton(new Button(var1, 45, var0, 20, TAB_ALL, param0 -> this.showPage(SocialInteractionsScreen.Page.ALL)));
        this.hiddenButton = this.addButton(
            new Button((var1 + var2 - var0) / 2 + 1, 45, var0, 20, TAB_HIDDEN, param0 -> this.showPage(SocialInteractionsScreen.Page.HIDDEN))
        );
        this.blockedButton = this.addButton(
            new Button(var2 - var0 + 1, 45, var0, 20, TAB_BLOCKED, param0 -> this.showPage(SocialInteractionsScreen.Page.BLOCKED))
        );
        this.blockingHintButton = this.addButton(
            new Button(var5, var4, var3, 20, BLOCKING_HINT, param0 -> this.minecraft.setScreen(new ConfirmLinkScreen(param0x -> {
                    if (param0x) {
                        Util.getPlatform().openUri("https://aka.ms/javablocking");
                    }
    
                    this.minecraft.setScreen(this);
                }, "https://aka.ms/javablocking", true)))
        );
        String var6 = this.searchBox != null ? this.searchBox.getValue() : "";
        this.searchBox = new EditBox(this.font, this.marginX() + 28, 78, 196, 16, SEARCH_HINT) {
            @Override
            protected MutableComponent createNarrationMessage() {
                return !SocialInteractionsScreen.this.searchBox.getValue().isEmpty() && SocialInteractionsScreen.this.socialInteractionsPlayerList.isEmpty()
                    ? super.createNarrationMessage().append(", ").append(SocialInteractionsScreen.EMPTY_SEARCH)
                    : super.createNarrationMessage();
            }
        };
        this.searchBox.setMaxLength(16);
        this.searchBox.setBordered(false);
        this.searchBox.setVisible(true);
        this.searchBox.setTextColor(16777215);
        this.searchBox.setValue(var6);
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
        this.blockedButton.setMessage(TAB_BLOCKED);
        Collection<UUID> var0;
        switch(param0) {
            case ALL:
                this.allButton.setMessage(TAB_ALL_SELECTED);
                var0 = this.minecraft.player.connection.getOnlinePlayerIds();
                break;
            case HIDDEN:
                this.hiddenButton.setMessage(TAB_HIDDEN_SELECTED);
                var0 = this.minecraft.getPlayerSocialManager().getHiddenPlayers();
                break;
            case BLOCKED:
                this.blockedButton.setMessage(TAB_BLOCKED_SELECTED);
                PlayerSocialManager var2 = this.minecraft.getPlayerSocialManager();
                var0 = this.minecraft.player.connection.getOnlinePlayerIds().stream().filter(var2::isBlocked).collect(Collectors.toSet());
                break;
            default:
                var0 = ImmutableList.of();
        }

        this.page = param0;
        this.socialInteractionsPlayerList.updatePlayerList(var0, this.socialInteractionsPlayerList.getScrollAmount());
        if (!this.searchBox.getValue().isEmpty() && this.socialInteractionsPlayerList.isEmpty() && !this.searchBox.isFocused()) {
            NarratorChatListener.INSTANCE.sayNow(EMPTY_SEARCH.getString());
        } else if (var0.isEmpty()) {
            if (param0 == SocialInteractionsScreen.Page.HIDDEN) {
                NarratorChatListener.INSTANCE.sayNow(EMPTY_HIDDEN.getString());
            } else if (param0 == SocialInteractionsScreen.Page.BLOCKED) {
                NarratorChatListener.INSTANCE.sayNow(EMPTY_BLOCKED.getString());
            }
        }

    }

    @Override
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
    }

    @Override
    public void renderBackground(PoseStack param0) {
        int var0 = this.marginX() + 3;
        super.renderBackground(param0);
        RenderSystem.setShaderTexture(0, SOCIAL_INTERACTIONS_LOCATION);
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
        } else if (!this.searchBox.getValue().isEmpty()) {
            drawCenteredString(param0, this.minecraft.font, EMPTY_SEARCH, this.width / 2, (78 + this.listEnd()) / 2, -1);
        } else {
            switch(this.page) {
                case HIDDEN:
                    drawCenteredString(param0, this.minecraft.font, EMPTY_HIDDEN, this.width / 2, (78 + this.listEnd()) / 2, -1);
                    break;
                case BLOCKED:
                    drawCenteredString(param0, this.minecraft.font, EMPTY_BLOCKED, this.width / 2, (78 + this.listEnd()) / 2, -1);
            }
        }

        if (!this.searchBox.isFocused() && this.searchBox.getValue().isEmpty()) {
            drawString(param0, this.minecraft.font, SEARCH_HINT, this.searchBox.x, this.searchBox.y, -1);
        } else {
            this.searchBox.render(param0, param1, param2, param3);
        }

        this.blockingHintButton.visible = this.page == SocialInteractionsScreen.Page.BLOCKED;
        super.render(param0, param1, param2, param3);
        if (this.postRenderRunnable != null) {
            this.postRenderRunnable.run();
        }

    }

    @Override
    public boolean mouseClicked(double param0, double param1, int param2) {
        if (this.searchBox.isFocused()) {
            this.searchBox.mouseClicked(param0, param1, param2);
        }

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
            if (param0.isLocalServer()) {
                var1 = param0.getSingleplayerServer().getMotd();
            } else if (var2 != null) {
                var1 = var2.name;
            }

            if (var0 > 1) {
                this.serverLabel = new TranslatableComponent("gui.socialInteractions.server_label.multiple", var1, var0);
            } else {
                this.serverLabel = new TranslatableComponent("gui.socialInteractions.server_label.single", var1, var0);
            }

            this.playerCount = var0;
        }

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
        HIDDEN,
        BLOCKED;
    }
}
