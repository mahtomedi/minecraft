package net.minecraft.tags;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

public class TagEntry {
    private static final Codec<TagEntry> FULL_CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    ExtraCodecs.TAG_OR_ELEMENT_ID.fieldOf("id").forGetter(TagEntry::elementOrTag),
                    Codec.BOOL.optionalFieldOf("required", Boolean.valueOf(true)).forGetter(param0x -> param0x.required)
                )
                .apply(param0, TagEntry::new)
    );
    public static final Codec<TagEntry> CODEC = Codec.either(ExtraCodecs.TAG_OR_ELEMENT_ID, FULL_CODEC)
        .xmap(
            param0 -> param0.map(param0x -> new TagEntry(param0x, true), param0x -> param0x),
            param0 -> param0.required ? Either.left(param0.elementOrTag()) : Either.right(param0)
        );
    private final ResourceLocation id;
    private final boolean tag;
    private final boolean required;

    private TagEntry(ResourceLocation param0, boolean param1, boolean param2) {
        this.id = param0;
        this.tag = param1;
        this.required = param2;
    }

    private TagEntry(ExtraCodecs.TagOrElementLocation param0, boolean param1) {
        this.id = param0.id();
        this.tag = param0.tag();
        this.required = param1;
    }

    private ExtraCodecs.TagOrElementLocation elementOrTag() {
        return new ExtraCodecs.TagOrElementLocation(this.id, this.tag);
    }

    public static TagEntry element(ResourceLocation param0) {
        return new TagEntry(param0, false, true);
    }

    public static TagEntry optionalElement(ResourceLocation param0) {
        return new TagEntry(param0, false, false);
    }

    public static TagEntry tag(ResourceLocation param0) {
        return new TagEntry(param0, true, true);
    }

    public static TagEntry optionalTag(ResourceLocation param0) {
        return new TagEntry(param0, true, false);
    }

    public <T> boolean build(TagEntry.Lookup<T> param0, Consumer<T> param1) {
        if (this.tag) {
            Collection<T> var0 = param0.tag(this.id);
            if (var0 == null) {
                return !this.required;
            }

            var0.forEach(param1);
        } else {
            T var1 = param0.element(this.id);
            if (var1 == null) {
                return !this.required;
            }

            param1.accept(var1);
        }

        return true;
    }

    public void visitRequiredDependencies(Consumer<ResourceLocation> param0) {
        if (this.tag && this.required) {
            param0.accept(this.id);
        }

    }

    public void visitOptionalDependencies(Consumer<ResourceLocation> param0) {
        if (this.tag && !this.required) {
            param0.accept(this.id);
        }

    }

    public boolean verifyIfPresent(Predicate<ResourceLocation> param0, Predicate<ResourceLocation> param1) {
        return !this.required || (this.tag ? param1 : param0).test(this.id);
    }

    @Override
    public String toString() {
        StringBuilder var0 = new StringBuilder();
        if (this.tag) {
            var0.append('#');
        }

        var0.append(this.id);
        if (!this.required) {
            var0.append('?');
        }

        return var0.toString();
    }

    public interface Lookup<T> {
        @Nullable
        T element(ResourceLocation var1);

        @Nullable
        Collection<T> tag(ResourceLocation var1);
    }
}
