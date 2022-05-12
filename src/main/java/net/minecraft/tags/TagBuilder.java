package net.minecraft.tags;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;

public class TagBuilder {
    private final List<TagEntry> entries = new ArrayList<>();

    public static TagBuilder create() {
        return new TagBuilder();
    }

    public List<TagEntry> build() {
        return List.copyOf(this.entries);
    }

    public TagBuilder add(TagEntry param0) {
        this.entries.add(param0);
        return this;
    }

    public TagBuilder addElement(ResourceLocation param0) {
        return this.add(TagEntry.element(param0));
    }

    public TagBuilder addOptionalElement(ResourceLocation param0) {
        return this.add(TagEntry.optionalElement(param0));
    }

    public TagBuilder addTag(ResourceLocation param0) {
        return this.add(TagEntry.tag(param0));
    }

    public TagBuilder addOptionalTag(ResourceLocation param0) {
        return this.add(TagEntry.optionalTag(param0));
    }
}
