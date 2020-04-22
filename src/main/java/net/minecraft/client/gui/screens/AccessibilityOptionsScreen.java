package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AccessibilityOptionsScreen extends OptionsSubScreen {
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
        Option.TOGGLE_SPRINT
    };
    private AbstractWidget narratorButton;

    public AccessibilityOptionsScreen(Screen param0, Options param1) {
        super(param0, param1, new TranslatableComponent("options.accessibility.title"));
    }

    @Override
    protected void init() {
        int var0 = 0;

        for(Option var1 : OPTIONS) {
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
            new Button(this.width / 2 - 100, this.height / 6 + 144, 200, 20, CommonComponents.GUI_DONE, param0 -> this.minecraft.setScreen(this.lastScreen))
        );
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        this.drawCenteredString(param0, this.font, this.title, this.width / 2, 20, 16777215);
        super.render(param0, param1, param2, param3);
    }

    public void updateNarratorButton() {
        this.narratorButton.setMessage(Option.NARRATOR.getMessage(this.options));
    }
}
