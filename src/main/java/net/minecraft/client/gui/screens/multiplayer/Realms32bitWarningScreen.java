package net.minecraft.client.gui.screens.multiplayer;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Realms32bitWarningScreen extends WarningScreen {
    private static final Component TITLE = Component.translatable("title.32bit.deprecation.realms.header").withStyle(ChatFormatting.BOLD);
    private static final Component CONTENT = Component.translatable("title.32bit.deprecation.realms");
    private static final Component CHECK = Component.translatable("title.32bit.deprecation.realms.check");
    private static final Component NARRATION = TITLE.copy().append("\n").append(CONTENT);

    public Realms32bitWarningScreen(Screen param0) {
        super(TITLE, CONTENT, CHECK, NARRATION, param0);
    }

    @Override
    protected void initButtons(int param0) {
        this.addRenderableWidget(new Button(this.width / 2 - 75, 100 + param0, 150, 20, CommonComponents.GUI_DONE, param0x -> {
            if (this.stopShowing.selected()) {
                this.minecraft.options.skipRealms32bitWarning = true;
                this.minecraft.options.save();
            }

            this.minecraft.setScreen(this.previous);
        }));
    }
}
