package net.minecraft.server.network;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface TextFilter {
    TextFilter DUMMY = new TextFilter() {
        @Override
        public void join() {
        }

        @Override
        public void leave() {
        }

        @Override
        public CompletableFuture<TextFilter.FilteredText> processStreamMessage(String param0) {
            return CompletableFuture.completedFuture(TextFilter.FilteredText.passThrough(param0));
        }

        @Override
        public CompletableFuture<List<TextFilter.FilteredText>> processMessageBundle(List<String> param0) {
            return CompletableFuture.completedFuture(param0.stream().map(TextFilter.FilteredText::passThrough).collect(ImmutableList.toImmutableList()));
        }
    };

    void join();

    void leave();

    CompletableFuture<TextFilter.FilteredText> processStreamMessage(String var1);

    CompletableFuture<List<TextFilter.FilteredText>> processMessageBundle(List<String> var1);

    public static class FilteredText {
        public static final TextFilter.FilteredText EMPTY = new TextFilter.FilteredText("", "");
        private final String raw;
        private final String filtered;

        public FilteredText(String param0, String param1) {
            this.raw = param0;
            this.filtered = param1;
        }

        public String getRaw() {
            return this.raw;
        }

        public String getFiltered() {
            return this.filtered;
        }

        public static TextFilter.FilteredText passThrough(String param0) {
            return new TextFilter.FilteredText(param0, param0);
        }

        public static TextFilter.FilteredText fullyFiltered(String param0) {
            return new TextFilter.FilteredText(param0, "");
        }
    }
}
