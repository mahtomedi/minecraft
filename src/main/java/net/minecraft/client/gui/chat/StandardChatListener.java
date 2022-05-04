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
        param0.chat().ifPresent(param2x -> {
            Component var0 = param2x.decorate(param1, param2);
            if (param2 == null) {
                this.minecraft.gui.getChat().addMessage(var0);
            } else {
                this.minecraft.gui.getChat().enqueueMessage(var0);
            }

        });
    }
}
