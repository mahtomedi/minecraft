package net.minecraft.client.gui.screens;

import java.util.List;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DisconnectedScreen extends Screen {
    private final Component reason;
    private List<String> lines;
    private final Screen parent;
    private int textHeight;

    public DisconnectedScreen(Screen param0, String param1, Component param2) {
        super(new TranslatableComponent(param1));
        this.parent = param0;
        this.reason = param2;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    protected void init() {
        this.lines = this.font.split(this.reason.getColoredString(), this.width - 50);
        this.textHeight = this.lines.size() * 9;
        this.addButton(
            new Button(
                this.width / 2 - 100,
                Math.min(this.height / 2 + this.textHeight / 2 + 9, this.height - 30),
                200,
                20,
                I18n.get("gui.toMenu"),
                param0 -> this.minecraft.setScreen(this.parent)
            )
        );
    }

    @Override
    public void render(int param0, int param1, float param2) {
        this.renderBackground();
        this.drawCenteredString(this.font, this.title.getColoredString(), this.width / 2, this.height / 2 - this.textHeight / 2 - 9 * 2, 11184810);
        int var0 = this.height / 2 - this.textHeight / 2;
        if (this.lines != null) {
            for(String var1 : this.lines) {
                this.drawCenteredString(this.font, var1, this.width / 2, var0, 16777215);
                var0 += 9;
            }
        }

        super.render(param0, param1, param2);
    }
}
