package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.realmsclient.dto.RealmsServer;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.realms.RealmsLabel;
import net.minecraft.realms.RealmsScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsSettingsScreen extends RealmsScreen {
    private static final int COMPONENT_WIDTH = 212;
    private static final Component NAME_LABEL = new TranslatableComponent("mco.configure.world.name");
    private static final Component DESCRIPTION_LABEL = new TranslatableComponent("mco.configure.world.description");
    private final RealmsConfigureWorldScreen configureWorldScreen;
    private final RealmsServer serverData;
    private Button doneButton;
    private EditBox descEdit;
    private EditBox nameEdit;
    private RealmsLabel titleLabel;

    public RealmsSettingsScreen(RealmsConfigureWorldScreen param0, RealmsServer param1) {
        this.configureWorldScreen = param0;
        this.serverData = param1;
    }

    @Override
    public void tick() {
        this.nameEdit.tick();
        this.descEdit.tick();
        this.doneButton.active = !this.nameEdit.getValue().trim().isEmpty();
    }

    @Override
    public void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        int var0 = this.width / 2 - 106;
        this.doneButton = this.addButton(
            new Button(var0 - 2, row(12), 106, 20, new TranslatableComponent("mco.configure.world.buttons.done"), param0 -> this.save())
        );
        this.addButton(
            new Button(this.width / 2 + 2, row(12), 106, 20, CommonComponents.GUI_CANCEL, param0 -> this.minecraft.setScreen(this.configureWorldScreen))
        );
        String var1 = this.serverData.state == RealmsServer.State.OPEN ? "mco.configure.world.buttons.close" : "mco.configure.world.buttons.open";
        Button var2 = new Button(this.width / 2 - 53, row(0), 106, 20, new TranslatableComponent(var1), param0 -> {
            if (this.serverData.state == RealmsServer.State.OPEN) {
                Component var0x = new TranslatableComponent("mco.configure.world.close.question.line1");
                Component var1x = new TranslatableComponent("mco.configure.world.close.question.line2");
                this.minecraft.setScreen(new RealmsLongConfirmationScreen(param0x -> {
                    if (param0x) {
                        this.configureWorldScreen.closeTheWorld(this);
                    } else {
                        this.minecraft.setScreen(this);
                    }

                }, RealmsLongConfirmationScreen.Type.Info, var0x, var1x, true));
            } else {
                this.configureWorldScreen.openTheWorld(false, this);
            }

        });
        this.addButton(var2);
        this.nameEdit = new EditBox(this.minecraft.font, var0, row(4), 212, 20, null, new TranslatableComponent("mco.configure.world.name"));
        this.nameEdit.setMaxLength(32);
        this.nameEdit.setValue(this.serverData.getName());
        this.addWidget(this.nameEdit);
        this.magicalSpecialHackyFocus(this.nameEdit);
        this.descEdit = new EditBox(this.minecraft.font, var0, row(8), 212, 20, null, new TranslatableComponent("mco.configure.world.description"));
        this.descEdit.setMaxLength(32);
        this.descEdit.setValue(this.serverData.getDescription());
        this.addWidget(this.descEdit);
        this.titleLabel = this.addWidget(new RealmsLabel(new TranslatableComponent("mco.configure.world.settings.title"), this.width / 2, 17, 16777215));
        this.narrateLabels();
    }

    @Override
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
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
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        this.titleLabel.render(this, param0);
        this.font.draw(param0, NAME_LABEL, (float)(this.width / 2 - 106), (float)row(3), 10526880);
        this.font.draw(param0, DESCRIPTION_LABEL, (float)(this.width / 2 - 106), (float)row(7), 10526880);
        this.nameEdit.render(param0, param1, param2, param3);
        this.descEdit.render(param0, param1, param2, param3);
        super.render(param0, param1, param2, param3);
    }

    public void save() {
        this.configureWorldScreen.saveSettings(this.nameEdit.getValue(), this.descEdit.getValue());
    }
}
