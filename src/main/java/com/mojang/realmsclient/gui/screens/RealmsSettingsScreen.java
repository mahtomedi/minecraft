package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.gui.RealmsConstants;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsEditBox;
import net.minecraft.realms.RealmsLabel;
import net.minecraft.realms.RealmsScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsSettingsScreen extends RealmsScreen {
    private final RealmsConfigureWorldScreen configureWorldScreen;
    private final RealmsServer serverData;
    private final int COMPONENT_WIDTH = 212;
    private RealmsButton doneButton;
    private RealmsEditBox descEdit;
    private RealmsEditBox nameEdit;
    private RealmsLabel titleLabel;

    public RealmsSettingsScreen(RealmsConfigureWorldScreen param0, RealmsServer param1) {
        this.configureWorldScreen = param0;
        this.serverData = param1;
    }

    @Override
    public void tick() {
        this.nameEdit.tick();
        this.descEdit.tick();
        this.doneButton.active(this.nameEdit.getValue() != null && !this.nameEdit.getValue().trim().isEmpty());
    }

    @Override
    public void init() {
        this.setKeyboardHandlerSendRepeatsToGui(true);
        int var0 = this.width() / 2 - 106;
        this.buttonsAdd(
            this.doneButton = new RealmsButton(1, var0 - 2, RealmsConstants.row(12), 106, 20, getLocalizedString("mco.configure.world.buttons.done")) {
                @Override
                public void onPress() {
                    RealmsSettingsScreen.this.save();
                }
            }
        );
        this.buttonsAdd(new RealmsButton(0, this.width() / 2 + 2, RealmsConstants.row(12), 106, 20, getLocalizedString("gui.cancel")) {
            @Override
            public void onPress() {
                Realms.setScreen(RealmsSettingsScreen.this.configureWorldScreen);
            }
        });
        this.buttonsAdd(
            new RealmsButton(
                5,
                this.width() / 2 - 53,
                RealmsConstants.row(0),
                106,
                20,
                getLocalizedString(
                    this.serverData.state.equals(RealmsServer.State.OPEN) ? "mco.configure.world.buttons.close" : "mco.configure.world.buttons.open"
                )
            ) {
                @Override
                public void onPress() {
                    if (RealmsSettingsScreen.this.serverData.state.equals(RealmsServer.State.OPEN)) {
                        String var0 = RealmsScreen.getLocalizedString("mco.configure.world.close.question.line1");
                        String var1 = RealmsScreen.getLocalizedString("mco.configure.world.close.question.line2");
                        Realms.setScreen(
                            new RealmsLongConfirmationScreen(RealmsSettingsScreen.this, RealmsLongConfirmationScreen.Type.Info, var0, var1, true, 5)
                        );
                    } else {
                        RealmsSettingsScreen.this.configureWorldScreen.openTheWorld(false, RealmsSettingsScreen.this);
                    }
    
                }
            }
        );
        this.nameEdit = this.newEditBox(2, var0, RealmsConstants.row(4), 212, 20, getLocalizedString("mco.configure.world.name"));
        this.nameEdit.setMaxLength(32);
        if (this.serverData.getName() != null) {
            this.nameEdit.setValue(this.serverData.getName());
        }

        this.addWidget(this.nameEdit);
        this.focusOn(this.nameEdit);
        this.descEdit = this.newEditBox(3, var0, RealmsConstants.row(8), 212, 20, getLocalizedString("mco.configure.world.description"));
        this.descEdit.setMaxLength(32);
        if (this.serverData.getDescription() != null) {
            this.descEdit.setValue(this.serverData.getDescription());
        }

        this.addWidget(this.descEdit);
        this.addWidget(this.titleLabel = new RealmsLabel(getLocalizedString("mco.configure.world.settings.title"), this.width() / 2, 17, 16777215));
        this.narrateLabels();
    }

    @Override
    public void removed() {
        this.setKeyboardHandlerSendRepeatsToGui(false);
    }

    @Override
    public void confirmResult(boolean param0, int param1) {
        switch(param1) {
            case 5:
                if (param0) {
                    this.configureWorldScreen.closeTheWorld(this);
                } else {
                    Realms.setScreen(this);
                }
        }
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        switch(param0) {
            case 256:
                Realms.setScreen(this.configureWorldScreen);
                return true;
            default:
                return super.keyPressed(param0, param1, param2);
        }
    }

    @Override
    public void render(int param0, int param1, float param2) {
        this.renderBackground();
        this.titleLabel.render(this);
        this.drawString(getLocalizedString("mco.configure.world.name"), this.width() / 2 - 106, RealmsConstants.row(3), 10526880);
        this.drawString(getLocalizedString("mco.configure.world.description"), this.width() / 2 - 106, RealmsConstants.row(7), 10526880);
        this.nameEdit.render(param0, param1, param2);
        this.descEdit.render(param0, param1, param2);
        super.render(param0, param1, param2);
    }

    public void save() {
        this.configureWorldScreen.saveSettings(this.nameEdit.getValue(), this.descEdit.getValue());
    }
}
