package net.minecraft.client.gui.screens;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.net.IDN;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.resources.language.I18n;
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
                } catch (IllegalArgumentException var3) {
                    return false;
                }
            }
        }
    };

    public EditServerScreen(BooleanConsumer param0, ServerData param1) {
        super(new TranslatableComponent("addServer.title"));
        this.callback = param0;
        this.serverData = param1;
    }

    @Override
    public void tick() {
        this.nameEdit.tick();
        this.ipEdit.tick();
    }

    @Override
    protected void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.nameEdit = new EditBox(this.font, this.width / 2 - 100, 66, 200, 20, I18n.get("addServer.enterName"));
        this.nameEdit.setFocus(true);
        this.nameEdit.setValue(this.serverData.name);
        this.nameEdit.setResponder(this::onEdited);
        this.children.add(this.nameEdit);
        this.ipEdit = new EditBox(this.font, this.width / 2 - 100, 106, 200, 20, I18n.get("addServer.enterIp"));
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
                I18n.get("addServer.resourcePack") + ": " + this.serverData.getResourcePackStatus().getName().getColoredString(),
                param0 -> {
                    this.serverData
                        .setResourcePackStatus(
                            ServerData.ServerPackStatus.values()[(this.serverData.getResourcePackStatus().ordinal() + 1)
                                % ServerData.ServerPackStatus.values().length]
                        );
                    this.serverPackButton
                        .setMessage(I18n.get("addServer.resourcePack") + ": " + this.serverData.getResourcePackStatus().getName().getColoredString());
                }
            )
        );
        this.addButton = this.addButton(new Button(this.width / 2 - 100, this.height / 4 + 96 + 18, 200, 20, I18n.get("addServer.add"), param0 -> this.onAdd()));
        this.addButton(new Button(this.width / 2 - 100, this.height / 4 + 120 + 18, 200, 20, I18n.get("gui.cancel"), param0 -> this.callback.accept(false)));
        this.onClose();
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
        this.onClose();
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
        this.addButton.active = !this.ipEdit.getValue().isEmpty() && this.ipEdit.getValue().split(":").length > 0 && !this.nameEdit.getValue().isEmpty();
    }

    @Override
    public void render(int param0, int param1, float param2) {
        this.renderBackground();
        this.drawCenteredString(this.font, this.title.getColoredString(), this.width / 2, 17, 16777215);
        this.drawString(this.font, I18n.get("addServer.enterName"), this.width / 2 - 100, 53, 10526880);
        this.drawString(this.font, I18n.get("addServer.enterIp"), this.width / 2 - 100, 94, 10526880);
        this.nameEdit.render(param0, param1, param2);
        this.ipEdit.render(param0, param1, param2);
        super.render(param0, param1, param2);
    }
}
