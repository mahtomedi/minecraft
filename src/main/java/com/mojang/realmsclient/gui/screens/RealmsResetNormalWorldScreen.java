package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
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
public class RealmsResetNormalWorldScreen extends RealmsScreen {
    private static final Component SEED_LABEL = new TranslatableComponent("mco.reset.world.seed");
    private static final Component[] LEVEL_TYPES = new Component[]{
        new TranslatableComponent("generator.default"),
        new TranslatableComponent("generator.flat"),
        new TranslatableComponent("generator.large_biomes"),
        new TranslatableComponent("generator.amplified")
    };
    private final RealmsResetWorldScreen lastScreen;
    private RealmsLabel titleLabel;
    private EditBox seedEdit;
    private Boolean generateStructures = true;
    private Integer levelTypeIndex = 0;
    private final Component buttonTitle;

    public RealmsResetNormalWorldScreen(RealmsResetWorldScreen param0, Component param1) {
        this.lastScreen = param0;
        this.buttonTitle = param1;
    }

    @Override
    public void tick() {
        this.seedEdit.tick();
        super.tick();
    }

    @Override
    public void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.titleLabel = new RealmsLabel(new TranslatableComponent("mco.reset.world.generate"), this.width / 2, 17, 16777215);
        this.addWidget(this.titleLabel);
        this.seedEdit = new EditBox(this.minecraft.font, this.width / 2 - 100, row(2), 200, 20, null, new TranslatableComponent("mco.reset.world.seed"));
        this.seedEdit.setMaxLength(32);
        this.addWidget(this.seedEdit);
        this.setInitialFocus(this.seedEdit);
        this.addButton(new Button(this.width / 2 - 102, row(4), 205, 20, this.levelTypeTitle(), param0 -> {
            this.levelTypeIndex = (this.levelTypeIndex + 1) % LEVEL_TYPES.length;
            param0.setMessage(this.levelTypeTitle());
        }));
        this.addButton(new Button(this.width / 2 - 102, row(6) - 2, 205, 20, this.generateStructuresTitle(), param0 -> {
            this.generateStructures = !this.generateStructures;
            param0.setMessage(this.generateStructuresTitle());
        }));
        this.addButton(
            new Button(
                this.width / 2 - 102,
                row(12),
                97,
                20,
                this.buttonTitle,
                param0 -> this.lastScreen
                        .resetWorld(new RealmsResetWorldScreen.ResetWorldInfo(this.seedEdit.getValue(), this.levelTypeIndex, this.generateStructures))
            )
        );
        this.addButton(new Button(this.width / 2 + 8, row(12), 97, 20, CommonComponents.GUI_BACK, param0 -> this.minecraft.setScreen(this.lastScreen)));
        this.narrateLabels();
    }

    @Override
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        if (param0 == 256) {
            this.minecraft.setScreen(this.lastScreen);
            return true;
        } else {
            return super.keyPressed(param0, param1, param2);
        }
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        this.titleLabel.render(this, param0);
        this.font.draw(param0, SEED_LABEL, (float)(this.width / 2 - 100), (float)row(1), 10526880);
        this.seedEdit.render(param0, param1, param2, param3);
        super.render(param0, param1, param2, param3);
    }

    private Component levelTypeTitle() {
        return new TranslatableComponent("selectWorld.mapType").append(" ").append(LEVEL_TYPES[this.levelTypeIndex]);
    }

    private Component generateStructuresTitle() {
        return CommonComponents.optionStatus(new TranslatableComponent("selectWorld.mapFeatures"), this.generateStructures);
    }
}
