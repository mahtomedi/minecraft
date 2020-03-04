package net.minecraft.realms;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DisconnectedRealmsScreen extends RealmsScreen {
    private final String title;
    private final Component reason;
    private List<String> lines;
    private final Screen parent;
    private int textHeight;

    public DisconnectedRealmsScreen(Screen param0, String param1, Component param2) {
        this.parent = param0;
        this.title = I18n.get(param1);
        this.reason = param2;
    }

    @Override
    public void init() {
        Minecraft var0 = Minecraft.getInstance();
        var0.setConnectedToRealms(false);
        var0.getClientPackSource().clearServerPack();
        NarrationHelper.now(this.title + ": " + this.reason.getString());
        this.lines = this.font.split(this.reason.getColoredString(), this.width - 50);
        this.textHeight = this.lines.size() * 9;
        this.addButton(
            new Button(
                this.width / 2 - 100,
                this.height / 2 + this.textHeight / 2 + 9,
                200,
                20,
                I18n.get("gui.back"),
                param0 -> Minecraft.getInstance().setScreen(this.parent)
            )
        );
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        if (param0 == 256) {
            Minecraft.getInstance().setScreen(this.parent);
            return true;
        } else {
            return super.keyPressed(param0, param1, param2);
        }
    }

    @Override
    public void render(int param0, int param1, float param2) {
        this.renderBackground();
        this.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - this.textHeight / 2 - 9 * 2, 11184810);
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
