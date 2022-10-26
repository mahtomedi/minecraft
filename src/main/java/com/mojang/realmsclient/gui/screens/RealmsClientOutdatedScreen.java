package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsClientOutdatedScreen extends RealmsScreen {
    private static final Component INCOMPATIBLE_TITLE = Component.translatable("mco.client.incompatible.title");
    private static final Component[] INCOMPATIBLE_MESSAGES_SNAPSHOT = new Component[]{
        Component.translatable("mco.client.incompatible.msg.line1"),
        Component.translatable("mco.client.incompatible.msg.line2"),
        Component.translatable("mco.client.incompatible.msg.line3")
    };
    private static final Component[] INCOMPATIBLE_MESSAGES = new Component[]{
        Component.translatable("mco.client.incompatible.msg.line1"), Component.translatable("mco.client.incompatible.msg.line2")
    };
    private final Screen lastScreen;

    public RealmsClientOutdatedScreen(Screen param0) {
        super(INCOMPATIBLE_TITLE);
        this.lastScreen = param0;
    }

    @Override
    public void init() {
        this.addRenderableWidget(
            Button.builder(CommonComponents.GUI_BACK, param0 -> this.minecraft.setScreen(this.lastScreen))
                .bounds(this.width / 2 - 100, row(12), 200, 20)
                .build()
        );
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        drawCenteredString(param0, this.font, this.title, this.width / 2, row(3), 16711680);
        Component[] var0 = this.getMessages();

        for(int var1 = 0; var1 < var0.length; ++var1) {
            drawCenteredString(param0, this.font, var0[var1], this.width / 2, row(5) + var1 * 12, 16777215);
        }

        super.render(param0, param1, param2, param3);
    }

    private Component[] getMessages() {
        return this.minecraft.getGame().getVersion().isStable() ? INCOMPATIBLE_MESSAGES : INCOMPATIBLE_MESSAGES_SNAPSHOT;
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
