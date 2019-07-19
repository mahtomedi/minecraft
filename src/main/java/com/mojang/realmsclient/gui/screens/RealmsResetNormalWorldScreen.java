package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.gui.RealmsConstants;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsEditBox;
import net.minecraft.realms.RealmsLabel;
import net.minecraft.realms.RealmsScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsResetNormalWorldScreen extends RealmsScreen {
    private final RealmsResetWorldScreen lastScreen;
    private RealmsLabel titleLabel;
    private RealmsEditBox seedEdit;
    private Boolean generateStructures = true;
    private Integer levelTypeIndex = 0;
    String[] levelTypes;
    private final int BUTTON_CANCEL_ID = 0;
    private final int BUTTON_RESET_ID = 1;
    private final int SEED_EDIT_BOX = 4;
    private RealmsButton resetButton;
    private RealmsButton levelTypeButton;
    private RealmsButton generateStructuresButton;
    private String buttonTitle = getLocalizedString("mco.backup.button.reset");

    public RealmsResetNormalWorldScreen(RealmsResetWorldScreen param0) {
        this.lastScreen = param0;
    }

    public RealmsResetNormalWorldScreen(RealmsResetWorldScreen param0, String param1) {
        this(param0);
        this.buttonTitle = param1;
    }

    @Override
    public void tick() {
        this.seedEdit.tick();
        super.tick();
    }

    @Override
    public void init() {
        this.levelTypes = new String[]{
            getLocalizedString("generator.default"),
            getLocalizedString("generator.flat"),
            getLocalizedString("generator.largeBiomes"),
            getLocalizedString("generator.amplified")
        };
        this.setKeyboardHandlerSendRepeatsToGui(true);
        this.buttonsAdd(new RealmsButton(0, this.width() / 2 + 8, RealmsConstants.row(12), 97, 20, getLocalizedString("gui.back")) {
            @Override
            public void onPress() {
                Realms.setScreen(RealmsResetNormalWorldScreen.this.lastScreen);
            }
        });
        this.buttonsAdd(this.resetButton = new RealmsButton(1, this.width() / 2 - 102, RealmsConstants.row(12), 97, 20, this.buttonTitle) {
            @Override
            public void onPress() {
                RealmsResetNormalWorldScreen.this.onReset();
            }
        });
        this.seedEdit = this.newEditBox(4, this.width() / 2 - 100, RealmsConstants.row(2), 200, 20, getLocalizedString("mco.reset.world.seed"));
        this.seedEdit.setMaxLength(32);
        this.seedEdit.setValue("");
        this.addWidget(this.seedEdit);
        this.focusOn(this.seedEdit);
        this.buttonsAdd(
            this.levelTypeButton = new RealmsButton(2, this.width() / 2 - 102, RealmsConstants.row(4), 205, 20, this.levelTypeTitle()) {
                @Override
                public void onPress() {
                    RealmsResetNormalWorldScreen.this.levelTypeIndex = (RealmsResetNormalWorldScreen.this.levelTypeIndex + 1)
                        % RealmsResetNormalWorldScreen.this.levelTypes.length;
                    this.setMessage(RealmsResetNormalWorldScreen.this.levelTypeTitle());
                }
            }
        );
        this.buttonsAdd(
            this.generateStructuresButton = new RealmsButton(3, this.width() / 2 - 102, RealmsConstants.row(6) - 2, 205, 20, this.generateStructuresTitle()) {
                @Override
                public void onPress() {
                    RealmsResetNormalWorldScreen.this.generateStructures = !RealmsResetNormalWorldScreen.this.generateStructures;
                    this.setMessage(RealmsResetNormalWorldScreen.this.generateStructuresTitle());
                }
            }
        );
        this.titleLabel = new RealmsLabel(getLocalizedString("mco.reset.world.generate"), this.width() / 2, 17, 16777215);
        this.addWidget(this.titleLabel);
        this.narrateLabels();
    }

    @Override
    public void removed() {
        this.setKeyboardHandlerSendRepeatsToGui(false);
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        if (param0 == 256) {
            Realms.setScreen(this.lastScreen);
            return true;
        } else {
            return super.keyPressed(param0, param1, param2);
        }
    }

    private void onReset() {
        this.lastScreen.resetWorld(new RealmsResetWorldScreen.ResetWorldInfo(this.seedEdit.getValue(), this.levelTypeIndex, this.generateStructures));
    }

    @Override
    public void render(int param0, int param1, float param2) {
        this.renderBackground();
        this.titleLabel.render(this);
        this.drawString(getLocalizedString("mco.reset.world.seed"), this.width() / 2 - 100, RealmsConstants.row(1), 10526880);
        this.seedEdit.render(param0, param1, param2);
        super.render(param0, param1, param2);
    }

    private String levelTypeTitle() {
        String var0 = getLocalizedString("selectWorld.mapType");
        return var0 + " " + this.levelTypes[this.levelTypeIndex];
    }

    private String generateStructuresTitle() {
        return getLocalizedString("selectWorld.mapFeatures")
            + " "
            + getLocalizedString(this.generateStructures ? "mco.configure.world.on" : "mco.configure.world.off");
    }
}
