package net.minecraft.client.gui.screens;

import com.google.common.util.concurrent.Runnables;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.renderer.CubeMap;
import net.minecraft.client.renderer.PanoramaRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.realms.RealmsBridge;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TitleScreen extends Screen {
    public static final CubeMap CUBE_MAP = new CubeMap(new ResourceLocation("textures/gui/title/background/panorama"));
    private static final ResourceLocation PANORAMA_OVERLAY = new ResourceLocation("textures/gui/title/background/panorama_overlay.png");
    private static final ResourceLocation ACCESSIBILITY_TEXTURE = new ResourceLocation("textures/gui/accessibility.png");
    private final boolean minceraftEasterEgg;
    @Nullable
    private String splash;
    private Button resetDemoButton;
    @Nullable
    private TitleScreen.WarningMessageWidget warningMessage;
    private static final ResourceLocation MINECRAFT_LOGO = new ResourceLocation("textures/gui/title/minecraft.png");
    private static final ResourceLocation MINECRAFT_EDITION = new ResourceLocation("textures/gui/title/edition.png");
    private boolean realmsNotificationsInitialized;
    private Screen realmsNotificationsScreen;
    private int copyrightWidth;
    private int copyrightX;
    private final PanoramaRenderer panorama = new PanoramaRenderer(CUBE_MAP);
    private final boolean fading;
    private long fadeInStart;

    public TitleScreen() {
        this(false);
    }

    public TitleScreen(boolean param0) {
        super(new TranslatableComponent("narrator.screen.title"));
        this.fading = param0;
        this.minceraftEasterEgg = (double)new Random().nextFloat() < 1.0E-4;
        if (!GLX.supportsOpenGL2() && !GLX.isNextGen()) {
            this.warningMessage = new TitleScreen.WarningMessageWidget(
                new TranslatableComponent("title.oldgl.eol.line1").withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD),
                new TranslatableComponent("title.oldgl.eol.line2").withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD),
                "https://help.mojang.com/customer/portal/articles/325948?ref=game"
            );
        }

    }

    private boolean realmsNotificationsEnabled() {
        return this.minecraft.options.realmsNotifications && this.realmsNotificationsScreen != null;
    }

    @Override
    public void tick() {
        if (this.realmsNotificationsEnabled()) {
            this.realmsNotificationsScreen.tick();
        }

    }

    public static CompletableFuture<Void> preloadResources(TextureManager param0, Executor param1) {
        return CompletableFuture.allOf(
            param0.preload(MINECRAFT_LOGO, param1),
            param0.preload(MINECRAFT_EDITION, param1),
            param0.preload(PANORAMA_OVERLAY, param1),
            CUBE_MAP.preload(param0, param1)
        );
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    protected void init() {
        if (this.splash == null) {
            this.splash = this.minecraft.getSplashManager().getSplash();
        }

        this.copyrightWidth = this.font.width("Copyright Mojang AB. Do not distribute!");
        this.copyrightX = this.width - this.copyrightWidth - 2;
        int var0 = 24;
        int var1 = this.height / 4 + 48;
        if (this.minecraft.isDemo()) {
            this.createDemoMenuOptions(var1, 24);
        } else {
            this.createNormalMenuOptions(var1, 24);
        }

        this.addButton(
            new ImageButton(
                this.width / 2 - 124,
                var1 + 72 + 12,
                20,
                20,
                0,
                106,
                20,
                Button.WIDGETS_LOCATION,
                256,
                256,
                param0 -> this.minecraft.setScreen(new LanguageSelectScreen(this, this.minecraft.options, this.minecraft.getLanguageManager())),
                I18n.get("narrator.button.language")
            )
        );
        this.addButton(
            new Button(
                this.width / 2 - 100,
                var1 + 72 + 12,
                98,
                20,
                I18n.get("menu.options"),
                param0 -> this.minecraft.setScreen(new OptionsScreen(this, this.minecraft.options))
            )
        );
        this.addButton(new Button(this.width / 2 + 2, var1 + 72 + 12, 98, 20, I18n.get("menu.quit"), param0 -> this.minecraft.stop()));
        this.addButton(
            new ImageButton(
                this.width / 2 + 104,
                var1 + 72 + 12,
                20,
                20,
                0,
                0,
                20,
                ACCESSIBILITY_TEXTURE,
                32,
                64,
                param0 -> this.minecraft.setScreen(new AccessibilityOptionsScreen(this, this.minecraft.options)),
                I18n.get("narrator.button.accessibility")
            )
        );
        if (this.warningMessage != null) {
            this.warningMessage.updatePosition(var1);
        }

        this.minecraft.setConnectedToRealms(false);
        if (this.minecraft.options.realmsNotifications && !this.realmsNotificationsInitialized) {
            RealmsBridge var2 = new RealmsBridge();
            this.realmsNotificationsScreen = var2.getNotificationScreen(this);
            this.realmsNotificationsInitialized = true;
        }

        if (this.realmsNotificationsEnabled()) {
            this.realmsNotificationsScreen.init(this.minecraft, this.width, this.height);
        }

    }

    private void createNormalMenuOptions(int param0, int param1) {
        this.addButton(
            new Button(this.width / 2 - 100, param0, 200, 20, I18n.get("menu.singleplayer"), param0x -> this.minecraft.setScreen(new SelectWorldScreen(this)))
        );
        this.addButton(
            new Button(
                this.width / 2 - 100,
                param0 + param1 * 1,
                200,
                20,
                I18n.get("menu.multiplayer"),
                param0x -> this.minecraft.setScreen(new JoinMultiplayerScreen(this))
            )
        );
        this.addButton(new Button(this.width / 2 - 100, param0 + param1 * 2, 200, 20, I18n.get("menu.online"), param0x -> this.realmsButtonClicked()));
    }

    private void createDemoMenuOptions(int param0, int param1) {
        this.addButton(
            new Button(
                this.width / 2 - 100,
                param0,
                200,
                20,
                I18n.get("menu.playdemo"),
                param0x -> this.minecraft.selectLevel("Demo_World", "Demo_World", MinecraftServer.DEMO_SETTINGS)
            )
        );
        this.resetDemoButton = this.addButton(
            new Button(
                this.width / 2 - 100,
                param0 + param1 * 1,
                200,
                20,
                I18n.get("menu.resetdemo"),
                param0x -> {
                    LevelStorageSource var0x = this.minecraft.getLevelSource();
                    LevelData var1x = var0x.getDataTagFor("Demo_World");
                    if (var1x != null) {
                        this.minecraft
                            .setScreen(
                                new ConfirmScreen(
                                    this::confirmDemo,
                                    new TranslatableComponent("selectWorld.deleteQuestion"),
                                    new TranslatableComponent("selectWorld.deleteWarning", var1x.getLevelName()),
                                    I18n.get("selectWorld.deleteButton"),
                                    I18n.get("gui.cancel")
                                )
                            );
                    }
        
                }
            )
        );
        LevelStorageSource var0 = this.minecraft.getLevelSource();
        LevelData var1 = var0.getDataTagFor("Demo_World");
        if (var1 == null) {
            this.resetDemoButton.active = false;
        }

    }

    private void realmsButtonClicked() {
        RealmsBridge var0 = new RealmsBridge();
        var0.switchToRealms(this);
    }

    @Override
    public void render(int param0, int param1, float param2) {
        if (this.fadeInStart == 0L && this.fading) {
            this.fadeInStart = Util.getMillis();
        }

        float var0 = this.fading ? (float)(Util.getMillis() - this.fadeInStart) / 1000.0F : 1.0F;
        fill(0, 0, this.width, this.height, -1);
        this.panorama.render(param2, Mth.clamp(var0, 0.0F, 1.0F));
        int var1 = 274;
        int var2 = this.width / 2 - 137;
        int var3 = 30;
        this.minecraft.getTextureManager().bind(PANORAMA_OVERLAY);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, this.fading ? (float)Mth.ceil(Mth.clamp(var0, 0.0F, 1.0F)) : 1.0F);
        blit(0, 0, this.width, this.height, 0.0F, 0.0F, 16, 128, 16, 128);
        float var4 = this.fading ? Mth.clamp(var0 - 1.0F, 0.0F, 1.0F) : 1.0F;
        int var5 = Mth.ceil(var4 * 255.0F) << 24;
        if ((var5 & -67108864) != 0) {
            this.minecraft.getTextureManager().bind(MINECRAFT_LOGO);
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, var4);
            if (this.minceraftEasterEgg) {
                this.blit(var2 + 0, 30, 0, 0, 99, 44);
                this.blit(var2 + 99, 30, 129, 0, 27, 44);
                this.blit(var2 + 99 + 26, 30, 126, 0, 3, 44);
                this.blit(var2 + 99 + 26 + 3, 30, 99, 0, 26, 44);
                this.blit(var2 + 155, 30, 0, 45, 155, 44);
            } else {
                this.blit(var2 + 0, 30, 0, 0, 155, 44);
                this.blit(var2 + 155, 30, 0, 45, 155, 44);
            }

            this.minecraft.getTextureManager().bind(MINECRAFT_EDITION);
            blit(var2 + 88, 67, 0.0F, 0.0F, 98, 14, 128, 16);
            if (this.splash != null) {
                GlStateManager.pushMatrix();
                GlStateManager.translatef((float)(this.width / 2 + 90), 70.0F, 0.0F);
                GlStateManager.rotatef(-20.0F, 0.0F, 0.0F, 1.0F);
                float var6 = 1.8F - Mth.abs(Mth.sin((float)(Util.getMillis() % 1000L) / 1000.0F * (float) (Math.PI * 2)) * 0.1F);
                var6 = var6 * 100.0F / (float)(this.font.width(this.splash) + 32);
                GlStateManager.scalef(var6, var6, var6);
                this.drawCenteredString(this.font, this.splash, 0, -8, 16776960 | var5);
                GlStateManager.popMatrix();
            }

            String var7 = "Minecraft " + SharedConstants.getCurrentVersion().getName();
            if (this.minecraft.isDemo()) {
                var7 = var7 + " Demo";
            } else {
                var7 = var7 + ("release".equalsIgnoreCase(this.minecraft.getVersionType()) ? "" : "/" + this.minecraft.getVersionType());
            }

            this.drawString(this.font, var7, 2, this.height - 10, 16777215 | var5);
            this.drawString(this.font, "Copyright Mojang AB. Do not distribute!", this.copyrightX, this.height - 10, 16777215 | var5);
            if (param0 > this.copyrightX && param0 < this.copyrightX + this.copyrightWidth && param1 > this.height - 10 && param1 < this.height) {
                fill(this.copyrightX, this.height - 1, this.copyrightX + this.copyrightWidth, this.height, 16777215 | var5);
            }

            if (this.warningMessage != null) {
                this.warningMessage.render(var5);
            }

            for(AbstractWidget var8 : this.buttons) {
                var8.setAlpha(var4);
            }

            super.render(param0, param1, param2);
            if (this.realmsNotificationsEnabled() && var4 >= 1.0F) {
                this.realmsNotificationsScreen.render(param0, param1, param2);
            }

        }
    }

    @Override
    public boolean mouseClicked(double param0, double param1, int param2) {
        if (super.mouseClicked(param0, param1, param2)) {
            return true;
        } else if (this.warningMessage != null && this.warningMessage.mouseClicked(param0, param1)) {
            return true;
        } else if (this.realmsNotificationsEnabled() && this.realmsNotificationsScreen.mouseClicked(param0, param1, param2)) {
            return true;
        } else {
            if (param0 > (double)this.copyrightX
                && param0 < (double)(this.copyrightX + this.copyrightWidth)
                && param1 > (double)(this.height - 10)
                && param1 < (double)this.height) {
                this.minecraft.setScreen(new WinScreen(false, Runnables.doNothing()));
            }

            return false;
        }
    }

    @Override
    public void removed() {
        if (this.realmsNotificationsScreen != null) {
            this.realmsNotificationsScreen.removed();
        }

    }

    private void confirmDemo(boolean param0) {
        if (param0) {
            LevelStorageSource var0 = this.minecraft.getLevelSource();
            var0.deleteLevel("Demo_World");
        }

        this.minecraft.setScreen(this);
    }

    @OnlyIn(Dist.CLIENT)
    class WarningMessageWidget {
        private int warningClickWidth;
        private int warningx0;
        private int warningy0;
        private int warningx1;
        private int warningy1;
        private final Component warningMessageTop;
        private final Component warningMessageBottom;
        private final String warningMessageUrl;

        public WarningMessageWidget(Component param0, Component param1, String param2) {
            this.warningMessageTop = param0;
            this.warningMessageBottom = param1;
            this.warningMessageUrl = param2;
        }

        public void updatePosition(int param0) {
            int var0 = TitleScreen.this.font.width(this.warningMessageTop.getString());
            this.warningClickWidth = TitleScreen.this.font.width(this.warningMessageBottom.getString());
            int var1 = Math.max(var0, this.warningClickWidth);
            this.warningx0 = (TitleScreen.this.width - var1) / 2;
            this.warningy0 = param0 - 24;
            this.warningx1 = this.warningx0 + var1;
            this.warningy1 = this.warningy0 + 24;
        }

        public void render(int param0) {
            GuiComponent.fill(this.warningx0 - 2, this.warningy0 - 2, this.warningx1 + 2, this.warningy1 - 1, 1428160512);
            TitleScreen.this.drawString(TitleScreen.this.font, this.warningMessageTop.getColoredString(), this.warningx0, this.warningy0, 16777215 | param0);
            TitleScreen.this.drawString(
                TitleScreen.this.font,
                this.warningMessageBottom.getColoredString(),
                (TitleScreen.this.width - this.warningClickWidth) / 2,
                this.warningy0 + 12,
                16777215 | param0
            );
        }

        public boolean mouseClicked(double param0, double param1) {
            if (!StringUtil.isNullOrEmpty(this.warningMessageUrl)
                && param0 >= (double)this.warningx0
                && param0 <= (double)this.warningx1
                && param1 >= (double)this.warningy0
                && param1 <= (double)this.warningy1) {
                TitleScreen.this.minecraft.setScreen(new ConfirmLinkScreen(param0x -> {
                    if (param0x) {
                        Util.getPlatform().openUri(this.warningMessageUrl);
                    }

                    TitleScreen.this.minecraft.setScreen(TitleScreen.this);
                }, this.warningMessageUrl, true));
                return true;
            } else {
                return false;
            }
        }
    }
}
