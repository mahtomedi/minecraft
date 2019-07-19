package net.minecraft.client.gui.screens;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ErrorScreen extends Screen {
    private final String message;

    public ErrorScreen(Component param0, String param1) {
        super(param0);
        this.message = param1;
    }

    @Override
    protected void init() {
        super.init();
        this.addButton(new Button(this.width / 2 - 100, 140, 200, 20, I18n.get("gui.cancel"), param0 -> this.minecraft.setScreen(null)));
    }

    @Override
    public void render(int param0, int param1, float param2) {
        this.fillGradient(0, 0, this.width, this.height, -12574688, -11530224);
        this.drawCenteredString(this.font, this.title.getColoredString(), this.width / 2, 90, 16777215);
        this.drawCenteredString(this.font, this.message, this.width / 2, 110, 16777215);
        super.render(param0, param1, param2);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
