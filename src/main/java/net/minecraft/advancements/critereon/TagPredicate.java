package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

public record TagPredicate<T>(TagKey<T> tag, boolean expected) {
    public static <T> Codec<TagPredicate<T>> codec(ResourceKey<? extends Registry<T>> param0) {
        return RecordCodecBuilder.create(
            param1 -> param1.group(
                        TagKey.codec(param0).fieldOf("id").forGetter(TagPredicate::tag), Codec.BOOL.fieldOf("expected").forGetter(TagPredicate::expected)
                    )
                    .apply(param1, TagPredicate::new)
        );
    }

    public static <T> TagPredicate<T> is(TagKey<T> param0) {
        return new TagPredicate<>(param0, true);
    }

    public static <T> TagPredicate<T> isNot(TagKey<T> param0) {
        return new TagPredicate<>(param0, false);
    }

    public boolean matches(Holder<T> param0) {
        return param0.is(this.tag) == this.expected;
    }
}
