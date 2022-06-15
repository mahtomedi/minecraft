package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.realmsclient.RealmsMainScreen;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.achievement.StatsScreen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.social.SocialInteractionsScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PauseScreen extends Screen {
    private static final String URL_FEEDBACK_SNAPSHOT = "https://aka.ms/snapshotfeedback?ref=game";
    private static final String URL_FEEDBACK_RELEASE = "https://aka.ms/javafeedback?ref=game";
    private static final String URL_BUGS = "https://aka.ms/snapshotbugs?ref=game";
    private final boolean showPauseMenu;

    public PauseScreen(boolean param0) {
        super(param0 ? Component.translatable("menu.game") : Component.translatable("menu.paused"));
        this.showPauseMenu = param0;
    }

    @Override
    protected void init() {
        if (this.showPauseMenu) {
            this.createPauseMenu();
        }

    }

    private void createPauseMenu() {
        int var0 = -16;
        int var1 = 98;
        this.addRenderableWidget(new Button(this.width / 2 - 102, this.height / 4 + 24 + -16, 204, 20, Component.translatable("menu.returnToGame"), param0 -> {
            this.minecraft.setScreen(null);
            this.minecraft.mouseHandler.grabMouse();
        }));
        this.addRenderableWidget(
            new Button(
                this.width / 2 - 102,
                this.height / 4 + 48 + -16,
                98,
                20,
                Component.translatable("gui.advancements"),
                param0 -> this.minecraft.setScreen(new AdvancementsScreen(this.minecraft.player.connection.getAdvancements()))
            )
        );
        this.addRenderableWidget(
            new Button(
                this.width / 2 + 4,
                this.height / 4 + 48 + -16,
                98,
                20,
                Component.translatable("gui.stats"),
                param0 -> this.minecraft.setScreen(new StatsScreen(this, this.minecraft.player.getStats()))
            )
        );
        String var2 = SharedConstants.getCurrentVersion().isStable() ? "https://aka.ms/javafeedback?ref=game" : "https://aka.ms/snapshotfeedback?ref=game";
        this.addRenderableWidget(
            new Button(
                this.width / 2 - 102,
                this.height / 4 + 72 + -16,
                98,
                20,
                Component.translatable("menu.sendFeedback"),
                param1 -> this.minecraft.setScreen(new ConfirmLinkScreen(param1x -> {
                        if (param1x) {
                            Util.getPlatform().openUri(var2);
                        }
        
                        this.minecraft.setScreen(this);
                    }, var2, true))
            )
        );
        Button var3 = this.addRenderableWidget(
            new Button(
                this.width / 2 + 4,
                this.height / 4 + 72 + -16,
                98,
                20,
                Component.translatable("menu.reportBugs"),
                param0 -> this.minecraft.setScreen(new ConfirmLinkScreen(param0x -> {
                        if (param0x) {
                            Util.getPlatform().openUri("https://aka.ms/snapshotbugs?ref=game");
                        }
        
                        this.minecraft.setScreen(this);
                    }, "https://aka.ms/snapshotbugs?ref=game", true))
            )
        );
        var3.active = !SharedConstants.getCurrentVersion().getDataVersion().isSideSeries();
        this.addRenderableWidget(
            new Button(
                this.width / 2 - 102,
                this.height / 4 + 96 + -16,
                98,
                20,
                Component.translatable("menu.options"),
                param0 -> this.minecraft.setScreen(new OptionsScreen(this, this.minecraft.options))
            )
        );
        if (this.minecraft.hasSingleplayerServer() && !this.minecraft.getSingleplayerServer().isPublished()) {
            this.addRenderableWidget(
                new Button(
                    this.width / 2 + 4,
                    this.height / 4 + 96 + -16,
                    98,
                    20,
                    Component.translatable("menu.shareToLan"),
                    param0 -> this.minecraft.setScreen(new ShareToLanScreen(this))
                )
            );
        } else {
            this.addRenderableWidget(
                new Button(
                    this.width / 2 + 4,
                    this.height / 4 + 96 + -16,
                    98,
                    20,
                    Component.translatable("menu.playerReporting"),
                    param0 -> this.minecraft.setScreen(SocialInteractionsScreen.createWithWarning())
                )
            );
        }

        Component var4 = this.minecraft.isLocalServer() ? Component.translatable("menu.returnToMenu") : Component.translatable("menu.disconnect");
        this.addRenderableWidget(new Button(this.width / 2 - 102, this.height / 4 + 120 + -16, 204, 20, var4, param0 -> {
            boolean var0x = this.minecraft.isLocalServer();
            boolean var1x = this.minecraft.isConnectedToRealms();
            param0.active = false;
            this.minecraft.level.disconnect();
            if (var0x) {
                this.minecraft.clearLevel(new GenericDirtMessageScreen(Component.translatable("menu.savingLevel")));
            } else {
                this.minecraft.clearLevel();
            }

            TitleScreen var2x = new TitleScreen();
            if (var0x) {
                this.minecraft.setScreen(var2x);
            } else if (var1x) {
                this.minecraft.setScreen(new RealmsMainScreen(var2x));
            } else {
                this.minecraft.setScreen(new JoinMultiplayerScreen(var2x));
            }

        }));
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        if (this.showPauseMenu) {
            this.renderBackground(param0);
            drawCenteredString(param0, this.font, this.title, this.width / 2, 40, 16777215);
        } else {
            drawCenteredString(param0, this.font, this.title, this.width / 2, 10, 16777215);
        }

        super.render(param0, param1, param2, param3);
    }
}
