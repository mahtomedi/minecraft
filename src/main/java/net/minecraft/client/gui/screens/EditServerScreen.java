package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EditServerScreen extends Screen {
    private static final Component NAME_LABEL = Component.translatable("addServer.enterName");
    private static final Component IP_LABEL = Component.translatable("addServer.enterIp");
    private Button addButton;
    private final BooleanConsumer callback;
    private final ServerData serverData;
    private EditBox ipEdit;
    private EditBox nameEdit;
    private final Screen lastScreen;

    public EditServerScreen(Screen param0, BooleanConsumer param1, ServerData param2) {
        super(Component.translatable("addServer.title"));
        this.lastScreen = param0;
        this.callback = param1;
        this.serverData = param2;
    }

    @Override
    public void tick() {
        this.nameEdit.tick();
        this.ipEdit.tick();
    }

    @Override
    protected void init() {
        this.nameEdit = new EditBox(this.font, this.width / 2 - 100, 66, 200, 20, Component.translatable("addServer.enterName"));
        this.nameEdit.setValue(this.serverData.name);
        this.nameEdit.setResponder(param0 -> this.updateAddButtonStatus());
        this.addWidget(this.nameEdit);
        this.ipEdit = new EditBox(this.font, this.width / 2 - 100, 106, 200, 20, Component.translatable("addServer.enterIp"));
        this.ipEdit.setMaxLength(128);
        this.ipEdit.setValue(this.serverData.ip);
        this.ipEdit.setResponder(param0 -> this.updateAddButtonStatus());
        this.addWidget(this.ipEdit);
        this.addRenderableWidget(
            CycleButton.builder(ServerData.ServerPackStatus::getName)
                .withValues(ServerData.ServerPackStatus.values())
                .withInitialValue(this.serverData.getResourcePackStatus())
                .create(
                    this.width / 2 - 100,
                    this.height / 4 + 72,
                    200,
                    20,
                    Component.translatable("addServer.resourcePack"),
                    (param0, param1) -> this.serverData.setResourcePackStatus(param1)
                )
        );
        this.addButton = this.addRenderableWidget(
            Button.builder(Component.translatable("addServer.add"), param0 -> this.onAdd())
                .bounds(this.width / 2 - 100, this.height / 4 + 96 + 18, 200, 20)
                .build()
        );
        this.addRenderableWidget(
            Button.builder(CommonComponents.GUI_CANCEL, param0 -> this.callback.accept(false))
                .bounds(this.width / 2 - 100, this.height / 4 + 120 + 18, 200, 20)
                .build()
        );
        this.setInitialFocus(this.nameEdit);
        this.updateAddButtonStatus();
    }

    @Override
    public void resize(Minecraft param0, int param1, int param2) {
        String var0 = this.ipEdit.getValue();
        String var1 = this.nameEdit.getValue();
        this.init(param0, param1, param2);
        this.ipEdit.setValue(var0);
        this.nameEdit.setValue(var1);
    }

    private void onAdd() {
        this.serverData.name = this.nameEdit.getValue();
        this.serverData.ip = this.ipEdit.getValue();
        this.callback.accept(true);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
    }

    private void updateAddButtonStatus() {
        this.addButton.active = ServerAddress.isValidAddress(this.ipEdit.getValue()) && !this.nameEdit.getValue().isEmpty();
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        drawCenteredString(param0, this.font, this.title, this.width / 2, 17, 16777215);
        drawString(param0, this.font, NAME_LABEL, this.width / 2 - 100, 53, 10526880);
        drawString(param0, this.font, IP_LABEL, this.width / 2 - 100, 94, 10526880);
        this.nameEdit.render(param0, param1, param2, param3);
        this.ipEdit.render(param0, param1, param2, param3);
        super.render(param0, param1, param2, param3);
    }
}
