package net.minecraft.client.gui.screens.multiplayer;

import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class WarningScreen extends Screen {
    private final Component content;
    @Nullable
    private final Component check;
    private final Component narration;
    @Nullable
    protected Checkbox stopShowing;
    private MultiLineLabel message = MultiLineLabel.EMPTY;

    protected WarningScreen(Component param0, Component param1, Component param2) {
        this(param0, param1, null, param2);
    }

    protected WarningScreen(Component param0, Component param1, @Nullable Component param2, Component param3) {
        super(param0);
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
        if (this.check != null) {
            int var1 = this.font.width(this.check);
            this.stopShowing = new Checkbox(this.width / 2 - var1 / 2 - 8, 76 + var0, var1 + 24, 20, this.check, false);
            this.addRenderableWidget(this.stopShowing);
        }

        this.initButtons(var0);
    }

    @Override
    public Component getNarrationMessage() {
        return this.narration;
    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        super.render(param0, param1, param2, param3);
        this.renderTitle(param0);
        int var0 = this.width / 2 - this.message.getWidth() / 2;
        this.message.renderLeftAligned(param0, var0, 70, this.getLineHeight(), 16777215);
    }

    protected void renderTitle(GuiGraphics param0) {
        param0.drawString(this.font, this.title, 25, 30, 16777215);
    }

    protected int getLineHeight() {
        return 9 * 2;
    }
}
