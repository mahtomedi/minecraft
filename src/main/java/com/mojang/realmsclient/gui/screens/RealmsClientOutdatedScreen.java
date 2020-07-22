package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.realms.RealmsScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsClientOutdatedScreen extends RealmsScreen {
    private static final Component OUTDATED_TITLE = new TranslatableComponent("mco.client.outdated.title");
    private static final Component[] OUTDATED_MESSAGES = new Component[]{
        new TranslatableComponent("mco.client.outdated.msg.line1"), new TranslatableComponent("mco.client.outdated.msg.line2")
    };
    private static final Component INCOMPATIBLE_TITLE = new TranslatableComponent("mco.client.incompatible.title");
    private static final Component[] INCOMPATIBLE_MESSAGES = new Component[]{
        new TranslatableComponent("mco.client.incompatible.msg.line1"),
        new TranslatableComponent("mco.client.incompatible.msg.line2"),
        new TranslatableComponent("mco.client.incompatible.msg.line3")
    };
    private final Screen lastScreen;
    private final boolean outdated;

    public RealmsClientOutdatedScreen(Screen param0, boolean param1) {
        this.lastScreen = param0;
        this.outdated = param1;
    }

    @Override
    public void init() {
        this.addButton(new Button(this.width / 2 - 100, row(12), 200, 20, CommonComponents.GUI_BACK, param0 -> this.minecraft.setScreen(this.lastScreen)));
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        Component var0;
        Component[] var1;
        if (this.outdated) {
            var0 = INCOMPATIBLE_TITLE;
            var1 = INCOMPATIBLE_MESSAGES;
        } else {
            var0 = OUTDATED_TITLE;
            var1 = OUTDATED_MESSAGES;
        }

        drawCenteredString(param0, this.font, var0, this.width / 2, row(3), 16711680);

        for(int var4 = 0; var4 < var1.length; ++var4) {
            drawCenteredString(param0, this.font, var1[var4], this.width / 2, row(5) + var4 * 12, 16777215);
        }

        super.render(param0, param1, param2, param3);
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        if (param0 != 257 && param0 != 335 && param0 != 256) {
            return super.keyPressed(param0, param1, param2);
        } else {
            this.minecraft.setScreen(this.lastScreen);
            return true;
        }
    }
}
