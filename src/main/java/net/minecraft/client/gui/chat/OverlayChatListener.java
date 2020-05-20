package net.minecraft.client.gui.chat;

import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class OverlayChatListener implements ChatListener {
    private final Minecraft minecraft;

    public OverlayChatListener(Minecraft param0) {
        this.minecraft = param0;
    }

    @Override
    public void handle(ChatType param0, Component param1, UUID param2) {
        if (!this.minecraft.isBlocked(param2)) {
            this.minecraft.gui.setOverlayMessage(param1, false);
        }
    }
}
