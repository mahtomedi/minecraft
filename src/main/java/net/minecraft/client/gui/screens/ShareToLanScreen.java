package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.PublishCommand;
import net.minecraft.util.HttpUtil;
import net.minecraft.world.level.GameType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ShareToLanScreen extends Screen {
    private static final Component ALLOW_COMMANDS_LABEL = Component.translatable("selectWorld.allowCommands");
    private static final Component GAME_MODE_LABEL = Component.translatable("selectWorld.gameMode");
    private static final Component INFO_TEXT = Component.translatable("lanServer.otherPlayers");
    private static final Component PORT_INFO_TEXT = Component.translatable("lanServer.port");
    private static final Component INVALID_PORT = Component.translatable("lanServer.port.invalid");
    public static final Component PORT_UNAVAILABLE = Component.translatable("lanServer.port.unavailable");
    public static final int INVALID_PORT_COLOR = 16733525;
    private final Screen lastScreen;
    private GameType gameMode = GameType.SURVIVAL;
    private boolean commands;
    private int port = HttpUtil.getAvailablePort();
    @Nullable
    private EditBox portEdit;

    public ShareToLanScreen(Screen param0) {
        super(Component.translatable("lanServer.title"));
        this.lastScreen = param0;
    }

    @Override
    protected void init() {
        IntegratedServer var0 = this.minecraft.getSingleplayerServer();
        this.gameMode = var0.getDefaultGameType();
        this.commands = var0.getWorldData().getAllowCommands();
        this.addRenderableWidget(
            CycleButton.builder(GameType::getShortDisplayName)
                .withValues(GameType.SURVIVAL, GameType.SPECTATOR, GameType.CREATIVE, GameType.ADVENTURE)
                .withInitialValue(this.gameMode)
                .create(this.width / 2 - 155, 100, 150, 20, GAME_MODE_LABEL, (param0, param1) -> this.gameMode = param1)
        );
        this.addRenderableWidget(
            CycleButton.onOffBuilder(this.commands).create(this.width / 2 + 5, 100, 150, 20, ALLOW_COMMANDS_LABEL, (param0, param1) -> this.commands = param1)
        );
        Button var1 = Button.builder(Component.translatable("lanServer.start"), param1 -> {
            this.minecraft.setScreen(null);
            Component var0x;
            if (var0.publishServer(this.gameMode, this.commands, this.port)) {
                var0x = PublishCommand.getSuccessMessage(this.port);
            } else {
                var0x = Component.translatable("commands.publish.failed");
            }

            this.minecraft.gui.getChat().addMessage(var0x);
            this.minecraft.updateTitle();
        }).bounds(this.width / 2 - 155, this.height - 28, 150, 20).build();
        this.portEdit = new EditBox(this.font, this.width / 2 - 75, 160, 150, 20, Component.translatable("lanServer.port"));
        this.portEdit.setResponder(param1 -> {
            Component var0x = this.tryParsePort(param1);
            this.portEdit.setHint(Component.literal(this.port + "").withStyle(ChatFormatting.DARK_GRAY));
            if (var0x == null) {
                this.portEdit.setTextColor(14737632);
                this.portEdit.setTooltip(null);
                var1.active = true;
            } else {
                this.portEdit.setTextColor(16733525);
                this.portEdit.setTooltip(Tooltip.create(var0x));
                var1.active = false;
            }

        });
        this.portEdit.setHint(Component.literal(this.port + "").withStyle(ChatFormatting.DARK_GRAY));
        this.addRenderableWidget(this.portEdit);
        this.addRenderableWidget(var1);
        this.addRenderableWidget(
            Button.builder(CommonComponents.GUI_CANCEL, param0 -> this.minecraft.setScreen(this.lastScreen))
                .bounds(this.width / 2 + 5, this.height - 28, 150, 20)
                .build()
        );
    }

    @Nullable
    private Component tryParsePort(String param0) {
        if (param0.isBlank()) {
            this.port = HttpUtil.getAvailablePort();
            return null;
        } else {
            try {
                this.port = Integer.parseInt(param0);
                if (this.port < 1 || this.port > 65535) {
                    return INVALID_PORT;
                } else {
                    return !HttpUtil.isPortAvailable(this.port) ? PORT_UNAVAILABLE : null;
                }
            } catch (NumberFormatException var3) {
                this.port = -1;
                return INVALID_PORT;
            }
        }
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        drawCenteredString(param0, this.font, this.title, this.width / 2, 50, 16777215);
        drawCenteredString(param0, this.font, INFO_TEXT, this.width / 2, 82, 16777215);
        drawCenteredString(param0, this.font, PORT_INFO_TEXT, this.width / 2, 142, 16777215);
        super.render(param0, param1, param2, param3);
    }
}
