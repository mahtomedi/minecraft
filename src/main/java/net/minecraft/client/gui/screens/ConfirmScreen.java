package net.minecraft.client.gui.screens;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.util.List;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ConfirmScreen extends Screen {
    private final Component title2;
    private final List<String> lines = Lists.newArrayList();
    protected String yesButton;
    protected String noButton;
    private int delayTicker;
    protected final BooleanConsumer callback;

    public ConfirmScreen(BooleanConsumer param0, Component param1, Component param2) {
        this(param0, param1, param2, I18n.get("gui.yes"), I18n.get("gui.no"));
    }

    public ConfirmScreen(BooleanConsumer param0, Component param1, Component param2, String param3, String param4) {
        super(param1);
        this.callback = param0;
        this.title2 = param2;
        this.yesButton = param3;
        this.noButton = param4;
    }

    @Override
    public String getNarrationMessage() {
        return super.getNarrationMessage() + ". " + this.title2.getString();
    }

    @Override
    protected void init() {
        super.init();
        this.addButton(new Button(this.width / 2 - 155, this.height / 6 + 96, 150, 20, this.yesButton, param0 -> this.callback.accept(true)));
        this.addButton(new Button(this.width / 2 - 155 + 160, this.height / 6 + 96, 150, 20, this.noButton, param0 -> this.callback.accept(false)));
        this.lines.clear();
        this.lines.addAll(this.font.split(this.title2.getColoredString(), this.width - 50));
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

    public void setDelay(int param0) {
        this.delayTicker = param0;

        for(AbstractWidget var0 : this.buttons) {
            var0.active = false;
        }

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

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        if (param0 == 256) {
            this.callback.accept(false);
            return true;
        } else {
            return super.keyPressed(param0, param1, param2);
        }
    }
}
