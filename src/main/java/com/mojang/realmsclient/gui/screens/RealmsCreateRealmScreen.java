package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.util.task.WorldCreationTask;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.realms.RealmsLabel;
import net.minecraft.realms.RealmsScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsCreateRealmScreen extends RealmsScreen {
    private final RealmsServer server;
    private final RealmsMainScreen lastScreen;
    private EditBox nameBox;
    private EditBox descriptionBox;
    private Button createButton;
    private RealmsLabel createRealmLabel;

    public RealmsCreateRealmScreen(RealmsServer param0, RealmsMainScreen param1) {
        this.server = param0;
        this.lastScreen = param1;
    }

    @Override
    public void tick() {
        if (this.nameBox != null) {
            this.nameBox.tick();
        }

        if (this.descriptionBox != null) {
            this.descriptionBox.tick();
        }

    }

    @Override
    public void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.createButton = this.addButton(
            new Button(this.width / 2 - 100, this.height / 4 + 120 + 17, 97, 20, new TranslatableComponent("mco.create.world"), param0 -> this.createWorld())
        );
        this.addButton(
            new Button(this.width / 2 + 5, this.height / 4 + 120 + 17, 95, 20, CommonComponents.GUI_CANCEL, param0 -> this.minecraft.setScreen(this.lastScreen))
        );
        this.createButton.active = false;
        this.nameBox = new EditBox(this.minecraft.font, this.width / 2 - 100, 65, 200, 20, null, new TranslatableComponent("mco.configure.world.name"));
        this.addWidget(this.nameBox);
        this.setInitialFocus(this.nameBox);
        this.descriptionBox = new EditBox(
            this.minecraft.font, this.width / 2 - 100, 115, 200, 20, null, new TranslatableComponent("mco.configure.world.description")
        );
        this.addWidget(this.descriptionBox);
        this.createRealmLabel = new RealmsLabel(new TranslatableComponent("mco.selectServer.create"), this.width / 2, 11, 16777215);
        this.addWidget(this.createRealmLabel);
        this.narrateLabels();
    }

    @Override
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
    }

    @Override
    public boolean charTyped(char param0, int param1) {
        boolean var0 = super.charTyped(param0, param1);
        this.createButton.active = this.valid();
        return var0;
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        if (param0 == 256) {
            this.minecraft.setScreen(this.lastScreen);
            return true;
        } else {
            boolean var0 = super.keyPressed(param0, param1, param2);
            this.createButton.active = this.valid();
            return var0;
        }
    }

    private void createWorld() {
        if (this.valid()) {
            RealmsResetWorldScreen var0 = new RealmsResetWorldScreen(
                this.lastScreen,
                this.server,
                new TranslatableComponent("mco.selectServer.create"),
                new TranslatableComponent("mco.create.world.subtitle"),
                10526880,
                new TranslatableComponent("mco.create.world.skip"),
                () -> this.minecraft.setScreen(this.lastScreen.newScreen()),
                () -> this.minecraft.setScreen(this.lastScreen.newScreen())
            );
            var0.setResetTitle(I18n.get("mco.create.world.reset.title"));
            this.minecraft
                .setScreen(
                    new RealmsLongRunningMcoTaskScreen(
                        this.lastScreen, new WorldCreationTask(this.server.id, this.nameBox.getValue(), this.descriptionBox.getValue(), var0)
                    )
                );
        }

    }

    private boolean valid() {
        return !this.nameBox.getValue().trim().isEmpty();
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        this.createRealmLabel.render(this, param0);
        this.font.draw(param0, I18n.get("mco.configure.world.name"), (float)(this.width / 2 - 100), 52.0F, 10526880);
        this.font.draw(param0, I18n.get("mco.configure.world.description"), (float)(this.width / 2 - 100), 102.0F, 10526880);
        if (this.nameBox != null) {
            this.nameBox.render(param0, param1, param2, param3);
        }

        if (this.descriptionBox != null) {
            this.descriptionBox.render(param0, param1, param2, param3);
        }

        super.render(param0, param1, param2, param3);
    }
}
