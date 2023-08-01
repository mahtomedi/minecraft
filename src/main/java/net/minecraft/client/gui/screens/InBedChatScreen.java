package net.minecraft.client.gui.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class InBedChatScreen extends ChatScreen {
    private Button leaveBedButton;

    public InBedChatScreen() {
        super("");
    }

    @Override
    protected void init() {
        super.init();
        this.leaveBedButton = Button.builder(Component.translatable("multiplayer.stopSleeping"), param0 -> this.sendWakeUp())
            .bounds(this.width / 2 - 100, this.height - 40, 200, 20)
            .build();
        this.addRenderableWidget(this.leaveBedButton);
    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        if (!this.minecraft.getChatStatus().isChatAllowed(this.minecraft.isLocalServer())) {
            this.leaveBedButton.render(param0, param1, param2, param3);
        } else {
            super.render(param0, param1, param2, param3);
        }
    }

    @Override
    public void renderBackground(GuiGraphics param0, int param1, int param2, float param3) {
    }

    @Override
    public void onClose() {
        this.sendWakeUp();
    }

    @Override
    public boolean charTyped(char param0, int param1) {
        return !this.minecraft.getChatStatus().isChatAllowed(this.minecraft.isLocalServer()) ? true : super.charTyped(param0, param1);
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        if (param0 == 256) {
            this.sendWakeUp();
        }

        if (!this.minecraft.getChatStatus().isChatAllowed(this.minecraft.isLocalServer())) {
            return true;
        } else if (param0 != 257 && param0 != 335) {
            return super.keyPressed(param0, param1, param2);
        } else {
            if (this.handleChatInput(this.input.getValue(), true)) {
                this.minecraft.setScreen(null);
                this.input.setValue("");
                this.minecraft.gui.getChat().resetChatScroll();
            }

            return true;
        }
    }

    private void sendWakeUp() {
        ClientPacketListener var0 = this.minecraft.player.connection;
        var0.send(new ServerboundPlayerCommandPacket(this.minecraft.player, ServerboundPlayerCommandPacket.Action.STOP_SLEEPING));
    }

    public void onPlayerWokeUp() {
        if (this.input.getValue().isEmpty()) {
            this.minecraft.setScreen(null);
        } else {
            this.minecraft.setScreen(new ChatScreen(this.input.getValue()));
        }

    }
}
