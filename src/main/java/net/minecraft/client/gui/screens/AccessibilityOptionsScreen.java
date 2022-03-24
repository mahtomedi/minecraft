package net.minecraft.client.gui.screens;

import net.minecraft.Util;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AccessibilityOptionsScreen extends SimpleOptionsSubScreen {
    private static final String GUIDE_LINK = "https://aka.ms/MinecraftJavaAccessibility";

    private static OptionInstance<?>[] options(Options param0) {
        return new OptionInstance[]{
            param0.narrator(),
            param0.showSubtitles(),
            param0.textBackgroundOpacity(),
            param0.backgroundForChatOnly(),
            param0.chatOpacity(),
            param0.chatLineSpacing(),
            param0.chatDelay(),
            param0.autoJump(),
            param0.toggleCrouch(),
            param0.toggleSprint(),
            param0.screenEffectScale(),
            param0.fovEffectScale(),
            param0.darkMojangStudiosBackground(),
            param0.hideLightningFlash(),
            param0.darknessEffectScale()
        };
    }

    public AccessibilityOptionsScreen(Screen param0, Options param1) {
        super(param0, param1, new TranslatableComponent("options.accessibility.title"), options(param1));
    }

    @Override
    protected void createFooter() {
        this.addRenderableWidget(
            new Button(
                this.width / 2 - 155,
                this.height - 27,
                150,
                20,
                new TranslatableComponent("options.accessibility.link"),
                param0 -> this.minecraft.setScreen(new ConfirmLinkScreen(param0x -> {
                        if (param0x) {
                            Util.getPlatform().openUri("https://aka.ms/MinecraftJavaAccessibility");
                        }
        
                        this.minecraft.setScreen(this);
                    }, "https://aka.ms/MinecraftJavaAccessibility", true))
            )
        );
        this.addRenderableWidget(
            new Button(this.width / 2 + 5, this.height - 27, 150, 20, CommonComponents.GUI_DONE, param0 -> this.minecraft.setScreen(this.lastScreen))
        );
    }
}
