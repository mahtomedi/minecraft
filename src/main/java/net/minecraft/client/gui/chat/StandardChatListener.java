package net.minecraft.client.gui.chat;

import java.util.UUID;
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
    public void handle(ChatType param0, Component param1, UUID param2) {
        if (param0 != ChatType.CHAT) {
            this.minecraft.gui.getChat().addMessage(param1);
        } else {
            this.minecraft.gui.getChat().enqueueMessage(param1);
        }

    }
}
