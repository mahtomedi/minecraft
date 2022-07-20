package net.minecraft.network.chat;

import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.FilteredText;

@FunctionalInterface
public interface ChatDecorator {
    ChatDecorator PLAIN = (param0, param1) -> CompletableFuture.completedFuture(param1);

    CompletableFuture<Component> decorate(@Nullable ServerPlayer var1, Component var2);

    default CompletableFuture<FilteredText<Component>> rebuildFiltered(@Nullable ServerPlayer param0, FilteredText<Component> param1, Component param2) {
        return param1.rebuildIfNeededAsync(param2, param1x -> this.decorate(param0, param1x));
    }

    static FilteredText<PlayerChatMessage> attachUnsignedDecoration(FilteredText<PlayerChatMessage> param0, FilteredText<Component> param1) {
        return param0.map(
            param1x -> param1x.withUnsignedContent(param1.raw()),
            param1x -> param1.filtered() != null ? param1x.withUnsignedContent(param1.filtered()) : param1x
        );
    }
}
