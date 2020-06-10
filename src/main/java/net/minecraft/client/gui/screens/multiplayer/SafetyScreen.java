package net.minecraft.client.gui.screens.multiplayer;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SafetyScreen extends Screen {
    private final Screen previous;
    private static final Component TITLE = new TranslatableComponent("multiplayerWarning.header").withStyle(ChatFormatting.BOLD);
    private static final Component CONTENT = new TranslatableComponent("multiplayerWarning.message");
    private static final Component CHECK = new TranslatableComponent("multiplayerWarning.check");
    private static final Component NARRATION = TITLE.copy().append("\n").append(CONTENT);
    private Checkbox stopShowing;
    private final List<FormattedText> lines = Lists.newArrayList();

    public SafetyScreen(Screen param0) {
        super(NarratorChatListener.NO_TITLE);
        this.previous = param0;
    }

    @Override
    protected void init() {
        super.init();
        this.lines.clear();
        this.lines.addAll(this.font.split(CONTENT, this.width - 50));
        int var0 = (this.lines.size() + 1) * 9;
        this.addButton(new Button(this.width / 2 - 155, 100 + var0, 150, 20, CommonComponents.GUI_PROCEED, param0 -> {
            if (this.stopShowing.selected()) {
                this.minecraft.options.skipMultiplayerWarning = true;
                this.minecraft.options.save();
            }

            this.minecraft.setScreen(new JoinMultiplayerScreen(this.previous));
        }));
        this.addButton(
            new Button(this.width / 2 - 155 + 160, 100 + var0, 150, 20, CommonComponents.GUI_BACK, param0 -> this.minecraft.setScreen(this.previous))
        );
        this.stopShowing = new Checkbox(this.width / 2 - 155 + 80, 76 + var0, 150, 20, CHECK, false);
        this.addButton(this.stopShowing);
    }

    @Override
    public String getNarrationMessage() {
        return NARRATION.getString();
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderDirtBackground(0);
        this.drawCenteredString(param0, this.font, TITLE, this.width / 2, 30, 16777215);
        int var0 = 70;

        for(FormattedText var1 : this.lines) {
            this.drawCenteredString(param0, this.font, var1, this.width / 2, var0, 16777215);
            var0 += 9;
        }

        super.render(param0, param1, param2, param3);
    }
}
