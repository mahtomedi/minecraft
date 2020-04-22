package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DirectJoinServerScreen extends Screen {
    private Button selectButton;
    private final ServerData serverData;
    private EditBox ipEdit;
    private final BooleanConsumer callback;
    private final Screen lastScreen;

    public DirectJoinServerScreen(Screen param0, BooleanConsumer param1, ServerData param2) {
        super(new TranslatableComponent("selectServer.direct"));
        this.lastScreen = param0;
        this.serverData = param2;
        this.callback = param1;
    }

    @Override
    public void tick() {
        this.ipEdit.tick();
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        if (this.getFocused() != this.ipEdit || param0 != 257 && param0 != 335) {
            return super.keyPressed(param0, param1, param2);
        } else {
            this.onSelect();
            return true;
        }
    }

    @Override
    protected void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.selectButton = this.addButton(
            new Button(this.width / 2 - 100, this.height / 4 + 96 + 12, 200, 20, new TranslatableComponent("selectServer.select"), param0 -> this.onSelect())
        );
        this.addButton(
            new Button(this.width / 2 - 100, this.height / 4 + 120 + 12, 200, 20, CommonComponents.GUI_CANCEL, param0 -> this.callback.accept(false))
        );
        this.ipEdit = new EditBox(this.font, this.width / 2 - 100, 116, 200, 20, new TranslatableComponent("addServer.enterIp"));
        this.ipEdit.setMaxLength(128);
        this.ipEdit.setFocus(true);
        this.ipEdit.setValue(this.minecraft.options.lastMpIp);
        this.ipEdit.setResponder(param0 -> this.updateSelectButtonStatus());
        this.children.add(this.ipEdit);
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
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
        this.minecraft.options.lastMpIp = this.ipEdit.getValue();
        this.minecraft.options.save();
    }

    private void updateSelectButtonStatus() {
        String var0 = this.ipEdit.getValue();
        this.selectButton.active = !var0.isEmpty() && var0.split(":").length > 0 && var0.indexOf(32) == -1;
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        this.drawCenteredString(param0, this.font, this.title, this.width / 2, 20, 16777215);
        this.drawString(param0, this.font, I18n.get("addServer.enterIp"), this.width / 2 - 100, 100, 10526880);
        this.ipEdit.render(param0, param1, param2, param3);
        super.render(param0, param1, param2, param3);
    }
}
