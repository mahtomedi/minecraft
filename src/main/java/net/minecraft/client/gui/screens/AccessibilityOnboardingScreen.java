package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.text2speech.Narrator;
import javax.annotation.Nullable;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.AccessibilityOnboardingTextWidget;
import net.minecraft.client.gui.components.Button;
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
    private boolean hasNarrated;
    private float timer;
    @Nullable
    private AccessibilityOnboardingTextWidget textWidget;

    public AccessibilityOnboardingScreen(Options param0) {
        super(Component.translatable("accessibility.onboarding.screen.title"));
        this.options = param0;
        this.logoRenderer = new LogoRenderer(true);
    }

    @Override
    public void init() {
        FrameLayout var0 = new FrameLayout();
        var0.defaultChildLayoutSetting().alignVerticallyTop().padding(4);
        var0.setMinDimensions(this.width, this.height - this.initTitleYPos());
        GridLayout var1 = var0.addChild(new GridLayout());
        var1.defaultCellSetting().alignHorizontallyCenter().padding(4);
        GridLayout.RowHelper var2 = var1.createRowHelper(1);
        this.textWidget = new AccessibilityOnboardingTextWidget(this.font, this.title, this.width);
        var2.addChild(this.textWidget, var2.newCellSettings().padding(16));
        AbstractWidget var3 = this.options.narrator().createButton(this.options, 0, 0, 150);
        var2.addChild(var3);
        this.setInitialFocus(var3);
        var2.addChild(
            Button.builder(
                    Component.translatable("options.accessibility.title"),
                    param0 -> this.minecraft.setScreen(new AccessibilityOptionsScreen(new TitleScreen(true), this.minecraft.options))
                )
                .build()
        );
        var0.addChild(
            Button.builder(CommonComponents.GUI_CONTINUE, param0 -> this.minecraft.setScreen(new TitleScreen(true, this.logoRenderer))).build(),
            var0.newChildLayoutSettings().alignVerticallyBottom().padding(8)
        );
        var0.arrangeElements();
        FrameLayout.alignInRectangle(var0, 0, this.initTitleYPos(), this.width, this.height, 0.5F, 0.0F);
        var0.visitWidgets(this::addRenderableWidget);
    }

    private int initTitleYPos() {
        return 90;
    }

    @Override
    public void onClose() {
        this.minecraft.getNarrator().clear();
        this.minecraft.setScreen(new TitleScreen(true, this.logoRenderer));
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
        if (!this.hasNarrated) {
            if (this.timer < 40.0F) {
                ++this.timer;
            } else {
                Narrator.getNarrator().say(ONBOARDING_NARRATOR_MESSAGE.getString(), true);
                this.hasNarrated = true;
            }
        }

    }
}
