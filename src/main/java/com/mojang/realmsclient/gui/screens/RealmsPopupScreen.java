package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.FittingMultiLineTextWidget;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsPopupScreen extends RealmsScreen {
    private static final Component POPUP_TEXT = Component.translatable("mco.selectServer.popup");
    private static final Component CLOSE_TEXT = Component.translatable("mco.selectServer.close");
    private static final ResourceLocation BACKGROUND_SPRITE = new ResourceLocation("popup/background");
    private static final ResourceLocation TRIAL_AVAILABLE_SPRITE = new ResourceLocation("icon/trial_available");
    private static final WidgetSprites CROSS_BUTTON_SPRITES = new WidgetSprites(
        new ResourceLocation("widget/cross_button"), new ResourceLocation("widget/cross_button_highlighted")
    );
    private static final int BG_TEXTURE_WIDTH = 236;
    private static final int BG_TEXTURE_HEIGHT = 34;
    private static final int BG_BORDER_SIZE = 6;
    private static final int IMAGE_WIDTH = 195;
    private static final int IMAGE_HEIGHT = 152;
    private static final int BUTTON_SPACING = 4;
    private static final int PADDING = 10;
    private static final int WIDTH = 320;
    private static final int HEIGHT = 172;
    private static final int TEXT_WIDTH = 100;
    private static final int BUTTON_WIDTH = 99;
    private static final int CAROUSEL_SWITCH_INTERVAL = 100;
    private static List<ResourceLocation> carouselImages = List.of();
    private final Screen backgroundScreen;
    private final boolean trialAvailable;
    @Nullable
    private Button createTrialButton;
    private int carouselIndex;
    private int carouselTick;

    public RealmsPopupScreen(Screen param0, boolean param1) {
        super(POPUP_TEXT);
        this.backgroundScreen = param0;
        this.trialAvailable = param1;
    }

    public static void updateCarouselImages(ResourceManager param0) {
        Collection<ResourceLocation> var0 = param0.listResources("textures/gui/images", param0x -> param0x.getPath().endsWith(".png")).keySet();
        carouselImages = var0.stream().filter(param0x -> param0x.getNamespace().equals("realms")).toList();
    }

    @Override
    protected void init() {
        this.backgroundScreen.resize(this.minecraft, this.width, this.height);
        if (this.trialAvailable) {
            this.createTrialButton = this.addRenderableWidget(
                Button.builder(Component.translatable("mco.selectServer.trial"), param0 -> this.minecraft.setScreen(new ConfirmLinkScreen(param0x -> {
                        if (param0x) {
                            Util.getPlatform().openUri("https://aka.ms/startjavarealmstrial");
                        }
    
                        this.minecraft.setScreen(this);
                    }, "https://aka.ms/startjavarealmstrial", true))).bounds(this.right() - 10 - 99, this.bottom() - 10 - 4 - 40, 99, 20).build()
            );
        }

        this.setFocused(
            this.addRenderableWidget(
                Button.builder(Component.translatable("mco.selectServer.buy"), param0 -> this.minecraft.setScreen(new ConfirmLinkScreen(param0x -> {
                        if (param0x) {
                            Util.getPlatform().openUri("https://aka.ms/BuyJavaRealms");
                        }
        
                        this.minecraft.setScreen(this);
                    }, "https://aka.ms/BuyJavaRealms", true))).bounds(this.right() - 10 - 99, this.bottom() - 10 - 20, 99, 20).build()
            )
        );
        ImageButton var0 = this.addRenderableWidget(
            new ImageButton(this.left() + 4, this.top() + 4, 14, 14, CROSS_BUTTON_SPRITES, param0 -> this.onClose(), CLOSE_TEXT)
        );
        var0.setTooltip(Tooltip.create(CLOSE_TEXT));
        int var1 = 142 - (this.trialAvailable ? 40 : 20);
        FittingMultiLineTextWidget var2 = new FittingMultiLineTextWidget(this.right() - 10 - 100, this.top() + 10, 100, var1, POPUP_TEXT, this.font);
        if (var2.showingScrollBar()) {
            var2.setWidth(100 - var2.scrollbarWidth());
        }

        this.addRenderableWidget(var2);
    }

    @Override
    public void tick() {
        super.tick();
        if (++this.carouselTick > 100) {
            this.carouselTick = 0;
            this.carouselIndex = (this.carouselIndex + 1) % carouselImages.size();
        }

    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        super.render(param0, param1, param2, param3);
        if (this.createTrialButton != null) {
            renderDiamond(param0, this.createTrialButton);
        }

    }

    public static void renderDiamond(GuiGraphics param0, Button param1) {
        int var0 = 8;
        param0.pose().pushPose();
        param0.pose().translate(0.0F, 0.0F, 110.0F);
        param0.blitSprite(TRIAL_AVAILABLE_SPRITE, param1.getX() + param1.getWidth() - 8 - 4, param1.getY() + param1.getHeight() / 2 - 4, 8, 8);
        param0.pose().popPose();
    }

    @Override
    public void renderBackground(GuiGraphics param0, int param1, int param2, float param3) {
        this.backgroundScreen.render(param0, -1, -1, param3);
        param0.flush();
        RenderSystem.clear(256, Minecraft.ON_OSX);
        this.renderTransparentBackground(param0);
        param0.blitSprite(BACKGROUND_SPRITE, this.left(), this.top(), 320, 172);
        if (!carouselImages.isEmpty()) {
            param0.blit(carouselImages.get(this.carouselIndex), this.left() + 10, this.top() + 10, 0, 0.0F, 0.0F, 195, 152, 195, 152);
        }

    }

    private int left() {
        return (this.width - 320) / 2;
    }

    private int top() {
        return (this.height - 172) / 2;
    }

    private int right() {
        return this.left() + 320;
    }

    private int bottom() {
        return this.top() + 172;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.backgroundScreen);
    }
}
