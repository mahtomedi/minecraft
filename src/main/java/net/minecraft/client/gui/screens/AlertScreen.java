package net.minecraft.client.gui.screens;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AlertScreen extends Screen {
    private final Runnable callback;
    protected final Component text;
    private final List<String> lines = Lists.newArrayList();
    protected final String okButton;
    private int delayTicker;

    public AlertScreen(Runnable param0, Component param1, Component param2) {
        this(param0, param1, param2, "gui.back");
    }

    public AlertScreen(Runnable param0, Component param1, Component param2, String param3) {
        super(param1);
        this.callback = param0;
        this.text = param2;
        this.okButton = I18n.get(param3);
    }

    @Override
    protected void init() {
        super.init();
        this.addButton(new Button(this.width / 2 - 100, this.height / 6 + 168, 200, 20, this.okButton, param0 -> this.callback.run()));
        this.lines.clear();
        this.lines.addAll(this.font.split(this.text.getColoredString(), this.width - 50));
    }

    @Override
    public void render(int param0, int param1, float param2) {
        this.renderBackground();
        this.drawCenteredString(this.font, this.title.getColoredString(), this.width / 2, 70, 16777215);
        int var0 = 90;

        for(String var1 : this.lines) {
            this.drawCenteredString(this.font, var1, this.width / 2, var0, 16777215);
            var0 += 9;
        }

        super.render(param0, param1, param2);
    }

    @Override
    public void tick() {
        super.tick();
        if (--this.delayTicker == 0) {
            for(AbstractWidget var0 : this.buttons) {
                var0.active = true;
            }
        }

    }
}
