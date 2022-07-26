package net.minecraft.network.chat;

import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerPlayer;

@FunctionalInterface
public interface ChatDecorator {
    ChatDecorator PLAIN = (param0, param1) -> CompletableFuture.completedFuture(param1);

    CompletableFuture<Component> decorate(@Nullable ServerPlayer var1, Component var2);

    default CompletableFuture<PlayerChatMessage> decorate(@Nullable ServerPlayer param0, PlayerChatMessage param1) {
        return param1.signedContent().isDecorated()
            ? CompletableFuture.completedFuture(param1)
            : this.decorate(param0, param1.serverContent()).thenApply(param1::withUnsignedContent);
    }

    static PlayerChatMessage attachIfNotDecorated(PlayerChatMessage param0, Component param1) {
        return !param0.signedContent().isDecorated() ? param0.withUnsignedContent(param1) : param0;
    }
}
