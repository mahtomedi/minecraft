package net.minecraft.client.gui.screens;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class InBedChatScreen extends ChatScreen {
    public InBedChatScreen() {
        super("");
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(
            new Button(this.width / 2 - 100, this.height - 40, 200, 20, new TranslatableComponent("multiplayer.stopSleeping"), param0 -> this.sendWakeUp())
        );
    }

    @Override
    public void onClose() {
        this.sendWakeUp();
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        if (param0 == 256) {
            this.sendWakeUp();
        } else if (param0 == 257 || param0 == 335) {
            String var0 = this.input.getValue().trim();
            if (!var0.isEmpty()) {
                this.sendMessage(var0);
            }

            this.input.setValue("");
            this.minecraft.gui.getChat().resetChatScroll();
            return true;
        }

        return super.keyPressed(param0, param1, param2);
    }

    private void sendWakeUp() {
        ClientPacketListener var0 = this.minecraft.player.connection;
        var0.send(new ServerboundPlayerCommandPacket(this.minecraft.player, ServerboundPlayerCommandPacket.Action.STOP_SLEEPING));
    }
}
