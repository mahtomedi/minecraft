package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.util.task.WorldCreationTask;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsCreateRealmScreen extends RealmsScreen {
    private static final Component NAME_LABEL = Component.translatable("mco.configure.world.name");
    private static final Component DESCRIPTION_LABEL = Component.translatable("mco.configure.world.description");
    private final RealmsServer server;
    private final RealmsMainScreen lastScreen;
    private EditBox nameBox;
    private EditBox descriptionBox;
    private Button createButton;

    public RealmsCreateRealmScreen(RealmsServer param0, RealmsMainScreen param1) {
        super(Component.translatable("mco.selectServer.create"));
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
        this.createButton = this.addRenderableWidget(
            new Button(this.width / 2 - 100, this.height / 4 + 120 + 17, 97, 20, Component.translatable("mco.create.world"), param0 -> this.createWorld())
        );
        this.addRenderableWidget(
            new Button(this.width / 2 + 5, this.height / 4 + 120 + 17, 95, 20, CommonComponents.GUI_CANCEL, param0 -> this.minecraft.setScreen(this.lastScreen))
        );
        this.createButton.active = false;
        this.nameBox = new EditBox(this.minecraft.font, this.width / 2 - 100, 65, 200, 20, null, Component.translatable("mco.configure.world.name"));
        this.addWidget(this.nameBox);
        this.setInitialFocus(this.nameBox);
        this.descriptionBox = new EditBox(
            this.minecraft.font, this.width / 2 - 100, 115, 200, 20, null, Component.translatable("mco.configure.world.description")
        );
        this.addWidget(this.descriptionBox);
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
                Component.translatable("mco.selectServer.create"),
                Component.translatable("mco.create.world.subtitle"),
                10526880,
                Component.translatable("mco.create.world.skip"),
                () -> this.minecraft.execute(() -> this.minecraft.setScreen(this.lastScreen.newScreen())),
                () -> this.minecraft.setScreen(this.lastScreen.newScreen())
            );
            var0.setResetTitle(Component.translatable("mco.create.world.reset.title"));
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
        drawCenteredString(param0, this.font, this.title, this.width / 2, 11, 16777215);
        this.font.draw(param0, NAME_LABEL, (float)(this.width / 2 - 100), 52.0F, 10526880);
        this.font.draw(param0, DESCRIPTION_LABEL, (float)(this.width / 2 - 100), 102.0F, 10526880);
        if (this.nameBox != null) {
            this.nameBox.render(param0, param1, param2, param3);
        }

        if (this.descriptionBox != null) {
            this.descriptionBox.render(param0, param1, param2, param3);
        }

        super.render(param0, param1, param2, param3);
    }
}
