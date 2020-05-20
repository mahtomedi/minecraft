package net.minecraft.client.gui.chat;

import java.util.UUID;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface ChatListener {
    void handle(ChatType var1, Component var2, UUID var3);
}
