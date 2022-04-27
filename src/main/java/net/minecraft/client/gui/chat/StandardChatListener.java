package net.minecraft.client.gui.chat;

import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ChatSender;
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
    public void handle(ChatType param0, Component param1, @Nullable ChatSender param2) {
        if (param0 != ChatType.CHAT) {
            this.minecraft.gui.getChat().addMessage(param1);
        } else {
            Component var0 = param2 != null ? decorateMessage(param1, param2) : param1;
            this.minecraft.gui.getChat().enqueueMessage(var0);
        }

    }

    private static Component decorateMessage(Component param0, ChatSender param1) {
        return Component.translatable("chat.type.text", param1.name(), param0);
    }
}
