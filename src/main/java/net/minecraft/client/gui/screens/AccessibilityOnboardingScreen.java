package net.minecraft.client.gui.screens;

import com.mojang.text2speech.Narrator;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CommonButtons;
import net.minecraft.client.gui.components.FocusableTextWidget;
import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
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
    private final Runnable onClose;
    @Nullable
    private FocusableTextWidget textWidget;

    public AccessibilityOnboardingScreen(Options param0, Runnable param1) {
        super(Component.translatable("accessibility.onboarding.screen.title"));
        this.options = param0;
        this.onClose = param1;
        this.logoRenderer = new LogoRenderer(true);
        this.narratorAvailable = Minecraft.getInstance().getNarrator().isActive();
    }

    @Override
    public void init() {
        int var0 = this.initTitleYPos();
        FrameLayout var1 = new FrameLayout(this.width, this.height - var0);
        var1.defaultChildLayoutSetting().alignVerticallyTop().padding(4);
        LinearLayout var2 = var1.addChild(LinearLayout.vertical());
        var2.defaultCellSetting().alignHorizontallyCenter().padding(2);
        this.textWidget = new FocusableTextWidget(this.width - 16, this.title, this.font);
        var2.addChild(this.textWidget, param0 -> param0.paddingBottom(16));
        AbstractWidget var3 = this.options.narrator().createButton(this.options, 0, 0, 150);
        var3.active = this.narratorAvailable;
        var2.addChild(var3);
        if (this.narratorAvailable) {
            this.setInitialFocus(var3);
        }

        var2.addChild(CommonButtons.accessibility(150, param0 -> this.closeAndSetScreen(new AccessibilityOptionsScreen(this, this.minecraft.options)), false));
        var2.addChild(
            CommonButtons.language(
                150, param0 -> this.closeAndSetScreen(new LanguageSelectScreen(this, this.minecraft.options, this.minecraft.getLanguageManager())), false
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
        this.close(this.onClose);
    }

    private void closeAndSetScreen(Screen param0) {
        this.close(() -> this.minecraft.setScreen(param0));
    }

    private void close(Runnable param0) {
        this.options.onboardAccessibility = false;
        this.options.save();
        Narrator.getNarrator().clear();
        param0.run();
    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        super.render(param0, param1, param2, param3);
        this.handleInitialNarrationDelay();
        this.logoRenderer.renderLogo(param0, this.width, 1.0F);
        if (this.textWidget != null) {
            this.textWidget.render(param0, param1, param2, param3);
        }

    }

    @Override
    public void renderBackground(GuiGraphics param0, int param1, int param2, float param3) {
        this.panorama.render(0.0F, 1.0F);
        param0.fill(0, 0, this.width, this.height, -1877995504);
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
