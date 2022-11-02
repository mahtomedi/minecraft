package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.realmsclient.RealmsMainScreen;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CenteredStringWidget;
import net.minecraft.client.gui.components.FrameWidget;
import net.minecraft.client.gui.components.GridWidget;
import net.minecraft.client.gui.components.LayoutSettings;
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
    private Button disconnectButton;

    public PauseScreen(boolean param0) {
        super(param0 ? Component.translatable("menu.game") : Component.translatable("menu.paused"));
        this.showPauseMenu = param0;
    }

    @Override
    protected void init() {
        if (this.showPauseMenu) {
            this.createPauseMenu();
        } else {
            FrameWidget var0 = this.addRenderableWidget(FrameWidget.withMinDimensions(this.width, this.height));
            var0.defaultChildLayoutSetting().alignHorizontallyCenter().alignVerticallyTop().paddingTop(10);
            var0.addChild(new CenteredStringWidget(this.title, this.font));
            var0.pack();
        }

    }

    private void createPauseMenu() {
        int var0 = 204;
        int var1 = 98;
        GridWidget var2 = new GridWidget();
        var2.defaultCellSetting().padding(0, 2).alignHorizontallyCenter();
        LayoutSettings var3 = var2.newCellSettings().alignHorizontallyLeft();
        LayoutSettings var4 = var2.newCellSettings().alignHorizontallyRight();
        int var5 = 0;
        var2.addChild(new CenteredStringWidget(this.title, this.minecraft.font), var5, 0, 1, 2, var2.newCellSettings().paddingBottom(5));
        var2.addChild(Button.builder(Component.translatable("menu.returnToGame"), param0 -> {
            this.minecraft.setScreen(null);
            this.minecraft.mouseHandler.grabMouse();
        }).width(204).build(), ++var5, 0, 1, 2);
        var2.addChild(
            Button.builder(
                    Component.translatable("gui.advancements"),
                    param0 -> this.minecraft.setScreen(new AdvancementsScreen(this.minecraft.player.connection.getAdvancements()))
                )
                .width(98)
                .build(),
            ++var5,
            0,
            var3
        );
        var2.addChild(
            Button.builder(Component.translatable("gui.stats"), param0 -> this.minecraft.setScreen(new StatsScreen(this, this.minecraft.player.getStats())))
                .width(98)
                .build(),
            var5,
            1,
            var4
        );
        ++var5;
        String var6 = SharedConstants.getCurrentVersion().isStable() ? "https://aka.ms/javafeedback?ref=game" : "https://aka.ms/snapshotfeedback?ref=game";
        var2.addChild(Button.builder(Component.translatable("menu.sendFeedback"), param1 -> this.minecraft.setScreen(new ConfirmLinkScreen(param1x -> {
                if (param1x) {
                    Util.getPlatform().openUri(var6);
                }

                this.minecraft.setScreen(this);
            }, var6, true))).width(98).build(), var5, 0, var3);
        Button var7 = var2.addChild(
            Button.builder(Component.translatable("menu.reportBugs"), param0 -> this.minecraft.setScreen(new ConfirmLinkScreen(param0x -> {
                    if (param0x) {
                        Util.getPlatform().openUri("https://aka.ms/snapshotbugs?ref=game");
                    }
    
                    this.minecraft.setScreen(this);
                }, "https://aka.ms/snapshotbugs?ref=game", true))).width(98).build(), var5, 1, var4
        );
        var7.active = !SharedConstants.getCurrentVersion().getDataVersion().isSideSeries();
        var2.addChild(
            Button.builder(Component.translatable("menu.options"), param0 -> this.minecraft.setScreen(new OptionsScreen(this, this.minecraft.options)))
                .width(98)
                .build(),
            ++var5,
            0,
            var3
        );
        if (this.minecraft.hasSingleplayerServer() && !this.minecraft.getSingleplayerServer().isPublished()) {
            var2.addChild(
                Button.builder(Component.translatable("menu.shareToLan"), param0 -> this.minecraft.setScreen(new ShareToLanScreen(this))).width(98).build(),
                var5,
                1,
                var4
            );
        } else {
            var2.addChild(
                Button.builder(Component.translatable("menu.playerReporting"), param0 -> this.minecraft.setScreen(new SocialInteractionsScreen()))
                    .width(98)
                    .build(),
                var5,
                1,
                var4
            );
        }

        ++var5;
        Component var8 = this.minecraft.isLocalServer() ? Component.translatable("menu.returnToMenu") : Component.translatable("menu.disconnect");
        this.disconnectButton = var2.addChild(Button.builder(var8, param0 -> {
            if (this.minecraft.getReportingContext().draftReportHandled(this.minecraft, this, true)) {
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
            }

        }).width(204).build(), var5, 0, 1, 2);
        var2.pack();
        FrameWidget.centerInRectangle(var2, 0, 0, this.width, this.height);
        this.addRenderableWidget(var2);
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        if (this.showPauseMenu) {
            this.renderBackground(param0);
        }

        super.render(param0, param1, param2, param3);
        if (this.showPauseMenu && this.minecraft != null && this.minecraft.getReportingContext().hasDraftReport()) {
            RenderSystem.setShaderTexture(0, AbstractWidget.WIDGETS_LOCATION);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            this.blit(param0, this.disconnectButton.getX() + this.disconnectButton.getWidth() - 17, this.disconnectButton.getY() + 3, 182, 24, 15, 15);
        }

    }
}
