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
        if (param1.filtered() == null) {
            return var0.thenApply(FilteredText::fullyFiltered);
        } else if (!param1.isFiltered()) {
            return var0.thenApply(FilteredText::passThrough);
        } else {
            CompletableFuture<Component> var1 = this.decorate(param0, param1.filtered());
            return CompletableFuture.allOf(var0, var1).thenApply(param2 -> new FilteredText<>(var0.join(), var1.join()));
        }
    }

    default CompletableFuture<FilteredText<PlayerChatMessage>> decorateSignedChat(@Nullable ServerPlayer param0, FilteredText<PlayerChatMessage> param1) {
        FilteredText<Component> var0 = param1.map(PlayerChatMessage::signedContent);
        return this.decorateFiltered(param0, var0).thenApply(param1x -> attachDecoration(param1, param1x));
    }

    static FilteredText<PlayerChatMessage> attachDecoration(FilteredText<PlayerChatMessage> param0, FilteredText<Component> param1) {
        return param0.map(
            param1x -> param1x.withDecoratedContent(param1.raw()),
            param1x -> param1.filtered() != null ? param1x.withDecoratedContent(param1.filtered()) : param1x
        );
    }
}
