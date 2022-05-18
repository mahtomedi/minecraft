package net.minecraft.network.chat;

import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.FilteredText;

@FunctionalInterface
public interface ChatDecorator {
    ChatDecorator PLAIN = (param0, param1) -> CompletableFuture.completedFuture(param1);

    CompletableFuture<Component> decorate(@Nullable ServerPlayer var1, Component var2);

    default CompletableFuture<FilteredText<Component>> decorateFiltered(@Nullable ServerPlayer param0, FilteredText<Component> param1) {
        CompletableFuture<Component> var0 = this.decorate(param0, param1.raw());
        if (!param1.isFiltered()) {
            return var0.thenApply(FilteredText::passThrough);
        } else if (param1.filtered() == null) {
            return var0.thenApply(FilteredText::fullyFiltered);
        } else {
            CompletableFuture<Component> var1 = this.decorate(param0, param1.filtered());
            return CompletableFuture.allOf(var0, var1).thenApply(param2 -> new FilteredText<>(var0.join(), var1.join()));
        }
    }

    default CompletableFuture<FilteredText<PlayerChatMessage>> decorateChat(
        @Nullable ServerPlayer param0, FilteredText<Component> param1, MessageSignature param2, boolean param3
    ) {
        return this.decorateFiltered(param0, param1).thenApply(param3x -> PlayerChatMessage.filteredSigned(param1, param3x, param2, param3));
    }
}
