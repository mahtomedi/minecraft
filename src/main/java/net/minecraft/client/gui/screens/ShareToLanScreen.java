package net.minecraft.client.gui.screens;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.HttpUtil;
import net.minecraft.world.level.GameType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ShareToLanScreen extends Screen {
    private final Screen lastScreen;
    private Button commandsButton;
    private Button modeButton;
    private String gameModeName = "survival";
    private boolean commands;

    public ShareToLanScreen(Screen param0) {
        super(new TranslatableComponent("lanServer.title"));
        this.lastScreen = param0;
    }

    @Override
    protected void init() {
        this.addButton(new Button(this.width / 2 - 155, this.height - 28, 150, 20, I18n.get("lanServer.start"), param0 -> {
            this.minecraft.setScreen(null);
            int var0 = HttpUtil.getAvailablePort();
            Component var1;
            if (this.minecraft.getSingleplayerServer().publishServer(GameType.byName(this.gameModeName), this.commands, var0)) {
                var1 = new TranslatableComponent("commands.publish.started", var0);
            } else {
                var1 = new TranslatableComponent("commands.publish.failed");
            }

            this.minecraft.gui.getChat().addMessage(var1);
            this.minecraft.updateTitle();
        }));
        this.addButton(new Button(this.width / 2 + 5, this.height - 28, 150, 20, I18n.get("gui.cancel"), param0 -> this.minecraft.setScreen(this.lastScreen)));
        this.modeButton = this.addButton(new Button(this.width / 2 - 155, 100, 150, 20, I18n.get("selectWorld.gameMode"), param0 -> {
            if ("spectator".equals(this.gameModeName)) {
                this.gameModeName = "creative";
            } else if ("creative".equals(this.gameModeName)) {
                this.gameModeName = "adventure";
            } else if ("adventure".equals(this.gameModeName)) {
                this.gameModeName = "survival";
            } else {
                this.gameModeName = "spectator";
            }

            this.updateSelectionStrings();
        }));
        this.commandsButton = this.addButton(new Button(this.width / 2 + 5, 100, 150, 20, I18n.get("selectWorld.allowCommands"), param0 -> {
            this.commands = !this.commands;
            this.updateSelectionStrings();
        }));
        this.updateSelectionStrings();
    }

    private void updateSelectionStrings() {
        this.modeButton.setMessage(I18n.get("selectWorld.gameMode") + ": " + I18n.get("selectWorld.gameMode." + this.gameModeName));
        this.commandsButton.setMessage(I18n.get("selectWorld.allowCommands") + ' ' + I18n.get(this.commands ? "options.on" : "options.off"));
    }

    @Override
    public void render(int param0, int param1, float param2) {
        this.renderBackground();
        this.drawCenteredString(this.font, this.title.getColoredString(), this.width / 2, 50, 16777215);
        this.drawCenteredString(this.font, I18n.get("lanServer.otherPlayers"), this.width / 2, 82, 16777215);
        super.render(param0, param1, param2);
    }
}
