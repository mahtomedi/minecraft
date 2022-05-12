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
    @Nullable
    protected Checkbox stopShowing;
    private MultiLineLabel message = MultiLineLabel.EMPTY;

    protected WarningScreen(Component param0, Component param1, Component param2, Component param3) {
        super(NarratorChatListener.NO_TITLE);
        this.titleComponent = param0;
        this.content = param1;
        this.check = param2;
        this.narration = param3;
    }

    protected abstract void initButtons(int var1);

    @Override
    protected void init() {
        super.init();
        this.message = MultiLineLabel.create(this.font, this.content, this.width - 100);
        int var0 = (this.message.getLineCount() + 1) * this.getLineHeight();
        int var1 = this.font.width(this.check);
        this.stopShowing = new Checkbox(this.width / 2 - var1 / 2 - 8, 76 + var0, var1 + 24, 20, this.check, false);
        this.addRenderableWidget(this.stopShowing);
        this.initButtons(var0);
    }

    @Override
    public Component getNarrationMessage() {
        return this.narration;
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        drawString(param0, this.font, this.titleComponent, 25, 30, 16777215);
        int var0 = this.width / 2 - this.message.getWidth() / 2;
        this.message.renderLeftAligned(param0, var0, 70, this.getLineHeight(), 16777215);
        super.render(param0, param1, param2, param3);
    }

    protected int getLineHeight() {
        return 9 * 2;
    }
}
