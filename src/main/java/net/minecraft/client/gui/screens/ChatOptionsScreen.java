package net.minecraft.client.gui.screens;

import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ChatOptionsScreen extends SimpleOptionsSubScreen {
    public ChatOptionsScreen(Screen param0, Options param1) {
        super(
            param0,
            param1,
            new TranslatableComponent("options.chat.title"),
            new Option[]{
                Option.CHAT_VISIBILITY,
                Option.CHAT_COLOR,
                Option.CHAT_LINKS,
                Option.CHAT_LINKS_PROMPT,
                Option.CHAT_OPACITY,
                Option.TEXT_BACKGROUND_OPACITY,
                Option.CHAT_SCALE,
                Option.CHAT_LINE_SPACING,
                Option.CHAT_DELAY,
                Option.CHAT_WIDTH,
                param1.chatHeightFocused(),
                param1.chatHeightUnfocused(),
                Option.NARRATOR,
                Option.AUTO_SUGGESTIONS,
                Option.HIDE_MATCHED_NAMES,
                Option.REDUCED_DEBUG_INFO
            }
        );
    }
}
