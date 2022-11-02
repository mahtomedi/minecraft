package net.minecraft.data.tags;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;

public abstract class IntrinsicHolderTagsProvider<T> extends TagsProvider<T> {
    private final Function<T, ResourceKey<T>> keyExtractor;

    public IntrinsicHolderTagsProvider(
        PackOutput param0, ResourceKey<? extends Registry<T>> param1, CompletableFuture<HolderLookup.Provider> param2, Function<T, ResourceKey<T>> param3
    ) {
        super(param0, param1, param2);
        this.keyExtractor = param3;
    }

    protected IntrinsicHolderTagsProvider.IntrinsicTagAppender<T> tag(TagKey<T> param0) {
        TagBuilder var0 = this.getOrCreateRawBuilder(param0);
        return new IntrinsicHolderTagsProvider.IntrinsicTagAppender<>(var0, this.keyExtractor);
    }

    protected static class IntrinsicTagAppender<T> extends TagsProvider.TagAppender<T> {
        private final Function<T, ResourceKey<T>> keyExtractor;

        IntrinsicTagAppender(TagBuilder param0, Function<T, ResourceKey<T>> param1) {
            super(param0);
            this.keyExtractor = param1;
        }

        public IntrinsicHolderTagsProvider.IntrinsicTagAppender<T> addTag(TagKey<T> param0) {
            super.addTag(param0);
            return this;
        }

        public final IntrinsicHolderTagsProvider.IntrinsicTagAppender<T> add(T param0) {
            this.add(this.keyExtractor.apply(param0));
            return this;
        }

        @SafeVarargs
        public final IntrinsicHolderTagsProvider.IntrinsicTagAppender<T> add(T... param0) {
            Stream.<T>of(param0).map(this.keyExtractor).forEach(this::add);
            return this;
        }
    }
}
