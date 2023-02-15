package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.text2speech.Narrator;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.AccessibilityOnboardingTextWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CommonButtons;
import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.renderer.PanoramaRenderer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AccessibilityOnboardingScreen extends Screen {
    private static final Component ONBOARDING_NARRATOR_MESSAGE = Component.translatable("accessibility.onboarding.screen.narrator");
    private static final int PADDING = 4;
    private static final int TITLE_PADDING = 16;
    private final PanoramaRenderer panorama = new PanoramaRenderer(TitleScreen.CUBE_MAP);
    private final LogoRenderer logoRenderer;
    private final Options options;
    private final boolean narratorAvailable;
    private boolean hasNarrated;
    private float timer;
    @Nullable
    private AccessibilityOnboardingTextWidget textWidget;

    public AccessibilityOnboardingScreen(Options param0) {
        super(Component.translatable("accessibility.onboarding.screen.title"));
        this.options = param0;
        this.logoRenderer = new LogoRenderer(true);
        this.narratorAvailable = Minecraft.getInstance().getNarrator().isActive();
    }

    @Override
    public void init() {
        int var0 = this.initTitleYPos();
        FrameLayout var1 = new FrameLayout(this.width, this.height - var0);
        var1.defaultChildLayoutSetting().alignVerticallyTop().padding(4);
        GridLayout var2 = var1.addChild(new GridLayout());
        var2.defaultCellSetting().alignHorizontallyCenter().padding(4);
        GridLayout.RowHelper var3 = var2.createRowHelper(1);
        var3.defaultCellSetting().padding(2);
        this.textWidget = new AccessibilityOnboardingTextWidget(this.font, this.title, this.width);
        var3.addChild(this.textWidget, var3.newCellSettings().paddingBottom(16));
        AbstractWidget var4 = this.options.narrator().createButton(this.options, 0, 0, 150);
        var4.active = this.narratorAvailable;
        var3.addChild(var4);
        if (this.narratorAvailable) {
            this.setInitialFocus(var4);
        }

        var3.addChild(CommonButtons.accessibilityTextAndImage(param0 -> this.closeAndSetScreen(new AccessibilityOptionsScreen(this, this.minecraft.options))));
        var3.addChild(
            CommonButtons.languageTextAndImage(
                param0 -> this.closeAndSetScreen(new LanguageSelectScreen(this, this.minecraft.options, this.minecraft.getLanguageManager()))
            )
        );
        var1.addChild(
            Button.builder(CommonComponents.GUI_CONTINUE, param0 -> this.onClose()).build(), var1.newChildLayoutSettings().alignVerticallyBottom().padding(8)
        );
        var1.arrangeElements();
        FrameLayout.alignInRectangle(var1, 0, var0, this.width, this.height, 0.5F, 0.0F);
        var1.visitWidgets(this::addRenderableWidget);
    }

    private int initTitleYPos() {
        return 90;
    }

    @Override
    public void onClose() {
        this.closeAndSetScreen(new TitleScreen(true, this.logoRenderer));
    }

    private void closeAndSetScreen(Screen param0) {
        this.options.onboardAccessibility = false;
        this.options.save();
        Narrator.getNarrator().clear();
        this.minecraft.setScreen(param0);
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.handleInitialNarrationDelay();
        this.panorama.render(0.0F, 1.0F);
        fill(param0, 0, 0, this.width, this.height, -1877995504);
        this.logoRenderer.renderLogo(param0, this.width, 1.0F);
        if (this.textWidget != null) {
            this.textWidget.render(param0, param1, param2, param3);
        }

        super.render(param0, param1, param2, param3);
    }

    private void handleInitialNarrationDelay() {
        if (!this.hasNarrated && this.narratorAvailable) {
            if (this.timer < 40.0F) {
                ++this.timer;
            } else if (this.minecraft.isWindowActive()) {
                Narrator.getNarrator().say(ONBOARDING_NARRATOR_MESSAGE.getString(), true);
                this.hasNarrated = true;
            }
        }

    }
}
