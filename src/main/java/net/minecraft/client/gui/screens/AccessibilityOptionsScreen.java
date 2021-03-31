package net.minecraft.client.gui.screens;

import net.minecraft.Util;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AccessibilityOptionsScreen extends SimpleOptionsSubScreen {
    private static final Option[] OPTIONS = new Option[]{
        Option.NARRATOR,
        Option.SHOW_SUBTITLES,
        Option.TEXT_BACKGROUND_OPACITY,
        Option.TEXT_BACKGROUND,
        Option.CHAT_OPACITY,
        Option.CHAT_LINE_SPACING,
        Option.CHAT_DELAY,
        Option.AUTO_JUMP,
        Option.TOGGLE_CROUCH,
        Option.TOGGLE_SPRINT,
        Option.SCREEN_EFFECTS_SCALE,
        Option.FOV_EFFECTS_SCALE,
        Option.DARK_MOJANG_STUDIOS_BACKGROUND_COLOR
    };
    private static final String GUIDE_LINK = "https://aka.ms/MinecraftJavaAccessibility";

    public AccessibilityOptionsScreen(Screen param0, Options param1) {
        super(param0, param1, new TranslatableComponent("options.accessibility.title"), OPTIONS);
    }

    @Override
    protected void createFooter() {
        this.addButton(
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
        this.addButton(
            new Button(this.width / 2 + 5, this.height - 27, 150, 20, CommonComponents.GUI_DONE, param0 -> this.minecraft.setScreen(this.lastScreen))
        );
    }
}
