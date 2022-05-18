package net.minecraft.client.gui.screens;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ChatOptionsScreen extends SimpleOptionsSubScreen {
    public ChatOptionsScreen(Screen param0, Options param1) {
        super(
            param0,
            param1,
            Component.translatable("options.chat.title"),
            new OptionInstance[]{
                param1.chatVisibility(),
                param1.chatColors(),
                param1.chatLinks(),
                param1.chatLinksPrompt(),
                param1.chatOpacity(),
                param1.textBackgroundOpacity(),
                param1.chatScale(),
                param1.chatLineSpacing(),
                param1.chatDelay(),
                param1.chatWidth(),
                param1.chatHeightFocused(),
                param1.chatHeightUnfocused(),
                param1.narrator(),
                param1.autoSuggestions(),
                param1.hideMatchedNames(),
                param1.reducedDebugInfo(),
                param1.chatPreview(),
                param1.onlyShowSecureChat()
            }
        );
    }
}
