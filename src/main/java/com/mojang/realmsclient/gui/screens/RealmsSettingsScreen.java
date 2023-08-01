package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.dto.RealmsServer;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsSettingsScreen extends RealmsScreen {
    private static final int COMPONENT_WIDTH = 212;
    private static final Component NAME_LABEL = Component.translatable("mco.configure.world.name");
    private static final Component DESCRIPTION_LABEL = Component.translatable("mco.configure.world.description");
    private final RealmsConfigureWorldScreen configureWorldScreen;
    private final RealmsServer serverData;
    private Button doneButton;
    private EditBox descEdit;
    private EditBox nameEdit;

    public RealmsSettingsScreen(RealmsConfigureWorldScreen param0, RealmsServer param1) {
        super(Component.translatable("mco.configure.world.settings.title"));
        this.configureWorldScreen = param0;
        this.serverData = param1;
    }

    @Override
    public void tick() {
        this.doneButton.active = !Util.isBlank(this.nameEdit.getValue());
    }

    @Override
    public void init() {
        int var0 = this.width / 2 - 106;
        this.doneButton = this.addRenderableWidget(
            Button.builder(Component.translatable("mco.configure.world.buttons.done"), param0 -> this.save()).bounds(var0 - 2, row(12), 106, 20).build()
        );
        this.addRenderableWidget(
            Button.builder(CommonComponents.GUI_CANCEL, param0 -> this.minecraft.setScreen(this.configureWorldScreen))
                .bounds(this.width / 2 + 2, row(12), 106, 20)
                .build()
        );
        String var1 = this.serverData.state == RealmsServer.State.OPEN ? "mco.configure.world.buttons.close" : "mco.configure.world.buttons.open";
        Button var2 = Button.builder(Component.translatable(var1), param0 -> {
            if (this.serverData.state == RealmsServer.State.OPEN) {
                Component var0x = Component.translatable("mco.configure.world.close.question.line1");
                Component var1x = Component.translatable("mco.configure.world.close.question.line2");
                this.minecraft.setScreen(new RealmsLongConfirmationScreen(param0x -> {
                    if (param0x) {
                        this.configureWorldScreen.closeTheWorld(this);
                    } else {
                        this.minecraft.setScreen(this);
                    }

                }, RealmsLongConfirmationScreen.Type.INFO, var0x, var1x, true));
            } else {
                this.configureWorldScreen.openTheWorld(false, this);
            }

        }).bounds(this.width / 2 - 53, row(0), 106, 20).build();
        this.addRenderableWidget(var2);
        this.nameEdit = new EditBox(this.minecraft.font, var0, row(4), 212, 20, null, Component.translatable("mco.configure.world.name"));
        this.nameEdit.setMaxLength(32);
        this.nameEdit.setValue(this.serverData.getName());
        this.addWidget(this.nameEdit);
        this.magicalSpecialHackyFocus(this.nameEdit);
        this.descEdit = new EditBox(this.minecraft.font, var0, row(8), 212, 20, null, Component.translatable("mco.configure.world.description"));
        this.descEdit.setMaxLength(32);
        this.descEdit.setValue(this.serverData.getDescription());
        this.addWidget(this.descEdit);
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        if (param0 == 256) {
            this.minecraft.setScreen(this.configureWorldScreen);
            return true;
        } else {
            return super.keyPressed(param0, param1, param2);
        }
    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        super.render(param0, param1, param2, param3);
        param0.drawCenteredString(this.font, this.title, this.width / 2, 17, -1);
        param0.drawString(this.font, NAME_LABEL, this.width / 2 - 106, row(3), -6250336, false);
        param0.drawString(this.font, DESCRIPTION_LABEL, this.width / 2 - 106, row(7), -6250336, false);
        this.nameEdit.render(param0, param1, param2, param3);
        this.descEdit.render(param0, param1, param2, param3);
    }

    public void save() {
        this.configureWorldScreen.saveSettings(this.nameEdit.getValue(), this.descEdit.getValue());
    }
}
