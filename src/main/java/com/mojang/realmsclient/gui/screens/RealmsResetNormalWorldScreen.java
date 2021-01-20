package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.realmsclient.util.LevelType;
import com.mojang.realmsclient.util.WorldGenerationInfo;
import java.util.function.Consumer;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
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
    private final Consumer<WorldGenerationInfo> callback;
    private RealmsLabel titleLabel;
    private EditBox seedEdit;
    private LevelType levelType = LevelType.DEFAULT;
    private boolean generateStructures = true;
    private final Component buttonTitle;

    public RealmsResetNormalWorldScreen(Consumer<WorldGenerationInfo> param0, Component param1) {
        this.callback = param0;
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
        this.addButton(
            CycleButton.builder(LevelType::getName)
                .withValues(LevelType.values())
                .withInitialValue(this.levelType)
                .create(this.width / 2 - 102, row(4), 205, 20, new TranslatableComponent("selectWorld.mapType"), (param0, param1) -> this.levelType = param1)
        );
        this.addButton(
            CycleButton.onOffBuilder(this.generateStructures)
                .create(
                    this.width / 2 - 102,
                    row(6) - 2,
                    205,
                    20,
                    new TranslatableComponent("selectWorld.mapFeatures"),
                    (param0, param1) -> this.generateStructures = param1
                )
        );
        this.addButton(
            new Button(
                this.width / 2 - 102,
                row(12),
                97,
                20,
                this.buttonTitle,
                param0 -> this.callback.accept(new WorldGenerationInfo(this.seedEdit.getValue(), this.levelType, this.generateStructures))
            )
        );
        this.addButton(new Button(this.width / 2 + 8, row(12), 97, 20, CommonComponents.GUI_BACK, param0 -> this.onClose()));
        this.narrateLabels();
    }

    @Override
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
    }

    @Override
    public void onClose() {
        this.callback.accept(null);
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        this.titleLabel.render(this, param0);
        this.font.draw(param0, SEED_LABEL, (float)(this.width / 2 - 100), (float)row(1), 10526880);
        this.seedEdit.render(param0, param1, param2, param3);
        super.render(param0, param1, param2, param3);
    }
}
