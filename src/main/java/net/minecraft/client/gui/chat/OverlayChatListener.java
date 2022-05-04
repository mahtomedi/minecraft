package net.minecraft.client.gui.chat;

import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ChatSender;
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
    public void handle(ChatType param0, Component param1, @Nullable ChatSender param2) {
        param0.overlay().ifPresent(param2x -> {
            Component var0 = param2x.decorate(param1, param2);
            this.minecraft.gui.setOverlayMessage(var0, false);
        });
    }
}
