package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.net.IDN;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.StringUtil;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EditServerScreen extends Screen {
    private Button addButton;
    private final BooleanConsumer callback;
    private final ServerData serverData;
    private EditBox ipEdit;
    private EditBox nameEdit;
    private Button serverPackButton;
    private final Screen lastScreen;
    private final Predicate<String> addressFilter = param0x -> {
        if (StringUtil.isNullOrEmpty(param0x)) {
            return true;
        } else {
            String[] var0 = param0x.split(":");
            if (var0.length == 0) {
                return true;
            } else {
                try {
                    String var2x = IDN.toASCII(var0[0]);
                    return true;
                } catch (IllegalArgumentException var3x) {
                    return false;
                }
            }
        }
    };

    public EditServerScreen(Screen param0, BooleanConsumer param1, ServerData param2) {
        super(new TranslatableComponent("addServer.title"));
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
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.nameEdit = new EditBox(this.font, this.width / 2 - 100, 66, 200, 20, new TranslatableComponent("addServer.enterName"));
        this.nameEdit.setFocus(true);
        this.nameEdit.setValue(this.serverData.name);
        this.nameEdit.setResponder(this::onEdited);
        this.children.add(this.nameEdit);
        this.ipEdit = new EditBox(this.font, this.width / 2 - 100, 106, 200, 20, new TranslatableComponent("addServer.enterIp"));
        this.ipEdit.setMaxLength(128);
        this.ipEdit.setValue(this.serverData.ip);
        this.ipEdit.setFilter(this.addressFilter);
        this.ipEdit.setResponder(this::onEdited);
        this.children.add(this.ipEdit);
        this.serverPackButton = this.addButton(
            new Button(
                this.width / 2 - 100,
                this.height / 4 + 72,
                200,
                20,
                createServerButtonText(this.serverData.getResourcePackStatus()),
                param0 -> {
                    this.serverData
                        .setResourcePackStatus(
                            ServerData.ServerPackStatus.values()[(this.serverData.getResourcePackStatus().ordinal() + 1)
                                % ServerData.ServerPackStatus.values().length]
                        );
                    this.serverPackButton.setMessage(createServerButtonText(this.serverData.getResourcePackStatus()));
                }
            )
        );
        this.addButton = this.addButton(
            new Button(this.width / 2 - 100, this.height / 4 + 96 + 18, 200, 20, new TranslatableComponent("addServer.add"), param0 -> this.onAdd())
        );
        this.addButton(
            new Button(this.width / 2 - 100, this.height / 4 + 120 + 18, 200, 20, CommonComponents.GUI_CANCEL, param0 -> this.callback.accept(false))
        );
        this.cleanUp();
    }

    private static Component createServerButtonText(ServerData.ServerPackStatus param0) {
        return new TranslatableComponent("addServer.resourcePack").append(": ").append(param0.getName());
    }

    @Override
    public void resize(Minecraft param0, int param1, int param2) {
        String var0 = this.ipEdit.getValue();
        String var1 = this.nameEdit.getValue();
        this.init(param0, param1, param2);
        this.ipEdit.setValue(var0);
        this.nameEdit.setValue(var1);
    }

    private void onEdited(String param0) {
        this.cleanUp();
    }

    @Override
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
    }

    private void onAdd() {
        this.serverData.name = this.nameEdit.getValue();
        this.serverData.ip = this.ipEdit.getValue();
        this.callback.accept(true);
    }

    @Override
    public void onClose() {
        this.cleanUp();
        this.minecraft.setScreen(this.lastScreen);
    }

    private void cleanUp() {
        String var0 = this.ipEdit.getValue();
        boolean var1 = !var0.isEmpty() && var0.split(":").length > 0 && var0.indexOf(32) == -1;
        this.addButton.active = var1 && !this.nameEdit.getValue().isEmpty();
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        this.drawCenteredString(param0, this.font, this.title, this.width / 2, 17, 16777215);
        this.drawString(param0, this.font, I18n.get("addServer.enterName"), this.width / 2 - 100, 53, 10526880);
        this.drawString(param0, this.font, I18n.get("addServer.enterIp"), this.width / 2 - 100, 94, 10526880);
        this.nameEdit.render(param0, param1, param2, param3);
        this.ipEdit.render(param0, param1, param2, param3);
        super.render(param0, param1, param2, param3);
    }
}
