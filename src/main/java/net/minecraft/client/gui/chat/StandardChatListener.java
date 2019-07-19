package net.minecraft.client.gui.chat;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StandardChatListener implements ChatListener {
    private final Minecraft minecraft;

    public StandardChatListener(Minecraft param0) {
        this.minecraft = param0;
    }

    @Override
    public void handle(ChatType param0, Component param1) {
        this.minecraft.gui.getChat().addMessage(param1);
    }
}
