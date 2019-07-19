package net.minecraft.client.gui.screens;

import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ChatOptionsScreen extends Screen {
    private static final Option[] CHAT_OPTIONS = new Option[]{
        Option.CHAT_VISIBILITY,
        Option.CHAT_COLOR,
        Option.CHAT_LINKS,
        Option.CHAT_LINKS_PROMPT,
        Option.CHAT_OPACITY,
        Option.TEXT_BACKGROUND_OPACITY,
        Option.CHAT_SCALE,
        Option.CHAT_WIDTH,
        Option.CHAT_HEIGHT_FOCUSED,
        Option.CHAT_HEIGHT_UNFOCUSED,
        Option.REDUCED_DEBUG_INFO,
        Option.AUTO_SUGGESTIONS,
        Option.NARRATOR
    };
    private final Screen lastScreen;
    private final Options options;
    private AbstractWidget narratorButton;

    public ChatOptionsScreen(Screen param0, Options param1) {
        super(new TranslatableComponent("options.chat.title"));
        this.lastScreen = param0;
        this.options = param1;
    }

    @Override
    protected void init() {
        int var0 = 0;

        for(Option var1 : CHAT_OPTIONS) {
            int var2 = this.width / 2 - 155 + var0 % 2 * 160;
            int var3 = this.height / 6 + 24 * (var0 >> 1);
            AbstractWidget var4 = this.addButton(var1.createButton(this.minecraft.options, var2, var3, 150));
            if (var1 == Option.NARRATOR) {
                this.narratorButton = var4;
                var4.active = NarratorChatListener.INSTANCE.isActive();
            }

            ++var0;
        }

        this.addButton(
            new Button(
                this.width / 2 - 100, this.height / 6 + 24 * (var0 + 1) / 2, 200, 20, I18n.get("gui.done"), param0 -> this.minecraft.setScreen(this.lastScreen)
            )
        );
    }

    @Override
    public void removed() {
        this.minecraft.options.save();
    }

    @Override
    public void render(int param0, int param1, float param2) {
        this.renderBackground();
        this.drawCenteredString(this.font, this.title.getColoredString(), this.width / 2, 20, 16777215);
        super.render(param0, param1, param2);
    }

    public void updateNarratorButton() {
        this.narratorButton.setMessage(Option.NARRATOR.getMessage(this.options));
    }
}
