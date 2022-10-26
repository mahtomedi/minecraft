package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.HttpUtil;
import net.minecraft.world.level.GameType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ShareToLanScreen extends Screen {
    private static final Component ALLOW_COMMANDS_LABEL = Component.translatable("selectWorld.allowCommands");
    private static final Component GAME_MODE_LABEL = Component.translatable("selectWorld.gameMode");
    private static final Component INFO_TEXT = Component.translatable("lanServer.otherPlayers");
    private final Screen lastScreen;
    private GameType gameMode = GameType.SURVIVAL;
    private boolean commands;

    public ShareToLanScreen(Screen param0) {
        super(Component.translatable("lanServer.title"));
        this.lastScreen = param0;
    }

    @Override
    protected void init() {
        this.addRenderableWidget(
            CycleButton.builder(GameType::getShortDisplayName)
                .withValues(GameType.SURVIVAL, GameType.SPECTATOR, GameType.CREATIVE, GameType.ADVENTURE)
                .withInitialValue(this.gameMode)
                .create(this.width / 2 - 155, 100, 150, 20, GAME_MODE_LABEL, (param0, param1) -> this.gameMode = param1)
        );
        this.addRenderableWidget(
            CycleButton.onOffBuilder(this.commands).create(this.width / 2 + 5, 100, 150, 20, ALLOW_COMMANDS_LABEL, (param0, param1) -> this.commands = param1)
        );
        this.addRenderableWidget(Button.builder(Component.translatable("lanServer.start"), param0 -> {
            this.minecraft.setScreen(null);
            int var0 = HttpUtil.getAvailablePort();
            Component var1;
            if (this.minecraft.getSingleplayerServer().publishServer(this.gameMode, this.commands, var0)) {
                var1 = Component.translatable("commands.publish.started", var0);
            } else {
                var1 = Component.translatable("commands.publish.failed");
            }

            this.minecraft.gui.getChat().addMessage(var1);
            this.minecraft.updateTitle();
        }).bounds(this.width / 2 - 155, this.height - 28, 150, 20).build());
        this.addRenderableWidget(
            Button.builder(CommonComponents.GUI_CANCEL, param0 -> this.minecraft.setScreen(this.lastScreen))
                .bounds(this.width / 2 + 5, this.height - 28, 150, 20)
                .build()
        );
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        drawCenteredString(param0, this.font, this.title, this.width / 2, 50, 16777215);
        drawCenteredString(param0, this.font, INFO_TEXT, this.width / 2, 82, 16777215);
        super.render(param0, param1, param2, param3);
    }
}
