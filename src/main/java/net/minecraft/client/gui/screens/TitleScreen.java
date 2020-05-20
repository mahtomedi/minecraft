package net.minecraft.client.gui.screens;

import com.google.common.util.concurrent.Runnables;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.SafetyScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.renderer.CubeMap;
import net.minecraft.client.renderer.PanoramaRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.realms.RealmsBridge;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class TitleScreen extends Screen {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final CubeMap CUBE_MAP = new CubeMap(new ResourceLocation("textures/gui/title/background/panorama"));
    private static final ResourceLocation PANORAMA_OVERLAY = new ResourceLocation("textures/gui/title/background/panorama_overlay.png");
    private static final ResourceLocation ACCESSIBILITY_TEXTURE = new ResourceLocation("textures/gui/accessibility.png");
    private final boolean minceraftEasterEgg;
    @Nullable
    private String splash;
    private Button resetDemoButton;
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
                new TranslatableComponent("narrator.button.language")
            )
        );
        this.addButton(
            new Button(
                this.width / 2 - 100,
                var1 + 72 + 12,
                98,
                20,
                new TranslatableComponent("menu.options"),
                param0 -> this.minecraft.setScreen(new OptionsScreen(this, this.minecraft.options))
            )
        );
        this.addButton(new Button(this.width / 2 + 2, var1 + 72 + 12, 98, 20, new TranslatableComponent("menu.quit"), param0 -> this.minecraft.stop()));
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
                new TranslatableComponent("narrator.button.accessibility")
            )
        );
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
            new Button(
                this.width / 2 - 100,
                param0,
                200,
                20,
                new TranslatableComponent("menu.singleplayer"),
                param0x -> this.minecraft.setScreen(new SelectWorldScreen(this))
            )
        );
        boolean var0 = this.minecraft.allowsMultiplayer();
        Button.OnTooltip var1 = var0
            ? Button.NO_TOOLTIP
            : (param0x, param1x, param2, param3) -> {
                if (!param0x.active) {
                    this.renderTooltip(
                        param1x,
                        this.minecraft.font.split(new TranslatableComponent("title.multiplayer.disabled"), Math.max(this.width / 2 - 43, 170)),
                        param2,
                        param3
                    );
                }
    
            };
        this.addButton(new Button(this.width / 2 - 100, param0 + param1 * 1, 200, 20, new TranslatableComponent("menu.multiplayer"), param0x -> {
            Screen var0x = (Screen)(this.minecraft.options.skipMultiplayerWarning ? new JoinMultiplayerScreen(this) : new SafetyScreen(this));
            this.minecraft.setScreen(var0x);
        }, var1)).active = var0;
        this.addButton(
                new Button(
                    this.width / 2 - 100, param0 + param1 * 2, 200, 20, new TranslatableComponent("menu.online"), param0x -> this.realmsButtonClicked(), var1
                )
            )
            .active = var0;
    }

    private void createDemoMenuOptions(int param0, int param1) {
        this.addButton(
            new Button(
                this.width / 2 - 100,
                param0,
                200,
                20,
                new TranslatableComponent("menu.playdemo"),
                param0x -> this.minecraft.selectLevel("Demo_World", MinecraftServer.DEMO_SETTINGS)
            )
        );
        this.resetDemoButton = this.addButton(
            new Button(
                this.width / 2 - 100,
                param0 + param1 * 1,
                200,
                20,
                new TranslatableComponent("menu.resetdemo"),
                param0x -> {
                    LevelStorageSource var0x = this.minecraft.getLevelSource();
        
                    try (LevelStorageSource.LevelStorageAccess var3x = var0x.createAccess("Demo_World")) {
                        WorldData var2x = var3x.getDataTag();
                        if (var2x != null) {
                            this.minecraft
                                .setScreen(
                                    new ConfirmScreen(
                                        this::confirmDemo,
                                        new TranslatableComponent("selectWorld.deleteQuestion"),
                                        new TranslatableComponent("selectWorld.deleteWarning", var2x.getLevelName()),
                                        new TranslatableComponent("selectWorld.deleteButton"),
                                        CommonComponents.GUI_CANCEL
                                    )
                                );
                        }
                    } catch (IOException var16x) {
                        SystemToast.onWorldAccessFailure(this.minecraft, "Demo_World");
                        LOGGER.warn("Failed to access demo world", (Throwable)var16x);
                    }
        
                }
            )
        );

        try (LevelStorageSource.LevelStorageAccess var0 = this.minecraft.getLevelSource().createAccess("Demo_World")) {
            WorldData var1 = var0.getDataTag();
            if (var1 == null) {
                this.resetDemoButton.active = false;
            }
        } catch (IOException var16) {
            SystemToast.onWorldAccessFailure(this.minecraft, "Demo_World");
            LOGGER.warn("Failed to read demo world data", (Throwable)var16);
        }

    }

    private void realmsButtonClicked() {
        RealmsBridge var0 = new RealmsBridge();
        var0.switchToRealms(this);
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        if (this.fadeInStart == 0L && this.fading) {
            this.fadeInStart = Util.getMillis();
        }

        float var0 = this.fading ? (float)(Util.getMillis() - this.fadeInStart) / 1000.0F : 1.0F;
        fill(param0, 0, 0, this.width, this.height, -1);
        this.panorama.render(param3, Mth.clamp(var0, 0.0F, 1.0F));
        int var1 = 274;
        int var2 = this.width / 2 - 137;
        int var3 = 30;
        this.minecraft.getTextureManager().bind(PANORAMA_OVERLAY);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.fading ? (float)Mth.ceil(Mth.clamp(var0, 0.0F, 1.0F)) : 1.0F);
        blit(param0, 0, 0, this.width, this.height, 0.0F, 0.0F, 16, 128, 16, 128);
        float var4 = this.fading ? Mth.clamp(var0 - 1.0F, 0.0F, 1.0F) : 1.0F;
        int var5 = Mth.ceil(var4 * 255.0F) << 24;
        if ((var5 & -67108864) != 0) {
            this.minecraft.getTextureManager().bind(MINECRAFT_LOGO);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, var4);
            if (this.minceraftEasterEgg) {
                this.blitOutline(var2, 30, (param1x, param2x) -> {
                    this.blit(param0, param1x + 0, param2x, 0, 0, 99, 44);
                    this.blit(param0, param1x + 99, param2x, 129, 0, 27, 44);
                    this.blit(param0, param1x + 99 + 26, param2x, 126, 0, 3, 44);
                    this.blit(param0, param1x + 99 + 26 + 3, param2x, 99, 0, 26, 44);
                    this.blit(param0, param1x + 155, param2x, 0, 45, 155, 44);
                });
            } else {
                this.blitOutline(var2, 30, (param1x, param2x) -> {
                    this.blit(param0, param1x + 0, param2x, 0, 0, 155, 44);
                    this.blit(param0, param1x + 155, param2x, 0, 45, 155, 44);
                });
            }

            this.minecraft.getTextureManager().bind(MINECRAFT_EDITION);
            blit(param0, var2 + 88, 67, 0.0F, 0.0F, 98, 14, 128, 16);
            if (this.splash != null) {
                RenderSystem.pushMatrix();
                RenderSystem.translatef((float)(this.width / 2 + 90), 70.0F, 0.0F);
                RenderSystem.rotatef(-20.0F, 0.0F, 0.0F, 1.0F);
                float var6 = 1.8F - Mth.abs(Mth.sin((float)(Util.getMillis() % 1000L) / 1000.0F * (float) (Math.PI * 2)) * 0.1F);
                var6 = var6 * 100.0F / (float)(this.font.width(this.splash) + 32);
                RenderSystem.scalef(var6, var6, var6);
                this.drawCenteredString(param0, this.font, this.splash, 0, -8, 16776960 | var5);
                RenderSystem.popMatrix();
            }

            String var7 = "Minecraft " + SharedConstants.getCurrentVersion().getName();
            if (this.minecraft.isDemo()) {
                var7 = var7 + " Demo";
            } else {
                var7 = var7 + ("release".equalsIgnoreCase(this.minecraft.getVersionType()) ? "" : "/" + this.minecraft.getVersionType());
            }

            if (this.minecraft.isProbablyModded()) {
                var7 = var7 + I18n.get("menu.modded");
            }

            this.drawString(param0, this.font, var7, 2, this.height - 10, 16777215 | var5);
            this.drawString(param0, this.font, "Copyright Mojang AB. Do not distribute!", this.copyrightX, this.height - 10, 16777215 | var5);
            if (param1 > this.copyrightX && param1 < this.copyrightX + this.copyrightWidth && param2 > this.height - 10 && param2 < this.height) {
                fill(param0, this.copyrightX, this.height - 1, this.copyrightX + this.copyrightWidth, this.height, 16777215 | var5);
            }

            for(AbstractWidget var8 : this.buttons) {
                var8.setAlpha(var4);
            }

            super.render(param0, param1, param2, param3);
            if (this.realmsNotificationsEnabled() && var4 >= 1.0F) {
                this.realmsNotificationsScreen.render(param0, param1, param2, param3);
            }

        }
    }

    private void blitOutline(int param0, int param1, BiConsumer<Integer, Integer> param2) {
        param2.accept(param0 + 1, param1);
        param2.accept(param0 - 1, param1);
        param2.accept(param0, param1 + 1);
        param2.accept(param0, param1 - 1);
        param2.accept(param0, param1);
    }

    @Override
    public boolean mouseClicked(double param0, double param1, int param2) {
        if (super.mouseClicked(param0, param1, param2)) {
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
            try (LevelStorageSource.LevelStorageAccess var0 = this.minecraft.getLevelSource().createAccess("Demo_World")) {
                var0.deleteLevel();
            } catch (IOException var15) {
                SystemToast.onWorldDeleteFailure(this.minecraft, "Demo_World");
                LOGGER.warn("Failed to delete demo world", (Throwable)var15);
            }
        }

        this.minecraft.setScreen(this);
    }
}
