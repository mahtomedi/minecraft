package net.minecraft.network.chat;

import javax.annotation.Nullable;
import net.minecraft.server.level.ServerPlayer;

@FunctionalInterface
public interface ChatDecorator {
    ChatDecorator PLAIN = (param0, param1) -> param1;

    Component decorate(@Nullable ServerPlayer var1, Component var2);
}
