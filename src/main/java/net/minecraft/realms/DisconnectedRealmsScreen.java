package net.minecraft.realms;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DisconnectedRealmsScreen extends RealmsScreen {
    private final String title;
    private final Component reason;
    private List<String> lines;
    private final RealmsScreen parent;
    private int textHeight;

    public DisconnectedRealmsScreen(RealmsScreen param0, String param1, Component param2) {
        this.parent = param0;
        this.title = getLocalizedString(param1);
        this.reason = param2;
    }

    @Override
    public void init() {
        Realms.setConnectedToRealms(false);
        Realms.clearResourcePack();
        Realms.narrateNow(this.title + ": " + this.reason.getString());
        this.lines = this.fontSplit(this.reason.getColoredString(), this.width() - 50);
        this.textHeight = this.lines.size() * this.fontLineHeight();
        this.buttonsAdd(
            new RealmsButton(0, this.width() / 2 - 100, this.height() / 2 + this.textHeight / 2 + this.fontLineHeight(), getLocalizedString("gui.back")) {
                @Override
                public void onPress() {
                    Realms.setScreen(DisconnectedRealmsScreen.this.parent);
                }
            }
        );
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        if (param0 == 256) {
            Realms.setScreen(this.parent);
            return true;
        } else {
            return super.keyPressed(param0, param1, param2);
        }
    }

    @Override
    public void render(int param0, int param1, float param2) {
        this.renderBackground();
        this.drawCenteredString(this.title, this.width() / 2, this.height() / 2 - this.textHeight / 2 - this.fontLineHeight() * 2, 11184810);
        int var0 = this.height() / 2 - this.textHeight / 2;
        if (this.lines != null) {
            for(String var1 : this.lines) {
                this.drawCenteredString(var1, this.width() / 2, var0, 16777215);
                var0 += this.fontLineHeight();
            }
        }

        super.render(param0, param1, param2);
    }
}
