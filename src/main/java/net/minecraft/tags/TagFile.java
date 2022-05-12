package net.minecraft.tags;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;

public record TagFile(List<TagEntry> entries, boolean replace) {
    public static final Codec<TagFile> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    TagEntry.CODEC.listOf().fieldOf("values").forGetter(TagFile::entries),
                    Codec.BOOL.optionalFieldOf("replace", Boolean.valueOf(false)).forGetter(TagFile::replace)
                )
                .apply(param0, TagFile::new)
    );
}
