package net.minecraft.client.gui.screens.multiplayer;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SafetyScreen extends WarningScreen {
    private static final Component TITLE = Component.translatable("multiplayerWarning.header").withStyle(ChatFormatting.BOLD);
    private static final Component CONTENT = Component.translatable("multiplayerWarning.message");
    private static final Component CHECK = Component.translatable("multiplayerWarning.check");
    private static final Component NARRATION = TITLE.copy().append("\n").append(CONTENT);
    private final Screen previous;

    public SafetyScreen(Screen param0) {
        super(TITLE, CONTENT, CHECK, NARRATION);
        this.previous = param0;
    }

    @Override
    protected void initButtons(int param0) {
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_PROCEED, param0x -> {
            if (this.stopShowing.selected()) {
                this.minecraft.options.skipMultiplayerWarning = true;
                this.minecraft.options.save();
            }

            this.minecraft.setScreen(new JoinMultiplayerScreen(this.previous));
        }).bounds(this.width / 2 - 155, 100 + param0, 150, 20).build());
        this.addRenderableWidget(
            Button.builder(CommonComponents.GUI_BACK, param0x -> this.minecraft.setScreen(this.previous))
                .bounds(this.width / 2 - 155 + 160, 100 + param0, 150, 20)
                .build()
        );
    }
}
