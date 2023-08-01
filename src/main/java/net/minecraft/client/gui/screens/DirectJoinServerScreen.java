package net.minecraft.client.gui.screens;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DirectJoinServerScreen extends Screen {
    private static final Component ENTER_IP_LABEL = Component.translatable("addServer.enterIp");
    private Button selectButton;
    private final ServerData serverData;
    private EditBox ipEdit;
    private final BooleanConsumer callback;
    private final Screen lastScreen;

    public DirectJoinServerScreen(Screen param0, BooleanConsumer param1, ServerData param2) {
        super(Component.translatable("selectServer.direct"));
        this.lastScreen = param0;
        this.serverData = param2;
        this.callback = param1;
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        if (!this.selectButton.active || this.getFocused() != this.ipEdit || param0 != 257 && param0 != 335) {
            return super.keyPressed(param0, param1, param2);
        } else {
            this.onSelect();
            return true;
        }
    }

    @Override
    protected void init() {
        this.ipEdit = new EditBox(this.font, this.width / 2 - 100, 116, 200, 20, Component.translatable("addServer.enterIp"));
        this.ipEdit.setMaxLength(128);
        this.ipEdit.setValue(this.minecraft.options.lastMpIp);
        this.ipEdit.setResponder(param0 -> this.updateSelectButtonStatus());
        this.addWidget(this.ipEdit);
        this.selectButton = this.addRenderableWidget(
            Button.builder(Component.translatable("selectServer.select"), param0 -> this.onSelect())
                .bounds(this.width / 2 - 100, this.height / 4 + 96 + 12, 200, 20)
                .build()
        );
        this.addRenderableWidget(
            Button.builder(CommonComponents.GUI_CANCEL, param0 -> this.callback.accept(false))
                .bounds(this.width / 2 - 100, this.height / 4 + 120 + 12, 200, 20)
                .build()
        );
        this.setInitialFocus(this.ipEdit);
        this.updateSelectButtonStatus();
    }

    @Override
    public void resize(Minecraft param0, int param1, int param2) {
        String var0 = this.ipEdit.getValue();
        this.init(param0, param1, param2);
        this.ipEdit.setValue(var0);
    }

    private void onSelect() {
        this.serverData.ip = this.ipEdit.getValue();
        this.callback.accept(true);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
    }

    @Override
    public void removed() {
        this.minecraft.options.lastMpIp = this.ipEdit.getValue();
        this.minecraft.options.save();
    }

    private void updateSelectButtonStatus() {
        this.selectButton.active = ServerAddress.isValidAddress(this.ipEdit.getValue());
    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        super.render(param0, param1, param2, param3);
        param0.drawCenteredString(this.font, this.title, this.width / 2, 20, 16777215);
        param0.drawString(this.font, ENTER_IP_LABEL, this.width / 2 - 100, 100, 10526880);
        this.ipEdit.render(param0, param1, param2, param3);
    }
}
