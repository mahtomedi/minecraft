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
        Component var0 = new TranslatableComponent(this.outdated ? "mco.client.outdated.title" : "mco.client.incompatible.title");
        this.drawCenteredString(param0, this.font, var0, this.width / 2, row(3), 16711680);
        int var1 = this.outdated ? 2 : 3;

        for(int var2 = 0; var2 < var1; ++var2) {
            String var3 = (this.outdated ? "mco.client.outdated.msg.line" : "mco.client.incompatible.msg.line") + (var2 + 1);
            this.drawCenteredString(param0, this.font, new TranslatableComponent(var3), this.width / 2, row(5) + var2 * 12, 16777215);
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
