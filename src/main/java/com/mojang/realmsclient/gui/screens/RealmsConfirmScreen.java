package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsConfirmScreen extends RealmsScreen {
    protected BooleanConsumer callback;
    private final Component title1;
    private final Component title2;

    public RealmsConfirmScreen(BooleanConsumer param0, Component param1, Component param2) {
        super(GameNarrator.NO_TITLE);
        this.callback = param0;
        this.title1 = param1;
        this.title2 = param2;
    }

    @Override
    public void init() {
        this.addRenderableWidget(new Button(this.width / 2 - 105, row(9), 100, 20, CommonComponents.GUI_YES, param0 -> this.callback.accept(true)));
        this.addRenderableWidget(new Button(this.width / 2 + 5, row(9), 100, 20, CommonComponents.GUI_NO, param0 -> this.callback.accept(false)));
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        drawCenteredString(param0, this.font, this.title1, this.width / 2, row(3), 16777215);
        drawCenteredString(param0, this.font, this.title2, this.width / 2, row(5), 16777215);
        super.render(param0, param1, param2, param3);
    }
}
