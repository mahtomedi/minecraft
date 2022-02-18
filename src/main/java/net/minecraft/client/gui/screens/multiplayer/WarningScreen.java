package net.minecraft.client.gui.screens.multiplayer;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nullable;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class WarningScreen extends Screen {
    private final Component titleComponent;
    private final Component content;
    private final Component check;
    private final Component narration;
    protected final Screen previous;
    @Nullable
    protected Checkbox stopShowing;
    private MultiLineLabel message = MultiLineLabel.EMPTY;

    protected WarningScreen(Component param0, Component param1, Component param2, Component param3, Screen param4) {
        super(NarratorChatListener.NO_TITLE);
        this.titleComponent = param0;
        this.content = param1;
        this.check = param2;
        this.narration = param3;
        this.previous = param4;
    }

    protected abstract void initButtons(int var1);

    @Override
    protected void init() {
        super.init();
        this.message = MultiLineLabel.create(this.font, this.content, this.width - 50);
        int var0 = (this.message.getLineCount() + 1) * 9 * 2;
        this.stopShowing = new Checkbox(this.width / 2 - 155 + 80, 76 + var0, 150, 20, this.check, false);
        this.addRenderableWidget(this.stopShowing);
        this.initButtons(var0);
    }

    @Override
    public Component getNarrationMessage() {
        return this.narration;
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderDirtBackground(0);
        drawString(param0, this.font, this.titleComponent, 25, 30, 16777215);
        this.message.renderLeftAligned(param0, 25, 70, 9 * 2, 16777215);
        super.render(param0, param1, param2, param3);
    }
}
