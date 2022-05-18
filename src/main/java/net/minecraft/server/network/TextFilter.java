package net.minecraft.server.network;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;

public interface TextFilter {
    TextFilter DUMMY = new TextFilter() {
        @Override
        public void join() {
        }

        @Override
        public void leave() {
        }

        @Override
        public CompletableFuture<FilteredText<String>> processStreamMessage(String param0) {
            return CompletableFuture.completedFuture(FilteredText.passThrough(param0));
        }

        @Override
        public CompletableFuture<List<FilteredText<String>>> processMessageBundle(List<String> param0) {
            return CompletableFuture.completedFuture(param0.stream().map(FilteredText::passThrough).collect(ImmutableList.toImmutableList()));
        }
    };

    void join();

    void leave();

    CompletableFuture<FilteredText<String>> processStreamMessage(String var1);

    CompletableFuture<List<FilteredText<String>>> processMessageBundle(List<String> var1);

    default CompletableFuture<FilteredText<Component>> processStreamComponent(Component param0) {
        return this.processStreamMessage(param0.getString()).thenApply(param1 -> {
            Component var0 = Util.mapNullable(param1.filtered(), Component::literal);
            return new FilteredText<>(param0, var0);
        });
    }
}
