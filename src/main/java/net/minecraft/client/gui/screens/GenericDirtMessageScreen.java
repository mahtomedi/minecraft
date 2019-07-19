package net.minecraft.client.gui.screens;

import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GenericDirtMessageScreen extends Screen {
    public GenericDirtMessageScreen(Component param0) {
        super(param0);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void render(int param0, int param1, float param2) {
        this.renderDirtBackground(0);
        this.drawCenteredString(this.font, this.title.getColoredString(), this.width / 2, 70, 16777215);
        super.render(param0, param1, param2);
    }
}
