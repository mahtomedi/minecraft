package com.mojang.realmsclient.gui.screens;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.realms.RealmsScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsConfirmScreen extends RealmsScreen {
    protected BooleanConsumer callback;
    protected String title1;
    private final String title2;
    protected String yesButton = I18n.get("gui.yes");
    protected String noButton = I18n.get("gui.no");
    private int delayTicker;

    public RealmsConfirmScreen(BooleanConsumer param0, String param1, String param2) {
        this.callback = param0;
        this.title1 = param1;
        this.title2 = param2;
    }

    @Override
    public void init() {
        this.addButton(new Button(this.width / 2 - 105, row(9), 100, 20, this.yesButton, param0 -> this.callback.accept(true)));
        this.addButton(new Button(this.width / 2 + 5, row(9), 100, 20, this.noButton, param0 -> this.callback.accept(false)));
    }

    @Override
    public void render(int param0, int param1, float param2) {
        this.renderBackground();
        this.drawCenteredString(this.font, this.title1, this.width / 2, row(3), 16777215);
        this.drawCenteredString(this.font, this.title2, this.width / 2, row(5), 16777215);
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
